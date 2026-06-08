package cn.labzen.file.format.excel;

import cn.labzen.file.bean.Property;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.format.DataFileImporter;
import cn.labzen.file.format.FormatTestHelper;
import cn.labzen.file.format.core.reader.process.ImportResult;
import org.junit.jupiter.api.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Excel 文件导入器测试")
public class ExcelFileImportTest {

  @BeforeEach
  void setUp() {
    FormatTestHelper.setup();
  }

  @AfterEach
  void tearDown() {
    FormatTestHelper.tearDown();
  }

  @Test
  @DisplayName("通过 DataFileImporter 导入 Excel 文件")
  void testDataFileImporter() {
    ImportResult<Property> result = DataFileImporter.by(Property.class)
      .as(FileFormat.EXCEL)
      .locale("zh-CN")
      .from(new File("C:\\Working\\labzen\\file\\.testing\\property-import-test.xlsx"));

    assertNotNull(result);
  }
}
