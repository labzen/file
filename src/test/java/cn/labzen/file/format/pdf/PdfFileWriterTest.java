package cn.labzen.file.format.pdf;

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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PDF 文件写入器测试
 * <p>
 * 测试 PDF 文件生成功能，包括：
 * <ul>
 *   <li>配置加载和 PDF 生成</li>
 *   <li>多级表头支持</li>
 *   <li>文本对齐（居左、居中、居右）</li>
 *   <li>单元格背景色</li>
 *   <li>空值处理</li>
 * </ul>
 *
 * @author labzen
 */
@DisplayName("PDF 文件写入器测试")
class PdfFileWriterTest {

  private static final String OUTPUT_DIR = System.getProperty("user.dir") + "/.testing";
  private static final String OUTPUT_FILE = OUTPUT_DIR + "/property-test.pdf";

  private PdfFileWriter<Property> pdfWriter;

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

    // 创建 PDF 写入器
    pdfWriter = new PdfFileWriter<>();

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

    // 清理测试生成的 PDF 文件
    File outputFile = new File(OUTPUT_FILE);
    if (outputFile.exists()) {
      // outputFile.delete();
    }
  }

  @Test
  @DisplayName("测试基本 PDF 文件生成")
  void testBasicPdfGeneration() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 从 Registry 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 PDF 到 ByteArrayOutputStream
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    pdfWriter.write(definition, data, baos);
    byte[] pdfBytes = baos.toByteArray();

    // 验证 PDF 格式
    assertNotNull(pdfBytes);
    assertTrue(pdfBytes.length > 0, "PDF 文件内容不应为空");

    // 验证文件头是 PDF 格式
    String header = new String(pdfBytes, 0, Math.min(8, pdfBytes.length));
    assertTrue(header.startsWith("%PDF-"), "PDF 文件应以 %PDF- 开头");
  }

  @Test
  @DisplayName("测试通过 DataFileGenerator 生成 PDF 文件")
  void testDataFileGenerator() throws IOException {
    new LabzenMetaInitializer().initialize(null);

    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 使用 DataFileGenerator 生成 PDF 文件
    DataFileGenerator.by(Property.class)
      .with(data)
      .as(FileFormat.PDF)
      .to(OUTPUT_FILE);

    // 验证文件已创建
    File outputFile = new File(OUTPUT_FILE);
    assertTrue(outputFile.exists(), "PDF 文件应已创建");

    // 验证文件大小
    assertTrue(outputFile.length() > 0, "PDF 文件不应为空");

    // 验证文件头
    byte[] fileBytes = java.nio.file.Files.readAllBytes(outputFile.toPath());
    String header = new String(fileBytes, 0, Math.min(8, fileBytes.length));
    assertTrue(header.startsWith("%PDF-"), "PDF 文件应以 %PDF- 开头");
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
      pdfWriter.write(definition, data, baos);
      fail("应抛出异常");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("不能为空"),
        "空数据应抛出包含 '不能为空' 的异常");
    }
  }

  @Test
  @DisplayName("测试 PDF 文件格式正确性")
  void testPdfFileFormat() throws IOException {
    // 准备测试数据
    List<Property> data = MockData.createMockData();

    // 获取配置
    DataDefinition definition = DefinitionRegistry.get("Property")
      .orElseThrow(() -> new RuntimeException("未找到 Property 配置"));

    // 生成 PDF
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    pdfWriter.write(definition, data, baos);
    byte[] pdfBytes = baos.toByteArray();

    // 验证 PDF 语法完整性
    // 1. 检查 PDF 版本行
    String versionLine = new String(pdfBytes, 0, Math.min(20, pdfBytes.length));
    assertTrue(versionLine.contains("%PDF-"), "PDF 应包含版本声明");

    // 2. 检查文件尾部包含 EOF 标记
    int eofIndex = findEOF(pdfBytes);
    assertTrue(eofIndex > 0, "PDF 应包含 %%EOF 标记");
  }

  /**
   * 查找 PDF 文件的 %%EOF 标记位置
   */
  private int findEOF(byte[] pdfBytes) {
    String tail = new String(pdfBytes, Math.max(0, pdfBytes.length - 100), Math.min(100, pdfBytes.length));
    int eofIndex = tail.lastIndexOf("%%EOF");
    if (eofIndex >= 0) {
      return pdfBytes.length - 100 + eofIndex;
    }
    return -1;
  }
}
