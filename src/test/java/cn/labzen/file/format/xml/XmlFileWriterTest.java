package cn.labzen.file.format.xml;

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
 * XML 文件写入器测试
 * <p>
 * 测试 XML 文件生成功能，包括：
 * <ul>
 *   <li>配置加载和 XML 生成</li>
 *   <li>空值处理</li>
 *   <li>数值格式化</li>
 *   <li>XML 数组输出格式</li>
 *   <li>属性键使用字段名而非表头</li>
 * </ul>
 *
 * @author labzen
 */
@DisplayName("XML 文件写入器测试")
class XmlFileWriterTest {

  private static final String OUTPUT_DIR = System.getProperty("user.dir") + "/.testing";
  private static final String OUTPUT_FILE = OUTPUT_DIR + "/property-test.xml";

  private XmlFileWriter<Property> xmlWriter;

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

    // 创建 XML 写入器
    xmlWriter = new XmlFileWriter<>();

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

    // 清理测试生成的 XML 文件
    File outputFile = new File(OUTPUT_FILE);
    if (outputFile.exists()) {
      // outputFile.delete();
    }
  }

  @Test
  @DisplayName("测试基本 XML 文件生成")
  void testBasicXmlGeneration() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 从 Registry 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 XML 到 ByteArrayOutputStream
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    xmlWriter.write(definition, data, baos);
    byte[] xmlBytes = baos.toByteArray();
    String xmlContent = new String(xmlBytes, StandardCharsets.UTF_8);

    // 验证 XML 格式 - 根节点为 filename（property），带有 title 属性
    assertNotNull(xmlContent);
    assertTrue(xmlContent.contains("<property title=\""), "XML 应以 property 标签开始并包含 title 属性");
    assertTrue(xmlContent.contains("</property>"), "XML 应以 property 标签结束");

    // 验证包含3条数据（3个 record 标签）
    int recordCount = xmlContent.split("<record>").length - 1;
    assertEquals(4, recordCount, "应包含4个 record 节点");
  }

  @Test
  @DisplayName("测试 XML 对象属性键为字段名")
  void testXmlFieldNames() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 XML 文件
    xmlWriter.write(definition, data, OUTPUT_FILE);

    // 读取文件内容
    String xmlContent = Files.readString(Paths.get(OUTPUT_FILE));

    // 验证属性键使用字段名而非表头
    // columns 的 key 是 name, indexical, value, createTime, size
    assertTrue(xmlContent.contains("<name>"), "XML 属性键应为 'name' 而非 '属性名称'");
    assertTrue(xmlContent.contains("<indexical>"), "XML 属性键应为 'indexical' 而非 '索引'");
    assertTrue(xmlContent.contains("<value>"), "XML 属性键应为 'value' 而非 '属性值'");
    assertTrue(xmlContent.contains("<createTime>"), "XML 属性键应为 'createTime' 而非 '创建时间'");
    assertTrue(xmlContent.contains("<size>"), "XML 属性键应为 'size' 而非 '大小'");

    // 验证不包含表头信息
    assertFalse(xmlContent.contains("属性名称"), "XML 不应包含表头信息：属性名称");
    assertFalse(xmlContent.contains("索引"), "XML 不应包含表头信息：索引");
  }

  @Test
  @DisplayName("测试空值处理 - 不输出空节点内容")
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

    // 生成 XML 文件
    xmlWriter.write(definition, data, OUTPUT_FILE);

    // 读取文件内容
    String xmlContent = Files.readString(Paths.get(OUTPUT_FILE));

    // 验证空值处理 - null 值的标签应该有开始和结束但中间为空
    assertTrue(xmlContent.contains("<name></name>"), "null 值应输出为 <name></name>");
    assertTrue(xmlContent.contains("<createTime></createTime>"), "null 值应输出为 <createTime></createTime>");
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

    // 生成 XML
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    xmlWriter.write(definition, data, baos);
    String xmlContent = new String(baos.toByteArray(), StandardCharsets.UTF_8);

    // 验证数值输出
    assertTrue(xmlContent.contains("<indexical>42</indexical>"), "整数应正确输出");
    assertTrue(xmlContent.contains("<size>1024.567</size>"), "浮点数应正确输出");
  }

  @Test
  @DisplayName("测试 XML 格式缩进")
  void testXmlIndentation() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 XML
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    xmlWriter.write(definition, data, baos);
    String xmlContent = new String(baos.toByteArray(), StandardCharsets.UTF_8);

    // 验证 XML 格式化输出（缩进）
    assertTrue(xmlContent.contains("\n"), "XML 应包含换行符");
    assertTrue(xmlContent.contains("  "), "XML 应包含缩进空格");
  }

  @Test
  @DisplayName("测试通过 DataFileGenerator 生成文件")
  void testDataFileGenerator() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 使用 DataFileGenerator 生成 XML 文件
    DataFileGenerator.by(Property.class)
      .with(data)
      .as(FileFormat.XML)
      .to(OUTPUT_FILE);

    // 验证文件已创建
    File outputFile = new File(OUTPUT_FILE);
    assertTrue(outputFile.exists(), "XML 文件应已创建");

    // 读取文件内容并验证
    String xmlContent = Files.readString(outputFile.toPath());
    // 验证根节点为 filename（property），带有 title 属性
    assertTrue(xmlContent.contains("<property title=\""), "文件内容应以 property 标签开始并包含 title 属性");
    assertTrue(xmlContent.contains("</property>"), "文件内容应以 property 标签结束");
    // 验证包含 record 标签
    assertTrue(xmlContent.contains("<record>"), "文件应包含 record 标签");
    assertTrue(xmlContent.contains("</record>"), "文件应包含 record 结束标签");
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
      xmlWriter.write(definition, data, baos);
      assert false : "应抛出异常";
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("不能为空"),
        "空数据应抛出包含 '不能为空' 的异常");
    }
  }

}