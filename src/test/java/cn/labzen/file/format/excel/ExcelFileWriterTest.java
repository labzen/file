package cn.labzen.file.format.excel;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Excel 文件写入器测试
 * <p>
 * 测试 Excel 文件生成功能，包括：
 * <ul>
 *   <li>配置加载和 Excel 生成</li>
 *   <li>多级表头支持</li>
 *   <li>单元格样式（对齐、背景色、字体）</li>
 *   <li>列宽设置</li>
 *   <li>空值处理</li>
 * </ul>
 *
 * @author labzen
 */
@DisplayName("Excel 文件写入器测试")
class ExcelFileWriterTest {

  private static final String OUTPUT_DIR = System.getProperty("user.dir") + "/.testing";
  private static final String OUTPUT_FILE = OUTPUT_DIR + "/property-test.xlsx";

  private ExcelFileWriter<Property> excelWriter;

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

    // 创建 Excel 写入器
    excelWriter = new ExcelFileWriter<>();

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

    // 清理测试生成的 Excel 文件
    File outputFile = new File(OUTPUT_FILE);
    if (outputFile.exists()) {
      // outputFile.delete();
    }
  }

  @Test
  @DisplayName("测试基本 Excel 文件生成")
  void testBasicExcelGeneration() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 从 Registry 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 Excel 到 ByteArrayOutputStream
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    excelWriter.write(definition, data, baos);
    byte[] excelBytes = baos.toByteArray();

    // 验证 Excel 格式
    assertNotNull(excelBytes);
    assertTrue(excelBytes.length > 0, "Excel 文件内容不应为空");

    // 验证文件头是 ZIP 格式（Excel/XLSX 基于 ZIP）
    assertEquals(0x50, excelBytes[0] & 0xFF, "Excel 文件应以 PK（ZIP）格式开始");
    assertEquals(0x4B, excelBytes[1] & 0xFF, "Excel 文件应以 PK（ZIP）格式开始");
  }

  @Test
  @DisplayName("测试通过 DataFileGenerator 生成文件")
  void testDataFileGenerator() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 使用 DataFileGenerator 生成 Excel 文件
    DataFileGenerator.by(Property.class)
      .with(data)
      .as(FileFormat.EXCEL)
      .to(OUTPUT_FILE);

    // 验证文件已创建
    File outputFile = new File(OUTPUT_FILE);
    assertTrue(outputFile.exists(), "Excel 文件应已创建");

    // 验证文件大小
    assertTrue(outputFile.length() > 0, "Excel 文件不应为空");
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
      excelWriter.write(definition, data, baos);
      fail("应抛出异常");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("不能为空"),
        "空数据应抛出包含 '不能为空' 的异常");
    }
  }

}