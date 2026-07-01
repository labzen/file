package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.file.converter.ExportableConverter;
import cn.labzen.tool.util.Strings;

import java.util.List;

/**
 * 当输入值为 null 时返回指定默认值（仅导出）
 *
 * @author labzen
 */
@DataConverter(name = Converter.WHEN_NULL_NAME,
  exportPriority = Converter.WHEN_NULL_EXPORT_PRIORITY, importPriority = Converter.UNUSED_PRIORITY)
public class WhenNullConverter implements ExportableConverter<String> {

  @Override
  public String doConvertForExport(Object input, List<Object> arguments) {
    return input == null ? Strings.value(arguments.getFirst(), "") : input.toString();
  }

  @Override
  public boolean supportsExport(Class<?> sourceType) {
    return true;
  }
}
