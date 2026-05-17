package cn.labzen.file.format.csv;

import cn.labzen.file.bean.Property;
import cn.labzen.file.definition.DefinitionLoader;
import cn.labzen.file.definition.DefinitionRegistry;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.format.DataFileGenerator;
import cn.labzen.file.format.DataFileWriter;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CSV 文件写入器测试
 * <p>
 * 测试 CSV 文件生成功能，包括：
 * <ul>
 *   <li>配置加载和 CSV 生成</li>
 *   <li>多级表头解析</li>
 *   <li>日期和数值格式化</li>
 *   <li>空值处理</li>
 *   <li>CSV 转义字符处理</li>
 * </ul>
 *
 * @author labzen
 */
@DisplayName("CSV 文件写入器测试")
class CsvFileWriterTest {

  private static final String OUTPUT_DIR = System.getProperty("user.dir") + "/.testing";
  private static final String OUTPUT_FILE = OUTPUT_DIR + "/property-test.csv";

  private DataFileWriter<Property> csvWriter;

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

    // 创建 CSV 写入器
    csvWriter = new CsvFileWriter<>();

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

    // 清理测试生成的 CSV 文件
    File outputFile = new File(OUTPUT_FILE);
    if (outputFile.exists()) {
//      outputFile.delete();
    }
  }

  @Test
  @DisplayName("测试基本 CSV 文件生成")
  void testBasicCsvGeneration() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 从 Registry 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 CSV 文件到 ByteArrayOutputStream
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    csvWriter.write(definition, data, baos);
    byte[] csvBytes = baos.toByteArray();
    String csvContent = new String(csvBytes, StandardCharsets.UTF_8);

    // 解析 CSV 内容进行验证
    String[] lines = csvContent.split("\n");
    assertEquals(5, lines.length, "应包含1行表头和4行数据");

    // 验证表头行 - 取多级表头的最后一级
    String headerRow = lines[0];
    assertTrue(headerRow.contains("属性名称"), "表头应包含属性名称");
    assertTrue(headerRow.contains("索引"), "表头应包含索引");
    assertTrue(headerRow.contains("属性值"), "表头应包含属性值");
    assertTrue(headerRow.contains("创建时间"), "表头应包含创建时间");
    assertTrue(headerRow.contains("大小"), "表头应包含大小");

    // 验证第一行数据
    String firstDataRow = lines[1];
    assertTrue(firstDataRow.contains("系统配置"), "第一行 name 字段值应正确");
    assertTrue(firstDataRow.contains("debug=true"), "第一行 value 字段值应正确");
    assertTrue(firstDataRow.contains("1"), "第一行 index 字段值应正确");
  }

  @Test
  @DisplayName("测试日期格式化")
  void testDateFormatting() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 CSV 文件
    csvWriter.write(definition, data, OUTPUT_FILE);

    // 读取文件内容
    List<String> lines = Files.readAllLines(Paths.get(OUTPUT_FILE));

    // 验证日期格式
    String firstDataRow = lines.get(1);
    // createTime 字段应使用 yyyy-MM-dd HH:mm:ss 格式
    assertTrue(firstDataRow.matches(".*\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.*"),
      "日期应按配置的格式 yyyy-MM-dd HH:mm:ss 输出");
  }

  @Test
  @DisplayName("测试空值处理")
  void testNullValueHandling() throws IOException {
    // 准备包含 null 值的数据
    Property propertyWithNull = new Property();
    propertyWithNull.setName(null); // 当 null 时应使用 when-null 的值
    propertyWithNull.setValue("test-value");
    propertyWithNull.setIndexical(100);
    propertyWithNull.setCreateTime(null);
    propertyWithNull.setSize(null);

    List<Property> data = Arrays.asList(propertyWithNull);

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 CSV 文件
    csvWriter.write(definition, data, OUTPUT_FILE);

    // 读取文件内容
    List<String> lines = Files.readAllLines(Paths.get(OUTPUT_FILE));
    String dataRow = lines.get(1);

    // 验证空值处理 - null 值应使用 when-null 配置的值
    assertTrue(dataRow.contains("__未命名"), "name 为 null 时应使用 when-null 的值 '__未命名'");
    assertTrue(dataRow.contains("0KG"), "size 为 null 时应使用 when-null 的值 '0KG'");
  }

  @Test
  @DisplayName("测试 CSV 转义字符处理")
  void testCsvEscaping() throws IOException {
    // 准备包含特殊字符的数据
    Property propertyWithSpecialChars = new Property();
    propertyWithSpecialChars.setName("配置\"名称");
    propertyWithSpecialChars.setValue("value,with,commas");
    propertyWithSpecialChars.setIndexical(200);
    propertyWithSpecialChars.setCreateTime(new Date());
    propertyWithSpecialChars.setSize(123.45);

    List<Property> data = Arrays.asList(propertyWithSpecialChars);

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 CSV 文件
    csvWriter.write(definition, data, OUTPUT_FILE);

    // 读取文件内容
    List<String> lines = Files.readAllLines(Paths.get(OUTPUT_FILE));
    String dataRow = lines.get(1);

    // 验证转义 - 包含逗号的字段应该被引号包裹
    assertTrue(dataRow.contains("\"value,with,commas\""), "包含逗号的字段应被引号包裹");
    // 验证引号转义
    assertTrue(dataRow.contains("\"__配置\"\"名称\""), "包含引号的字段应进行转义（带 prefix）");
  }

  @Test
  @DisplayName("测试文件编码 - UTF-8 BOM")
  void testUtf8Bom() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 CSV 文件
    csvWriter.write(definition, data, OUTPUT_FILE);

    // 读取文件的前3个字节（UTF-8 BOM）
    byte[] bom = new byte[3];
    try (java.io.FileInputStream fis = new java.io.FileInputStream(OUTPUT_FILE)) {
      fis.read(bom);
    }

    // 验证 UTF-8 BOM (EF BB BF)
    assertEquals((byte) 0xEF, bom[0], "UTF-8 BOM 第一字节应为 0xEF");
    assertEquals((byte) 0xBB, bom[1], "UTF-8 BOM 第二字节应为 0xBB");
    assertEquals((byte) 0xBF, bom[2], "UTF-8 BOM 第三字节应为 0xBF");
  }

  @Test
  @DisplayName("测试通过 DataFileGenerator 生成文件")
  void testDataFileGenerator() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 使用 DataFileGenerator 生成 CSV 文件
    DataFileGenerator.by(Property.class)
      .with(data)
      .as(FileFormat.CSV)
      .to(OUTPUT_FILE);

    // 验证文件已创建
    File outputFile = new File(OUTPUT_FILE);
    assertTrue(outputFile.exists(), "CSV 文件应已创建");


    // 读取文件内容并验证
    List<String> lines = Files.readAllLines(outputFile.toPath());
    assertTrue(lines.get(0).contains("属性名称"), "表头应包含属性名称");
    assertEquals(5, lines.size(), "应包含1行表头和4行数据");
  }

}
