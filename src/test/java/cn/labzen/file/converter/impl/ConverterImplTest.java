package cn.labzen.file.converter.impl;

import cn.labzen.file.converter.exportable.ExportableConverter;
import cn.labzen.file.converter.importable.ImportableConverter;
import cn.labzen.file.exception.DataConvertException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("转换器接口集成测试")
class ConverterImplTest {

  // ==================== WhenNullConverter ====================

  @Nested
  @DisplayName("WhenNullConverter")
  class WhenNullTests {

    private final WhenNullConverter converter = new WhenNullConverter();

    @Test
    @DisplayName("convertForExport: null 输入返回默认值")
    void exportNull() {
      Object result = converter.convertForExport(null, List.of("默认值"));
      assertEquals("默认值", result);
    }

    @Test
    @DisplayName("convertForExport: 非 null 输入返回原值")
    void exportNonNull() {
      Object result = converter.convertForExport("hello", List.of("默认值"));
      assertEquals("hello", result);
    }

    @Test
    @DisplayName("仅实现 ExportableConverter 接口")
    void interfaceType() {
      assertInstanceOf(ExportableConverter.class, converter);
      assertFalse(converter instanceof ImportableConverter);
    }
  }

  // ==================== WhenEmptyConverter ====================

  @Nested
  @DisplayName("WhenEmptyConverter")
  class WhenEmptyTests {

    private final WhenEmptyConverter converter = new WhenEmptyConverter();

    @Test
    @DisplayName("convertForExport: 空字符串输入返回默认值")
    void exportEmpty() {
      Object result = converter.convertForExport("", List.of("空"));
      assertEquals("空", result);
    }

    @Test
    @DisplayName("convertForExport: 非 null 输入返回原值")
    void exportNonEmpty() {
      Object result = converter.convertForExport("hello", List.of("空"));
      assertEquals("hello", result);
    }
  }

  // ==================== MappingConverter ====================

  @Nested
  @DisplayName("MappingConverter")
  class MappingTests {

    private final MappingConverter converter = new MappingConverter();

    @Test
    @DisplayName("convertForExport: 正向映射 key → value")
    void exportForward() {
      Map<String, String> mapping = Map.of("1", "启用", "2", "禁用");
      Object result = converter.convertForExport("1", List.of((Object) mapping));
      assertEquals("启用", result);
    }

    @Test
    @DisplayName("convertForExport: 未知 key 返回 unknown")
    void exportUnknownKey() {
      Map<String, String> mapping = Map.of("1", "启用", "2", "禁用");
      Object result = converter.convertForExport("99", List.of((Object) mapping));
      assertEquals("unknown", result);
    }

    @Test
    @DisplayName("convertForImport: 反向映射 value → key")
    void importReverse() {
      Map<String, String> mapping = Map.of("1", "启用", "2", "禁用");
      Object result = converter.convertForImport("启用", List.of((Object) mapping), String.class);
      assertEquals("1", result);
    }

    @Test
    @DisplayName("convertForImport: 未知 value 抛出 DataConvertException")
    void importUnknownValue() {
      Map<String, String> mapping = Map.of("1", "启用", "2", "禁用");
      assertThrows(DataConvertException.class,
        () -> converter.convertForImport("未知", List.of((Object) mapping), String.class));
    }
  }

  // ==================== BoolConverter ====================

  @Nested
  @DisplayName("BoolConverter")
  class BoolTests {

    private final BoolConverter converter = new BoolConverter();

    @Test
    @DisplayName("convertForExport: true 转换为指定文本")
    void exportTrue() {
      Object result = converter.convertForExport(true, List.of("是", "否"));
      assertEquals("是", result);
    }

    @Test
    @DisplayName("convertForExport: false 转换为指定文本")
    void exportFalse() {
      Object result = converter.convertForExport(false, List.of("是", "否"));
      assertEquals("否", result);
    }

    @Test
    @DisplayName("convertForImport: 指定文本转换为 Boolean.TRUE")
    void importTrue() {
      Object result = converter.convertForImport("是", List.of("是", "否"), Boolean.class);
      assertEquals(true, result);
    }

    @Test
    @DisplayName("convertForImport: 默认文本 true 转换为 Boolean.TRUE")
    void importDefaultTrue() {
      Object result = converter.convertForImport("true", List.of(), Boolean.class);
      assertEquals(true, result);
    }

    @Test
    @DisplayName("convertForImport: 无效文本抛出 DataConvertException")
    void importInvalid() {
      assertThrows(DataConvertException.class,
        () -> converter.convertForImport("maybe", List.of(), Boolean.class));
    }
  }

  // ==================== DateConverter ====================

  @Nested
  @DisplayName("DateConverter")
  class DateTests {

    private final DateConverter converter = new DateConverter();

    @Test
    @DisplayName("convertForExport: LocalDate 格式化输出")
    void exportLocalDate() {
      Object result = converter.convertForExport(LocalDate.of(2024, 1, 15), List.of("yyyy-MM-dd"));
      assertEquals("2024-01-15", result);
    }

    @Test
    @DisplayName("convertForImport: 字符串解析为 LocalDate")
    void importLocalDate() {
      Object result = converter.convertForImport("2024-01-15", List.of("yyyy-MM-dd"), LocalDate.class);
      assertEquals(LocalDate.of(2024, 1, 15), result);
    }

    @Test
    @DisplayName("convertForImport: 格式不匹配抛出 DataConvertException")
    void importInvalidFormat() {
      assertThrows(DataConvertException.class,
        () -> converter.convertForImport("not-a-date", List.of("yyyy-MM-dd"), LocalDate.class));
    }
  }

  // ==================== NumberConverter ====================

  @Nested
  @DisplayName("NumberConverter")
  class NumberTests {

    private final NumberConverter converter = new NumberConverter();

    @Test
    @DisplayName("convertForExport: 带格式化模式输出")
    void exportWithPattern() {
      Object result = converter.convertForExport(1234.5, List.of("#,##0.00"));
      assertEquals("1,234.50", result);
    }

    @Test
    @DisplayName("convertForImport: 字符串解析为 Integer")
    void importInteger() {
      Object result = converter.convertForImport("42", List.of(), Integer.class);
      assertEquals(42, result);
    }

    @Test
    @DisplayName("convertForImport: 字符串解析为 BigDecimal")
    void importBigDecimal() {
      Object result = converter.convertForImport("123.45", List.of(), BigDecimal.class);
      assertEquals(new BigDecimal("123.45"), result);
    }
  }

  // ==================== UppercaseConverter ====================

  @Nested
  @DisplayName("UppercaseConverter")
  class UppercaseTests {

    private final UppercaseConverter converter = new UppercaseConverter();

    @Test
    @DisplayName("convertForImport: 转为大写")
    void importUppercase() {
      Object result = converter.convertForImport("hello", List.of(), String.class);
      assertEquals("HELLO", result);
    }

    @Test
    @DisplayName("仅实现 ImportableConverter 接口")
    void interfaceType() {
      assertFalse(converter instanceof ExportableConverter);
      assertInstanceOf(ImportableConverter.class, converter);
    }
  }

  // ==================== LowercaseConverter ====================

  @Nested
  @DisplayName("LowercaseConverter")
  class LowercaseTests {

    private final LowercaseConverter converter = new LowercaseConverter();

    @Test
    @DisplayName("convertForImport: 转为小写")
    void importLowercase() {
      Object result = converter.convertForImport("HELLO", List.of(), String.class);
      assertEquals("hello", result);
    }

    @Test
    @DisplayName("仅实现 ImportableConverter 接口")
    void interfaceType() {
      assertFalse(converter instanceof ExportableConverter);
      assertInstanceOf(ImportableConverter.class, converter);
    }
  }

  // ==================== DesensitizeConverter ====================

  @Nested
  @DisplayName("DesensitizeConverter")
  class DesensitizeTests {

    private final DesensitizeConverter converter = new DesensitizeConverter();

    @Test
    @DisplayName("convertForExport: 正则替换脱敏")
    void exportDesensitize() {
      Object result = converter.convertForExport("13812345678",
        List.of("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
      assertEquals("138****5678", result);
    }

    @Test
    @DisplayName("仅实现 ExportableConverter 接口")
    void interfaceType() {
      assertFalse(converter instanceof ImportableConverter);
    }
  }

  // ==================== PrefixConverter ====================

  @Nested
  @DisplayName("PrefixConverter")
  class PrefixTests {

    private final PrefixConverter converter = new PrefixConverter();

    @Test
    @DisplayName("convertForExport: 添加前缀")
    void exportPrefix() {
      Object result = converter.convertForExport("测试", List.of("【"));
      assertEquals("【测试", result);
    }
  }

  // ==================== SuffixConverter ====================

  @Nested
  @DisplayName("SuffixConverter")
  class SuffixTests {

    private final SuffixConverter converter = new SuffixConverter();

    @Test
    @DisplayName("convertForExport: 添加后缀")
    void exportSuffix() {
      Object result = converter.convertForExport("测试", List.of("】"));
      assertEquals("测试】", result);
    }
  }

  // ==================== TruncateConverter ====================

  @Nested
  @DisplayName("TruncateConverter")
  class TruncateTests {

    private final TruncateConverter converter = new TruncateConverter();

    @Test
    @DisplayName("convertForExport: 截断长文本")
    void exportTruncate() {
      Object result = converter.convertForExport("abcdefghij", List.of("6"));
      assertEquals("abc...", result);
    }

    @Test
    @DisplayName("仅实现 ExportableConverter 接口")
    void interfaceType() {
      assertFalse(converter instanceof ImportableConverter);
    }
  }
}
