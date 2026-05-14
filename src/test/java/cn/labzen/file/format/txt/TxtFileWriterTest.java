package cn.labzen.file.format.txt;

import cn.labzen.file.bean.Property;
import cn.labzen.file.definition.DefinitionLoader;
import cn.labzen.file.definition.DefinitionRegistry;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.format.DataFileGenerator;
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
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TXT 文件写入器测试
 * <p>
 * 测试 TXT 文件生成功能，包括：
 * <ul>
 *   <li>配置加载和 TXT 生成</li>
 *   <li>标题行使用 header 内容</li>
 *   <li>数据行使用字段值</li>
 *   <li>制表符分隔</li>
 *   <li>空值处理</li>
 * </ul>
 *
 * @author labzen
 */
@DisplayName("TXT 文件写入器测试")
class TxtFileWriterTest {

  private static final String OUTPUT_DIR = System.getProperty("user.dir") + "/.testing";
  private static final String OUTPUT_FILE = OUTPUT_DIR + "/property-test.txt";

  private TxtFileWriter<Property> txtWriter;

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

    // 创建 TXT 写入器
    txtWriter = new TxtFileWriter<>();

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

    // 清理测试生成的 TXT 文件
    File outputFile = new File(OUTPUT_FILE);
    if (outputFile.exists()) {
      // outputFile.delete();
    }
  }

  @Test
  @DisplayName("测试基本 TXT 文件生成")
  void testBasicTxtGeneration() throws IOException {
    // 准备测试数据
    List<Property> data = createMockData();

    // 从 Registry 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 TXT 到 ByteArrayOutputStream
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    txtWriter.write(definition, data, baos);
    byte[] txtBytes = baos.toByteArray();
    String txtContent = new String(txtBytes, StandardCharsets.UTF_8);

    // 验证 TXT 格式
    assertNotNull(txtContent);

    // 验证包含标题行和3条数据行（共4行）
    String[] lines = txtContent.trim().split("\n");
    assertEquals(4, lines.length, "应包含标题行和3条数据行");

    // 验证标题行包含 header 内容（使用最低级表头）
    assertTrue(lines[0].contains("属性名称"), "标题行应包含 '属性名称' 作为第一列标题");
  }

  @Test
  @DisplayName("测试标题行使用 header 内容")
  void testHeaderContent() throws IOException {
    // 准备测试数据
    List<Property> data = createMockData();

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 TXT 文件
    txtWriter.write(definition, data, OUTPUT_FILE);

    // 读取文件内容
    String txtContent = Files.readString(Paths.get(OUTPUT_FILE));
    String[] lines = txtContent.trim().split("\n");

    // 验证标题行使用最低级表头内容
    // columns 的 header 最后一个值：name->属性名称, indexical->索引, value->属性值, createTime->创建时间, size->大小
    String headerLine = lines[0];
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
  @DisplayName("测试制表符分隔")
  void testTabSeparator() throws IOException {
    // 准备测试数据
    List<Property> data = createMockData();

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 TXT
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    txtWriter.write(definition, data, baos);
    String txtContent = new String(baos.toByteArray(), StandardCharsets.UTF_8);

    // 验证使用制表符分隔
    assertTrue(txtContent.contains("\t"), "TXT 应使用制表符分隔");
  }

  @Test
  @DisplayName("测试数据行内容")
  void testDataLineContent() throws IOException {
    // 准备测试数据
    List<Property> data = createMockData();

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 TXT 文件
    txtWriter.write(definition, data, OUTPUT_FILE);

    // 读取文件内容
    String txtContent = Files.readString(Paths.get(OUTPUT_FILE));
    String[] lines = txtContent.trim().split("\n");

    // 验证数据行包含实际值（配置中的 prefix: "__" 会自动添加前缀）
    String firstDataLine = lines[1];
    assertTrue(firstDataLine.contains("__系统配置"), "数据行应包含 name 值（带前缀）");
    assertTrue(firstDataLine.contains("debug=true"), "数据行应包含 value 值");
    assertTrue(firstDataLine.contains("1"), "数据行应包含 indexical 值");
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

    // 生成 TXT
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    txtWriter.write(definition, data, baos);
    String txtContent = new String(baos.toByteArray(), StandardCharsets.UTF_8);
    String[] lines = txtContent.trim().split("\n");

    // 验证空值处理 - null 值应为空字符串
    String dataLine = lines[1];
    // 按 columns 顺序: name, indexical, value, createTime, size
    // null 的字段应为空
    String[] values = dataLine.split("\t");
    assertEquals("", values[0], "name 为 null 时应为空字符串");
    assertEquals("100", values[1], "indexical 应为 100");
    assertEquals("test-value", values[2], "value 应为 test-value");
    assertEquals("", values[3], "createTime 为 null 时应为空字符串");
    assertEquals("", values[4], "size 为 null 时应为空字符串");
  }

  @Test
  @DisplayName("测试通过 DataFileGenerator 生成文件")
  void testDataFileGenerator() throws IOException {
    // 准备测试数据
    List<Property> data = createMockData();

    // 使用 DataFileGenerator 生成 TXT 文件
    DataFileGenerator.by(Property.class)
      .with(data)
      .as(FileFormat.TXT)
      .to(OUTPUT_FILE);

    // 验证文件已创建
    File outputFile = new File(OUTPUT_FILE);
    assertTrue(outputFile.exists(), "TXT 文件应已创建");

    // 读取文件内容并验证
    String txtContent = Files.readString(outputFile.toPath());
    assertTrue(txtContent.contains("属性名称"), "文件应包含标题行（属性名称）");
    assertTrue(txtContent.contains("索引"), "文件应包含标题行（索引）");
    assertTrue(txtContent.contains("__系统配置"), "文件应包含带前缀的数据值");
    assertTrue(txtContent.contains("\t"), "文件应使用制表符分隔");
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
      txtWriter.write(definition, data, baos);
      fail("应抛出异常");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("不能为空"),
        "空数据应抛出包含 '不能为空' 的异常");
    }
  }

  /**
   * 创建模拟数据
   * <p>
   * 注意：name 字段不带前缀，配置中的 prefix: "__" 会自动添加
   */
  private List<Property> createMockData() {
    Property p1 = new Property();
    p1.setName("系统配置");
    p1.setValue("debug=true");
    p1.setIndexical(1);
    p1.setCreateTime(new Date());
    p1.setSize(1024.5);

    Property p2 = new Property();
    p2.setName("数据库连接");
    p2.setValue("jdbc:mysql://localhost:3306/test");
    p2.setIndexical(2);
    p2.setCreateTime(new Date(System.currentTimeMillis() - 86400000));
    p2.setSize(2048.75);

    Property p3 = new Property();
    p3.setName("日志级别");
    p3.setValue("INFO");
    p3.setIndexical(3);
    p3.setCreateTime(new Date(System.currentTimeMillis() - 172800000));
    p3.setSize(512.0);

    return Arrays.asList(p1, p2, p3);
  }
}