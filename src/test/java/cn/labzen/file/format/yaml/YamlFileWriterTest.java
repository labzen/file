package cn.labzen.file.format.yaml;

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
 * YAML 文件写入器测试
 *
 * @author labzen
 */
@DisplayName("YAML 文件写入器测试")
class YamlFileWriterTest {

  private static final String OUTPUT_DIR = System.getProperty("user.dir") + "/.testing";
  private static final String OUTPUT_FILE = OUTPUT_DIR + "/property-test.yaml";

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
  @DisplayName("通过 DataFileGenerator 生成 YAML 文件")
  void testDataFileGenerator() throws IOException {
    var data = MockData.createMockData();

    DataFileExporter.by(Property.class)
      .with(data)
      .as(FileFormat.YAML)
      .to(OUTPUT_FILE);

    File outputFile = new File(OUTPUT_FILE);
    assertTrue(outputFile.exists(), "YAML 文件应已创建");

    String yamlContent = Files.readString(outputFile.toPath());
    assertTrue(yamlContent.trim().startsWith("-"), "文件内容应以数组元素开始");
    assertTrue(yamlContent.contains("name:"), "文件应包含 'name' 字段");
    assertTrue(yamlContent.contains("indexical:"), "文件应包含 'indexical' 字段");
  }
}
