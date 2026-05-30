package cn.labzen.file.converter.impl;

import cn.labzen.file.converter.exportable.ExportableConverter;
import cn.labzen.file.converter.importable.ImportableConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Uppercase 转换器测试")
class UppercaseConverterTest {

  private final UppercaseConverter converter = new UppercaseConverter();

  // ==================== 接口类型 ====================

  @Test
  @DisplayName("仅实现 ImportableConverter，不实现 ExportableConverter")
  void testInterfaceType() {
    assertInstanceOf(ImportableConverter.class, converter);
    assertFalse(converter instanceof ExportableConverter);
  }

  // ==================== supportsImport ====================

  @Test
  @DisplayName("导入方向仅支持 String 类型")
  void testSupportsImport() {
    assertTrue(converter.supportsImport(String.class));
    assertFalse(converter.supportsImport(Integer.class));
  }

  // ==================== doConvertForImport ====================

  @Test
  @DisplayName("导入：小写字母转为大写")
  void testImportLowercase() {
    Object result = converter.doConvertForImport("hello", List.of(), String.class);
    assertEquals("HELLO", result);
  }

  @Test
  @DisplayName("导入：已是大写字母保持不变")
  void testImportUppercase() {
    Object result = converter.doConvertForImport("HELLO", List.of(), String.class);
    assertEquals("HELLO", result);
  }

  @Test
  @DisplayName("导入：混合大小写转为大写")
  void testImportMixedCase() {
    Object result = converter.doConvertForImport("Hello World", List.of(), String.class);
    assertEquals("HELLO WORLD", result);
  }

  @Test
  @DisplayName("导入：中文字符保持不变")
  void testImportChinese() {
    Object result = converter.doConvertForImport("你好", List.of(), String.class);
    assertEquals("你好", result);
  }

  @Test
  @DisplayName("导入：null 输入返回 null")
  void testImportNull() {
    Object result = converter.doConvertForImport(null, List.of(), String.class);
    assertNull(result);
  }
}
