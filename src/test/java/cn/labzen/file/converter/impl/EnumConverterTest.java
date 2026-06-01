package cn.labzen.file.converter.impl;

import cn.labzen.file.converter.ExportableConverter;
import cn.labzen.file.converter.ImportableConverter;
import cn.labzen.file.exception.DataConvertException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Enum 转换器测试")
class EnumConverterTest {

  private final EnumConverter converter = new EnumConverter();

  // ==================== 接口类型 ====================

  @Test
  @DisplayName("实现 ExportableConverter 和 ImportableConverter 双向接口")
  void testInterfaceType() {
    assertInstanceOf(ExportableConverter.class, converter);
    assertInstanceOf(ImportableConverter.class, converter);
  }

  // ==================== supportsExport ====================

  @Test
  @DisplayName("导出方向仅支持 String 类型")
  void testSupportsExport() {
    assertTrue(converter.supportsExport(String.class));
    assertFalse(converter.supportsExport(Integer.class));
  }

  // ==================== supportsImport ====================

  @Test
  @DisplayName("导入方向始终返回 true")
  void testSupportsImport() {
    assertTrue(converter.supportsImport(String.class));
    assertTrue(converter.supportsImport(Integer.class));
    assertTrue(converter.supportsImport(Object.class));
  }

  // ==================== doConvertForExport ====================

  @Test
  @DisplayName("导出：通过枚举方法转换值为标签")
  void testExportValidEnum() {
    String enumRef = TestStatusEnum.class.getName() + "#getLabel";
    String result = converter.doConvertForExport("ACTIVE", List.of(enumRef));
    assertEquals("启用", result);
  }

  @Test
  @DisplayName("导出：忽略大小写匹配枚举值")
  void testExportCaseInsensitive() {
    String enumRef = TestStatusEnum.class.getName() + "#getLabel";
    String result = converter.doConvertForExport("inactive", List.of(enumRef));
    assertEquals("禁用", result);
  }

  @Test
  @DisplayName("导出：枚举值不存在时返回失败标记")
  void testExportInvalidEnumValue() {
    String enumRef = TestStatusEnum.class.getName() + "#getLabel";
    String result = converter.doConvertForExport("UNKNOWN", List.of(enumRef));
    assertEquals("convert-enum-failed", result);
  }

  @Test
  @DisplayName("导出：null 输入返回 null")
  void testExportNull() {
    String enumRef = TestStatusEnum.class.getName() + "#getLabel";
    String result = converter.doConvertForExport(null, List.of(enumRef));
    assertNull(result);
  }

  @Test
  @DisplayName("导出：无效的枚举类引用返回失败标记")
  void testExportInvalidEnumClass() {
    String result = converter.doConvertForExport("ACTIVE",
      List.of("com.nonexistent.Enum#getLabel"));
    assertEquals("convert-enum-failed", result);
  }

  @Test
  @DisplayName("导出：无效的方法名返回失败标记")
  void testExportInvalidMethodName() {
    String enumRef = TestStatusEnum.class.getName() + "#nonExistentMethod";
    String result = converter.doConvertForExport("ACTIVE", List.of(enumRef));
    assertEquals("convert-enum-failed", result);
  }

  // ==================== doConvertForImport ====================

  @Test
  @DisplayName("导入：枚举配置无效时抛出 DataConvertException")
  void testImportByLabel() {
    String enumRef = TestStatusEnum.class.getName() + "#getLabel";
    assertThrows(DataConvertException.class,
      () -> converter.doConvertForImport("启用", List.of(enumRef), TestStatusEnum.class));
  }

  @Test
  @DisplayName("导入：枚举配置无效时抛出 DataConvertException")
  void testImportByName() {
    String enumRef = TestStatusEnum.class.getName() + "#getLabel";
    assertThrows(DataConvertException.class,
      () -> converter.doConvertForImport("ACTIVE", List.of(enumRef), TestStatusEnum.class));
  }

  // ==================== 测试用枚举 ====================

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
