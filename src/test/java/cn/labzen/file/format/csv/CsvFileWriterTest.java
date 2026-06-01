package cn.labzen.file.format.csv;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CSV 文件写入器测试
 *
 * @author labzen
 */
@DisplayName("CSV 文件写入器测试")
class CsvFileWriterTest {

  private static final String OUTPUT_FILE = "property-test.csv";

  @BeforeEach
  void setUp() {
    FormatTestHelper.setup();
  }

  @AfterEach
  void tearDown() {
    FormatTestHelper.tearDown();
  }

  @Test
  @DisplayName("通过 DataFileGenerator 生成 CSV 文件")
  void testDataFileGenerator() throws IOException {
    List<Property> data = MockData.create();

    File outputFile = DataFileExporter.by(Property.class)
      .with(data)
      .as(FileFormat.CSV)
      .folder(FormatTestHelper.outputFolder())
      .name()
      .to();

    assertTrue(outputFile.exists(), "CSV 文件应已创建");

    List<String> lines = Files.readAllLines(outputFile.toPath());
    assertTrue(lines.getFirst().contains("属性名称"), "表头应包含属性名称");
    assertEquals(11, lines.size(), "应包含1行表头和11行数据");
  }
}
