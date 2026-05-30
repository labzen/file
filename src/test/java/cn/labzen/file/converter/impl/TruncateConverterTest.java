package cn.labzen.file.converter.impl;

import cn.labzen.file.converter.exportable.ExportableConverter;
import cn.labzen.file.converter.importable.ImportableConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Truncate 转换器测试")
class TruncateConverterTest {

  private final TruncateConverter converter = new TruncateConverter();

  // ==================== 接口类型 ====================

  @Test
  @DisplayName("仅实现 ExportableConverter，不实现 ImportableConverter")
  void testInterfaceType() {
    assertInstanceOf(ExportableConverter.class, converter);
    assertFalse(converter instanceof ImportableConverter);
  }

  // ==================== supportsExport ====================

  @Test
  @DisplayName("导出方向仅支持 String 类型")
  void testSupportsExport() {
    assertTrue(converter.supportsExport(String.class));
    assertFalse(converter.supportsExport(Integer.class));
  }

  // ==================== doConvertForExport ====================

  @Test
  @DisplayName("导出：超长文本截断并添加省略号")
  void testExportTruncateLongText() {
    String text = "这是一段非常长的文本，需要被截断处理";
    String result = converter.doConvertForExport(text, List.of("10"));
    assertEquals("这是一段非常长...", result);
  }

  @Test
  @DisplayName("导出：文本长度等于阈值时不截断")
  void testExportExactLength() {
    String text = "刚好七个字的长度测试文本";
    String result = converter.doConvertForExport(text, List.of("20"));
    assertEquals(text, result);
  }

  @Test
  @DisplayName("导出：短文本不截断")
  void testExportShortText() {
    String text = "短文本";
    String result = converter.doConvertForExport(text, List.of("20"));
    assertEquals("短文本", result);
  }

  @Test
  @DisplayName("导出：阈值小于等于省略号长度时不截断")
  void testExportThresholdTooSmall() {
    String text = "abcdef";
    String result = converter.doConvertForExport(text, List.of("2"));
    assertEquals("abcdef", result);
  }

  @Test
  @DisplayName("导出：null 输入返回 null")
  void testExportNull() {
    String result = converter.doConvertForExport(null, List.of("10"));
    assertNull(result);
  }

  @Test
  @DisplayName("导出：参数为空时不截断")
  void testExportEmptyArguments() {
    String text = "这是一段非常长的文本";
    String result = converter.doConvertForExport(text, List.of());
    assertEquals(text, result);
  }
}
