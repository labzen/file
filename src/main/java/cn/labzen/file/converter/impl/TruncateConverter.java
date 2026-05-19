package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.CacheableConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.tool.util.Objects;
import cn.labzen.tool.util.Strings;

import java.util.List;

/**
 * 字符串缩短转换器
 * <p>
 * 将输入字符串缩短到指定长度，并在末尾添加省略号
 * <ul>
 *   <li>输入支持: String</li>
 *   <li>输出: String</li>
 * </ul>
 *
 * @author labzen
 */
@DataConverter(name = Converter.TRUNCATE_NAME, priority = Converter.TRUNCATE_PRIORITY)
public class TruncateConverter extends CacheableConverter<String> {

  public static final String NAME = "truncate";
  private static final String DEFAULT_ELLIPSIS = "...";
  private static final int DEFAULT_ELLIPSIS_LENGTH = 3;

  @Override
  public boolean supports(Class<?> type) {
    return String.class.isAssignableFrom(type);
  }

  @Override
  protected String doConvert(Object input, List<Object> arguments) {
    if (input == null) {
      return null;
    }

    String value = input.toString();
    if (arguments == null || arguments.isEmpty()) {
      return value;
    }
    int length = Objects.canBeInt(Strings.value(arguments.getFirst(), "0"));
    if (length <= DEFAULT_ELLIPSIS_LENGTH) {
      return value;
    }

    // 如果包含省略号长度，最大长度需要减去省略号长度
    if (value.length() > length) {
      int effectiveMaxLength = length - DEFAULT_ELLIPSIS_LENGTH;
      return value.substring(0, effectiveMaxLength) + DEFAULT_ELLIPSIS;
    }

    return value;
  }
}
