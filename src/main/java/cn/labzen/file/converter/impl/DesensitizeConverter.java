package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.CacheableConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.file.exception.DataConvertException;
import cn.labzen.tool.util.Strings;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 脱敏转换器
 * <p>
 * 对输入值进行脱敏处理
 * <ul>
 *   <li>输入支持: 任意类型（会调用toString()）</li>
 *   <li>输出: String</li>
 * </ul>
 *
 * @author labzen
 */
@DataConverter(name = Converter.DESENSITIZE_NAME, priority = Converter.DESENSITIZE_PRIORITY)
public class DesensitizeConverter extends CacheableConverter<String> {

  private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

  @Override
  public boolean supports(Class<?> type) {
    return String.class.isAssignableFrom(type);
  }

  @Override
  protected String doConvert(Object input, List<Object> arguments) {
    if (input == null) {
      return null;
    }
    if (arguments.size() != 2) {
      throw new DataConvertException("数据文件导出的desensitize转换器参数数量错误");
    }

    String regexString = Strings.value(arguments.getFirst(), "");
    String replacement = Strings.value(arguments.getLast(), "");
    String key = regexString + "|@|" + replacement;
    Pattern pattern = PATTERN_CACHE.computeIfAbsent(key, k -> Pattern.compile(regexString));
    return pattern.matcher(input.toString()).replaceAll(replacement);
  }
}
