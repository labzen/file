package cn.labzen.file.format.txt;

import cn.labzen.file.bean.Property;
import cn.labzen.file.definition.DefinitionLoader;
import cn.labzen.file.definition.DefinitionRegistry;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.format.DataFileExporter;
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
 * TXT 文件写入器测试
 *
 * @author labzen
 */
@DisplayName("TXT 文件写入器测试")
class TxtFileWriterTest {

  private static final String OUTPUT_DIR = System.getProperty("user.dir") + "/.testing";
  private static final String OUTPUT_FILE = OUTPUT_DIR + "/property-test.txt";

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
  @DisplayName("通过 DataFileGenerator 生成 TXT 文件")
  void testDataFileGenerator() throws IOException {
    var data = MockData.createMockData();

    DataFileExporter.by(Property.class)
      .with(data)
      .as(FileFormat.TXT)
      .to(OUTPUT_FILE);

    File outputFile = new File(OUTPUT_FILE);
    assertTrue(outputFile.exists(), "TXT 文件应已创建");

    String txtContent = Files.readString(outputFile.toPath());
    assertTrue(txtContent.contains("属性名称"), "文件应包含标题行（属性名称）");
    assertTrue(txtContent.contains("索引"), "文件应包含标题行（索引）");
    assertTrue(txtContent.contains("【系统配置】"), "文件应包含带前缀的数据值");
    assertTrue(txtContent.contains("    "), "文件应使用制表符分隔");
  }
}
