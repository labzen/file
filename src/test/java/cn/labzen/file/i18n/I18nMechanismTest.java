package cn.labzen.file.i18n;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.Column;
import cn.labzen.file.definition.bean.column.Exporting;
import cn.labzen.file.definition.bean.column.Importing;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 国际化机制单元测试
 * <p>
 * 覆盖 NopI18NStoreProvider、I18nStoreHolder、I18nResolver 三个核心组件
 *
 * @author labzen
 */
@DisplayName("国际化机制测试")
class I18nMechanismTest {

  // ===== NopI18NStoreProvider 测试 =====

  @Nested
  @DisplayName("NopI18NStoreProvider 测试")
  class NopI18NStoreProviderTest {

    private ManualI18NStoreProvider store;

    @BeforeEach
    void setUp() {
      store = new ManualI18NStoreProvider();
      store.setDefaultLocale("zh-CN");
    }

    @Test
    @DisplayName("精准匹配 locale 获取文本")
    void testGetTextPreciseMatch() {
      store.put("zh-CN", "title", "用户信息");
      store.put("en-US", "title", "User Info");

      assertEquals("用户信息", store.getText("zh-CN", "title"));
      assertEquals("User Info", store.getText("en-US", "title"));
    }

    @Test
    @DisplayName("精准匹配不到时回退至默认 locale")
    void testGetTextFallbackToDefaultLocale() {
      store.put("zh-CN", "title", "用户信息");

      // ja-JP 没有配置，回退到 zh-CN
      assertEquals("用户信息", store.getText("ja-JP", "title"));
    }

    @Test
    @DisplayName("精准匹配和默认 locale 都无时返回 key 本身")
    void testGetTextReturnKeyWhenNotFound() {
      store.put("zh-CN", "title", "用户信息");

      // "header" 这个 key 在任何 locale 下都没有
      assertEquals("header", store.getText("ja-JP", "header"));
    }

    @Test
    @DisplayName("putAll 批量写入")
    void testPutAll() {
      Map<String, String> zhTexts = new LinkedHashMap<>();
      zhTexts.put("title", "用户信息");
      zhTexts.put("header", "用户名");
      store.putAll("zh-CN", zhTexts);

      Map<String, String> enTexts = new LinkedHashMap<>();
      enTexts.put("title", "User Info");
      enTexts.put("header", "Username");
      store.putAll("en-US", enTexts);

      assertEquals("用户信息", store.getText("zh-CN", "title"));
      assertEquals("用户名", store.getText("zh-CN", "header"));
      assertEquals("User Info", store.getText("en-US", "title"));
      assertEquals("Username", store.getText("en-US", "header"));
    }

    @Test
    @DisplayName("put 覆盖已有文本")
    void testPutOverride() {
      store.put("zh-CN", "title", "旧标题");
      assertEquals("旧标题", store.getText("zh-CN", "title"));

      store.put("zh-CN", "title", "新标题");
      assertEquals("新标题", store.getText("zh-CN", "title"));
    }

    @Test
    @DisplayName("defaultLocale 可变更")
    void testDefaultLocaleChange() {
      store.put("zh-CN", "title", "中文标题");
      store.put("en-US", "title", "English Title");

      // 默认 locale 为 zh-CN，ja-JP 回退到 zh-CN
      assertEquals("中文标题", store.getText("ja-JP", "title"));

      // 切换默认 locale
      store.setDefaultLocale("en-US");
      assertEquals("English Title", store.getText("ja-JP", "title"));
    }

    @Test
    @DisplayName("不同 locale 下同一 key 可有不同文本")
    void testDifferentLocalesForSameKey() {
      store.put("zh-CN", "gender", "性别");
      store.put("en-US", "gender", "Gender");
      store.put("ja-JP", "gender", "性別");

      assertEquals("性别", store.getText("zh-CN", "gender"));
      assertEquals("Gender", store.getText("en-US", "gender"));
      assertEquals("性別", store.getText("ja-JP", "gender"));
    }
  }

  // ===== I18nStoreHolder 测试 =====

  @Nested
  @DisplayName("I18nStoreHolder 测试")
  class I18nStoreHolderTest {

    @AfterEach
    void tearDown() {
      // 清理 holder 状态，避免影响其他测试
      I18nStoreHolder.register(null);
    }

    @Test
    @DisplayName("未注册时 get 返回 null")
    void testGetWhenNotRegistered() {
      assertNull(I18nStoreHolder.get());
    }

    @Test
    @DisplayName("注册后 get 返回注册的实例")
    void testRegisterAndGet() {
      ManualI18NStoreProvider store = new ManualI18NStoreProvider();
      I18nStoreHolder.register(store);

      assertSame(store, I18nStoreHolder.get());
    }

    @Test
    @DisplayName("重复注册覆盖之前的实例")
    void testRegisterOverride() {
      ManualI18NStoreProvider store1 = new ManualI18NStoreProvider();
      ManualI18NStoreProvider store2 = new ManualI18NStoreProvider();

      I18nStoreHolder.register(store1);
      assertSame(store1, I18nStoreHolder.get());

      I18nStoreHolder.register(store2);
      assertSame(store2, I18nStoreHolder.get());
    }

    @Test
    @DisplayName("注册 null 可清除持有者")
    void testRegisterNull() {
      ManualI18NStoreProvider store = new ManualI18NStoreProvider();
      I18nStoreHolder.register(store);
      assertNotNull(I18nStoreHolder.get());

      I18nStoreHolder.register(null);
      assertNull(I18nStoreHolder.get());
    }
  }

  // ===== I18nResolver 测试 =====

  @Nested
  @DisplayName("I18nResolver 测试")
  class I18nResolverTest {

    private ManualI18NStoreProvider store;
    private I18nResolver resolver;

    @BeforeEach
    void setUp() {
      store = new ManualI18NStoreProvider();
      store.setDefaultLocale("zh-CN");

      // 准备中文文案
      store.put("zh-CN", "user-title", "用户信息导出");
      store.put("zh-CN", "username-header", "用户名");
      store.put("zh-CN", "age-header", "年龄");
      store.put("zh-CN", "gender-header", "性别");
      store.put("zh-CN", "null-text", "未知");
      store.put("zh-CN", "blank-text", "空");
      store.put("zh-CN", "bool-true", "是");
      store.put("zh-CN", "bool-false", "否");
      store.put("zh-CN", "male", "男");
      store.put("zh-CN", "female", "女");

      // 准备英文文案
      store.put("en-US", "user-title", "User Info Export");
      store.put("en-US", "username-header", "Username");
      store.put("en-US", "age-header", "Age");
      store.put("en-US", "gender-header", "Gender");
      store.put("en-US", "null-text", "Unknown");
      store.put("en-US", "blank-text", "Empty");
      store.put("en-US", "bool-true", "Yes");
      store.put("en-US", "bool-false", "No");
      store.put("en-US", "male", "Male");
      store.put("en-US", "female", "Female");

      resolver = new I18nResolver(store);
    }

    @Test
    @DisplayName("解析 title 中的占位符")
    void testResolveTitle() {
      DataDefinition template = createTemplate();
      template.setTitle("${user-title}");

      DataDefinition resolved = resolver.resolve(template, "zh-CN");
      assertEquals("用户信息导出", resolved.getTitle());

      resolved = resolver.resolve(template, "en-US");
      assertEquals("User Info Export", resolved.getTitle());
    }

    @Test
    @DisplayName("解析列 header 中的占位符")
    void testResolveColumnHeader() {
      DataDefinition template = createTemplate();
      addColumn(template, "username", "${username-header}", null, null, null, null);
      addColumn(template, "age", "${age-header}", null, null, null, null);

      DataDefinition resolved = resolver.resolve(template, "zh-CN");
      assertEquals("用户名", getColumn(resolved, "username").getHeader());
      assertEquals("年龄", getColumn(resolved, "age").getHeader());

      resolved = resolver.resolve(template, "en-US");
      assertEquals("Username", getColumn(resolved, "username").getHeader());
      assertEquals("Age", getColumn(resolved, "age").getHeader());
    }

    @Test
    @DisplayName("解析导出配置 whenNull 中的占位符")
    void testResolveWhenNull() {
      DataDefinition template = createTemplate();
      Exporting exporting = new Exporting();
      exporting.setWhenNull("${null-text}");
      addColumn(template, "username", "用户名", exporting, null, null, null);

      DataDefinition resolved = resolver.resolve(template, "zh-CN");
      assertEquals("未知", getColumn(resolved, "username").getExporting().getWhenNull());

      resolved = resolver.resolve(template, "en-US");
      assertEquals("Unknown", getColumn(resolved, "username").getExporting().getWhenNull());
    }

    @Test
    @DisplayName("解析导出配置 whenBlank 中的占位符")
    void testResolveWhenBlank() {
      DataDefinition template = createTemplate();
      Exporting exporting = new Exporting();
      exporting.setWhenBlank("${blank-text}");
      addColumn(template, "username", "用户名", exporting, null, null, null);

      DataDefinition resolved = resolver.resolve(template, "zh-CN");
      assertEquals("空", getColumn(resolved, "username").getExporting().getWhenBlank());

      resolved = resolver.resolve(template, "en-US");
      assertEquals("Empty", getColumn(resolved, "username").getExporting().getWhenBlank());
    }

    @Test
    @DisplayName("解析导出配置 converter 中的占位符")
    void testResolveExportingConverter() {
      DataDefinition template = createTemplate();
      Exporting exporting = new Exporting();
      exporting.setConverter("bool(${bool-true}, ${bool-false})");
      addColumn(template, "active", "是否激活", exporting, null, null, null);

      DataDefinition resolved = resolver.resolve(template, "zh-CN");
      assertEquals("bool(是, 否)", getColumn(resolved, "active").getExporting().getConverter());

      resolved = resolver.resolve(template, "en-US");
      assertEquals("bool(Yes, No)", getColumn(resolved, "active").getExporting().getConverter());
    }

    @Test
    @DisplayName("解析共享 mapping value 中的占位符")
    void testResolveSharedMapping() {
      DataDefinition template = createTemplate();
      Map<String, String> mapping = new LinkedHashMap<>();
      mapping.put("1", "${male}");
      mapping.put("2", "${female}");
      addColumn(template, "gender", "${gender-header}", null, null, mapping, null);

      DataDefinition resolved = resolver.resolve(template, "zh-CN");
      assertEquals("男", getColumn(resolved, "gender").getMapping().get("1"));
      assertEquals("女", getColumn(resolved, "gender").getMapping().get("2"));

      resolved = resolver.resolve(template, "en-US");
      assertEquals("Male", getColumn(resolved, "gender").getMapping().get("1"));
      assertEquals("Female", getColumn(resolved, "gender").getMapping().get("2"));
    }

    @Test
    @DisplayName("解析导出专属 mapping value 中的占位符")
    void testResolveExportingMapping() {
      DataDefinition template = createTemplate();
      Map<String, String> mapping = new LinkedHashMap<>();
      mapping.put("1", "${male}");
      mapping.put("2", "${female}");
      Exporting exporting = new Exporting();
      exporting.setMapping(mapping);
      addColumn(template, "gender", "${gender-header}", exporting, null, null, null);

      DataDefinition resolved = resolver.resolve(template, "zh-CN");
      assertEquals("男", getColumn(resolved, "gender").getExporting().getMapping().get("1"));
      assertEquals("女", getColumn(resolved, "gender").getExporting().getMapping().get("2"));
    }

    @Test
    @DisplayName("mapping 的 key 不被替换")
    void testMappingKeyNotReplaced() {
      DataDefinition template = createTemplate();
      Map<String, String> mapping = new LinkedHashMap<>();
      mapping.put("${some-key}", "值");
      addColumn(template, "test", "测试", null, null, mapping, null);

      DataDefinition resolved = resolver.resolve(template, "zh-CN");
      // key 保持原样（不被替换）
      assertTrue(resolved.getColumns().get("test").getMapping().containsKey("${some-key}"));
    }

    @Test
    @DisplayName("解析导入配置 converter 中的占位符")
    void testResolveImportingConverter() {
      DataDefinition template = createTemplate();
      Importing importing = new Importing();
      importing.setConverter("uppercase(${bool-true})");
      addColumn(template, "code", "编码", null, importing, null, null);

      DataDefinition resolved = resolver.resolve(template, "zh-CN");
      assertEquals("uppercase(是)", getColumn(resolved, "code").getImporting().getConverter());

      resolved = resolver.resolve(template, "en-US");
      assertEquals("uppercase(Yes)", getColumn(resolved, "code").getImporting().getConverter());
    }

    @Test
    @DisplayName("深拷贝 - 解析后的定义不影响原始模板")
    void testDeepCopyIsolation() {
      DataDefinition template = createTemplate();
      template.setTitle("${user-title}");
      addColumn(template, "username", "${username-header}", null, null, null, null);

      DataDefinition resolved = resolver.resolve(template, "zh-CN");

      // 原始模板未被修改
      assertEquals("${user-title}", template.getTitle());
      assertEquals("${username-header}", getColumn(template, "username").getHeader());

      // 解析后的定义已替换
      assertEquals("用户信息导出", resolved.getTitle());
      assertEquals("用户名", getColumn(resolved, "username").getHeader());
    }

    @Test
    @DisplayName("不含占位符的文本保持原样")
    void testPlainTextUnchanged() {
      DataDefinition template = createTemplate();
      template.setTitle("固定标题");
      Exporting exporting = new Exporting();
      exporting.setWhenNull("无");
      addColumn(template, "username", "用户名", exporting, null, null, null);

      DataDefinition resolved = resolver.resolve(template, "zh-CN");
      assertEquals("固定标题", resolved.getTitle());
      assertEquals("用户名", getColumn(resolved, "username").getHeader());
      assertEquals("无", getColumn(resolved, "username").getExporting().getWhenNull());
    }

    @Test
    @DisplayName("null 字段不被处理")
    void testNullFieldUnchanged() {
      DataDefinition template = createTemplate();
      template.setTitle(null);
      addColumn(template, "username", null, null, null, null, null);

      DataDefinition resolved = resolver.resolve(template, "zh-CN");
      assertNull(resolved.getTitle());
      assertNull(getColumn(resolved, "username").getHeader());
    }

    @Test
    @DisplayName("文本中混合占位符和普通文本")
    void testMixedPlaceholderAndText() {
      DataDefinition template = createTemplate();
      addColumn(template, "username", "【${username-header}】", null, null, null, null);

      DataDefinition resolved = resolver.resolve(template, "zh-CN");
      assertEquals("【用户名】", getColumn(resolved, "username").getHeader());

      resolved = resolver.resolve(template, "en-US");
      assertEquals("【Username】", getColumn(resolved, "username").getHeader());
    }

    @Test
    @DisplayName("占位符 key 不存在时保留 key 本身")
    void testPlaceholderKeyNotFound() {
      DataDefinition template = createTemplate();
      template.setTitle("${nonexistent-key}");

      DataDefinition resolved = resolver.resolve(template, "zh-CN");
      assertEquals("nonexistent-key", resolved.getTitle());
    }

    @Test
    @DisplayName("同一模板可多次解析为不同 locale 的定义")
    void testResolveSameTemplateMultipleLocales() {
      DataDefinition template = createTemplate();
      template.setTitle("${user-title}");
      addColumn(template, "username", "${username-header}", null, null, null, null);

      DataDefinition zhResolved = resolver.resolve(template, "zh-CN");
      DataDefinition enResolved = resolver.resolve(template, "en-US");

      assertEquals("用户信息导出", zhResolved.getTitle());
      assertEquals("用户名", getColumn(zhResolved, "username").getHeader());

      assertEquals("User Info Export", enResolved.getTitle());
      assertEquals("Username", getColumn(enResolved, "username").getHeader());

      // 原始模板始终不变
      assertEquals("${user-title}", template.getTitle());
    }

    @Test
    @DisplayName("完整场景 - 多字段同时解析")
    void testFullScenario() {
      DataDefinition template = createTemplate();
      template.setTitle("${user-title}");

      // gender 列：共享 mapping + 导出 whenNull
      Map<String, String> genderMapping = new LinkedHashMap<>();
      genderMapping.put("1", "${male}");
      genderMapping.put("2", "${female}");
      Exporting genderExporting = new Exporting();
      genderExporting.setWhenNull("${null-text}");
      addColumn(template, "gender", "${gender-header}", genderExporting, null, genderMapping, null);

      // active 列：导出 converter + 导出 whenBlank
      Exporting activeExporting = new Exporting();
      activeExporting.setWhenBlank("${blank-text}");
      activeExporting.setConverter("bool(${bool-true}, ${bool-false})");
      addColumn(template, "active", "${active-header}", activeExporting, null, null, null);

      // 补充 active-header
      store.put("zh-CN", "active-header", "是否激活");
      store.put("en-US", "active-header", "Active");

      // 中文解析
      DataDefinition zhResolved = resolver.resolve(template, "zh-CN");
      assertEquals("用户信息导出", zhResolved.getTitle());
      assertEquals("性别", getColumn(zhResolved, "gender").getHeader());
      assertEquals("未知", getColumn(zhResolved, "gender").getExporting().getWhenNull());
      assertEquals("男", getColumn(zhResolved, "gender").getMapping().get("1"));
      assertEquals("女", getColumn(zhResolved, "gender").getMapping().get("2"));
      assertEquals("是否激活", getColumn(zhResolved, "active").getHeader());
      assertEquals("空", getColumn(zhResolved, "active").getExporting().getWhenBlank());
      assertEquals("bool(是, 否)", getColumn(zhResolved, "active").getExporting().getConverter());

      // 英文解析
      DataDefinition enResolved = resolver.resolve(template, "en-US");
      assertEquals("User Info Export", enResolved.getTitle());
      assertEquals("Gender", getColumn(enResolved, "gender").getHeader());
      assertEquals("Unknown", getColumn(enResolved, "gender").getExporting().getWhenNull());
      assertEquals("Male", getColumn(enResolved, "gender").getMapping().get("1"));
      assertEquals("Female", getColumn(enResolved, "gender").getMapping().get("2"));
      assertEquals("Active", getColumn(enResolved, "active").getHeader());
      assertEquals("Empty", getColumn(enResolved, "active").getExporting().getWhenBlank());
      assertEquals("bool(Yes, No)", getColumn(enResolved, "active").getExporting().getConverter());
    }

    // ===== 辅助方法 =====

    private DataDefinition createTemplate() {
      DataDefinition definition = new DataDefinition();
      definition.setDomainName("TestDomain");
      definition.setFilename("test-export");
      definition.setColumns(new LinkedHashMap<>());
      return definition;
    }

    private void addColumn(DataDefinition definition, String name, String header,
                           Exporting exporting, Importing importing,
                           Map<String, String> mapping, Map<String, String> importingMapping) {
      Column column = new Column();
      column.setHeader(header);
      if (exporting != null) {
        column.setExporting(exporting);
      }
      if (importing != null) {
        column.setImporting(importing);
      }
      if (mapping != null) {
        column.setMapping(mapping);
      }
      if (importingMapping != null) {
        if (column.getImporting() == null) {
          column.setImporting(new Importing());
        }
        column.getImporting().setMapping(importingMapping);
      }
      definition.getColumns().put(name, column);
    }

    private Column getColumn(DataDefinition definition, String name) {
      return definition.getColumns().get(name);
    }
  }
}
