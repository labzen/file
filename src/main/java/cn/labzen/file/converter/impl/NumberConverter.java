package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.file.converter.exportable.ExportableConverter;
import cn.labzen.file.converter.importable.ImportableConverter;
import cn.labzen.file.exception.DataConvertException;
import cn.labzen.tool.util.Strings;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数字转换器（导出+导入双向）
 * <p>
 * 导出：数字类型 → 格式化字符串
 * 导入：字符串 → 数字类型
 *
 * @author labzen
 */
@DataConverter(name = Converter.NUMBER_NAME, priority = Converter.NUMBER_PRIORITY)
public class NumberConverter implements ExportableConverter<String>, ImportableConverter {

  private static final Map<String, DecimalFormat> FORMAT_CACHE = new ConcurrentHashMap<>();

  // ── 导出 ──

  @Override
  public boolean supportsExport(Class<?> sourceType) {
    return Number.class.isAssignableFrom(sourceType);
  }

  @Override
  public String doConvertForExport(Object input, List<Object> arguments) {
    if (input == null) {
      return "";
    }

    try {
      double value;
      switch (input) {
        case Double v -> value = v;
        case Float v -> value = v.doubleValue();
        case Long l -> value = l.doubleValue();
        case Integer i -> value = i.doubleValue();
        case Short i -> value = i.doubleValue();
        case Number number -> value = number.doubleValue();
        default -> {
          return input.toString();
        }
      }

      String pattern = Strings.value(arguments.getFirst(), "");
      if (Strings.isBlank(pattern)) {
        return String.valueOf(value);
      }

      DecimalFormat format = FORMAT_CACHE.computeIfAbsent(pattern, DecimalFormat::new);
      return format.format(value);
    } catch (Exception e) {
      return "convert-number-failed";
    }
  }

  // ── 导入 ──

  @Override
  public boolean supportsImport(Class<?> targetType) {
    return Number.class.isAssignableFrom(targetType);
  }

  @Override
  public Object doConvertForImport(Object input, List<Object> arguments, Class<?> targetType) {
    if (input == null) {
      return null;
    }

    String value = input.toString().trim();

    // 如果有格式化pattern，先尝试用DecimalFormat解析
    String pattern = Strings.value(arguments.getFirst(), "");
    if (Strings.isNotBlank(pattern)) {
      try {
        DecimalFormat format = FORMAT_CACHE.computeIfAbsent(pattern, key -> {
          DecimalFormat df = new DecimalFormat(key);
          df.setParseBigDecimal(true);
          return df;
        });
        Number parsed = format.parse(value);
        return convertToTargetType(parsed, targetType);
      } catch (ParseException e) {
        throw new DataConvertException("数字格式解析失败：[{}]，期望格式：[{}]", value, pattern);
      }
    }

    // 无pattern，直接解析
    try {
      BigDecimal decimal = new BigDecimal(value);
      return convertToTargetType(decimal, targetType);
    } catch (NumberFormatException e) {
      throw new DataConvertException("数字转换失败：[{}]", value);
    }
  }

  private Object convertToTargetType(Number number, Class<?> targetType) {
    if (BigDecimal.class.isAssignableFrom(targetType)) {
      if (number instanceof BigDecimal bd) return bd;
      return new BigDecimal(number.toString());
    } else if (Double.class.isAssignableFrom(targetType) || double.class.isAssignableFrom(targetType)) {
      return number.doubleValue();
    } else if (Float.class.isAssignableFrom(targetType) || float.class.isAssignableFrom(targetType)) {
      return number.floatValue();
    } else if (Long.class.isAssignableFrom(targetType) || long.class.isAssignableFrom(targetType)) {
      return number.longValue();
    } else if (Integer.class.isAssignableFrom(targetType) || int.class.isAssignableFrom(targetType)) {
      return number.intValue();
    } else if (Short.class.isAssignableFrom(targetType) || short.class.isAssignableFrom(targetType)) {
      return number.shortValue();
    } else if (Byte.class.isAssignableFrom(targetType) || byte.class.isAssignableFrom(targetType)) {
      return number.byteValue();
    }
    return number;
  }
}
