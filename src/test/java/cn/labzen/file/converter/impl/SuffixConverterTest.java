package cn.labzen.file.converter.impl;

import cn.labzen.file.converter.exportable.ExportableConverter;
import cn.labzen.file.converter.importable.ImportableConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Suffix 转换器测试")
class SuffixConverterTest {

  private final SuffixConverter converter = new SuffixConverter();

  // ==================== 接口类型 ====================

  @Test
  @DisplayName("仅实现 ExportableConverter，不实现 ImportableConverter")
  void testInterfaceType() {
    assertInstanceOf(ExportableConverter.class, converter);
    assertFalse(converter instanceof ImportableConverter);
  }

  // ==================== supportsExport ====================

  @Test
  @DisplayName("导出方向始终返回 true（支持任意类型）")
  void testSupportsExport() {
    assertTrue(converter.supportsExport(String.class));
    assertTrue(converter.supportsExport(Integer.class));
    assertTrue(converter.supportsExport(Object.class));
  }

  // ==================== doConvertForExport ====================

  @Test
  @DisplayName("导出：为字符串添加后缀")
  void testExportString() {
    String result = converter.doConvertForExport("hello", List.of("_world"));
    assertEquals("hello_world", result);
  }

  @Test
  @DisplayName("导出：为数字添加后缀")
  void testExportNumber() {
    String result = converter.doConvertForExport(100, List.of("MB"));
    assertEquals("100MB", result);
  }

  @Test
  @DisplayName("导出：中文后缀")
  void testExportChineseSuffix() {
    String result = converter.doConvertForExport("测试", List.of("】"));
    assertEquals("测试】", result);
  }

  @Test
  @DisplayName("导出：null 输入返回 null")
  void testExportNull() {
    String result = converter.doConvertForExport(null, List.of("suffix"));
    assertNull(result);
  }
}
