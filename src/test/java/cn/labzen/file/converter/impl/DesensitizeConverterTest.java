package cn.labzen.file.converter.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DesensitizeConverter 单元测试
 *
 * @author labzen
 */
@DisplayName("Desensitize 转换器测试")
class DesensitizeConverterTest {

  private final DesensitizeConverter converter = new DesensitizeConverter();

  @Test
  @DisplayName("手机号脱敏：中间四位隐藏")
  void testPhoneDesensitize() {
    String result = converter.convert("13800138000", List.of("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
    assertEquals("138****8000", result);
  }

  @Test
  @DisplayName("身份证号脱敏：中间部分隐藏")
  void testIdCardDesensitize() {
    String result = converter.convert("110101199001011234", List.of("(\\d{6})\\d{8}(\\d{4})", "$1********$2"));
    assertEquals("110101********1234", result);
  }

  @Test
  @DisplayName("邮箱脱敏：用户名部分隐藏")
  void testEmailDesensitize() {
    String result = converter.convert("admin@example.com", List.of("(^.{2})(.*)(@.*)$", "$1****$3"));
    assertEquals("ad****@example.com", result);
  }

  @Test
  @DisplayName("输入为 null 时返回 null")
  void testConvertNull() {
    String result = converter.convert(null, List.of(".*", "*"));
    assertNull(result);
  }

  @Test
  @DisplayName("参数数量错误时抛出异常")
  void testInvalidArgumentCount() {
    assertThrows(cn.labzen.file.exception.DataConvertException.class,
      () -> converter.convert("test", List.of("only-one-arg")));
  }

  @Test
  @DisplayName("支持字符串类型")
  void testSupportsString() {
    assertTrue(converter.supports(String.class));
    assertFalse(converter.supports(Integer.class));
  }
}
