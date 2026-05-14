package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.Converter;

import java.util.List;
import java.util.Map;

/**
 * 映射转换器
 * <p>
 * 将输入值根据映射关系转换为目标字符串
 * <ul>
 *   <li>输入支持: 任意类型</li>
 *   <li>输出: String</li>
 * </ul>
 *
 * @author labzen
 */
@DataConverter(name = Converter.MAPPING_NAME, priority = Converter.MAPPING_PRIORITY)
public class MappingConverter implements Converter<String> {

  @Override
  public String convert(Object input, List<Object> arguments) {
    if (input == null) {
      return null;
    }

    @SuppressWarnings("unchecked")
    Map<String, String> mapping = (Map<String, String>) arguments.getFirst();
    String key = input.toString();
    return mapping.getOrDefault(key, "unknown");
  }

  @Override
  public boolean supports(Class<?> type) {
    return true; // 支持所有类型
  }

}
