package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.file.converter.ExportableConverter;
import cn.labzen.tool.util.Strings;

import java.util.List;

/**
 * 前缀转换器（仅导出）
 *
 * @author labzen
 */
@DataConverter(name = Converter.PREFIX_NAME,
  exportPriority = Converter.PREFIX_EXPORT_PRIORITY, importPriority = Converter.UNUSED_PRIORITY)
public class PrefixConverter implements ExportableConverter<String> {

  @Override
  public boolean supportsExport(Class<?> sourceType) {
    return true;
  }

  @Override
  public String doConvertForExport(Object input, List<Object> arguments) {
    if (input == null) {
      return null;
    }

    String prefix = Strings.value(arguments.getFirst(), "");
    return prefix + input;
  }
}
