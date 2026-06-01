package cn.labzen.file.format.json;

import cn.labzen.file.bean.Property;
import cn.labzen.file.definition.DefinitionLoader;
import cn.labzen.file.definition.DefinitionRegistry;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.format.DataFileExporter;
import cn.labzen.file.format.FormatTestHelper;
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
 * JSON 文件写入器测试
 *
 * @author labzen
 */
@DisplayName("JSON 文件写入器测试")
class JsonFileWriterTest {

  private static final String OUTPUT_FILE = "property-test.json";

  @BeforeEach
  void setUp() {
    FormatTestHelper.setup();
  }

  @AfterEach
  void tearDown() {
    FormatTestHelper.tearDown();
  }

  @Test
  @DisplayName("通过 DataFileGenerator 生成 JSON 文件")
  void testDataFileGenerator() throws IOException {
    var data = MockData.create();

    File outputFile = FormatTestHelper.withFile(OUTPUT_FILE);
    DataFileExporter.by(Property.class)
      .with(data)
      .as(FileFormat.JSON)
      .locale("en-US")
      .to(outputFile);

    assertTrue(outputFile.exists(), "JSON 文件应已创建");

    String jsonContent = Files.readString(outputFile.toPath());
    assertTrue(jsonContent.startsWith("["), "文件内容应以数组开始");
    assertTrue(jsonContent.endsWith("]"), "文件内容应以数组结束");
    assertTrue(jsonContent.contains("\"name\""), "文件应包含 'name' 字段");
    assertTrue(jsonContent.contains("\"indexical\""), "文件应包含 'indexical' 字段");
  }
}
