package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.file.converter.ExportableConverter;
import cn.labzen.file.converter.ImportableConverter;
import cn.labzen.file.exception.DataConvertException;

import java.util.List;
import java.util.Map;

/**
 * 映射转换器（导出+导入双向）
 * <p>
 * mapping语义约定：key=存储值（Bean字段值/数据库值），value=展示值（用户看到的文本）
 * <ul>
 *   <li>导出：key → value（正向）</li>
 *   <li>导入：value → key（反向）</li>
 * </ul>
 *
 * @author labzen
 */
@DataConverter(name = Converter.MAPPING_NAME,
  exportPriority = Converter.MAPPING_EXPORT_PRIORITY, importPriority = Converter.MAPPING_IMPORT_PRIORITY)
public class MappingConverter implements ExportableConverter<String>, ImportableConverter {

  // ── 导出：key → value ──

  @Override
  public String doConvertForExport(Object input, List<Object> arguments) {
    if (input == null) {
      return null;
    }

    @SuppressWarnings("unchecked")
    Map<String, String> mapping = (Map<String, String>) arguments.getFirst();
    String key = input.toString();
    return mapping.getOrDefault(key, "unknown");
  }

  @Override
  public boolean supportsExport(Class<?> sourceType) {
    return true;
  }

  // ── 导入：value → key ──

  @Override
  public Object doConvertForImport(Object input, List<Object> arguments, Class<?> targetType) {
    if (input == null) {
      return null;
    }

    @SuppressWarnings("unchecked")
    Map<String, String> mapping = (Map<String, String>) arguments.getFirst();
    String value = input.toString();

    // 在mapping的values中查找，返回对应的key
    for (Map.Entry<String, String> entry : mapping.entrySet()) {
      if (entry.getValue().equals(value)) {
        return entry.getKey();
      }
    }

    throw new DataConvertException("映射转换失败：值[{}]在映射中不存在", value);
  }

  @Override
  public boolean supportsImport(Class<?> targetType) {
    return true;
  }
}
