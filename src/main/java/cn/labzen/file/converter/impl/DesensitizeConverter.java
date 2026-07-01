package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.file.converter.ExportableConverter;
import cn.labzen.file.exception.DataConvertException;
import cn.labzen.tool.util.Strings;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 脱敏转换器（仅导出）
 *
 * @author labzen
 */
@DataConverter(name = Converter.DESENSITIZE_NAME,
  exportPriority = Converter.DESENSITIZE_EXPORT_PRIORITY, importPriority = Converter.UNUSED_PRIORITY)
public class DesensitizeConverter implements ExportableConverter<String> {

  private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

  @Override
  public boolean supportsExport(Class<?> sourceType) {
    return String.class.isAssignableFrom(sourceType);
  }

  @Override
  public String doConvertForExport(Object input, List<Object> arguments) throws DataConvertException {
    if (input == null) {
      return null;
    }
    if (arguments.size() != 2) {
      throw new DataConvertException("数据文件导出的desensitize转换器参数数量错误");
    }

    String regexString = Strings.value(arguments.getFirst(), "");
    String replacement = Strings.value(arguments.getLast(), "");
    Pattern pattern = PATTERN_CACHE.computeIfAbsent(regexString, Pattern::compile);
    return pattern.matcher(input.toString()).replaceAll(replacement);
  }
}
