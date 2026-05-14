package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.tool.util.Strings;

import java.util.List;

/**
 * 当输入值为 null 时返回空字符串
 * <p>
 * <ul>
 *   <li>输入支持: 任意类型</li>
 *   <li>输出: String</li>
 * </ul>
 *
 * @author labzen
 */
@DataConverter(name = Converter.WHEN_NULL_NAME, priority = Converter.WHEN_NULL_PRIORITY)
public class WhenNullConverter implements Converter<String> {

  @Override
  public boolean supports(Class<?> type) {
    return true;
  }

  @Override
  public String convert(Object input, List<Object> arguments) {
    return input == null ? Strings.value(arguments.getFirst(), "") : input.toString();
  }
}
