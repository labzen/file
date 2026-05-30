package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.file.converter.exportable.ExportableConverter;
import cn.labzen.file.converter.importable.ImportableConverter;
import cn.labzen.file.exception.DataConvertException;
import cn.labzen.tool.util.Strings;

import java.util.List;
import java.util.Objects;

/**
 * 布尔转换器（导出+导入双向）
 * <p>
 * 导出：Boolean → 文本（trueText / falseText）
 * 导入：文本 → Boolean（反向匹配）
 *
 * @author labzen
 */
@DataConverter(name = Converter.BOOL_NAME, priority = Converter.BOOL_PRIORITY)
public class BoolConverter implements ExportableConverter<String>, ImportableConverter {

  // ── 导出 ──

  @Override
  public String doConvertForExport(Object input, List<Object> arguments) throws DataConvertException {
    if (input == null) {
      return null;
    }
    if (arguments == null || arguments.size() != 2) {
      return input.toString();
    }
    boolean value = (Boolean) input;
    return value ? Strings.value(arguments.getFirst(), "true") : Strings.value(arguments.getLast(), "false");
  }

  @Override
  public boolean supportsExport(Class<?> sourceType) {
    return Boolean.class.isAssignableFrom(sourceType) || boolean.class.isAssignableFrom(sourceType);
  }

  // ── 导入 ──

  @Override
  public Object doConvertForImport(Object input, List<Object> arguments, Class<?> targetType) {
    if (input == null) {
      return null;
    }
    if (arguments == null || arguments.size() != 2) {
      return input;
    }

    String value = input.toString().trim();

    // 如果有 trueText/falseText 参数，按参数匹配
    if (Objects.equals(value, arguments.getFirst())) {
      return true;
    }
    if (Objects.equals(value, arguments.getLast())) {
      return false;
    }
//    String trueText = Strings.value(arguments.getFirst(), "true");
//    String falseText = Strings.value(arguments.getLast(), "false");
//    if (trueText.equals(value)) {
//      return true;
//    }
//    if (falseText.equals(value)) {
//      return false;
//    }

//    // 默认匹配常见文本  !!!!!!! 已经配置bool转换器了，就严格按照配置的转换，这里不做处理
//    if ("true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value) || "是".equals(value)) {
//      return true;
//    }
//    if ("false".equalsIgnoreCase(value) || "0".equals(value) || "no".equalsIgnoreCase(value) || "否".equals(value)) {
//      return false;
//    }

    throw new DataConvertException("布尔转换失败：值[{}]无法识别为布尔值", value);
  }

  @Override
  public boolean supportsImport(Class<?> targetType) {
    return Boolean.class.isAssignableFrom(targetType) || boolean.class.isAssignableFrom(targetType);
  }
}
