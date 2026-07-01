package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.file.converter.ImportableConverter;

import java.util.List;

/**
 * 小写转换器（仅导入）
 * <p>
 * 将字符串转换为小写
 *
 * @author labzen
 */
@DataConverter(name = Converter.LOWERCASE_NAME,
  exportPriority = Converter.UNUSED_PRIORITY, importPriority = Converter.LOWERCASE_IMPORT_PRIORITY)
public class LowercaseConverter implements ImportableConverter {

  @Override
  public boolean supportsImport(Class<?> targetType) {
    return String.class.isAssignableFrom(targetType);
  }

  @Override
  public Object doConvertForImport(Object input, List<Object> arguments, Class<?> targetType) {
    if (input == null) {
      return null;
    }
    return input.toString().toLowerCase();
  }
}
