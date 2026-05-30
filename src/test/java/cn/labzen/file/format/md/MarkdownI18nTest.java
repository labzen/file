package cn.labzen.file.format.md;

import cn.labzen.file.bean.PropertyI18n;
import cn.labzen.file.definition.DefinitionLoader;
import cn.labzen.file.definition.DefinitionRegistry;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.format.DataFileExporter;
import cn.labzen.file.i18n.I18nStoreHolder;
import cn.labzen.file.i18n.ManualI18NStoreProvider;
import cn.labzen.meta.LabzenMetaInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Markdown 文件写入器国际化集成测试
 * <p>
 * 使用 PropertyI18n.yml（含 ${key} 占位符），验证端到端 i18n 解析流程：
 * YAML 定义 → I18nStoreProvider 注入 → I18nResolver 替换 → Markdown 文件输出
 *
 * @author labzen
 */
@DisplayName("Markdown 国际化集成测试")
class MarkdownI18nTest {

  private static final String OUTPUT_DIR = System.getProperty("user.dir") + "/.testing";

  private ManualI18NStoreProvider store;

  @BeforeEach
  void setUp() {
    new LabzenMetaInitializer().initialize(null);
    DefinitionRegistry.clear();

    DefinitionLoader loader = new DefinitionLoader(
      "classpath*:data-export/**/*.yml",
      "classpath*:data-export/__global__.yml"
    );
    loader.load();

    // 初始化 i18n 仓库
    store = new ManualI18NStoreProvider();
    store.setDefaultLocale("zh-CN");
    prepareChineseTexts();
    prepareEnglishTexts();

    // 注册到全局持有者
    I18nStoreHolder.register(store);

    File outputDir = new File(OUTPUT_DIR);
    if (!outputDir.exists()) {
      outputDir.mkdirs();
    }
  }

  @AfterEach
  void tearDown() {
    DefinitionRegistry.clear();
    I18nStoreHolder.register(null);
  }

  @Test
  @DisplayName("中文 locale 导出 - 验证占位符替换为中文")
  void testChineseLocaleExport() throws IOException {
    List<PropertyI18n> data = createSimpleData();
    File outputFile = DataFileExporter.by(PropertyI18n.class)
      .with(data)
      .as(FileFormat.MARKDOWN)
      .locale("zh-CN")
      .folder(OUTPUT_DIR)
      .to();

    String content = Files.readString(outputFile.toPath());

    // title 替换
    assertTrue(content.contains("# 系统属性"), "标题应为'系统属性'");

    // header 替换
    assertTrue(content.contains("| 属性名称 |"), "表头应包含'属性名称'");
    assertTrue(content.contains("| 索引 |"), "表头应包含'索引'");
    assertTrue(content.contains("| 属性值 |"), "表头应包含'属性值'");
    assertTrue(content.contains("| 状态 |"), "表头应包含'状态'");
    assertTrue(content.contains("| 分类 |"), "表头应包含'分类'");
    assertTrue(content.contains("| 手机号码 |"), "表头应包含'手机号码'");
    assertTrue(content.contains("| 电子邮箱 |"), "表头应包含'电子邮箱'");
    assertTrue(content.contains("| 内容描述 |"), "表头应包含'内容描述'");

    // when-null 替换（null 值的 name 列显示"未命名"）
    assertTrue(content.contains("| 【未命名】 |"), "null 值 name 列应显示'【未命名】'");

    // mapping 替换
    assertTrue(content.contains("调试开启"), "mapping 值应替换为'调试开启'");
    assertTrue(content.contains("启用"), "status mapping 应替换为'启用'");
    assertTrue(content.contains("系统"), "category mapping 应替换为'系统'");
  }

  @Test
  @DisplayName("英文 locale 导出 - 验证占位符替换为英文")
  void testEnglishLocaleExport() throws IOException {
    List<PropertyI18n> data = createSimpleData();
    File outputFile = DataFileExporter.by(PropertyI18n.class)
      .with(data)
      .as(FileFormat.MARKDOWN)
      .locale("en-US")
      .folder(OUTPUT_DIR)
      .to();

    String content = Files.readString(outputFile.toPath());

    // title 替换
    assertTrue(content.contains("# System Properties"), "标题应为'System Properties'");

    // header 替换
    assertTrue(content.contains("| Property Name |"), "表头应包含'Property Name'");
    assertTrue(content.contains("| Index |"), "表头应包含'Index'");
    assertTrue(content.contains("| Property Value |"), "表头应包含'Property Value'");
    assertTrue(content.contains("| Status |"), "表头应包含'Status'");
    assertTrue(content.contains("| Category |"), "表头应包含'Category'");
    assertTrue(content.contains("| Phone |"), "表头应包含'Phone'");
    assertTrue(content.contains("| Email |"), "表头应包含'Email'");
    assertTrue(content.contains("| Description |"), "表头应包含'Description'");

    // when-null 替换（null 值的 name 列显示"Unnamed"）
    assertTrue(content.contains("| 【Unnamed】 |"), "null 值 name 列应显示'【Unnamed】'");

    // mapping 替换
    assertTrue(content.contains("Debug On"), "mapping 值应替换为'Debug On'");
    assertTrue(content.contains("Enabled"), "status mapping 应替换为'Enabled'");
    assertTrue(content.contains("System"), "category mapping 应替换为'System'");
  }

  @Test
  @DisplayName("日文 locale 回退至默认中文 locale")
  void testLocaleFallback() throws IOException {
    List<PropertyI18n> data = createSimpleData();
    File outputFile = DataFileExporter.by(PropertyI18n.class)
      .with(data)
      .as(FileFormat.MARKDOWN)
      .locale("ja-JP")
      .folder(OUTPUT_DIR)
      .to();

    String content = Files.readString(outputFile.toPath());

    // ja-JP 没有配置文案，应回退到默认 locale zh-CN
    assertTrue(content.contains("# 系统属性"), "日文 locale 应回退到中文标题");
    assertTrue(content.contains("| 属性名称 |"), "日文 locale 应回退到中文表头");
    assertTrue(content.contains("调试开启"), "日文 locale 应回退到中文 mapping 值");
  }

  @Test
  @DisplayName("同一模板多次导出不同 locale 互不影响")
  void testMultipleLocaleExports() throws IOException {
    List<PropertyI18n> data = createSimpleData();
    String zhPath = OUTPUT_DIR + "/property-i18n-zh.md";
    String enPath = OUTPUT_DIR + "/property-i18n-en.md";

    // 中文导出
    DataFileExporter.by(PropertyI18n.class)
      .with(data)
      .as(FileFormat.MARKDOWN)
      .locale("zh-CN")
      .to(zhPath);

    // 英文导出
    DataFileExporter.by(PropertyI18n.class)
      .with(data)
      .as(FileFormat.MARKDOWN)
      .locale("en-US")
      .to(enPath);

    String zhContent = Files.readString(new File(zhPath).toPath());
    String enContent = Files.readString(new File(enPath).toPath());

    // 两个文件内容不同
    assertTrue(zhContent.contains("系统属性"), "中文文件应包含'系统属性'");
    assertTrue(enContent.contains("System Properties"), "英文文件应包含'System Properties'");
    assertFalse(zhContent.contains("System Properties"), "中文文件不应包含英文标题");
    assertFalse(enContent.contains("系统属性"), "英文文件不应包含中文标题");
  }

  @Test
  @DisplayName("i18n 仓库运行时更新后导出内容随之变化")
  void testStoreUpdateAtRuntime() throws IOException {
    List<PropertyI18n> data = createSimpleData();

    // 第一次中文导出
    File file1 = DataFileExporter.by(PropertyI18n.class)
      .with(data)
      .as(FileFormat.MARKDOWN)
      .locale("zh-CN")
      .folder(OUTPUT_DIR)
      .to();
    String content1 = Files.readString(file1.toPath());
    assertTrue(content1.contains("# 系统属性"), "初始中文标题应为'系统属性'");

    // 运行时更新 i18n 仓库
    store.put("zh-CN", "property-title", "属性列表（已更新）");

    // 第二次中文导出
    File file2 = DataFileExporter.by(PropertyI18n.class)
      .with(data)
      .as(FileFormat.MARKDOWN)
      .locale("zh-CN")
      .folder(OUTPUT_DIR)
      .to();
    String content2 = Files.readString(file2.toPath());
    assertTrue(content2.contains("# 属性列表（已更新）"), "更新后标题应为'属性列表（已更新）'");
  }

  // ===== 辅助方法 =====

  private List<PropertyI18n> createSimpleData() {
    long now = System.currentTimeMillis();

    PropertyI18n p1 = new PropertyI18n();
    p1.setName("系统配置");
    p1.setValue("debug=true");
    p1.setIndexical(1);
    p1.setPhone("13800138000");
    p1.setEmail("admin@example.com");
    p1.setCategory("A");
    p1.setDescription("系统级别配置项");
    p1.setCreateTime(new Date(now));
    p1.setEffectiveDate(LocalDate.now());
    p1.setLastModified(LocalDateTime.now());
    p1.setRemindTime(LocalTime.of(9, 30));
    p1.setSize(1024.5);
    p1.setStatus(1);

    // 包含 null 值的行，测试 when-null
    PropertyI18n p2 = new PropertyI18n();
    p2.setName(null);
    p2.setValue(null);
    p2.setIndexical(null);
    p2.setPhone("13912345678");
    p2.setEmail("test@example.com");
    p2.setCategory("B");
    p2.setDescription(null);
    p2.setCreateTime(null);
    p2.setEffectiveDate(null);
    p2.setLastModified(null);
    p2.setRemindTime(null);
    p2.setSize(null);
    p2.setStatus(2);

    return Arrays.asList(p1, p2);
  }

  private void prepareChineseTexts() {
    store.putAll("zh-CN", java.util.Map.ofEntries(
      java.util.Map.entry("property-title", "系统属性"),
      java.util.Map.entry("basic-info", "基本信息"),
      java.util.Map.entry("prop-name", "属性名称"),
      java.util.Map.entry("index", "索引"),
      java.util.Map.entry("prop-value", "属性值"),
      java.util.Map.entry("status-info", "状态信息"),
      java.util.Map.entry("status", "状态"),
      java.util.Map.entry("category", "分类"),
      java.util.Map.entry("contact-info", "联系方式"),
      java.util.Map.entry("phone-number", "手机号码"),
      java.util.Map.entry("email-addr", "电子邮箱"),
      java.util.Map.entry("desc", "内容描述"),
      // when-null / when-blank
      java.util.Map.entry("unnamed", "未命名"),
      java.util.Map.entry("no-value", "无值"),
      java.util.Map.entry("blank-value", "空值"),
      java.util.Map.entry("unknown", "未知"),
      java.util.Map.entry("uncategorized", "未分类"),
      java.util.Map.entry("no-desc", "暂无描述"),
      // mapping 值
      java.util.Map.entry("debug-on", "调试开启"),
      java.util.Map.entry("debug-off", "调试关闭"),
      java.util.Map.entry("info-level", "信息级别"),
      java.util.Map.entry("enabled", "启用"),
      java.util.Map.entry("disabled", "禁用"),
      java.util.Map.entry("pending", "待审核"),
      java.util.Map.entry("system", "系统"),
      java.util.Map.entry("business", "业务"),
      java.util.Map.entry("other", "其他")
    ));
  }

  private void prepareEnglishTexts() {
    store.putAll("en-US", java.util.Map.ofEntries(
      java.util.Map.entry("property-title", "System Properties"),
      java.util.Map.entry("basic-info", "Basic Info"),
      java.util.Map.entry("prop-name", "Property Name"),
      java.util.Map.entry("index", "Index"),
      java.util.Map.entry("prop-value", "Property Value"),
      java.util.Map.entry("status-info", "Status Info"),
      java.util.Map.entry("status", "Status"),
      java.util.Map.entry("category", "Category"),
      java.util.Map.entry("contact-info", "Contact"),
      java.util.Map.entry("phone-number", "Phone"),
      java.util.Map.entry("email-addr", "Email"),
      java.util.Map.entry("desc", "Description"),
      // when-null / when-blank
      java.util.Map.entry("unnamed", "Unnamed"),
      java.util.Map.entry("no-value", "No Value"),
      java.util.Map.entry("blank-value", "Blank"),
      java.util.Map.entry("unknown", "Unknown"),
      java.util.Map.entry("uncategorized", "Uncategorized"),
      java.util.Map.entry("no-desc", "No Description"),
      // mapping 值
      java.util.Map.entry("debug-on", "Debug On"),
      java.util.Map.entry("debug-off", "Debug Off"),
      java.util.Map.entry("info-level", "Info Level"),
      java.util.Map.entry("enabled", "Enabled"),
      java.util.Map.entry("disabled", "Disabled"),
      java.util.Map.entry("pending", "Pending"),
      java.util.Map.entry("system", "System"),
      java.util.Map.entry("business", "Business"),
      java.util.Map.entry("other", "Other")
    ));
  }
}
