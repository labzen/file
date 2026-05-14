package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.CacheableConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.tool.util.Strings;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * 日期转换器
 * <p>
 * 将日期/时间类型转换为字符串
 * <ul>
 *   <li>输入支持: Date, LocalDate, LocalDateTime, LocalTime</li>
 *   <li>输出: String</li>
 * </ul>
 *
 * @author labzen
 */
@DataConverter(name = Converter.DATE_NAME, priority = Converter.DATE_PRIORITY)
public class DateConverter extends CacheableConverter<String> {

  @Override
  public boolean supports(Class<?> type) {
    return Date.class.isAssignableFrom(type)
      || LocalDate.class.isAssignableFrom(type)
      || LocalDateTime.class.isAssignableFrom(type)
      || LocalTime.class.isAssignableFrom(type);
  }

  @Override
  protected String doConvert(Object input, List<Object> arguments) {
    if (input == null) {
      return null;
    }

    String pattern = Strings.value(arguments.getFirst(), "yyyy-MM-dd HH:mm:ss");
    if (pattern == null || pattern.isBlank()) {
      pattern = "yyyy-MM-dd HH:mm:ss";
    }

    try {
      switch (input) {
        case Date date -> {
          SimpleDateFormat sdf = new SimpleDateFormat(pattern);
          return sdf.format(date);
        }
        case LocalDateTime localDateTime -> {
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
          return localDateTime.format(formatter);
        }
        case LocalDate localDate -> {
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
          return localDate.format(formatter);
        }
        case LocalTime localTime -> {
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
          return localTime.format(formatter);
        }
        default -> {
        }
      }
      return input.toString();
    } catch (Exception e) {
      return "convert-number-failed";
    }
  }
}
