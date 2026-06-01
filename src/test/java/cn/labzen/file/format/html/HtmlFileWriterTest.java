package cn.labzen.file.format.html;

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

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * HTML 文件写入器测试
 *
 * @author labzen
 */
@DisplayName("HTML 文件写入器测试")
class HtmlFileWriterTest {

  private static final String OUTPUT_FILE = "property-test.html";

  @BeforeEach
  void setUp() {
    FormatTestHelper.setup();
  }

  @AfterEach
  void tearDown() {
    FormatTestHelper.tearDown();
  }

  @Test
  @DisplayName("通过 DataFileGenerator 生成 HTML 文件")
  void testDataFileGenerator() throws IOException {
    List<Property> data = MockData.create();
    File outputFile = FormatTestHelper.withFile(OUTPUT_FILE);
    DataFileExporter.by(Property.class)
      .with(data)
      .as(FileFormat.HTML)
      .locale("en-US")
      .to(outputFile);

    assertTrue(outputFile.exists(), "HTML 文件应已创建");

    String htmlContent = Files.readString(outputFile.toPath());
    assertTrue(htmlContent.startsWith("<!DOCTYPE html>"), "文件应以 DOCTYPE 开始");
//    assertTrue(htmlContent.contains(">系统属性</h1>"), "文件应包含标题");
    assertTrue(htmlContent.contains("<table"), "文件应包含表格");
//    assertTrue(htmlContent.contains(">基本信息</th>"), "文件应包含 '标识' 表头");
//    assertTrue(htmlContent.contains(">索引</th>"), "文件应包含 '索引' 表头");
//    assertTrue(htmlContent.contains(">属性值</th>"), "文件应包含 '属性值' 表头");
    assertTrue(htmlContent.contains("rowspan="), "文件应包含 rowspan 属性");
  }
}
