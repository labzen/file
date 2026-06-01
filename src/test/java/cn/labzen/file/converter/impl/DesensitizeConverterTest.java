package cn.labzen.file.converter.impl;

import cn.labzen.file.converter.ExportableConverter;
import cn.labzen.file.converter.ImportableConverter;
import cn.labzen.file.exception.DataConvertException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Desensitize 转换器测试")
class DesensitizeConverterTest {

  private final DesensitizeConverter converter = new DesensitizeConverter();

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
  @DisplayName("导出：手机号脱敏 - 中间四位隐藏")
  void testExportPhone() {
    String result = converter.doConvertForExport("13800138000",
      List.of("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
    assertEquals("138****8000", result);
  }

  @Test
  @DisplayName("导出：身份证号脱敏 - 中间部分隐藏")
  void testExportIdCard() {
    String result = converter.doConvertForExport("110101199001011234",
      List.of("(\\d{6})\\d{8}(\\d{4})", "$1********$2"));
    assertEquals("110101********1234", result);
  }

  @Test
  @DisplayName("导出：邮箱脱敏 - 用户名部分隐藏")
  void testExportEmail() {
    String result = converter.doConvertForExport("admin@example.com",
      List.of("(^.{2})(.*)(@.*)$", "$1****$3"));
    assertEquals("ad****@example.com", result);
  }

  @Test
  @DisplayName("导出：null 输入返回 null")
  void testExportNull() {
    String result = converter.doConvertForExport(null, List.of(".*", "*"));
    assertNull(result);
  }

  @Test
  @DisplayName("导出：参数数量错误时抛出 DataConvertException")
  void testExportInvalidArgumentCount() {
    assertThrows(DataConvertException.class,
      () -> converter.doConvertForExport("test", List.of("only-one-arg")));
  }
}
