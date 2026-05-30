package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.file.converter.exportable.ExportableConverter;
import cn.labzen.tool.util.Strings;

import java.util.List;

/**
 * 后缀转换器（仅导出）
 *
 * @author labzen
 */
@DataConverter(name = Converter.SUFFIX_NAME, priority = Converter.SUFFIX_PRIORITY)
public class SuffixConverter implements ExportableConverter<String> {

  @Override
  public boolean supportsExport(Class<?> sourceType) {
    return true;
  }

  @Override
  public String doConvertForExport(Object input, List<Object> arguments) {
    if (input == null) {
      return null;
    }

    String suffix = Strings.value(arguments.getFirst(), "");
    return input + suffix;
  }
}
