package cn.labzen.file.format.excel;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Excel 文件写入器测试
 *
 * @author labzen
 */
@DisplayName("Excel 文件写入器测试")
class ExcelFileWriterTest {

  private static final String OUTPUT_FILE = "property-test.xlsx";

  @BeforeEach
  void setUp() {
    FormatTestHelper.setup();
  }

  @AfterEach
  void tearDown() {
    FormatTestHelper.tearDown();
  }

  @Test
  @DisplayName("通过 DataFileGenerator 生成 Excel 文件")
  void testDataFileGenerator() throws IOException {
    List<Property> data = MockData.create();

    File outputFile = FormatTestHelper.withFile(OUTPUT_FILE);
    DataFileExporter.by(Property.class)
      .with(data)
      .as(FileFormat.EXCEL)
      .to(outputFile);

    assertTrue(outputFile.exists(), "Excel 文件应已创建");
    assertTrue(outputFile.length() > 0, "Excel 文件不应为空");
  }
}
