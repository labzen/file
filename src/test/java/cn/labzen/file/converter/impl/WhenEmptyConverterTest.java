package cn.labzen.file.converter.impl;

import cn.labzen.file.converter.exportable.ExportableConverter;
import cn.labzen.file.converter.importable.ImportableConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WhenEmpty 转换器测试")
class WhenEmptyConverterTest {

  private final WhenEmptyConverter converter = new WhenEmptyConverter();

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
  @DisplayName("导出：null 输入返回默认值")
  void testExportNull() {
    String result = converter.doConvertForExport(null, List.of("default"));
    assertEquals("default", result);
  }

  @Test
  @DisplayName("导出：空字符串输入返回默认值")
  void testExportEmpty() {
    String result = converter.doConvertForExport("", List.of("default"));
    assertEquals("default", result);
  }

  @Test
  @DisplayName("导出：空白字符串不触发默认值替换（仅空字符串触发）")
  void testExportBlank() {
    String result = converter.doConvertForExport("   ", List.of("default"));
    assertEquals("   ", result);
  }

  @Test
  @DisplayName("导出：非空输入返回原值")
  void testExportNonEmpty() {
    String result = converter.doConvertForExport("value", List.of("default"));
    assertEquals("value", result);
  }
}
