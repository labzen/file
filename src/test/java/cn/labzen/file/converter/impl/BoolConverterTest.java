package cn.labzen.file.converter.impl;

import cn.labzen.file.converter.ExportableConverter;
import cn.labzen.file.converter.ImportableConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Bool 转换器测试")
class BoolConverterTest {

  private final BoolConverter converter = new BoolConverter();

  // ==================== 接口类型 ====================

  @Test
  @DisplayName("实现 ExportableConverter 和 ImportableConverter 双向接口")
  void testInterfaceType() {
    assertInstanceOf(ExportableConverter.class, converter);
    assertInstanceOf(ImportableConverter.class, converter);
  }

  // ==================== supportsExport ====================

  @Test
  @DisplayName("导出方向支持 Boolean 和 boolean 类型")
  void testSupportsExport() {
    assertTrue(converter.supportsExport(Boolean.class));
    assertTrue(converter.supportsExport(boolean.class));
  }

  @Test
  @DisplayName("导出方向不支持非布尔类型")
  void testNotSupportsExportOtherTypes() {
    assertFalse(converter.supportsExport(String.class));
    assertFalse(converter.supportsExport(Integer.class));
  }

  // ==================== supportsImport ====================

  @Test
  @DisplayName("导入方向支持 Boolean 和 boolean 类型")
  void testSupportsImport() {
    assertTrue(converter.supportsImport(Boolean.class));
    assertTrue(converter.supportsImport(boolean.class));
  }

  @Test
  @DisplayName("导入方向不支持非布尔类型")
  void testNotSupportsImportOtherTypes() {
    assertFalse(converter.supportsImport(String.class));
    assertFalse(converter.supportsImport(Integer.class));
  }

  // ==================== doConvertForExport ====================

  @Test
  @DisplayName("导出：true 转换为指定文本")
  void testExportTrue() {
    String result = converter.doConvertForExport(true, List.of("是", "否"));
    assertEquals("是", result);
  }

  @Test
  @DisplayName("导出：false 转换为指定文本")
  void testExportFalse() {
    String result = converter.doConvertForExport(false, List.of("是", "否"));
    assertEquals("否", result);
  }

  @Test
  @DisplayName("导出：Boolean.TRUE 转换")
  void testExportBooleanTrue() {
    String result = converter.doConvertForExport(Boolean.TRUE, List.of("Yes", "No"));
    assertEquals("Yes", result);
  }

  @Test
  @DisplayName("导出：Boolean.FALSE 转换")
  void testExportBooleanFalse() {
    String result = converter.doConvertForExport(Boolean.FALSE, List.of("Yes", "No"));
    assertEquals("No", result);
  }

  @Test
  @DisplayName("导出：未指定参数时 true 返回字符串 true")
  void testExportTrueDefault() {
    String result = converter.doConvertForExport(true, List.of());
    assertEquals("true", result);
  }

  @Test
  @DisplayName("导出：未指定第二个参数时 false 返回字符串 false")
  void testExportFalseDefault() {
    String result = converter.doConvertForExport(false, List.of("是"));
    assertEquals("false", result);
  }

  @Test
  @DisplayName("导出：null 输入返回 null")
  void testExportNull() {
    String result = converter.doConvertForExport(null, List.of("是", "否"));
    assertNull(result);
  }

  @Test
  @DisplayName("导出：中文映射")
  void testExportChinese() {
    assertEquals("成功", converter.doConvertForExport(true, List.of("成功", "失败")));
    assertEquals("失败", converter.doConvertForExport(false, List.of("成功", "失败")));
  }

  // ==================== doConvertForImport ====================

  @Test
  @DisplayName("导入：指定文本转换为 Boolean.TRUE")
  void testImportTrue() {
    Object result = converter.doConvertForImport("是", List.of("是", "否"), Boolean.class);
    assertEquals(true, result);
  }

  @Test
  @DisplayName("导入：指定文本转换为 Boolean.FALSE")
  void testImportFalse() {
    Object result = converter.doConvertForImport("否", List.of("是", "否"), Boolean.class);
    assertEquals(false, result);
  }

  @Test
  @DisplayName("导入：未提供参数时原样返回字符串")
  void testImportDefaultTrue() {
    Object result = converter.doConvertForImport("true", List.of(), Boolean.class);
    assertEquals("true", result);
  }

  @Test
  @DisplayName("导入：未提供参数时原样返回字符串")
  void testImportDefaultFalse() {
    Object result = converter.doConvertForImport("false", List.of(), Boolean.class);
    assertEquals("false", result);
  }

  @Test
  @DisplayName("导入：未提供参数时无效文本原样返回，不抛异常")
  void testImportInvalidText() {
    Object result = converter.doConvertForImport("maybe", List.of(), Boolean.class);
    assertEquals("maybe", result);
  }

  @Test
  @DisplayName("导入：null 输入返回 null")
  void testImportNull() {
    Object result = converter.doConvertForImport(null, List.of("是", "否"), Boolean.class);
    assertNull(result);
  }
}
