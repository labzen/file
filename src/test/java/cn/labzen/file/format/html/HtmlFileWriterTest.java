package cn.labzen.file.format.html;

import cn.labzen.file.bean.Property;
import cn.labzen.file.definition.DefinitionLoader;
import cn.labzen.file.definition.DefinitionRegistry;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.format.DataFileGenerator;
import cn.labzen.file.format.MockData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HTML 文件写入器测试
 * <p>
 * 测试 HTML 文件生成功能，包括：
 * <ul>
 *   <li>配置加载和 HTML 生成</li>
 *   <li>页面标题使用 title</li>
 *   <li>表格标题行使用最低级 header 内容</li>
 *   <li>CSS 样式内联在 HTML 文件中</li>
 *   <li>空值处理</li>
 *   <li>斑马纹和悬停效果</li>
 * </ul>
 *
 * @author labzen
 */
@DisplayName("HTML 文件写入器测试")
class HtmlFileWriterTest {

  private static final String OUTPUT_DIR = System.getProperty("user.dir") + "/.testing";
  private static final String OUTPUT_FILE = OUTPUT_DIR + "/property-test.html";

  private HtmlFileWriter<Property> htmlWriter;

  @BeforeEach
  void setUp() {
    // 清理之前的注册数据
    DefinitionRegistry.clear();

    // 创建配置加载器
    DefinitionLoader loader = new DefinitionLoader(
      "classpath*:data-export/**/*.yml",
      "classpath*:global/__global__.yml"
    );
    loader.load();

    // 创建 HTML 写入器
    htmlWriter = new HtmlFileWriter<>();

    // 确保输出目录存在
    File outputDir = new File(OUTPUT_DIR);
    if (!outputDir.exists()) {
      outputDir.mkdirs();
    }

    // 清理可能存在的旧文件
    File oldFile = new File(OUTPUT_FILE);
    if (oldFile.exists()) {
      oldFile.delete();
    }
  }

  @AfterEach
  void tearDown() {
    DefinitionRegistry.clear();

    // 清理测试生成的 HTML 文件
    File outputFile = new File(OUTPUT_FILE);
    if (outputFile.exists()) {
      // outputFile.delete();
    }
  }

  @Test
  @DisplayName("测试基本 HTML 文件生成")
  void testBasicHtmlGeneration() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 从 Registry 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 HTML 到 ByteArrayOutputStream
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    htmlWriter.write(definition, data, baos);
    byte[] htmlBytes = baos.toByteArray();
    String htmlContent = new String(htmlBytes, StandardCharsets.UTF_8);

    // 验证 HTML 格式
    assertNotNull(htmlContent);

    // 验证包含 DOCTYPE 和基本结构
    assertTrue(htmlContent.startsWith("<!DOCTYPE html>"), "HTML 应以 DOCTYPE 开始");
    assertTrue(htmlContent.contains("<html"), "应包含 html 标签");
    assertTrue(htmlContent.contains("<head>"), "应包含 head 标签");
    assertTrue(htmlContent.contains("<body>"), "应包含 body 标签");

    // 验证包含表格
    assertTrue(htmlContent.contains("<table"), "应包含 table 标签");
    assertTrue(htmlContent.contains("</table>"), "应包含表格结束标签");
  }

  @Test
  @DisplayName("测试页面标题使用 title")
  void testTitleAsPageTitle() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 HTML 文件
    htmlWriter.write(definition, data, OUTPUT_FILE);

    // 读取文件内容
    String htmlContent = Files.readString(Paths.get(OUTPUT_FILE));

    // 验证页面标题使用 title
    assertTrue(htmlContent.contains("<title>系统属性</title>"), "title 标签应包含 title 内容");
    assertTrue(htmlContent.contains("class=\"page-title\">系统属性"), "页面标题应使用 title 内容");
  }

  @Test
  @DisplayName("测试多级表头生成")
  void testMultiLevelHeader() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 HTML 文件
    htmlWriter.write(definition, data, OUTPUT_FILE);

    // 读取文件内容
    String htmlContent = Files.readString(Paths.get(OUTPUT_FILE));

    // 验证多级表头结构
    // name 和 indexical 有二级表头：["标识", "属性名称/索引"]
    // value、createTime、size 只有一级表头

    // 验证 colspan（"标识"应跨越两列）
    assertTrue(htmlContent.contains("colspan=\""), "相同表头应包含 colspan");

    // 验证 rowspan（后三列只有一级表头，应该使用 rowspan 跨越两行）
    assertTrue(htmlContent.contains("rowspan=\""), "单级表头列应包含 rowspan 跨越到下一行");

    // 验证各级表头内容
    assertTrue(htmlContent.contains(">标识</th>"), "应包含 '标识' 表头");
    assertTrue(htmlContent.contains(">索引</th>"), "应包含 '索引' 表头");
    assertTrue(htmlContent.contains(">属性值</th>"), "应包含 '属性值' 表头");
    assertTrue(htmlContent.contains(">创建时间</th>"), "应包含 '创建时间' 表头");
    assertTrue(htmlContent.contains(">大小</th>"), "应包含 '大小' 表头");

    // 验证有两行表头
    String theadSection = htmlContent.substring(htmlContent.indexOf("<thead>"), htmlContent.indexOf("</thead>"));
    int trCount = theadSection.split("<tr>").length - 1;
    assertEquals(2, trCount, "多级表头应有 2 行");
  }

  @Test
  @DisplayName("测试列字体颜色配置")
  void testColumnFontColor() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 HTML
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    htmlWriter.write(definition, data, baos);
    String htmlContent = new String(baos.toByteArray(), StandardCharsets.UTF_8);

    // 验证 name 列的字体颜色配置（#3552d4）
    // CSS 样式中应包含 .data-table td.col-name{color:#3552d4}
    assertTrue(htmlContent.contains(".data-table td.col-name"), "name 列应有独立的 CSS 类");
    assertTrue(htmlContent.contains("color:#3552d4"), "name 列应有字体颜色 #3552d4");
  }

  @Test
  @DisplayName("测试 CSS 样式内联在文件中")
  void testCssInline() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 HTML
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    htmlWriter.write(definition, data, baos);
    String htmlContent = new String(baos.toByteArray(), StandardCharsets.UTF_8);

    // 验证 CSS 样式在 <style> 标签内
    assertTrue(htmlContent.contains("<style>"), "应包含 style 标签");
    assertTrue(htmlContent.contains("</style>"), "应包含 style 结束标签");

    // 验证 CSS 内容包含表格样式
    assertTrue(htmlContent.contains("data-table"), "CSS 应包含 data-table 类");
    assertTrue(htmlContent.contains("border-collapse"), "CSS 应包含边框样式");

    // 验证不产生外部 CSS 文件引用（无 link 标签）
    assertFalse(htmlContent.contains("<link"), "不应包含 link 标签（外部CSS文件）");

    // 验证不产生外部 JS 文件
    assertFalse(htmlContent.contains("<script src"), "不应包含外部 JS 引用");
  }

  @Test
  @DisplayName("测试斑马纹和悬停效果")
  void testZebraAndHoverEffect() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 HTML
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    htmlWriter.write(definition, data, baos);
    String htmlContent = new String(baos.toByteArray(), StandardCharsets.UTF_8);

    // 验证斑马纹样式
    assertTrue(htmlContent.contains("nth-child(even)"), "应包含斑马纹样式");

    // 验证悬停效果
    assertTrue(htmlContent.contains("tr:hover"), "应包含悬停效果样式");
  }

  @Test
  @DisplayName("测试数据行内容")
  void testDataLineContent() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 HTML 文件
    htmlWriter.write(definition, data, OUTPUT_FILE);

    // 读取文件内容
    String htmlContent = Files.readString(Paths.get(OUTPUT_FILE));

    // 验证数据行包含实际值（配置中的 prefix: "__" 会自动添加前缀）
    assertTrue(htmlContent.contains("__系统配置"), "数据行应包含 name 值（带前缀）");
    assertTrue(htmlContent.contains("debug=true"), "数据行应包含 value 值");
    assertTrue(htmlContent.contains(">1<"), "数据行应包含 indexical 值");
  }

  @Test
  @DisplayName("测试空值处理")
  void testNullValueHandling() throws IOException {
    // 准备包含 null 值的数据
    Property propertyWithNull = new Property();
    propertyWithNull.setName(null);
    propertyWithNull.setValue("test-value");
    propertyWithNull.setIndexical(100);
    propertyWithNull.setCreateTime(null);
    propertyWithNull.setSize(null);

    List<Property> data = Arrays.asList(propertyWithNull);

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 HTML
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    htmlWriter.write(definition, data, baos);
    String htmlContent = new String(baos.toByteArray(), StandardCharsets.UTF_8);

    // 验证空值处理 - null 值应为空
    assertTrue(htmlContent.contains("><"), "null 值应为空（td 内容为空）");
  }

  @Test
  @DisplayName("测试通过 DataFileGenerator 生成文件")
  void testDataFileGenerator() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 使用 DataFileGenerator 生成 HTML 文件
    DataFileGenerator.by(Property.class)
      .with(data)
      .as(FileFormat.HTML)
      .to(OUTPUT_FILE);

    // 验证文件已创建
    File outputFile = new File(OUTPUT_FILE);
    assertTrue(outputFile.exists(), "HTML 文件应已创建");

    // 读取文件内容并验证
    String htmlContent = Files.readString(outputFile.toPath());
    assertTrue(htmlContent.startsWith("<!DOCTYPE html>"), "文件应以 DOCTYPE 开始");
    assertTrue(htmlContent.contains(">系统属性</h1>"), "文件应包含标题");
    assertTrue(htmlContent.contains("<table"), "文件应包含表格");
    // 验证表头内容
    assertTrue(htmlContent.contains(">标识</th>"), "文件应包含 '标识' 表头");
    assertTrue(htmlContent.contains(">索引</th>"), "文件应包含 '索引' 表头");
    assertTrue(htmlContent.contains(">属性值</th>"), "文件应包含 '属性值' 表头");
    // 验证 rowspan
    assertTrue(htmlContent.contains("rowspan=\""), "文件应包含 rowspan 属性");
  }

  @Test
  @DisplayName("测试空数据集合抛出异常")
  void testEmptyDataThrowsException() {
    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 准备空数据集合
    List<Property> data = Arrays.asList();

    // 验证抛出异常
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      htmlWriter.write(definition, data, baos);
      fail("应抛出异常");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("不能为空"),
        "空数据应抛出包含 '不能为空' 的异常");
    }
  }

}