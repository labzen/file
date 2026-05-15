package cn.labzen.file.format.md;

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
 * Markdown 文件写入器测试
 * <p>
 * 测试 Markdown 文件生成功能，包括：
 * <ul>
 *   <li>配置加载和 Markdown 生成</li>
 *   <li>大标题使用 title</li>
 *   <li>标题行使用最低级 header 内容</li>
 *   <li>Markdown 表格格式</li>
 *   <li>空值处理</li>
 * </ul>
 *
 * @author labzen
 */
@DisplayName("Markdown 文件写入器测试")
class MarkdownFileWriterTest {

  private static final String OUTPUT_DIR = System.getProperty("user.dir") + "/.testing";
  private static final String OUTPUT_FILE = OUTPUT_DIR + "/property-test.md";

  private MarkdownFileWriter<Property> mdWriter;

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

    // 创建 Markdown 写入器
    mdWriter = new MarkdownFileWriter<>();

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

    // 清理测试生成的 Markdown 文件
    File outputFile = new File(OUTPUT_FILE);
    if (outputFile.exists()) {
      // outputFile.delete();
    }
  }

  @Test
  @DisplayName("测试基本 Markdown 文件生成")
  void testBasicMarkdownGeneration() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 从 Registry 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 Markdown 到 ByteArrayOutputStream
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    mdWriter.write(definition, data, baos);
    byte[] mdBytes = baos.toByteArray();
    String mdContent = new String(mdBytes, StandardCharsets.UTF_8);

    // 验证 Markdown 格式
    assertNotNull(mdContent);

    // 验证包含大标题
    assertTrue(mdContent.startsWith("# 系统属性"), "Markdown 应以大标题开始");

    // 验证包含表格分隔行
    assertTrue(mdContent.contains("|---|"), "应包含表格分隔行");

    // 验证包含3条数据行
    long dataRowCount = mdContent.lines()
      .filter(line -> line.startsWith("|") && !line.startsWith("|---|"))
      .count();
    assertEquals(4, dataRowCount, "应包含标题行和3条数据行（共4行）");
  }

  @Test
  @DisplayName("测试大标题使用 title")
  void testTitleAsHeading() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 Markdown 文件
    mdWriter.write(definition, data, OUTPUT_FILE);

    // 读取文件内容
    String mdContent = Files.readString(Paths.get(OUTPUT_FILE));

    // 验证大标题使用 title
    assertTrue(mdContent.startsWith("# 系统属性"), "大标题应使用 title 内容");
  }

  @Test
  @DisplayName("测试标题行使用最低级 header 内容")
  void testHeaderContent() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 Markdown 文件
    mdWriter.write(definition, data, OUTPUT_FILE);

    // 读取文件内容
    String mdContent = Files.readString(Paths.get(OUTPUT_FILE));
    String[] lines = mdContent.split("\n");

    // 验证标题行使用最低级表头内容
    // columns 的 header 最后一个值：name->属性名称, indexical->索引, value->属性值, createTime->创建时间, size->大小
    String headerLine = lines[2]; // 第3行是标题行（第1行是标题，第2行是空行）
    assertTrue(headerLine.contains("属性名称"), "标题行应包含 '属性名称'");
    assertTrue(headerLine.contains("索引"), "标题行应包含 '索引'");
    assertTrue(headerLine.contains("属性值"), "标题行应包含 '属性值'");
    assertTrue(headerLine.contains("创建时间"), "标题行应包含 '创建时间'");
    assertTrue(headerLine.contains("大小"), "标题行应包含 '大小'");

    // 验证不包含 columns 的 key 名
    assertFalse(headerLine.contains("name"), "标题行不应包含 'name'");
    assertFalse(headerLine.contains("indexical"), "标题行不应包含 'indexical'");
  }

  @Test
  @DisplayName("测试 Markdown 表格格式")
  void testTableFormat() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 Markdown
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    mdWriter.write(definition, data, baos);
    String mdContent = new String(baos.toByteArray(), StandardCharsets.UTF_8);

    // 验证 Markdown 表格格式
    assertTrue(mdContent.contains("|---|"), "应包含表格分隔行 |---|");
    assertTrue(mdContent.contains("|"), "应使用 | 作为表格边框");
  }

  @Test
  @DisplayName("测试数据行内容")
  void testDataLineContent() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 Markdown 文件
    mdWriter.write(definition, data, OUTPUT_FILE);

    // 读取文件内容
    String mdContent = Files.readString(Paths.get(OUTPUT_FILE));

    // 验证数据行包含实际值（配置中的 prefix: "__" 会自动添加前缀）
    assertTrue(mdContent.contains("__系统配置"), "数据行应包含 name 值（带前缀）");
    assertTrue(mdContent.contains("debug=true"), "数据行应包含 value 值");
    assertTrue(mdContent.contains("| 1 |"), "数据行应包含 indexical 值");
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

    // 生成 Markdown
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    mdWriter.write(definition, data, baos);
    String mdContent = new String(baos.toByteArray(), StandardCharsets.UTF_8);

    // 验证空值处理 - null 值应为空字符串
    assertTrue(mdContent.contains("| |"), "null 值应为空");
  }

  @Test
  @DisplayName("测试通过 DataFileGenerator 生成文件")
  void testDataFileGenerator() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 使用 DataFileGenerator 生成 Markdown 文件
    DataFileGenerator.by(Property.class)
      .with(data)
      .as(FileFormat.MARKDOWN)
      .to(OUTPUT_FILE);

    // 验证文件已创建
    File outputFile = new File(OUTPUT_FILE);
    assertTrue(outputFile.exists(), "Markdown 文件应已创建");

    // 读取文件内容并验证
    String mdContent = Files.readString(outputFile.toPath());
    assertTrue(mdContent.startsWith("# 系统属性"), "文件应以大标题开始");
    assertTrue(mdContent.contains("| 属性名称 |"), "文件应包含表头");
    assertTrue(mdContent.contains("| ---"), "文件应包含表格分隔行");
    assertTrue(mdContent.contains("__系统配置"), "文件应包含带前缀的数据值");
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
      mdWriter.write(definition, data, baos);
      fail("应抛出异常");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("不能为空"),
        "空数据应抛出包含 '不能为空' 的异常");
    }
  }

}