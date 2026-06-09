package cn.labzen.file.format.yaml;

import cn.labzen.file.bean.Property;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.format.DataFileExporter;
import cn.labzen.file.format.FormatTestHelper;
import cn.labzen.file.format.MockData;
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

  private static final String OUTPUT_FILE = "property-test.yaml";

  @BeforeEach
  void setUp() {
    FormatTestHelper.setup();
  }

  @AfterEach
  void tearDown() {
    FormatTestHelper.tearDown();
  }

  @Test
  @DisplayName("通过 DataFileGenerator 生成 YAML 文件")
  void testDataFileGenerator() throws IOException {
    var data = MockData.create();

    File outputFile = FormatTestHelper.withFile(OUTPUT_FILE);
    DataFileExporter.by(Property.class)
      .with(data)
      .as(FileFormat.YAML)
      .to(outputFile);

    assertTrue(outputFile.exists(), "YAML 文件应已创建");

    String yamlContent = Files.readString(outputFile.toPath());
    assertTrue(yamlContent.trim().startsWith("-"), "文件内容应以数组元素开始");
    assertTrue(yamlContent.contains("name:"), "文件应包含 'name' 字段");
    assertTrue(yamlContent.contains("indexical:"), "文件应包含 'indexical' 字段");
  }
}
