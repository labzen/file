package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.tool.util.Strings;

import java.util.List;

/**
 * 当输入值为空或null时返回指定值
 * <p>
 * <ul>
 *   <li>输入支持: 任意类型</li>
 *   <li>输出: String</li>
 * </ul>
 *
 * @author labzen
 */
@DataConverter(name = Converter.WHEN_EMPTY_NAME, priority = Converter.WHEN_EMPTY_PRIORITY)
public class WhenEmptyConverter implements Converter<String> {


  @Override
  public boolean supports(Class<?> type) {
    return true;
  }

  @Override
  public String convert(Object input, List<Object> arguments) {
    String defaultValue = Strings.value(arguments.getFirst(), "");

    if (input == null) {
      return defaultValue;
    }

    return input.toString().isEmpty() ? defaultValue : input.toString();
  }
}
