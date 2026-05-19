package cn.labzen.file.converter.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MappingConverter 单元测试
 *
 * @author labzen
 */
@DisplayName("Mapping 转换器测试")
class MappingConverterTest {

  private final MappingConverter converter = new MappingConverter();

  @Test
  @DisplayName("映射表中存在对应键时返回映射值")
  void testConvertExistingKey() {
    Map<String, String> mapping = Map.of("1", "男", "2", "女");
    String result = converter.convert("1", java.util.List.of(mapping));
    assertEquals("男", result);
  }

  @Test
  @DisplayName("映射表中不存在对应键时返回 unknown")
  void testConvertMissingKey() {
    Map<String, String> mapping = Map.of("1", "男", "2", "女");
    String result = converter.convert("3", java.util.List.of(mapping));
    assertEquals("unknown", result);
  }

  @Test
  @DisplayName("输入为 null 时返回 null")
  void testConvertNull() {
    Map<String, String> mapping = Map.of("1", "男");
    String result = converter.convert(null, java.util.List.of(mapping));
    assertNull(result);
  }

  @Test
  @DisplayName("输入自动转为字符串作为键")
  void testConvertIntegerInput() {
    Map<String, String> mapping = Map.of("1", "启用", "2", "禁用");
    String result = converter.convert(1, java.util.List.of(mapping));
    assertEquals("启用", result);
  }

  @Test
  @DisplayName("支持任意类型")
  void testSupportsAnyType() {
    assertTrue(converter.supports(String.class));
    assertTrue(converter.supports(Integer.class));
    assertTrue(converter.supports(Object.class));
  }
}
