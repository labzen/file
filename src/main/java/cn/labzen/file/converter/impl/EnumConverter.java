package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.file.converter.exportable.ExportableConverter;
import cn.labzen.file.converter.importable.ImportableConverter;
import cn.labzen.file.exception.DataConvertException;
import cn.labzen.tool.util.Strings;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 枚举转换器（导出+导入双向）
 * <p>
 * 格式：枚举类全限定名#方法名，如 com.example.StatusEnum#getLabel
 * <ul>
 *   <li>导出：枚举名称 → 方法返回值（正向）</li>
 *   <li>导入：方法返回值/枚举名称 → 枚举名称（反向）</li>
 * </ul>
 *
 * @author labzen
 */
@Slf4j
@DataConverter(name = Converter.ENUM_NAME, priority = Converter.ENUM_PRIORITY)
public class EnumConverter implements ExportableConverter<String>, ImportableConverter {

  private static final Map<String, EnumInfo> ENUM_CACHE = new ConcurrentHashMap<>();
  private static final Pattern PATTERN = Pattern.compile(
    "^(([a-zA-Z_][a-zA-Z0-9_]*\\.)*([A-Z_$][a-zA-Z0-9_$]*))#([a-zA-Z_$][a-zA-Z0-9_$]*)$"
  );

  // ── 导出 ──

  @Override
  public boolean supportsExport(Class<?> sourceType) {
    return String.class.isAssignableFrom(sourceType);
  }

  @Override
  public String doConvertForExport(Object input, List<Object> arguments) {
    if (input == null) {
      return null;
    }

    EnumInfo enumInfo = resolveEnumInfo(arguments);
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

  // ── 导入 ──

  @Override
  public boolean supportsImport(Class<?> targetType) {
    return true;
  }

  @Override
  public Object doConvertForImport(Object input, List<Object> arguments, Class<?> targetType) {
    if (input == null) {
      return null;
    }

    EnumInfo enumInfo = resolveEnumInfo(List.of(targetType));
    if (enumInfo == EnumInfo.EMPTY) {
      throw new DataConvertException("枚举转换失败：枚举配置无效");
    }

    String value = input.toString();

    // 先尝试按枚举名称匹配（支持忽略大小写）
    Enum<?> enumConstant = findEnumConstant(enumInfo.type(), value);
    if (enumConstant != null) {
      return enumConstant.name();
    }

    // 再尝试按方法返回值匹配（反向：label → 枚举名称）
    for (Enum<?> constant : enumInfo.type().getEnumConstants()) {
      try {
        Object result = enumInfo.method().invoke(constant);
        if (result != null && result.toString().equals(value)) {
          return constant.name();
        }
      } catch (Exception e) {
        // ignore
      }
    }

    throw new DataConvertException("枚举转换失败：值[{}]在枚举[{}]中不存在", value, enumInfo.type().getName());
  }

  // ── 公共方法 ──

  private EnumInfo resolveEnumInfo(List<Object> arguments) {
    String enumClassAndMethodName = Strings.value(arguments.getFirst(), "");
    return ENUM_CACHE.computeIfAbsent(enumClassAndMethodName, k -> {
      Matcher matcher = PATTERN.matcher(enumClassAndMethodName);
      if (!matcher.matches()) {
        return EnumInfo.EMPTY;
      }

      String className = matcher.group(1);
      String methodName = matcher.group(4);
      try {
        Class<?> clazz = Class.forName(className);
        if (!clazz.isEnum()) {
          logger.error("类 [{}] 不是枚举类", className);
          return EnumInfo.EMPTY;
        }
        Method method = clazz.getMethod(methodName);
        //noinspection unchecked
        return new EnumInfo((Class<? extends Enum<?>>) clazz, method);
      } catch (ClassNotFoundException e) {
        logger.error("枚举类 [{}] 不存在", className);
        return EnumInfo.EMPTY;
      } catch (NoSuchMethodException e) {
        logger.error("枚举类方法 [{}#{}()] 不存在", className, methodName);
        return EnumInfo.EMPTY;
      }
    });
  }

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
}
