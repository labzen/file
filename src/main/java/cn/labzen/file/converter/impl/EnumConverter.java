package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.file.converter.ExportableConverter;
import cn.labzen.file.converter.ImportableConverter;
import cn.labzen.file.exception.DataConvertException;
import cn.labzen.file.util.EnumResolver;
import cn.labzen.tool.util.Strings;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 枚举转换器（导出+导入双向）
 *
 * <h3>导出</h3>
 * 将数据库中的枚举名称字符串，转换为枚举的某个属性值（如 label）。
 * <p>
 * 配置格式：<b>枚举类FQCN#方法名</b>，如 {@code com.example.StatusEnum#getLabel}
 * <ul>
 *   <li>输入：枚举名称字符串（如 "ACTIVE"）→ 通过名称定位常量 → 调用实例方法</li>
 *   <li>输出：方法返回值字符串（如 "激活"）</li>
 * </ul>
 *
 * <h3>导入</h3>
 * 将用户录入的字符串值，通过指定类的静态方法转换为枚举实例。
 * <p>
 * 配置格式：<b>类FQCN#静态方法名</b>，如 {@code com.example.StatusEnum#fromCode}
 * <ul>
 *   <li>自动检测方法签名：优先 {@code method(Class, String)}（工具类模式），其次 {@code method(String)}（枚举自身模式）</li>
 *   <li>支持 {@link Optional} 返回值自动解包</li>
 *   <li>不配置或留空：使用 {@link EnumResolver#valueOfIgnoreCase(Class, String)} 忽略大小写匹配</li>
 * </ul>
 *
 * @author labzen
 */
@Slf4j
@DataConverter(name = Converter.ENUM_NAME, priority = Converter.ENUM_PRIORITY)
public class EnumConverter implements ExportableConverter<String>, ImportableConverter {

  private static final Pattern FQCN_METHOD_PATTERN = Pattern.compile(
    "^(([a-zA-Z_][a-zA-Z0-9_]*\\.)*([A-Z_$][a-zA-Z0-9_$]*))#([a-zA-Z_$][a-zA-Z0-9_$]*)$"
  );

  // 导出缓存：FQCN#方法名 → EnumInfo（强制要求类为枚举）
  private static final Map<String, EnumInfo> EXPORT_CACHE = new ConcurrentHashMap<>();

  // 导入方法缓存：FQCN#方法名 → Method
  private static final Map<String, ImportMethodInfo> IMPORT_CACHE = new ConcurrentHashMap<>();

  // ==================== 导出 ====================

  @Override
  public boolean supportsExport(Class<?> sourceType) {
    return String.class.isAssignableFrom(sourceType);
  }

  @Override
  public String doConvertForExport(Object input, List<Object> arguments) {
    if (input == null) {
      return null;
    }

    EnumInfo enumInfo = resolveExportEnumInfo(arguments);
    if (enumInfo == EnumInfo.EMPTY) {
      return "convert-enum-failed";
    }

    Enum<?> enumConstant = findEnumConstant(enumInfo.type(), input.toString());
    if (enumConstant == null) {
      return "convert-enum-failed";
    }

    try {
      Object result = enumInfo.method().invoke(enumConstant);
      return result != null ? result.toString() : "";
    } catch (Exception e) {
      return "convert-enum-failed";
    }
  }

  // ==================== 导入 ====================

  @Override
  public boolean supportsImport(Class<?> targetType) {
    return targetType != null && targetType.isEnum();
  }

  @Override
  public Object doConvertForImport(Object input, List<Object> arguments, Class<?> targetType) {
    if (input == null) {
      return null;
    }

    String config = arguments.isEmpty() ? "" : Strings.value(arguments.getFirst(), "");
    String value = input.toString();

    // 未配置：使用默认 valueOf 忽略大小写
    if (config.isEmpty()) {
      @SuppressWarnings("unchecked")
      Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) targetType;
      return EnumResolver.valueOfIgnoreCase(enumType, value);
    }

    // 配置了 FQCN#方法名：内部解析并调用
    return resolveImportByFqcnMethod(config, value, targetType);
  }

  // ==================== 导出解析 ====================

  private EnumInfo resolveExportEnumInfo(List<Object> arguments) {
    String fqcnAndMethod = Strings.value(arguments.getFirst(), "");
    return EXPORT_CACHE.computeIfAbsent(fqcnAndMethod, k -> {
      Matcher matcher = FQCN_METHOD_PATTERN.matcher(fqcnAndMethod);
      if (!matcher.matches()) {
        return EnumInfo.EMPTY;
      }

      String className = matcher.group(1);
      String methodName = matcher.group(4);
      try {
        Class<?> clazz = Class.forName(className);
        if (!clazz.isEnum()) {
          logger.error("导出枚举转换失败：类 [{}] 不是枚举类", className);
          return EnumInfo.EMPTY;
        }
        Method method = clazz.getMethod(methodName);
        @SuppressWarnings("unchecked")
        Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) clazz;
        return new EnumInfo(enumClass, method);
      } catch (ClassNotFoundException e) {
        logger.error("导出枚举转换失败：类 [{}] 不存在", className);
        return EnumInfo.EMPTY;
      } catch (NoSuchMethodException e) {
        logger.error("导出枚举转换失败：方法 [{}#{}()] 不存在", className, methodName);
        return EnumInfo.EMPTY;
      }
    });
  }

  // ==================== 导入解析（内部逻辑） ====================

  /**
   * 通过 FQCN#方法名 解析导入枚举值。
   * 自动检测方法签名（Class+String 或 String），自动解包 Optional 返回值。
   */
  private Object resolveImportByFqcnMethod(String fqcnAndMethod, String value, Class<?> targetType) {
    ImportMethodInfo info = resolveImportMethod(fqcnAndMethod);
    Object result = invokeImportMethod(info, value, targetType);
    result = unwrapOptional(result);

    if (result == null) {
      throw new DataConvertException("枚举转换失败：[{}#{}()]对值[{}]返回null（或Optional.empty）",
        info.className(), info.methodName(), value);
    }
    return result;
  }

  /**
   * 解析并缓存方法：优先 (Class, String)，其次 (String)
   */
  private ImportMethodInfo resolveImportMethod(String fqcnAndMethod) {
    return IMPORT_CACHE.computeIfAbsent(fqcnAndMethod, k -> {
      Matcher matcher = FQCN_METHOD_PATTERN.matcher(fqcnAndMethod);
      if (!matcher.matches()) {
        throw new DataConvertException("枚举转换配置格式无效：[{}]，应为 类全限定名#方法名", fqcnAndMethod);
      }
      String className = matcher.group(1);
      String methodName = matcher.group(4);

      Class<?> clazz;
      try {
        clazz = Class.forName(className);
      } catch (ClassNotFoundException e) {
        throw new DataConvertException("枚举转换失败：类[{}]不存在", className);
      }

      // 1. 优先工具类模式：method(Class, String)
      try {
        Method m = clazz.getMethod(methodName, Class.class, String.class);
        logger.debug("枚举导入匹配到方法签名：{}#{} (Class, String)", className, methodName);
        return new ImportMethodInfo(className, methodName, m, true);
      } catch (NoSuchMethodException ignored) {
        // fall through
      }

      // 2. 枚举自身模式：method(String)
      try {
        Method m = clazz.getMethod(methodName, String.class);
        logger.debug("枚举导入匹配到方法签名：{}#{} (String)", className, methodName);
        return new ImportMethodInfo(className, methodName, m, false);
      } catch (NoSuchMethodException ignored) {
        // fall through
      }

      throw new DataConvertException(
        "枚举转换失败：类[{}]中不存在匹配的静态方法[{}()]，支持的签名：static Xxx {}(Class, String) 或 static Xxx {}(String)",
        className, methodName, methodName, methodName);
    });
  }

  /**
   * 根据方法参数个数自动传入参数并调用
   */
  private Object invokeImportMethod(ImportMethodInfo info, String value, Class<?> targetType) {
    try {
      Object result;
      if (info.takesClassParam()) {
        result = info.method().invoke(null, targetType, value);
      } else {
        result = info.method().invoke(null, value);
      }
      logger.debug("枚举导入成功：[{}#{}()] 输入[{}] → 输出[{}]",
        info.className(), info.methodName(), value, result);
      return result;
    } catch (InvocationTargetException e) {
      throw new DataConvertException("枚举转换失败：值[{}]通过[{}#{}()]转换时抛出异常: {}",
        value, info.className(), info.methodName(), e.getCause().getMessage());
    } catch (Exception e) {
      throw new DataConvertException("枚举转换失败：调用[{}#{}()]异常: {}",
        info.className(), info.methodName(), e.getMessage());
    }
  }

  /**
   * 解包 Optional，其他类型原样返回
   */
  private static Object unwrapOptional(Object result) {
    if (result instanceof Optional<?> opt) {
      return opt.orElse(null);
    }
    return result;
  }

  // ==================== 公共工具方法 ====================

  private Enum<?> findEnumConstant(Class<? extends Enum<?>> enumType, String input) {
    String upperInput = input.toUpperCase();
    for (Enum<?> constant : enumType.getEnumConstants()) {
      if (constant.name().equals(upperInput)) {
        return constant;
      }
    }
    for (Enum<?> constant : enumType.getEnumConstants()) {
      if (constant.name().equalsIgnoreCase(input)) {
        return constant;
      }
    }
    return null;
  }

  private record EnumInfo(Class<? extends Enum<?>> type, Method method) {
    static final EnumInfo EMPTY = new EnumInfo(null, null);
  }

  private record ImportMethodInfo(String className, String methodName, Method method, boolean takesClassParam) {
  }
}
