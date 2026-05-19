package cn.labzen.file.format.pdf;

import cn.labzen.file.bean.Property;
import cn.labzen.file.definition.DefinitionLoader;
import cn.labzen.file.definition.DefinitionRegistry;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.format.DataFileGenerator;
import cn.labzen.file.format.MockData;
import cn.labzen.meta.LabzenMetaInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PDF 文件写入器测试
 *
 * @author labzen
 */
@DisplayName("PDF 文件写入器测试")
class PdfFileWriterTest {

  private static final String OUTPUT_DIR = System.getProperty("user.dir") + "/.testing";
  private static final String OUTPUT_FILE = OUTPUT_DIR + "/property-test.pdf";

  @BeforeEach
  void setUp() {
    new LabzenMetaInitializer().initialize(null);
    DefinitionRegistry.clear();

    DefinitionLoader loader = new DefinitionLoader(
      "classpath*:data-export/**/*.yml",
      "classpath*:data-export/__global__.yml"
    );
    loader.load();

    File outputDir = new File(OUTPUT_DIR);
    if (!outputDir.exists()) {
      outputDir.mkdirs();
    }

    File oldFile = new File(OUTPUT_FILE);
    if (oldFile.exists()) {
      oldFile.delete();
    }
  }

  @AfterEach
  void tearDown() {
    DefinitionRegistry.clear();
  }

  @Test
  @DisplayName("通过 DataFileGenerator 生成 PDF 文件")
  void testDataFileGenerator() throws IOException {
    var data = MockData.createMockData();

    DataFileGenerator.by(Property.class)
      .with(data)
      .as(FileFormat.PDF)
      .to(OUTPUT_FILE);

    File outputFile = new File(OUTPUT_FILE);
    assertTrue(outputFile.exists(), "PDF 文件应已创建");
    assertTrue(outputFile.length() > 0, "PDF 文件不应为空");

    byte[] fileBytes = Files.readAllBytes(outputFile.toPath());
    String header = new String(fileBytes, 0, Math.min(8, fileBytes.length));
    assertTrue(header.startsWith("%PDF-"), "PDF 文件应以 %PDF- 开头");
  }
}
