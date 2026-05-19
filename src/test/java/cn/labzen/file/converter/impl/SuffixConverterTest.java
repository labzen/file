package cn.labzen.file.converter.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SuffixConverter 单元测试
 *
 * @author labzen
 */
@DisplayName("Suffix 转换器测试")
class SuffixConverterTest {

  private final SuffixConverter converter = new SuffixConverter();

  @Test
  @DisplayName("为字符串添加后缀")
  void testConvertString() {
    String result = converter.convert("hello", List.of("_world"));
    assertEquals("hello_world", result);
  }

  @Test
  @DisplayName("为数字添加后缀")
  void testConvertNumber() {
    String result = converter.convert(100, List.of("MB"));
    assertEquals("100MB", result);
  }

  @Test
  @DisplayName("输入为 null 时返回 null")
  void testConvertNull() {
    String result = converter.convert(null, List.of("suffix"));
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
