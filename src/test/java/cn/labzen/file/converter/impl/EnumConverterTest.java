package cn.labzen.file.converter.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EnumConverter 单元测试
 * <p>
 * 使用测试枚举 {@link TestStatusEnum} 验证枚举转换功能。
 *
 * @author labzen
 */
@DisplayName("Enum 转换器测试")
class EnumConverterTest {

  private final EnumConverter converter = new EnumConverter();

  @Test
  @DisplayName("支持字符串类型")
  void testSupports() {
    assertTrue(converter.supports(String.class));
    assertFalse(converter.supports(Integer.class));
  }

  @Test
  @DisplayName("通过枚举方法转换值为标签")
  void testConvertValidEnum() {
    // 使用当前测试类所在包下的测试枚举
    String enumRef = TestStatusEnum.class.getName() + "#getLabel";
    String result = converter.convert("ACTIVE", List.of(enumRef));
    assertEquals("启用", result);
  }

  @Test
  @DisplayName("忽略大小写匹配枚举值")
  void testConvertCaseInsensitive() {
    String enumRef = TestStatusEnum.class.getName() + "#getLabel";
    String result = converter.convert("inactive", List.of(enumRef));
    assertEquals("禁用", result);
  }

  @Test
  @DisplayName("枚举值不存在时返回失败标记")
  void testConvertInvalidEnum() {
    String enumRef = TestStatusEnum.class.getName() + "#getLabel";
    String result = converter.convert("UNKNOWN", List.of(enumRef));
    assertEquals("convert-enum-failed", result);
  }

  @Test
  @DisplayName("输入为 null 时返回 null")
  void testConvertNull() {
    String enumRef = TestStatusEnum.class.getName() + "#getLabel";
    String result = converter.convert(null, List.of(enumRef));
    assertNull(result);
  }

  @Test
  @DisplayName("无效的枚举类引用返回失败标记")
  void testInvalidEnumClass() {
    String result = converter.convert("ACTIVE", List.of("com.nonexistent.Enum#getLabel"));
    assertEquals("convert-enum-failed", result);
  }

  @Test
  @DisplayName("无效的方法名返回失败标记")
  void testInvalidMethodName() {
    String enumRef = TestStatusEnum.class.getName() + "#nonExistentMethod";
    String result = converter.convert("ACTIVE", List.of(enumRef));
    assertEquals("convert-enum-failed", result);
  }

  /**
   * 测试用枚举
   */
  enum TestStatusEnum {
    ACTIVE("启用"),
    INACTIVE("禁用"),
    PENDING("待处理");

    private final String label;

    TestStatusEnum(String label) {
      this.label = label;
    }

    public String getLabel() {
      return label;
    }
  }
}
