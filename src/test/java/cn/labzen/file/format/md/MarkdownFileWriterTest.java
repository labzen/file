package cn.labzen.file.format.md;

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
 * Markdown 文件写入器测试
 *
 * @author labzen
 */
@DisplayName("Markdown 文件写入器测试")
class MarkdownFileWriterTest {

  private static final String OUTPUT_DIR = System.getProperty("user.dir") + "/.testing";
  private static final String OUTPUT_FILE = OUTPUT_DIR + "/property-test.md";

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
  @DisplayName("通过 DataFileGenerator 生成 Markdown 文件")
  void testDataFileGenerator() throws IOException {
    var data = MockData.createMockData();

//    DataFileGenerator.by(Property.class)
//      .with(data)
//      .as(FileFormat.MARKDOWN)
//      .to(OUTPUT_FILE);
    File outputFile = DataFileGenerator.by(Property.class)
      .with(data)
      .as(FileFormat.MARKDOWN)
      .folder(OUTPUT_DIR)
      .to();

//    File outputFile = new File(OUTPUT_FILE);
    assertTrue(outputFile.exists(), "Markdown 文件应已创建");

    String mdContent = Files.readString(outputFile.toPath());
    assertTrue(mdContent.startsWith("# 系统属性"), "文件应以大标题开始");
    assertTrue(mdContent.contains("| 属性名称 |"), "文件应包含表头");
    assertTrue(mdContent.contains("| ---"), "文件应包含表格分隔行");
    assertTrue(mdContent.contains("【系统配置】"), "文件应包含带前缀的数据值");
  }
}
