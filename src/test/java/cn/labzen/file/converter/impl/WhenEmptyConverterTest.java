package cn.labzen.file.converter.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WhenEmptyConverter 单元测试
 *
 * @author labzen
 */
@DisplayName("WhenEmpty 转换器测试")
class WhenEmptyConverterTest {

  private final WhenEmptyConverter converter = new WhenEmptyConverter();

  @Test
  @DisplayName("输入为 null 时返回默认值")
  void testConvertNull() {
    String result = converter.convert(null, List.of("default"));
    assertEquals("default", result);
  }

  @Test
  @DisplayName("输入为空字符串时返回默认值")
  void testConvertEmpty() {
    String result = converter.convert("", List.of("default"));
    assertEquals("default", result);
  }

  @Test
  @DisplayName("输入为空白字符串时不返回默认值（仅空字符串触发）")
  void testConvertBlank() {
    String result = converter.convert("   ", List.of("default"));
    assertEquals("   ", result);
  }

  @Test
  @DisplayName("输入非空时返回原值")
  void testConvertNonEmpty() {
    String result = converter.convert("value", List.of("default"));
    assertEquals("value", result);
  }

  @Test
  @DisplayName("支持任意类型")
  void testSupportsAnyType() {
    assertTrue(converter.supports(String.class));
    assertTrue(converter.supports(Object.class));
  }
}
