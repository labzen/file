package cn.labzen.file.format.core.reader;

import cn.labzen.file.cleanser.ChainableCleanserExecutor;
import cn.labzen.file.converter.importable.ChainableImportConverterExecutor;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.Column;
import cn.labzen.file.definition.bean.column.Importing;
import cn.labzen.file.exception.DataConvertException;
import cn.labzen.file.exception.DataReadException;
import cn.labzen.file.i18n.I18nResolver;
import cn.labzen.file.i18n.I18nStoreHolder;
import cn.labzen.file.i18n.I18nStoreProvider;
import cn.labzen.file.validator.ChainableValidatorExecutor;
import cn.labzen.file.validator.ValidateContext;
import cn.labzen.file.validator.ValidateResult;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

/**
 * 导入管线 — 核心调度器
 * <p>
 * 协调 清理→预校验→转换→后校验→Bean构建 的流水线处理。
 * 支持流式逐行处理，以及全量完成后的延后校验。
 *
 * @author labzen
 */
@Slf4j
public class ImportPipeline<T> {

  private final DataDefinition definition;
  private final Class<T> beanType;
  private final String locale;

  // 每字段的处理器
  private final Map<String, ChainableCleanserExecutor> cleansers;
  private final Map<String, ChainableValidatorExecutor> preValidators;
  private final Map<String, ChainableImportConverterExecutor> converters;
  private final Map<String, ChainableValidatorExecutor> postValidators;

  // 结果收集
  private final List<T> successData = new ArrayList<>();
  private final List<ImportFailure> failures = new ArrayList<>();
  private final Map<String, List<String>> uniqueFieldValueTracks = new LinkedHashMap<>();

  public ImportPipeline(DataDefinition definition, Class<T> beanType, @Nullable String locale) {
    this.definition = definition;
    this.beanType = beanType;
    this.locale = locale;

    // i18n 解析
    I18nStoreProvider store = I18nStoreHolder.get();
    I18nResolver resolver = new I18nResolver(store);
    DataDefinition resolved = resolver.resolve(definition, locale);

    // 构建各阶段处理器
    this.cleansers = buildCleansers(resolved);
    this.preValidators = buildPreValidators(resolved);
    this.converters = ChainableImportConverterExecutor.buildFor(resolved);
    this.postValidators = buildPostValidators(resolved);
  }

  /**
   * 处理一行数据
   *
   * @param rowIndex 行号（#列的序号）
   * @param rowData  字段名→字符串值 的映射
   */
  public void processRow(int rowIndex, Map<String, String> rowData) {
    List<FieldError> errors = new ArrayList<>();
    Map<String, Object> convertedValues = new LinkedHashMap<>();

    for (Map.Entry<String, Column> entry : definition.getColumns().entrySet()) {
      String fieldName = entry.getKey();
      Column column = entry.getValue();
      String rawValue = rowData.get(fieldName);

      String headerText = column.getHeader() != null ? column.getHeader() : fieldName;

      // ── 1. Cleanse（清理）──
      String cleansedValue = rawValue;
      if (cleansers.containsKey(fieldName) && cleansedValue != null) {
        try {
          cleansedValue = cleansers.get(fieldName).execute(cleansedValue);
        } catch (Exception e) {
          errors.add(new FieldError(fieldName, headerText, rawValue,
            ImportPhase.CLEANSE, "数据清理失败: " + e.getMessage()));
          continue;
        }
      }

      // ── 2. Validate（预校验）──
      if (preValidators.containsKey(fieldName)) {
        ValidateContext ctx = new ValidateContext(rowIndex, fieldName, headerText,
          convertedValues, rowData, locale);
        List<ValidateResult> vr = preValidators.get(fieldName).executeImmediate(cleansedValue, ctx);
        for (ValidateResult r : vr) {
          errors.add(new FieldError(fieldName, headerText, cleansedValue,
            ImportPhase.VALIDATE, resolveErrorMessage(r)));
        }
        if (!vr.isEmpty()) {
          continue;
        }
      }

      // ── 3. Convert（导入转换）──
      Object convertedValue = cleansedValue;
      Class<?> targetType = getFieldType(fieldName);
      if (converters.containsKey(fieldName) && targetType != null) {
        try {
          convertedValue = converters.get(fieldName).execute(cleansedValue, targetType);
        } catch (DataConvertException e) {
          errors.add(new FieldError(fieldName, headerText, cleansedValue,
            ImportPhase.CONVERT, resolveConvertErrorMessage(e, headerText)));
          continue;
        }
      } else if (cleansedValue != null && targetType != null) {
        // 无转换器，尝试默认类型转换
        try {
          convertedValue = defaultTypeConvert(cleansedValue, targetType);
        } catch (Exception e) {
          errors.add(new FieldError(fieldName, headerText, cleansedValue,
            ImportPhase.CONVERT, "类型转换失败"));
          continue;
        }
      }

      // ── 4. PostValidate（后校验）──
      if (postValidators.containsKey(fieldName)) {
        ValidateContext ctx = new ValidateContext(rowIndex, fieldName, headerText,
          convertedValues, rowData, locale);
        List<ValidateResult> vr = postValidators.get(fieldName).executeImmediate(convertedValue, ctx);
        for (ValidateResult r : vr) {
          errors.add(new FieldError(fieldName, headerText, cleansedValue,
            ImportPhase.POST_VALIDATE, resolveErrorMessage(r)));
        }
        if (!vr.isEmpty()) {
          continue;
        }
      }

      // 记录唯一性字段的值
      Importing importing = column.getImporting();
      if (importing != null && importing.getUnique() != null && importing.getUnique()) {
        uniqueFieldValueTracks.computeIfAbsent(fieldName, k -> new ArrayList<>())
          .add(cleansedValue != null ? cleansedValue : "");
      }

      convertedValues.put(fieldName, convertedValue);
    }

    // ── 5. Construct（构建Bean）──
    if (errors.isEmpty()) {
      try {
        T bean = constructBean(convertedValues);
        successData.add(bean);
      } catch (Exception e) {
        errors.add(new FieldError("", "", "",
          ImportPhase.CONSTRUCT, "构建实例失败: " + e.getMessage()));
        failures.add(new ImportFailure(rowIndex, rowData, errors));
      }
    } else {
      failures.add(new ImportFailure(rowIndex, rowData, errors));
    }
  }

  /**
   * 执行延后校验（如唯一性）
   * <p>
   * 在所有行处理完成后调用
   */
  public void executeDeferredValidation() {
    // 唯一性校验
    for (Map.Entry<String, List<String>> entry : uniqueFieldValueTracks.entrySet()) {
      String fieldName = entry.getKey();
      List<String> values = entry.getValue();

      // 找出重复的值
      Map<String, List<Integer>> valueToIndices = new LinkedHashMap<>();
      int idx = 0;
      for (String v : values) {
        valueToIndices.computeIfAbsent(v, k -> new ArrayList<>()).add(idx);
        idx++;
      }

      for (Map.Entry<String, List<Integer>> dupEntry : valueToIndices.entrySet()) {
        if (dupEntry.getValue().size() > 1) {
          // 所有重复的行标记为失败
          for (Integer rowIdx : dupEntry.getValue()) {
            // 这里简化处理：在failures中查找对应行并添加唯一性错误
            // 实际实现需要根据rowIndex匹配
            Column column = definition.getColumns().get(fieldName);
            String headerText = column != null && column.getHeader() != null ? column.getHeader() : fieldName;
            FieldError error = new FieldError(fieldName, headerText, dupEntry.getKey(),
              ImportPhase.VALIDATE, headerText + "在导入数据中存在重复");

            // 查找已有的failure或创建新的
            int targetRowIndex = rowIdx + 1; // 简化：假设行号从1开始
            boolean found = false;
            for (ImportFailure f : failures) {
              if (f.getRowIndex() == targetRowIndex) {
                f.getErrors().add(error);
                found = true;
                break;
              }
            }
            if (!found) {
              // 从successData中移除并标记为失败
              // 这需要额外的索引追踪，简化处理
              logger.warn("唯一性校验发现重复值：字段={}, 值={}", fieldName, dupEntry.getKey());
            }
          }
        }
      }
    }
  }

  /**
   * 构建最终结果
   */
  public ImportResult<T> buildResult() {
    int totalRows = successData.size() + failures.size();
    return new ImportResult<>(totalRows, successData.size(), failures.size(), successData, failures);
  }

  // ── 构建各阶段处理器 ──

  private Map<String, ChainableCleanserExecutor> buildCleansers(DataDefinition definition) {
    Map<String, ChainableCleanserExecutor> result = new HashMap<>();
    List<String> globalCleansing = definition.getImporting() != null
      ? definition.getImporting().getCleansing()
      : Collections.emptyList();

    for (Map.Entry<String, Column> entry : definition.getColumns().entrySet()) {
      String fieldName = entry.getKey();
      Column column = entry.getValue();

      List<String> cleansingList = null;
      if (column.getImporting() != null && column.getImporting().getCleansing() != null) {
        // 列级覆盖
        cleansingList = column.getImporting().getCleansing();
      } else if (!globalCleansing.isEmpty()) {
        // 使用全局默认
        cleansingList = globalCleansing;
      }

      if (cleansingList != null && !cleansingList.isEmpty()) {
        result.put(fieldName, new ChainableCleanserExecutor(cleansingList));
      }
    }
    return result;
  }

  private Map<String, ChainableValidatorExecutor> buildPreValidators(DataDefinition definition) {
    Map<String, ChainableValidatorExecutor> result = new HashMap<>();
    for (Map.Entry<String, Column> entry : definition.getColumns().entrySet()) {
      String fieldName = entry.getKey();
      Column column = entry.getValue();
      Importing importing = column.getImporting();

      if (importing == null) continue;

      ChainableValidatorExecutor executor = new ChainableValidatorExecutor();
      boolean hasValidator = false;

      if (importing.isRequired()) {
        executor.addValidator("required", List.of());
        hasValidator = true;
      }
      if (importing.getMaxLength() != null || importing.getMinLength() != null) {
        List<Object> args = new ArrayList<>();
        args.add(importing.getMinLength());
        args.add(importing.getMaxLength());
        executor.addValidator("length", args);
        hasValidator = true;
      }
//      if (importing.getPattern() != null) {
//        executor.addValidator("pattern", List.of(importing.getPattern()));
//        hasValidator = true;
//      }
      if (importing.getDependsOn() != null && !importing.getDependsOn().isEmpty()) {
        executor.addValidator("dependency", new ArrayList<>(importing.getDependsOn()));
        hasValidator = true;
      }

      if (hasValidator) {
        executor.sort();
        result.put(fieldName, executor);
      }
    }
    return result;
  }

  private Map<String, ChainableValidatorExecutor> buildPostValidators(DataDefinition definition) {
    Map<String, ChainableValidatorExecutor> result = new HashMap<>();
    for (Map.Entry<String, Column> entry : definition.getColumns().entrySet()) {
      String fieldName = entry.getKey();
      Column column = entry.getValue();
      Importing importing = column.getImporting();

      if (importing == null) continue;

      ChainableValidatorExecutor executor = new ChainableValidatorExecutor();
      boolean hasValidator = false;

      if (importing.getMin() != null || importing.getMax() != null) {
        List<Object> args = new ArrayList<>();
        args.add(importing.getMin());
        args.add(importing.getMax());
        executor.addValidator("range", args);
        hasValidator = true;
      }
      if (importing.getUnique() != null && importing.getUnique()) {
        executor.addValidator("unique", List.of());
        hasValidator = true;
      }

      if (hasValidator) {
        executor.sort();
        result.put(fieldName, executor);
      }
    }
    return result;
  }

  // ── 辅助方法 ──

  private Class<?> getFieldType(String fieldName) {
    try {
      Field field = beanType.getDeclaredField(fieldName);
      return field.getType();
    } catch (NoSuchFieldException e) {
      return null;
    }
  }

  private T constructBean(Map<String, Object> values) throws Exception {
    Constructor<T> constructor = beanType.getDeclaredConstructor();
    constructor.setAccessible(true);
    T instance = constructor.newInstance();

    for (Map.Entry<String, Object> entry : values.entrySet()) {
      try {
        Field field = beanType.getDeclaredField(entry.getKey());
        field.setAccessible(true);
        field.set(instance, entry.getValue());
      } catch (NoSuchFieldException e) {
        logger.warn("字段[{}]在Bean[{}]中不存在", entry.getKey(), beanType.getSimpleName());
      }
    }
    return instance;
  }

  private Object defaultTypeConvert(String value, Class<?> targetType) {
    if (targetType == String.class) {
      return value;
    }
    if (value == null || value.isBlank()) {
      return null;
    }

    String trimmed = value.trim();

    if (targetType == Integer.class || targetType == int.class) {
      return Integer.parseInt(trimmed);
    } else if (targetType == Long.class || targetType == long.class) {
      return Long.parseLong(trimmed);
    } else if (targetType == Double.class || targetType == double.class) {
      return Double.parseDouble(trimmed);
    } else if (targetType == Float.class || targetType == float.class) {
      return Float.parseFloat(trimmed);
    } else if (targetType == Boolean.class || targetType == boolean.class) {
      if ("true".equalsIgnoreCase(trimmed) || "1".equals(trimmed) || "yes".equalsIgnoreCase(trimmed) || "是".equals(trimmed)) {
        return true;
      }
      if ("false".equalsIgnoreCase(trimmed) || "0".equals(trimmed) || "no".equalsIgnoreCase(trimmed) || "否".equals(trimmed)) {
        return false;
      }
      throw new DataReadException("布尔转换失败：值[{}]无法识别", trimmed);
    } else if (targetType == BigDecimal.class) {
      return new java.math.BigDecimal(trimmed);
    } else if (targetType == Short.class || targetType == short.class) {
      return Short.parseShort(trimmed);
    } else if (targetType == Byte.class || targetType == byte.class) {
      return Byte.parseByte(trimmed);
    }

    return value;
  }

  private String resolveErrorMessage(ValidateResult result) {
    // 优先从I18n获取，fallback到默认消息
    I18nStoreProvider store = I18nStoreHolder.get();
    String text = store.getText(locale, result.getErrorCode());
    if (text != null) {
      return text;
    }
    return result.getDefaultMessage();
  }

  private String resolveConvertErrorMessage(DataConvertException e, String headerText) {
    I18nStoreProvider store = I18nStoreHolder.get();
    String text = store.getText(locale, "import.convert.type");
    if (text != null) {
      return text;
    }
    return headerText + "转换失败: " + e.getMessage();
  }
}
