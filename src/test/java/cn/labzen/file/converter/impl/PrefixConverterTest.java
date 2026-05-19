package cn.labzen.file.converter.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PrefixConverter 单元测试
 *
 * @author labzen
 */
@DisplayName("Prefix 转换器测试")
class PrefixConverterTest {

  private final PrefixConverter converter = new PrefixConverter();

  @Test
  @DisplayName("为字符串添加前缀")
  void testConvertString() {
    String result = converter.convert("world", List.of("hello_"));
    assertEquals("hello_world", result);
  }

  @Test
  @DisplayName("为数字添加前缀")
  void testConvertNumber() {
    String result = converter.convert(42, List.of("No."));
    assertEquals("No.42", result);
  }

  @Test
  @DisplayName("输入为 null 时返回 null")
  void testConvertNull() {
    String result = converter.convert(null, List.of("prefix"));
    assertNull(result);
  }

  @Test
  @DisplayName("支持任意类型")
  void testSupportsAnyType() {
    assertTrue(converter.supports(String.class));
    assertTrue(converter.supports(Integer.class));
    assertTrue(converter.supports(Object.class));
  }
}
