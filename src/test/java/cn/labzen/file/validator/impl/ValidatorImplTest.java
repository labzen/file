//package cn.labzen.file.validator.impl;
//
//import cn.labzen.file.validator.ChainableValidatorExecutor;
//import cn.labzen.file.validator.ValidateContext;
//import cn.labzen.file.validator.ValidateResult;
//import org.junit.jupiter.api.Test;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * 校验器实现类的单元测试
// */
//class ValidatorImplTest {
//
//  private ValidateContext createContext(String fieldName, String headerText) {
//    return new ValidateContext(1, fieldName, headerText, new HashMap<>(), new HashMap<>(), "zh-CN");
//  }
//
//  // ── RequiredValidator ──
//
//  @Test
//  void requiredValidator_null() {
//    RequiredValidator validator = new RequiredValidator();
//    ValidateContext ctx = createContext("name", "名称");
//    ValidateResult result = validator.validate(null, List.of(), ctx);
//    assertNotNull(result);
//    assertEquals("import.validate.required", result.getErrorI18nCode());
//  }
//
//  @Test
//  void requiredValidator_blank() {
//    RequiredValidator validator = new RequiredValidator();
//    ValidateContext ctx = createContext("name", "名称");
//    ValidateResult result = validator.validate("  ", List.of(), ctx);
//    assertNotNull(result);
//  }
//
//  @Test
//  void requiredValidator_valid() {
//    RequiredValidator validator = new RequiredValidator();
//    ValidateContext ctx = createContext("name", "名称");
//    ValidateResult result = validator.validate("hello", List.of(), ctx);
//    assertNull(result);
//  }
//
//  // ── LengthValidator ──
//
//  @Test
//  void lengthValidator_tooLong() {
//    RangeLengthValidator validator = new RangeLengthValidator();
//    ValidateContext ctx = createContext("name", "名称");
//    ValidateResult result = validator.validate("abcdefghij", Arrays.asList(null, 5), ctx);
//    assertNotNull(result);
//    assertEquals("import.validate.max-length", result.getErrorI18nCode());
//  }
//
//  @Test
//  void lengthValidator_tooShort() {
//    RangeLengthValidator validator = new RangeLengthValidator();
//    ValidateContext ctx = createContext("name", "名称");
//    ValidateResult result = validator.validate("ab", Arrays.asList(3, null), ctx);
//    assertNotNull(result);
//    assertEquals("import.validate.min-length", result.getErrorI18nCode());
//  }
//
//  @Test
//  void lengthValidator_valid() {
//    RangeLengthValidator validator = new RangeLengthValidator();
//    ValidateContext ctx = createContext("name", "名称");
//    ValidateResult result = validator.validate("abc", List.of(1, 10), ctx);
//    assertNull(result);
//  }
//
//  @Test
//  void lengthValidator_null() {
//    RangeLengthValidator validator = new RangeLengthValidator();
//    ValidateContext ctx = createContext("name", "名称");
//    ValidateResult result = validator.validate(null, List.of(1, 10), ctx);
//    assertNull(result);
//  }
//
//  // ── PatternValidator ──
//
//  @Test
//  void patternValidator_match() {
//    PatternValidator validator = new PatternValidator();
//    ValidateContext ctx = createContext("phone", "电话");
//    ValidateResult result = validator.validate("13800138000", List.of("^1[3-9]\\d{9}$"), ctx);
//    assertNull(result);
//  }
//
//  @Test
//  void patternValidator_noMatch() {
//    PatternValidator validator = new PatternValidator();
//    ValidateContext ctx = createContext("phone", "电话");
//    ValidateResult result = validator.validate("123", List.of("^1[3-9]\\d{9}$"), ctx);
//    assertNotNull(result);
//    assertEquals("import.validate.pattern", result.getErrorI18nCode());
//  }
//
//  @Test
//  void patternValidator_blank() {
//    PatternValidator validator = new PatternValidator();
//    ValidateContext ctx = createContext("phone", "电话");
//    ValidateResult result = validator.validate("", List.of("^1[3-9]\\d{9}$"), ctx);
//    assertNull(result); // 空白跳过校验（由required处理）
//  }
//
//  // ── RangeValidator ──
//
//  @Test
//  void rangeValidator_withinRange() {
//    RangeDateValidator validator = new RangeDateValidator();
//    ValidateContext ctx = createContext("amount", "金额");
//    ValidateResult result = validator.validate(50, List.of("0", "100"), ctx);
//    assertNull(result);
//  }
//
//  @Test
//  void rangeValidator_belowMin() {
//    RangeDateValidator validator = new RangeDateValidator();
//    ValidateContext ctx = createContext("amount", "金额");
//    ValidateResult result = validator.validate(-1, List.of("0", "100"), ctx);
//    assertNotNull(result);
//    assertEquals("import.validate.range", result.getErrorI18nCode());
//  }
//
//  @Test
//  void rangeValidator_aboveMax() {
//    RangeDateValidator validator = new RangeDateValidator();
//    ValidateContext ctx = createContext("amount", "金额");
//    ValidateResult result = validator.validate(200, List.of("0", "100"), ctx);
//    assertNotNull(result);
//  }
//
//  @Test
//  void rangeValidator_null() {
//    RangeDateValidator validator = new RangeDateValidator();
//    ValidateContext ctx = createContext("amount", "金额");
//    ValidateResult result = validator.validate(null, List.of("0", "100"), ctx);
//    assertNull(result);
//  }
//
//  // ── DependencyValidator ──
//
//  @Test
//  void dependencyValidator_dependentHasValue() {
//    DependsOnValidator validator = new DependsOnValidator();
//    Map<String, Object> rowData = new HashMap<>();
//    rowData.put("category", "其他");
//    ValidateContext ctx = new ValidateContext(1, "description", "描述", rowData, new HashMap<>(), "zh-CN");
//    ValidateResult result = validator.validate(null, List.of("category"), ctx);
//    assertNotNull(result);
//    assertEquals("import.validate.depends-on", result.getErrorI18nCode());
//  }
//
//  @Test
//  void dependencyValidator_dependentNoValue() {
//    DependsOnValidator validator = new DependsOnValidator();
//    Map<String, Object> rowData = new HashMap<>();
//    ValidateContext ctx = new ValidateContext(1, "description", "描述", rowData, new HashMap<>(), "zh-CN");
//    ValidateResult result = validator.validate(null, List.of("category"), ctx);
//    assertNull(result);
//  }
//
//  // ── UniqueValidator ──
//
//  @Test
//  void uniqueValidator_unique() {
//    UniqueValidator validator = new UniqueValidator();
//    Map<String, String> rawRowData = new HashMap<>();
//    rawRowData.put("code", "ABC");
//    ValidateContext ctx = new ValidateContext(1, "code", "编号", new HashMap<>(), rawRowData, "zh-CN");
//    ValidateResult result = validator.validate("ABC", List.of(), ctx);
//    // 单行数据，唯一
//    assertNull(result);
//  }
//
//  // ── ChainableValidatorExecutor ──
//
//  @Test
//  void chainableValidatorExecutor_immediateChain() {
//    ChainableValidatorExecutor executor = new ChainableValidatorExecutor();
//    executor.addValidator(REQUIRE_NAME, List.of());
//    executor.addValidator(RANGE_LENGTH_NAME, Arrays.asList(null, 5));
//    executor.sort();
//
//    ValidateContext ctx = createContext("name", "名称");
//
//    // null 应触发 required 失败
//    List<ValidateResult> failures = executor.executeImmediate(null, ctx);
//    assertEquals(1, failures.size());
//
//    // 超长应触发 length 失败
//    failures = executor.executeImmediate("abcdefghij", ctx);
//    assertEquals(1, failures.size());
//
//    // 正常值
//    failures = executor.executeImmediate("abc", ctx);
//    assertTrue(failures.isEmpty());
//  }
//}
