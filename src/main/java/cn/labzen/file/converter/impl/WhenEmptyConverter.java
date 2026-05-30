package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.file.converter.exportable.ExportableConverter;
import cn.labzen.tool.util.Strings;

import java.util.List;

/**
 * 当输入值为空或null时返回指定默认值（仅导出）
 *
 * @author labzen
 */
@DataConverter(name = Converter.WHEN_EMPTY_NAME, priority = Converter.WHEN_EMPTY_PRIORITY)
public class WhenEmptyConverter implements ExportableConverter<String> {

  @Override
  public String doConvertForExport(Object input, List<Object> arguments) {
    String defaultValue = Strings.value(arguments.getFirst(), "");
    if (input == null) {
      return defaultValue;
    }
    return input.toString().isEmpty() ? defaultValue : input.toString();
  }

  @Override
  public boolean supportsExport(Class<?> sourceType) {
    return true;
  }
}
