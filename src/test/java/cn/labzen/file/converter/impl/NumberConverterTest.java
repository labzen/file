package cn.labzen.file.converter.impl;

import cn.labzen.file.converter.exportable.ExportableConverter;
import cn.labzen.file.converter.importable.ImportableConverter;
import cn.labzen.file.exception.DataConvertException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Number 转换器测试")
class NumberConverterTest {

  private final NumberConverter converter = new NumberConverter();

  // ==================== 接口类型 ====================

  @Test
  @DisplayName("实现 ExportableConverter 和 ImportableConverter 双向接口")
  void testInterfaceType() {
    assertInstanceOf(ExportableConverter.class, converter);
    assertInstanceOf(ImportableConverter.class, converter);
  }

  // ==================== supportsExport ====================

  @Test
  @DisplayName("导出方向支持 Number 及其子类")
  void testSupportsExport() {
    assertTrue(converter.supportsExport(Double.class));
    assertTrue(converter.supportsExport(Integer.class));
    assertTrue(converter.supportsExport(BigDecimal.class));
    assertTrue(converter.supportsExport(Long.class));
  }

  @Test
  @DisplayName("导出方向不支持 String 类型")
  void testNotSupportsExportString() {
    assertFalse(converter.supportsExport(String.class));
  }

  // ==================== supportsImport ====================

  @Test
  @DisplayName("导入方向支持 Number 及其子类")
  void testSupportsImport() {
    assertTrue(converter.supportsImport(Integer.class));
    assertTrue(converter.supportsImport(Double.class));
    assertTrue(converter.supportsImport(BigDecimal.class));
    assertTrue(converter.supportsImport(Long.class));
  }

  @Test
  @DisplayName("导入方向不支持 String 类型")
  void testNotSupportsImportString() {
    assertFalse(converter.supportsImport(String.class));
  }

  // ==================== doConvertForExport ====================

  @Test
  @DisplayName("导出：Double 按千分位格式转换")
  void testExportDouble() {
    String result = converter.doConvertForExport(1234567.89, List.of("#,##0.00"));
    assertEquals("1,234,567.89", result);
  }

  @Test
  @DisplayName("导出：Integer 按指定格式转换")
  void testExportInteger() {
    String result = converter.doConvertForExport(42, List.of("0000"));
    assertEquals("0042", result);
  }

  @Test
  @DisplayName("导出：负数正确转换")
  void testExportNegative() {
    String result = converter.doConvertForExport(-99.5, List.of("#,##0.00"));
    assertEquals("-99.50", result);
  }

  @Test
  @DisplayName("导出：零值正确转换")
  void testExportZero() {
    String result = converter.doConvertForExport(0, List.of("#,##0.00"));
    assertEquals("0.00", result);
  }

  @Test
  @DisplayName("导出：BigDecimal 正确转换")
  void testExportBigDecimal() {
    String result = converter.doConvertForExport(new BigDecimal("9999.99"), List.of("#,##0.00"));
    assertEquals("9,999.99", result);
  }

  @Test
  @DisplayName("导出：未指定格式时返回原始字符串")
  void testExportWithoutPattern() {
    String result = converter.doConvertForExport(3.14, List.of());
    assertEquals("3.14", result);
  }

  @Test
  @DisplayName("导出：null 输入返回空字符串")
  void testExportNull() {
    String result = converter.doConvertForExport(null, List.of("#,##0.00"));
    assertEquals("", result);
  }

  // ==================== doConvertForImport ====================

  @Test
  @DisplayName("导入：字符串转换为 Integer")
  void testImportInteger() {
    Object result = converter.doConvertForImport("42", List.of(), Integer.class);
    assertEquals(42, result);
  }

  @Test
  @DisplayName("导入：字符串转换为 BigDecimal")
  void testImportBigDecimal() {
    Object result = converter.doConvertForImport("123.45", List.of(), BigDecimal.class);
    assertEquals(new BigDecimal("123.45"), result);
  }

  @Test
  @DisplayName("导入：字符串转换为 Double")
  void testImportDouble() {
    Object result = converter.doConvertForImport("3.14", List.of(), Double.class);
    assertEquals(3.14, result);
  }

  @Test
  @DisplayName("导入：字符串转换为 Long")
  void testImportLong() {
    Object result = converter.doConvertForImport("9999999999", List.of(), Long.class);
    assertEquals(9999999999L, result);
  }

  @Test
  @DisplayName("导入：无效数字格式抛出 DataConvertException")
  void testImportInvalidFormat() {
    assertThrows(DataConvertException.class,
      () -> converter.doConvertForImport("not-a-number", List.of(), Integer.class));
  }

  @Test
  @DisplayName("导入：null 输入返回 null")
  void testImportNull() {
    Object result = converter.doConvertForImport(null, List.of(), Integer.class);
    assertNull(result);
  }
}
