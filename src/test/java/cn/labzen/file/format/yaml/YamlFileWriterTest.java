package cn.labzen.file.format.yaml;

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
 * YAML 文件写入器测试
 * <p>
 * 测试 YAML 文件生成功能，包括：
 * <ul>
 *   <li>配置加载和 YAML 生成</li>
 *   <li>空值处理</li>
 *   <li>数值格式化</li>
 *   <li>YAML 数组输出格式</li>
 *   <li>属性键使用字段名而非表头</li>
 * </ul>
 *
 * @author labzen
 */
@DisplayName("YAML 文件写入器测试")
class YamlFileWriterTest {

  private static final String OUTPUT_DIR = System.getProperty("user.dir") + "/.testing";
  private static final String OUTPUT_FILE = OUTPUT_DIR + "/property-test.yaml";

  private YamlFileWriter<Property> yamlWriter;

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

    // 创建 YAML 写入器
    yamlWriter = new YamlFileWriter<>();

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

    // 清理测试生成的 YAML 文件
    File outputFile = new File(OUTPUT_FILE);
    if (outputFile.exists()) {
      // outputFile.delete();
    }
  }

  @Test
  @DisplayName("测试基本 YAML 文件生成")
  void testBasicYamlGeneration() throws IOException {
    // 准备测试数据
    List<Property> data = createMockData();

    // 从 Registry 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 YAML 到 ByteArrayOutputStream
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    yamlWriter.write(definition, data, baos);
    byte[] yamlBytes = baos.toByteArray();
    String yamlContent = new String(yamlBytes, StandardCharsets.UTF_8);

    // 验证 YAML 格式
    assertNotNull(yamlContent);
    assertTrue(yamlContent.trim().startsWith("-"), "YAML 应以数组元素开始");

    // 验证包含3条数据（3个 - 开头的对象）
    int objectCount = yamlContent.split("\n-").length - 1;
    assertEquals(3, objectCount, "应包含3个 YAML 对象");
  }

  @Test
  @DisplayName("测试 YAML 对象属性键为字段名")
  void testYamlFieldNames() throws IOException {
    // 准备测试数据
    List<Property> data = createMockData();

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 YAML 文件
    yamlWriter.write(definition, data, OUTPUT_FILE);

    // 读取文件内容
    String yamlContent = Files.readString(Paths.get(OUTPUT_FILE));

    // 验证属性键使用字段名而非表头
    // columns 的 key 是 name, index, value, createTime, size
    assertTrue(yamlContent.contains("name:"), "YAML 属性键应为 'name' 而非 '属性名称'");
    assertTrue(yamlContent.contains("index:"), "YAML 属性键应为 'index' 而非 '索引'");
    assertTrue(yamlContent.contains("value:"), "YAML 属性键应为 'value' 而非 '属性值'");
    assertTrue(yamlContent.contains("createTime:"), "YAML 属性键应为 'createTime' 而非 '创建时间'");
    assertTrue(yamlContent.contains("size:"), "YAML 属性键应为 'size' 而非 '大小'");

    // 验证不包含表头信息
    assertFalse(yamlContent.contains("属性名称"), "YAML 不应包含表头信息：属性名称");
    assertFalse(yamlContent.contains("索引"), "YAML 不应包含表头信息：索引");
  }

  @Test
  @DisplayName("测试空值处理 - 输出 YAML null")
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

    // 生成 YAML 文件
    yamlWriter.write(definition, data, OUTPUT_FILE);

    // 读取文件内容
    String yamlContent = Files.readString(Paths.get(OUTPUT_FILE));

    // 验证空值处理 - null 值应被跳过（使用 SkipNullRepresenter）
    assertFalse(yamlContent.contains("name: null"), "null 值应被跳过");
    assertFalse(yamlContent.contains("createTime: null"), "null 值应被跳过");
    assertFalse(yamlContent.contains("size: null"), "null 值应被跳过");
  }

  @Test
  @DisplayName("测试数值类型")
  void testNumberTypes() throws IOException {
    // 准备包含不同数值类型的数据
    Property property = new Property();
    property.setName("number-test");
    property.setValue("123");
    property.setIndexical(42);
    property.setCreateTime(new Date());
    property.setSize(1024.567);

    List<Property> data = Arrays.asList(property);

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 YAML
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    yamlWriter.write(definition, data, baos);
    String yamlContent = new String(baos.toByteArray(), StandardCharsets.UTF_8);

    // 验证数值输出
    assertTrue(yamlContent.contains("index: 42"), "整数应正确输出");
    assertTrue(yamlContent.contains("size: 1024.567"), "浮点数应正确输出");
  }

  @Test
  @DisplayName("测试通过 DataFileGenerator 生成文件")
  void testDataFileGenerator() throws IOException {
    // 准备测试数据
    List<Property> data = createMockData();

    // 使用 DataFileGenerator 生成 YAML 文件
    DataFileGenerator.by(Property.class)
      .with(data)
      .as(FileFormat.YAML)
      .to(OUTPUT_FILE);

    // 验证文件已创建
    File outputFile = new File(OUTPUT_FILE);
    assertTrue(outputFile.exists(), "YAML 文件应已创建");

    // 读取文件内容并验证
    String yamlContent = Files.readString(outputFile.toPath());
    assertTrue(yamlContent.trim().startsWith("-"), "文件内容应以数组元素开始");
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
      yamlWriter.write(definition, data, baos);
      assert false : "应抛出异常";
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("不能为空"),
        "空数据应抛出包含 '不能为空' 的异常");
    }
  }

  /**
   * 创建模拟数据
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