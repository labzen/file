package cn.labzen.file.converter.impl;

import cn.labzen.file.converter.exportable.ExportableConverter;
import cn.labzen.file.converter.importable.ImportableConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WhenNull 转换器测试")
class WhenNullConverterTest {

  private final WhenNullConverter converter = new WhenNullConverter();

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
  @DisplayName("导出：null 输入且默认值为空字符串时返回空字符串")
  void testExportNullWithEmptyDefault() {
    String result = converter.doConvertForExport(null, List.of(""));
    assertEquals("", result);
  }

  @Test
  @DisplayName("导出：非 null 输入返回原值")
  void testExportNonNull() {
    String result = converter.doConvertForExport("actual", List.of("default"));
    assertEquals("actual", result);
  }

  @Test
  @DisplayName("导出：空字符串不触发默认值替换")
  void testExportEmptyString() {
    String result = converter.doConvertForExport("", List.of("default"));
    assertEquals("", result);
  }
}
