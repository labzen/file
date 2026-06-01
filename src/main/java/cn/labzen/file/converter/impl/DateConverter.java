package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.file.converter.ExportableConverter;
import cn.labzen.file.converter.ImportableConverter;
import cn.labzen.file.exception.DataConvertException;
import cn.labzen.tool.util.DateTimes;
import cn.labzen.tool.util.Strings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日期转换器（导出+导入双向）
 * <p>
 * 导出：日期/时间类型 → 字符串
 * 导入：字符串 → 日期/时间类型
 *
 * @author labzen
 */
@DataConverter(name = Converter.DATE_NAME, priority = Converter.DATE_PRIORITY)
public class DateConverter implements ExportableConverter<String>, ImportableConverter {

  private static final Map<String, DateTimeFormatter> DATETIME_FORMATTER_CACHE = new ConcurrentHashMap<>();

  protected static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

  private DateTimeFormatter getDateTimeFormatter(String pattern) {
    return DATETIME_FORMATTER_CACHE.computeIfAbsent(pattern, DateTimeFormatter::ofPattern);
  }

  private String resolvePattern(List<Object> arguments) {
    String pattern = Strings.value(arguments.getFirst(), DEFAULT_DATE_PATTERN);
    if (pattern == null || pattern.isBlank()) {
      pattern = DEFAULT_DATE_PATTERN;
    }
    return pattern;
  }

  // ── 导出 ──

  @Override
  public boolean supportsExport(Class<?> sourceType) {
    return Date.class.isAssignableFrom(sourceType)
      || LocalDate.class.isAssignableFrom(sourceType)
      || LocalDateTime.class.isAssignableFrom(sourceType)
      || LocalTime.class.isAssignableFrom(sourceType);
  }

  @Override
  public String doConvertForExport(Object input, List<Object> arguments) {
    if (input == null) {
      return null;
    }

    String pattern = resolvePattern(arguments);
    try {
      switch (input) {
        case Date date -> {
          return DateTimes.format(date, pattern);
        }
        case LocalDateTime localDateTime -> {
          DateTimeFormatter formatter = getDateTimeFormatter(pattern);
          return formatter.format(localDateTime);
        }
        case LocalDate localDate -> {
          DateTimeFormatter formatter = getDateTimeFormatter(pattern);
          return localDate.format(formatter);
        }
        case LocalTime localTime -> {
          DateTimeFormatter formatter = getDateTimeFormatter(pattern);
          return localTime.format(formatter);
        }
        default -> {
        }
      }
      return input.toString();
    } catch (Exception e) {
      return "convert-date-failed";
    }
  }

  // ── 导入 ──

  @Override
  public boolean supportsImport(Class<?> targetType) {
    return Date.class.isAssignableFrom(targetType)
      || LocalDate.class.isAssignableFrom(targetType)
      || LocalDateTime.class.isAssignableFrom(targetType)
      || LocalTime.class.isAssignableFrom(targetType);
  }

  @Override
  public Object doConvertForImport(Object input, List<Object> arguments, Class<?> targetType) {
    if (input == null) {
      return null;
    }

    String value = input.toString().trim();
    String pattern = resolvePattern(arguments);

    try {
      if (Date.class.isAssignableFrom(targetType)) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.parse(value);
      } else if (LocalDateTime.class.isAssignableFrom(targetType)) {
        DateTimeFormatter formatter = getDateTimeFormatter(pattern);
        return LocalDateTime.parse(value, formatter);
      } else if (LocalDate.class.isAssignableFrom(targetType)) {
        DateTimeFormatter formatter = getDateTimeFormatter(pattern);
        return LocalDate.parse(value, formatter);
      } else if (LocalTime.class.isAssignableFrom(targetType)) {
        DateTimeFormatter formatter = getDateTimeFormatter(pattern);
        return LocalTime.parse(value, formatter);
      }
    } catch (ParseException e) {
      throw new DataConvertException("日期格式解析失败：[{}]，期望格式：[{}]", value, pattern);
    } catch (Exception e) {
      throw new DataConvertException("日期转换失败：[{}]", value);
    }

    throw new DataConvertException("不支持的目标日期类型：[{}]", targetType.getName());
  }
}
