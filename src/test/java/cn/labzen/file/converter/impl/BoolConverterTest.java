package cn.labzen.file.converter.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BoolConverter 单元测试
 *
 * @author labzen
 */
@DisplayName("Bool 转换器测试")
class BoolConverterTest {

  private final BoolConverter converter = new BoolConverter();

  @Test
  @DisplayName("支持 Boolean 类型")
  void testSupportsBoolean() {
    assertTrue(converter.supports(Boolean.class));
    assertTrue(converter.supports(boolean.class));
  }

  @Test
  @DisplayName("不支持非布尔类型")
  void testNotSupportOtherTypes() {
    assertFalse(converter.supports(String.class));
    assertFalse(converter.supports(Integer.class));
    assertFalse(converter.supports(Object.class));
  }

  @Test
  @DisplayName("true 值转换为指定字符串")
  void testConvertTrue() {
    String result = converter.convert(true, List.of("是", "否"));
    assertEquals("是", result);
  }

  @Test
  @DisplayName("false 值转换为指定字符串")
  void testConvertFalse() {
    String result = converter.convert(false, List.of("是", "否"));
    assertEquals("否", result);
  }

  @Test
  @DisplayName("Boolean.TRUE 转换")
  void testConvertBooleanTrue() {
    String result = converter.convert(Boolean.TRUE, List.of("Yes", "No"));
    assertEquals("Yes", result);
  }

  @Test
  @DisplayName("Boolean.FALSE 转换")
  void testConvertBooleanFalse() {
    String result = converter.convert(Boolean.FALSE, List.of("Yes", "No"));
    assertEquals("No", result);
  }

  @Test
  @DisplayName("未指定参数时 true 返回默认值")
  void testConvertTrueDefault() {
    String result = converter.convert(true, List.of());
    assertEquals("true", result);
  }

  @Test
  @DisplayName("未指定第二个参数时 false 返回默认值")
  void testConvertFalseDefault() {
    String result = converter.convert(false, List.of("是"));
    assertEquals("false", result);
  }

  @Test
  @DisplayName("null 输入按 false 处理")
  void testConvertNull() {
    String result = converter.convert(null, List.of("是", "否"));
    assertNull(result);
  }

  @Test
  @DisplayName("使用中文映射")
  void testConvertChinese() {
    assertEquals("成功", converter.convert(true, List.of("成功", "失败")));
    assertEquals("失败", converter.convert(false, List.of("成功", "失败")));
  }
}
