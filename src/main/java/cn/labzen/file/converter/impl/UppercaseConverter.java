package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.file.converter.importable.ImportableConverter;

import java.util.List;

/**
 * 大写转换器（仅导入）
 * <p>
 * 将字符串转换为大写
 *
 * @author labzen
 */
@DataConverter(name = Converter.UPPERCASE_NAME, priority = Converter.UPPERCASE_PRIORITY)
public class UppercaseConverter implements ImportableConverter {

  @Override
  public boolean supportsImport(Class<?> targetType) {
    return String.class.isAssignableFrom(targetType);
  }

  @Override
  public Object doConvertForImport(Object input, List<Object> arguments, Class<?> targetType) {
    if (input == null) {
      return null;
    }
    return input.toString().toUpperCase();
  }
}
