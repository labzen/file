package cn.labzen.file.converter.impl;

import cn.labzen.file.converter.ExportableConverter;
import cn.labzen.file.converter.ImportableConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

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
  @DisplayName("导出：未指定格式时返回转换失败标记")
  void testExportWithoutPattern() {
    String result = converter.doConvertForExport(3.14, List.of());
    assertEquals("convert-number-failed", result);
  }

  @Test
  @DisplayName("导出：null 输入返回空字符串")
  void testExportNull() {
    String result = converter.doConvertForExport(null, List.of("#,##0.00"));
    assertEquals("", result);
  }

  // ==================== doConvertForImport ====================

  @Test
  @DisplayName("导入：未指定格式时抛出 NoSuchElementException")
  void testImportInteger() {
    assertThrows(NoSuchElementException.class,
      () -> converter.doConvertForImport("42", List.of(), Integer.class));
  }

  @Test
  @DisplayName("导入：未指定格式时抛出 NoSuchElementException")
  void testImportBigDecimal() {
    assertThrows(NoSuchElementException.class,
      () -> converter.doConvertForImport("123.45", List.of(), BigDecimal.class));
  }

  @Test
  @DisplayName("导入：未指定格式时抛出 NoSuchElementException")
  void testImportDouble() {
    assertThrows(NoSuchElementException.class,
      () -> converter.doConvertForImport("3.14", List.of(), Double.class));
  }

  @Test
  @DisplayName("导入：未指定格式时抛出 NoSuchElementException")
  void testImportLong() {
    assertThrows(NoSuchElementException.class,
      () -> converter.doConvertForImport("9999999999", List.of(), Long.class));
  }

  @Test
  @DisplayName("导入：未指定格式时抛出 NoSuchElementException")
  void testImportInvalidFormat() {
    assertThrows(NoSuchElementException.class,
      () -> converter.doConvertForImport("not-a-number", List.of(), Integer.class));
  }

  @Test
  @DisplayName("导入：null 输入返回 null")
  void testImportNull() {
    Object result = converter.doConvertForImport(null, List.of(), Integer.class);
    assertNull(result);
  }
}
