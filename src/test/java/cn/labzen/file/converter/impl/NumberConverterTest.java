package cn.labzen.file.converter.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NumberConverter 单元测试
 *
 * @author labzen
 */
@DisplayName("Number 转换器测试")
class NumberConverterTest {

  private final NumberConverter converter = new NumberConverter();

  @Test
  @DisplayName("支持数值类型")
  void testSupports() {
    assertTrue(converter.supports(Double.class));
    assertTrue(converter.supports(Integer.class));
    assertTrue(converter.supports(BigDecimal.class));
    assertFalse(converter.supports(String.class));
  }

  @Test
  @DisplayName("Double 按千分位格式转换")
  void testConvertDouble() {
    String result = converter.convert(1234567.89, List.of("#,##0.00"));
    assertEquals("1,234,567.89", result);
  }

  @Test
  @DisplayName("Integer 按整数格式转换")
  void testConvertInteger() {
    String result = converter.convert(42, List.of("0000"));
    assertEquals("0042", result);
  }

  @Test
  @DisplayName("负数正确转换")
  void testConvertNegative() {
    String result = converter.convert(-99.5, List.of("#,##0.00"));
    assertEquals("-99.50", result);
  }

  @Test
  @DisplayName("零值正确转换")
  void testConvertZero() {
    String result = converter.convert(0, List.of("#,##0.00"));
    assertEquals("0.00", result);
  }

  @Test
  @DisplayName("BigDecimal 正确转换")
  void testConvertBigDecimal() {
    String result = converter.convert(new BigDecimal("9999.99"), List.of("#,##0.00"));
    assertEquals("9,999.99", result);
  }

  @Test
  @DisplayName("未指定格式时返回原始字符串")
  void testConvertWithoutPattern() {
    String result = converter.convert(3.14, List.of(""));
    assertEquals("3.14", result);
  }

  @Test
  @DisplayName("输入为 null 时返回空字符串")
  void testConvertNull() {
    String result = converter.convert(null, List.of("#,##0.00"));
    assertEquals("", result);
  }
}
