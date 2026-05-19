package cn.labzen.file.converter.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TruncateConverter 单元测试
 *
 * @author labzen
 */
@DisplayName("Truncate 转换器测试")
class TruncateConverterTest {

  private final TruncateConverter converter = new TruncateConverter();

  @Test
  @DisplayName("超长文本截断并添加省略号")
  void testTruncateLongText() {
    String text = "这是一段非常长的文本，需要被截断处理";
    String result = converter.convert(text, List.of("10"));
    // 阈值 10，有效长度 7（10-3），取前 7 个字符加省略号
    assertEquals("这是一段非常长...", result);
  }

  @Test
  @DisplayName("文本长度等于阈值时不截断")
  void testTruncateExactLength() {
    String text = "刚好七个字的长度测试文本";
    String result = converter.convert(text, List.of("20"));
    // 20 - 3 = 17 有效长度，文本长度 12 小于 17，不截断
    assertEquals(text, result);
  }

  @Test
  @DisplayName("短文本不截断")
  void testShortTextNotTruncated() {
    String text = "短文本";
    String result = converter.convert(text, List.of("20"));
    assertEquals("短文本", result);
  }

  @Test
  @DisplayName("阈值小于等于省略号长度时不截断")
  void testThresholdTooSmall() {
    String text = "abcdef";
    String result = converter.convert(text, List.of("2"));
    assertEquals("abcdef", result);
  }

  @Test
  @DisplayName("输入为 null 时返回 null")
  void testConvertNull() {
    String result = converter.convert(null, List.of("10"));
    assertNull(result);
  }

  @Test
  @DisplayName("参数为空时不截断")
  void testEmptyArguments() {
    String text = "这是一段非常长的文本";
    String result = converter.convert(text, List.of());
    assertEquals(text, result);
  }

  @Test
  @DisplayName("支持字符串类型")
  void testSupportsString() {
    assertTrue(converter.supports(String.class));
    assertFalse(converter.supports(Integer.class));
  }
}
