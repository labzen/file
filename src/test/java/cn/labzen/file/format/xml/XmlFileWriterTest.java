package cn.labzen.file.format.xml;

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
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * XML 文件写入器测试
 *
 * @author labzen
 */
@DisplayName("XML 文件写入器测试")
class XmlFileWriterTest {

  private static final String OUTPUT_FILE = "property-test.xml";

  @BeforeEach
  void setUp() {
    FormatTestHelper.setup();
  }

  @AfterEach
  void tearDown() {
    FormatTestHelper.tearDown();
  }

  @Test
  @DisplayName("通过 DataFileGenerator 生成 XML 文件")
  void testDataFileGenerator() throws IOException {
    var data = MockData.create();


    File outputFile =
      DataFileExporter.by(Property.class)
        .with(data)
        .as(FileFormat.XML)
        .locale(Locale.SIMPLIFIED_CHINESE)
        .folder(FormatTestHelper.outputFolder())
        .name()
        .to();
    assertTrue(outputFile.exists(), "XML 文件应已创建");

    String xmlContent = Files.readString(outputFile.toPath());
    assertTrue(xmlContent.contains("<property title=\""), "文件内容应以 property 标签开始并包含 title 属性");
    assertTrue(xmlContent.contains("</property>"), "文件内容应以 property 标签结束");
    assertTrue(xmlContent.contains("<record>"), "文件应包含 record 标签");
    assertTrue(xmlContent.contains("</record>"), "文件应包含 record 结束标签");
  }
}
