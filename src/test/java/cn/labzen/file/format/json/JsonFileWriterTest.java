package cn.labzen.file.format.json;

import cn.labzen.file.bean.Property;
import cn.labzen.file.definition.DefinitionLoader;
import cn.labzen.file.definition.DefinitionRegistry;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.format.DataFileGenerator;
import cn.labzen.file.format.MockData;
import cn.labzen.meta.LabzenMetaInitializer;
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
 * JSON 文件写入器测试
 * <p>
 * 测试 JSON 文件生成功能，包括：
 * <ul>
 *   <li>配置加载和 JSON 生成</li>
 *   <li>空值处理</li>
 *   <li>数值格式化</li>
 *   <li>JSON 数组输出格式</li>
 *   <li>属性键使用字段名而非表头</li>
 * </ul>
 *
 * @author labzen
 */
@DisplayName("JSON 文件写入器测试")
class JsonFileWriterTest {

  private static final String OUTPUT_DIR = System.getProperty("user.dir") + "/.testing";
  private static final String OUTPUT_FILE = OUTPUT_DIR + "/property-test.json";

  private JsonFileWriter<Property> jsonWriter;

  @BeforeEach
  void setUp() {
    new LabzenMetaInitializer().initialize(null);
    // 清理之前的注册数据
    DefinitionRegistry.clear();

    // 创建配置加载器
    DefinitionLoader loader = new DefinitionLoader(
      "classpath*:data-export/**/*.yml",
      "classpath*:data-export/__global__.yml"
    );
    loader.load();

    // 创建 JSON 写入器
    jsonWriter = new JsonFileWriter<>();

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

    // 清理测试生成的 JSON 文件
    File outputFile = new File(OUTPUT_FILE);
    if (outputFile.exists()) {
      // outputFile.delete();
    }
  }

  @Test
  @DisplayName("测试基本 JSON 文件生成")
  void testBasicJsonGeneration() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 从 Registry 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 JSON 到 ByteArrayOutputStream
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    jsonWriter.write(definition, data, baos);
    byte[] jsonBytes = baos.toByteArray();
    String jsonContent = new String(jsonBytes, StandardCharsets.UTF_8);

    // 验证 JSON 格式
    assertNotNull(jsonContent);
    assertTrue(jsonContent.trim().startsWith("["), "JSON 应以数组开始");
    assertTrue(jsonContent.trim().endsWith("]"), "JSON 应以数组结束");

    // 验证包含3条数据
    int objectCount = jsonContent.split("\\{").length - 1;
    assertEquals(4, objectCount, "应包含4个 JSON 对象");
  }

  @Test
  @DisplayName("测试 JSON 对象属性键为字段名")
  void testJsonFieldNames() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 JSON 文件
    jsonWriter.write(definition, data, OUTPUT_FILE);

    // 读取文件内容
    String jsonContent = Files.readString(Paths.get(OUTPUT_FILE));

    // 验证属性键使用字段名而非表头
    // columns 的 key 是 name, index, value, createTime, size
    assertTrue(jsonContent.contains("\"name\""), "JSON 属性键应为 'name' 而非 '属性名称'");
    assertTrue(jsonContent.contains("indexical"), "JSON 属性键应为 'indexical' 而非 '索引'");
    assertTrue(jsonContent.contains("\"value\""), "JSON 属性键应为 'value' 而非 '属性值'");
    assertTrue(jsonContent.contains("\"createTime\""), "JSON 属性键应为 'createTime' 而非 '创建时间'");
    assertTrue(jsonContent.contains("\"size\""), "JSON 属性键应为 'size' 而非 '大小'");

    // 验证不包含表头信息
    assertFalse(jsonContent.contains("属性名称"), "JSON 不应包含表头信息：属性名称");
    assertFalse(jsonContent.contains("索引"), "JSON 不应包含表头信息：索引");
  }

  @Test
  @DisplayName("测试空值处理 - 输出 JSON null")
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

    // 生成 JSON 文件
    jsonWriter.write(definition, data, OUTPUT_FILE);

    // 读取文件内容
    String jsonContent = Files.readString(Paths.get(OUTPUT_FILE));

    // 验证空值处理 - null 值应使用 when-null 配置的值（prefix 会被添加）
    assertTrue(jsonContent.contains("\"name\":\"__未命名\""),
      "name 为 null 时应使用 when-null 的值 '__未命名'");
    assertTrue(jsonContent.contains("\"createTime\":null"),
      "createTime 为 null 时应输出为 null");
    assertTrue(jsonContent.contains("\"size\":\"0KG\""),
      "size 为 null 时应使用 when-null 的值 '0KG'");
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

    // 生成 JSON
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    jsonWriter.write(definition, data, baos);
    String jsonContent = new String(baos.toByteArray(), StandardCharsets.UTF_8);

    // 验证数值输出
    assertTrue(jsonContent.contains("\"indexical\":42"), "整数应正确输出");
    assertTrue(jsonContent.contains("\"size\":1024.567"), "浮点数应正确输出");
  }

  @Test
  @DisplayName("测试 JSON 格式缩进")
  void testJsonIndentation() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 JSON
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    jsonWriter.write(definition, data, baos);
    String jsonContent = new String(baos.toByteArray(), StandardCharsets.UTF_8);

    // 验证 JSON 格式化输出（缩进）
    assertTrue(jsonContent.contains("\n"), "JSON 应包含换行符");
    assertTrue(jsonContent.contains("  "), "JSON 应包含缩进空格");
  }

  @Test
  @DisplayName("测试通过 DataFileGenerator 生成文件")
  void testDataFileGenerator() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 使用 DataFileGenerator 生成 JSON 文件
    DataFileGenerator.by(Property.class)
      .with(data)
      .as(FileFormat.JSON)
      .to(OUTPUT_FILE);

    // 验证文件已创建
    File outputFile = new File(OUTPUT_FILE);
    assertTrue(outputFile.exists(), "JSON 文件应已创建");

    // 读取文件内容并验证
    String jsonContent = Files.readString(outputFile.toPath());
    assertTrue(jsonContent.startsWith("["), "文件内容应以数组开始");
    assertTrue(jsonContent.endsWith("]"), "文件内容应以数组结束");
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
      jsonWriter.write(definition, data, baos);
      assert false : "应抛出异常";
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("不能为空"),
        "空数据应抛出包含 '不能为空' 的异常");
    }
  }
}