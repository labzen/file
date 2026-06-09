package cn.labzen.file.format.txt;

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
 * TXT 文件写入器测试
 *
 * @author labzen
 */
@DisplayName("TXT 文件写入器测试")
class TxtFileWriterTest {

  private static final String OUTPUT_FILE = "property-test.txt";

  @BeforeEach
  void setUp() {
    FormatTestHelper.setup();
  }

  @AfterEach
  void tearDown() {
    FormatTestHelper.tearDown();
  }

  @Test
  @DisplayName("通过 DataFileGenerator 生成 TXT 文件")
  void testDataFileGenerator() throws IOException {
    var data = MockData.create();

    File outputFile = FormatTestHelper.withFile(OUTPUT_FILE);
    DataFileExporter.by(Property.class)
      .with(data)
      .as(FileFormat.TXT)
      .to(outputFile);

    assertTrue(outputFile.exists(), "TXT 文件应已创建");

    String txtContent = Files.readString(outputFile.toPath());
    assertTrue(txtContent.contains("属性名称"), "文件应包含标题行（属性名称）");
    assertTrue(txtContent.contains("索引"), "文件应包含标题行（索引）");
//    assertTrue(txtContent.contains("【系统配置】"), "文件应包含带前缀的数据值");
    assertTrue(txtContent.contains("    "), "文件应使用制表符分隔");
  }
}
