package cn.labzen.file.converter.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WhenNullConverter 单元测试
 *
 * @author labzen
 */
@DisplayName("WhenNull 转换器测试")
class WhenNullConverterTest {

  private final WhenNullConverter converter = new WhenNullConverter();

  @Test
  @DisplayName("输入为 null 时返回默认值")
  void testConvertNull() {
    String result = converter.convert(null, List.of("default"));
    assertEquals("default", result);
  }

  @Test
  @DisplayName("输入为 null 且默认值为空字符串时返回空字符串")
  void testConvertNullWithEmptyDefault() {
    String result = converter.convert(null, List.of(""));
    assertEquals("", result);
  }

  @Test
  @DisplayName("输入不为 null 时返回原值")
  void testConvertNonNull() {
    String result = converter.convert("actual", List.of("default"));
    assertEquals("actual", result);
  }

  @Test
  @DisplayName("输入为空字符串时返回空字符串（不触发转换）")
  void testConvertEmptyString() {
    String result = converter.convert("", List.of("default"));
    assertEquals("", result);
  }

  @Test
  @DisplayName("支持任意类型")
  void testSupportsAnyType() {
    assertTrue(converter.supports(String.class));
    assertTrue(converter.supports(Integer.class));
    assertTrue(converter.supports(Object.class));
  }
}
