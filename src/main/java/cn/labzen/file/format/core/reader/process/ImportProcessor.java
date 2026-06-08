package cn.labzen.file.format.core.reader.process;

import cn.labzen.file.cleanser.ChainableCleanserExecutor;
import cn.labzen.file.converter.executor.ChainableImportConverterExecutor;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.Column;
import cn.labzen.file.definition.bean.column.Importing;
import cn.labzen.file.exception.DataConvertException;
import cn.labzen.file.exception.DataReadException;
import cn.labzen.file.i18n.I18nStoreHolder;
import cn.labzen.file.i18n.I18nStoreProvider;
import cn.labzen.file.validator.ChainableValidatorExecutor;
import cn.labzen.file.validator.ValidateContext;
import cn.labzen.file.validator.ValidateResult;
import cn.labzen.tool.util.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

import static cn.labzen.file.format.core.reader.DataFileReader.SEQUENCE_KEY;
import static cn.labzen.file.i18n.internal.Internal18nKeys.IMPORT_CLEANSING_ERROR_MESSAGE;
import static cn.labzen.file.i18n.internal.Internal18nKeys.IMPORT_CONVERTER_ERROR_MESSAGE;
import static cn.labzen.file.validator.Validator.*;

/**
 * 导入数据处理器 — 核心调度器
 * <p>
 * 协调 清理→预校验→转换→后校验→Bean构建 的流水线处理。
 * 支持流式逐行处理，以及全量完成后的延后校验。
 *
 * @author labzen
 */
@Slf4j
public final class ImportProcessor<T> {

  private final DataDefinition definition;
  private final Class<T> type;
  private final String locale;
  private final I18nStoreProvider i18nStore;

  private final Map<String, ChainableCleanserExecutor> cleansers = Maps.newHashMap();
  private final Map<String, ChainableValidatorExecutor> validators = Maps.newHashMap();
  private final Map<String, ChainableImportConverterExecutor> converters = Maps.newHashMap();

  //  private List<Map<String, Object>> proceedData = Lists.newArrayList();
  private List<ProceedRow<T>> proceedRows = Lists.newArrayList();
//  private List<FieldError> errors;

  //  private final Class<T> beanType;
//  private final String locale;
  public ImportProcessor(DataDefinition definition) {
    // todo 针对语言+定义，进行缓存
    this.definition = definition;
    //noinspection unchecked
    this.type = (Class<T>) definition.getDomainClass();
    this.locale = definition.getLocale();
    this.i18nStore = I18nStoreHolder.get();

    buildCleansers();
    buildValidators();
    buildConverters();
  }

  public ImportResult<T> process(List<Map<String, String>> rowsData) {
    int index = 1;
    for (Map<String, String> rowData : rowsData) {
      String sequence;
      if (rowData.containsKey(SEQUENCE_KEY)) {
        sequence = rowData.get(SEQUENCE_KEY);
      } else {
        sequence = String.valueOf(index);
      }
      processRow(sequence, rowData);
      index++;
    }

    int size = rowsData.size();
    int successCount = (int) proceedRows.stream().filter(ProceedRow::isSuccess).count();
    List<T> proceedBeans = proceedRows.stream().map(ProceedRow::getInstance).toList();
    List<ImportFailure> failures = proceedRows.stream().map(ProceedRow::toFailure).filter(Objects::nonNull).toList();
    return new ImportResult<>(type, size, successCount, size - successCount, proceedBeans, failures);
  }

  private void processRow(String sequence, Map<String, String> rawRowData) {
    // excel code 表头
    Set<String> codes = rawRowData.keySet();
    Map<String, Object> proceedRowData = Maps.newHashMap();
    List<FieldError> errors = Lists.newArrayList();

    for (String code : codes) {
      Column column = definition.getColumns().get(code);
      if (column == null) {
        continue;
      }

      errors = Lists.newArrayList();

      String fieldName = column.getFieldName();
      String header = column.getHeader();
      String rawValue = rawRowData.get(code);

      // ── 1. Cleanse（清理）──
      String cleansedValue = rawValue;
      if (cleansers.containsKey(fieldName) && cleansedValue != null) {
        try {
          cleansedValue = cleansers.get(fieldName).execute(cleansedValue);
        } catch (Exception e) {
          errors.add(new FieldError(fieldName, header, rawValue, ImportPhase.CLEANSE, resolveCleansingError(e)));
          continue;
        }
      }

      // ── 2. Validate（预校验）──
      if (validators.containsKey(fieldName)) {
        ValidateContext<T> context = new ValidateContext<>(fieldName, cleansedValue, rawRowData, proceedRows);
        List<ValidateResult> vr = validators.get(fieldName).executeImmediate(context);
        for (ValidateResult r : vr) {
          errors.add(new FieldError(fieldName, header, rawValue, ImportPhase.VALIDATE, resolveValidateResult(r)));
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
          errors.add(new FieldError(fieldName, header, rawValue, ImportPhase.CONVERT, resolveConvertError(e)));
          continue;
        }
      } else if (cleansedValue != null && targetType != null) {
        // 无转换器，尝试默认类型转换
        try {
          convertedValue = defaultTypeConvert(cleansedValue, targetType);
        } catch (Exception e) {
          errors.add(new FieldError(fieldName, header, rawValue, ImportPhase.CONVERT, resolveConvertError(e)));
          continue;
        }
      }

      // ── 4. Validate（后校验）──
      if (validators.containsKey(fieldName)) {
        ValidateContext<T> context = new ValidateContext<>(fieldName, convertedValue, rawRowData, proceedRows);
        List<ValidateResult> vr = validators.get(fieldName).executeDeferred(context);
        for (ValidateResult r : vr) {
          errors.add(new FieldError(fieldName, header, rawValue, ImportPhase.VALIDATE, resolveValidateResult(r)));
        }
        if (!vr.isEmpty()) {
          continue;
        }
      }

      proceedRowData.put(fieldName, convertedValue);
//      proceedData.add(Maps.newHashMap(fieldName, convertedValue)
    }

    ProceedRow<T> row = new ProceedRow<>(sequence, errors.isEmpty(), proceedRowData, errors, definition);
    proceedRows.add(row);
//    proceedData.add(proceedRowData);
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

  private String resolveCleansingError(Exception e) {
    String text = i18nStore.getText(locale, IMPORT_CLEANSING_ERROR_MESSAGE, e.getMessage());
    return Strings.value(text, "");
  }

  private String resolveValidateResult(ValidateResult result) {
    // 从I18n获取
    String text = i18nStore.getText(locale, result.getErrorI18nCode(), result.getErrorArgs());
    return Strings.value(text, "");
  }

  private String resolveConvertError(Exception e) {
    String text = i18nStore.getText(locale, IMPORT_CONVERTER_ERROR_MESSAGE, e.getMessage());
    return Strings.value(text, "");
  }

  // ── 预构建 ──

  private void buildCleansers() {
    for (Column column : definition.getColumns().values()) {
      String fieldName = column.getFieldName();

      if (column.getImporting() == null) {
        continue;
      }

      if (column.getImporting().getCleansing() != null) {
        ChainableCleanserExecutor executor = new ChainableCleanserExecutor(column.getImporting().getCleansing());
        cleansers.put(fieldName, executor);
      }
    }
  }

  private void buildValidators() {
    for (Column column : definition.getColumns().values()) {
      String fieldName = column.getFieldName();

      Importing importing = column.getImporting();
      if (importing == null) {
        continue;
      }

      ChainableValidatorExecutor executor = new ChainableValidatorExecutor();

      if (importing.getRequire()) {
        executor.addValidator(REQUIRE_NAME, List.of());
      }
      if (importing.getDependsOn() != null && !importing.getDependsOn().isEmpty()) {
        executor.addValidator(DEPENDS_ON_NAME, new ArrayList<>(importing.getDependsOn()));
      }
      if (importing.getLengthRange() != null) {
        List<Object> args = Lists.newArrayList(importing.getLengthRange().min(), importing.getLengthRange().max());
        executor.addValidator(RANGE_LENGTH_NAME, args);
      }
      if (importing.getNumericRange() != null) {
        List<Object> args = Lists.newArrayList(importing.getNumericRange().min(), importing.getNumericRange().max());
        executor.addValidator(RANGE_NUMERIC_NAME, args);
      }
      if (importing.getDateRange() != null) {
        List<Object> args = Lists.newArrayList(importing.getDateRange().min(), importing.getDateRange().max());
        executor.addValidator(RANGE_DATE_NAME, args);
      }
      if (importing.getUnique() != null && importing.getUnique()) {
        executor.addValidator(UNIQUE_NAME, List.of());
      }

      if (executor.hasValidator()) {
        executor.sort();
        validators.put(fieldName, executor);
      }
    }
  }

  private void buildConverters() {
    for (Column column : definition.getColumns().values()) {
      String fieldName = column.getFieldName();

      converters.put(fieldName, ChainableImportConverterExecutor.get(definition, fieldName));
    }
  }

  // ── 辅助方法 ──

//  @SuppressWarnings("unchecked")
//  public Class<T> getBeanType(String name) {
//    try {
//      return (Class<T>) Class.forName(name);
//    } catch (ClassNotFoundException e) {
//      throw new IllegalStateException();
//    }
//  }

  private Class<?> getFieldType(String fieldName) {
    try {
      Field field = type.getDeclaredField(fieldName);
      return field.getType();
    } catch (NoSuchFieldException e) {
      return null;
    }
  }

}
