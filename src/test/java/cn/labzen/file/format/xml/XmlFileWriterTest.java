package cn.labzen.file.format.xml;

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
 * XML 文件写入器测试
 *
 * @author labzen
 */
@DisplayName("XML 文件写入器测试")
class XmlFileWriterTest {

  private static final String OUTPUT_DIR = System.getProperty("user.dir") + "/.testing";
  private static final String OUTPUT_FILE = OUTPUT_DIR + "/property-test.xml";

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
  @DisplayName("通过 DataFileGenerator 生成 XML 文件")
  void testDataFileGenerator() throws IOException {
    var data = MockData.createMockData();

    DataFileGenerator.by(Property.class)
      .with(data)
      .as(FileFormat.XML)
      .to(OUTPUT_FILE);

    File outputFile = new File(OUTPUT_FILE);
    assertTrue(outputFile.exists(), "XML 文件应已创建");

    String xmlContent = Files.readString(outputFile.toPath());
    assertTrue(xmlContent.contains("<property title=\""), "文件内容应以 property 标签开始并包含 title 属性");
    assertTrue(xmlContent.contains("</property>"), "文件内容应以 property 标签结束");
    assertTrue(xmlContent.contains("<record>"), "文件应包含 record 标签");
    assertTrue(xmlContent.contains("</record>"), "文件应包含 record 结束标签");
  }
}
