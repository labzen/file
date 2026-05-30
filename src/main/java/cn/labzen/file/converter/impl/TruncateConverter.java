package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.file.converter.exportable.ExportableConverter;
import cn.labzen.tool.util.Objects;
import cn.labzen.tool.util.Strings;

import java.util.List;

/**
 * 字符串缩短转换器（仅导出）
 *
 * @author labzen
 */
@DataConverter(name = Converter.TRUNCATE_NAME, priority = Converter.TRUNCATE_PRIORITY)
public class TruncateConverter implements ExportableConverter<String> {

  private static final String DEFAULT_ELLIPSIS = "...";
  private static final int DEFAULT_ELLIPSIS_LENGTH = 3;

  @Override
  public boolean supportsExport(Class<?> sourceType) {
    return String.class.isAssignableFrom(sourceType);
  }

  @Override
  public String doConvertForExport(Object input, List<Object> arguments) {
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

    if (value.length() > length) {
      int effectiveMaxLength = length - DEFAULT_ELLIPSIS_LENGTH;
      return value.substring(0, effectiveMaxLength) + DEFAULT_ELLIPSIS;
    }

    return value;
  }
}
