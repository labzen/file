package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.CacheableConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.tool.util.Strings;

import java.util.List;

/**
 * 后缀转换器
 * <p>
 * 为输入值添加后缀
 * <ul>
 *   <li>输入支持: 任意类型</li>
 *   <li>输出: String</li>
 * </ul>
 *
 * @author labzen
 */
@DataConverter(name = Converter.SUFFIX_NAME, priority = Converter.SUFFIX_PRIORITY)
public class SuffixConverter extends CacheableConverter<String> {

  @Override
  public boolean supports(Class<?> type) {
    return true; // 支持所有类型
  }

  @Override
  protected String doConvert(Object input, List<Object> arguments) {
    if (input == null) {
      return null;
    }

    String suffix = Strings.value(arguments.getFirst(), "");
    String value = input.toString();
    return value + suffix;
  }
}
