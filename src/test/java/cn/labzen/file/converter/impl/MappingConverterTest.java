package cn.labzen.file.converter.impl;

import cn.labzen.file.converter.ExportableConverter;
import cn.labzen.file.converter.ImportableConverter;
import cn.labzen.file.exception.DataConvertException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Mapping 转换器测试")
class MappingConverterTest {

  private final MappingConverter converter = new MappingConverter();

  // ==================== 接口类型 ====================

  @Test
  @DisplayName("实现 ExportableConverter 和 ImportableConverter 双向接口")
  void testInterfaceType() {
    assertInstanceOf(ExportableConverter.class, converter);
    assertInstanceOf(ImportableConverter.class, converter);
  }

  // ==================== supportsExport ====================

  @Test
  @DisplayName("导出方向始终返回 true（支持任意类型）")
  void testSupportsExport() {
    assertTrue(converter.supportsExport(String.class));
    assertTrue(converter.supportsExport(Integer.class));
    assertTrue(converter.supportsExport(Object.class));
  }

  // ==================== supportsImport ====================

  @Test
  @DisplayName("导入方向始终返回 true（支持任意类型）")
  void testSupportsImport() {
    assertTrue(converter.supportsImport(String.class));
    assertTrue(converter.supportsImport(Integer.class));
    assertTrue(converter.supportsImport(Object.class));
  }

  // ==================== doConvertForExport ====================

  @Test
  @DisplayName("导出：映射表中存在对应键时返回映射值")
  void testExportExistingKey() {
    Map<String, String> mapping = Map.of("1", "男", "2", "女");
    String result = converter.doConvertForExport("1", List.of((Object) mapping));
    assertEquals("男", result);
  }

  @Test
  @DisplayName("导出：映射表中不存在对应键时返回 unknown")
  void testExportMissingKey() {
    Map<String, String> mapping = Map.of("1", "男", "2", "女");
    String result = converter.doConvertForExport("3", List.of((Object) mapping));
    assertEquals("unknown", result);
  }

  @Test
  @DisplayName("导出：null 输入返回 null")
  void testExportNull() {
    Map<String, String> mapping = Map.of("1", "男");
    String result = converter.doConvertForExport(null, List.of((Object) mapping));
    assertNull(result);
  }

  @Test
  @DisplayName("导出：非字符串输入自动转为字符串作为键")
  void testExportIntegerInput() {
    Map<String, String> mapping = Map.of("1", "启用", "2", "禁用");
    String result = converter.doConvertForExport(1, List.of((Object) mapping));
    assertEquals("启用", result);
  }

  // ==================== doConvertForImport ====================

  @Test
  @DisplayName("导入：映射值转换为映射键")
  void testImportExistingValue() {
    Map<String, String> mapping = Map.of("1", "启用", "2", "禁用");
    Object result = converter.doConvertForImport("启用", List.of((Object) mapping), String.class);
    assertEquals("1", result);
  }

  @Test
  @DisplayName("导入：映射值不存在时抛出 DataConvertException")
  void testImportMissingValue() {
    Map<String, String> mapping = Map.of("1", "启用", "2", "禁用");
    assertThrows(DataConvertException.class,
      () -> converter.doConvertForImport("未知", List.of((Object) mapping), String.class));
  }

  @Test
  @DisplayName("导入：null 输入返回 null")
  void testImportNull() {
    Map<String, String> mapping = Map.of("1", "男");
    Object result = converter.doConvertForImport(null, List.of((Object) mapping), String.class);
    assertNull(result);
  }
}
