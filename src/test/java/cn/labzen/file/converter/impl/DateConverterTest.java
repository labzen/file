package cn.labzen.file.converter.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DateConverter 单元测试
 *
 * @author labzen
 */
@DisplayName("Date 转换器测试")
class DateConverterTest {

  private final DateConverter converter = new DateConverter();

  @Test
  @DisplayName("支持日期相关类型")
  void testSupports() {
    assertTrue(converter.supports(Date.class));
    assertTrue(converter.supports(LocalDate.class));
    assertTrue(converter.supports(LocalDateTime.class));
    assertTrue(converter.supports(LocalTime.class));
    assertFalse(converter.supports(String.class));
  }

  @Test
  @DisplayName("Date 类型按指定格式转换")
  void testConvertDate() {
    Date date = new Date(1704067200000L); // 2024-01-01 00:00:00 UTC
    String result = converter.convert(date, List.of("yyyy-MM-dd"));
    assertEquals("2024-01-01", result);
  }

  @Test
  @DisplayName("LocalDate 类型按指定格式转换")
  void testConvertLocalDate() {
    LocalDate date = LocalDate.of(2024, 6, 15);
    String result = converter.convert(date, List.of("yyyy/MM/dd"));
    assertEquals("2024/06/15", result);
  }

  @Test
  @DisplayName("LocalDateTime 类型按指定格式转换")
  void testConvertLocalDateTime() {
    LocalDateTime dateTime = LocalDateTime.of(2024, 3, 8, 14, 30, 0);
    String result = converter.convert(dateTime, List.of("yyyy-MM-dd HH:mm"));
    assertEquals("2024-03-08 14:30", result);
  }

  @Test
  @DisplayName("LocalTime 类型按指定格式转换")
  void testConvertLocalTime() {
    LocalTime time = LocalTime.of(9, 5, 30);
    String result = converter.convert(time, List.of("HH:mm:ss"));
    assertEquals("09:05:30", result);
  }

  @Test
  @DisplayName("输入为 null 时返回 null")
  void testConvertNull() {
    String result = converter.convert(null, List.of("yyyy-MM-dd"));
    assertNull(result);
  }

  @Test
  @DisplayName("未指定格式时使用默认格式")
  void testConvertWithDefaultPattern() {
    Date date = new Date(1704067200000L);
    String result = converter.convert(date, List.of(""));
    assertNotNull(result);
    assertTrue(result.contains("2024"));
  }
}
