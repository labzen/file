package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.tool.util.Strings;

import java.util.List;

/**
 * 布尔转换器
 * <p>
 * 将布尔值转换为字符串表示
 * <ul>
 *   <li>输入支持: Boolean</li>
 *   <li>输出: String</li>
 * </ul>
 * 接受两个参数：第一个为 true 时的字符串，第二个为 false 时的字符串
 *
 * @author labzen
 */
@DataConverter(name = Converter.BOOL_NAME, priority = Converter.BOOL_PRIORITY)
public class BoolConverter implements Converter<String> {

  @Override
  public boolean supports(Class<?> type) {
    return Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type);
  }

  @Override
  public String convert(Object input, List<Object> arguments) {
    if (input == null) {
      return null;
    }

    if (arguments == null || arguments.size() != 2) {
      return input.toString();
    }

    boolean value = (Boolean) input;
    return value ? Strings.value(arguments.getFirst(), "true") : Strings.value(arguments.getLast(), "false");
  }
}
