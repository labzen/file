package cn.labzen.file.converter.impl;

import cn.labzen.file.converter.ExportableConverter;
import cn.labzen.file.converter.ImportableConverter;
import cn.labzen.file.exception.DataConvertException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Date 转换器测试")
class DateConverterTest {

  private final DateConverter converter = new DateConverter();

  // ==================== 接口类型 ====================

  @Test
  @DisplayName("实现 ExportableConverter 和 ImportableConverter 双向接口")
  void testInterfaceType() {
    assertInstanceOf(ExportableConverter.class, converter);
    assertInstanceOf(ImportableConverter.class, converter);
  }

  // ==================== supportsExport ====================

  @Test
  @DisplayName("导出方向支持 Date、LocalDate、LocalDateTime、LocalTime 类型")
  void testSupportsExport() {
    assertTrue(converter.supportsExport(Date.class));
    assertTrue(converter.supportsExport(LocalDate.class));
    assertTrue(converter.supportsExport(LocalDateTime.class));
    assertTrue(converter.supportsExport(LocalTime.class));
  }

  @Test
  @DisplayName("导出方向不支持 String 类型")
  void testNotSupportsExportString() {
    assertFalse(converter.supportsExport(String.class));
  }

  // ==================== supportsImport ====================

  @Test
  @DisplayName("导入方向支持 Date、LocalDate、LocalDateTime、LocalTime 类型")
  void testSupportsImport() {
    assertTrue(converter.supportsImport(Date.class));
    assertTrue(converter.supportsImport(LocalDate.class));
    assertTrue(converter.supportsImport(LocalDateTime.class));
    assertTrue(converter.supportsImport(LocalTime.class));
  }

  @Test
  @DisplayName("导入方向不支持 String 类型")
  void testNotSupportsImportString() {
    assertFalse(converter.supportsImport(String.class));
  }

  // ==================== doConvertForExport ====================

  @Test
  @DisplayName("导出：Date 类型按指定格式转换")
  void testExportDate() {
    Date date = new Date(1704067200000L); // 2024-01-01 00:00:00 UTC
    String result = converter.doConvertForExport(date, List.of("yyyy-MM-dd"));
    assertTrue(result.contains("2024"));
  }

  @Test
  @DisplayName("导出：LocalDate 类型按指定格式转换")
  void testExportLocalDate() {
    LocalDate date = LocalDate.of(2024, 6, 15);
    String result = converter.doConvertForExport(date, List.of("yyyy/MM/dd"));
    assertEquals("2024/06/15", result);
  }

  @Test
  @DisplayName("导出：LocalDateTime 类型按指定格式转换")
  void testExportLocalDateTime() {
    LocalDateTime dateTime = LocalDateTime.of(2024, 3, 8, 14, 30, 0);
    String result = converter.doConvertForExport(dateTime, List.of("yyyy-MM-dd HH:mm"));
    assertEquals("2024-03-08 14:30", result);
  }

  @Test
  @DisplayName("导出：LocalTime 类型按指定格式转换")
  void testExportLocalTime() {
    LocalTime time = LocalTime.of(9, 5, 30);
    String result = converter.doConvertForExport(time, List.of("HH:mm:ss"));
    assertEquals("09:05:30", result);
  }

  @Test
  @DisplayName("导出：null 输入返回 null")
  void testExportNull() {
    String result = converter.doConvertForExport(null, List.of("yyyy-MM-dd"));
    assertNull(result);
  }

  @Test
  @DisplayName("导出：未指定格式时抛出 NoSuchElementException")
  void testExportDefaultPattern() {
    LocalDate date = LocalDate.of(2024, 1, 15);
    assertThrows(NoSuchElementException.class,
      () -> converter.doConvertForExport(date, List.of()));
  }

  // ==================== doConvertForImport ====================

  @Test
  @DisplayName("导入：字符串按格式解析为 LocalDate")
  void testImportLocalDate() {
    Object result = converter.doConvertForImport("2024-01-15", List.of("yyyy-MM-dd"), LocalDate.class);
    assertEquals(LocalDate.of(2024, 1, 15), result);
  }

  @Test
  @DisplayName("导入：字符串按格式解析为 LocalDateTime")
  void testImportLocalDateTime() {
    Object result = converter.doConvertForImport("2024-03-08 14:30:00", List.of("yyyy-MM-dd HH:mm:ss"), LocalDateTime.class);
    assertEquals(LocalDateTime.of(2024, 3, 8, 14, 30, 0), result);
  }

  @Test
  @DisplayName("导入：字符串按格式解析为 LocalTime")
  void testImportLocalTime() {
    Object result = converter.doConvertForImport("09:05:30", List.of("HH:mm:ss"), LocalTime.class);
    assertEquals(LocalTime.of(9, 5, 30), result);
  }

  @Test
  @DisplayName("导入：字符串按格式解析为 Date")
  void testImportDate() {
    Object result = converter.doConvertForImport("2024-01-15", List.of("yyyy-MM-dd"), Date.class);
    assertNotNull(result);
    assertInstanceOf(Date.class, result);
  }

  @Test
  @DisplayName("导入：格式不匹配时抛出 DataConvertException")
  void testImportInvalidFormat() {
    assertThrows(DataConvertException.class,
      () -> converter.doConvertForImport("not-a-date", List.of("yyyy-MM-dd"), LocalDate.class));
  }

  @Test
  @DisplayName("导入：null 输入返回 null")
  void testImportNull() {
    Object result = converter.doConvertForImport(null, List.of("yyyy-MM-dd"), LocalDate.class);
    assertNull(result);
  }
}
