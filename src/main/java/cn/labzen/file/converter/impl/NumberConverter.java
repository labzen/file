package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.CacheableConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.tool.util.Strings;

import java.text.DecimalFormat;
import java.util.List;

/**
 * 数字转换器
 * <p>
 * 将数字类型转换为字符串
 * <ul>
 *   <li>输入支持: Float, Double, Integer, Long, Short, BigDecimal</li>
 *   <li>输出: String</li>
 * </ul>
 *
 * @author labzen
 */
@DataConverter(name = Converter.NUMBER_NAME, priority = Converter.NUMBER_PRIORITY)
public class NumberConverter extends CacheableConverter< String> {

  @Override
  public boolean supports(Class<?> type) {
    return Number.class.isAssignableFrom(type);
  }

  @Override
  protected String doConvert(Object input, List<Object> arguments) {
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
      if (pattern == null || pattern.isBlank()) {
        return String.valueOf(value);
      }

      DecimalFormat df = new DecimalFormat(pattern);
      return df.format(value);
    } catch (Exception e) {
      return "convert-number-failed";
    }
  }
}
