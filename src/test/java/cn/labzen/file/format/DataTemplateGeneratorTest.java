package cn.labzen.file.format;

import cn.labzen.file.bean.Property;
import cn.labzen.file.exception.DataReadException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataTemplateGenerator 单元测试
 *
 * @author labzen
 */
@DisplayName("数据导入模板生成器测试")
class DataTemplateGeneratorTest {

  @BeforeEach
  void setUp() {
    FormatTestHelper.setup();
  }

  @AfterEach
  void tearDown() {
    FormatTestHelper.tearDown();
  }

  // ── by() 构造测试 ──

  @Test
  @DisplayName("by() 正常创建生成器 - 已注册的Bean类型")
  void by_registeredType_shouldCreateGenerator() {
    assertDoesNotThrow(() -> DataTemplateGenerator.by(Property.class));
  }

  @Test
  @DisplayName("by() 未注册的Bean类型应抛出DataReadException")
  void by_unregisteredType_shouldThrowDataReadException() {
    class UnknownBean {
    }
    DataReadException ex = assertThrows(DataReadException.class,
      () -> DataTemplateGenerator.by(UnknownBean.class));
    assertTrue(ex.getMessage().contains("UnknownBean"),
      "异常消息应包含未注册的类名");
  }

  // ── locale() 测试 ──

  @Test
  @DisplayName("locale() 设置语言标签应返回同一实例（Fluent API）")
  void locale_shouldReturnSameInstance() {
    DataTemplateGenerator<Property> gen = DataTemplateGenerator.by(Property.class);
    DataTemplateGenerator<Property> returned = gen.locale("en-US");
    assertSame(gen, returned, "locale() 应返回同一实例以支持链式调用");
  }

  // ── to(OutputStream) 测试 ──

  @Test
  @DisplayName("to(OutputStream) 默认locale应生成非空Excel内容")
  void toOutputStream_defaultLocale_shouldGenerateContent() {
    DataTemplateGenerator<Property> gen = DataTemplateGenerator.by(Property.class);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    assertDoesNotThrow(() -> gen.to(baos));
    byte[] bytes = baos.toByteArray();
    assertTrue(bytes.length > 0, "生成的Excel模板不应为空");
    // Excel xlsx文件以PK(ZIP)头开始
    assertEquals(0x50, bytes[0] & 0xFF, "应为有效的xlsx文件（ZIP头）");
  }

  @Test
  @DisplayName("to(OutputStream) 指定zh-CN locale应生成内容")
  void toOutputStream_chineseLocale_shouldGenerateContent() {
    DataTemplateGenerator<Property> gen = DataTemplateGenerator.by(Property.class)
      .locale("zh-CN");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    assertDoesNotThrow(() -> gen.to(baos));
    assertTrue(baos.toByteArray().length > 0);
  }

  @Test
  @DisplayName("to(OutputStream) 指定en-US locale应生成内容")
  void toOutputStream_englishLocale_shouldGenerateContent() {
    DataTemplateGenerator<Property> gen = DataTemplateGenerator.by(Property.class)
      .locale("en-US");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    assertDoesNotThrow(() -> gen.to(baos));
    assertTrue(baos.toByteArray().length > 0);
  }

  // ── to(File) 测试 ──

  @Test
  @DisplayName("to(File) 应生成有效的Excel文件")
  void toFile_shouldGenerateExcelFile() throws IOException {
    File outputFile = FormatTestHelper.withFile("template-test-file.xlsx");
    DataTemplateGenerator.by(Property.class).to(outputFile);

    assertTrue(outputFile.exists(), "Excel文件应已创建");
    assertTrue(outputFile.length() > 0, "Excel文件不应为空");
  }

  // ── to(String) 测试 ──

  @Test
  @DisplayName("to(String) 路径应生成有效的Excel文件")
  void toFilePath_shouldGenerateExcelFile() {
    String filePath = FormatTestHelper.outputFolder() + "/template-test-path.xlsx";
    File file = new File(filePath);
    if (file.exists()) file.delete();

    DataTemplateGenerator<Property> gen = DataTemplateGenerator.by(Property.class);
    gen.to(filePath);

    assertTrue(file.exists(), "通过路径生成的Excel文件应存在");
    assertTrue(file.length() > 0, "文件不应为空");
  }

  // ── 链式调用测试 ──

  @Test
  @DisplayName("完整Fluent链式调用: by().locale().to(OutputStream)")
  void fluentChain_shouldWork() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    assertDoesNotThrow(() ->
      DataTemplateGenerator.by(Property.class)
        .locale("zh-CN")
        .to(baos)
    );
    assertTrue(baos.toByteArray().length > 0);
  }

  // ── Registry无匹配locale时 ──

  @Test
  @DisplayName("to(OutputStream) 不存在的locale不应抛异常（定义存在但locale无匹配）")
  void toOutputStream_nonExistentLocale_shouldNotThrow() {
    DataTemplateGenerator<Property> gen = DataTemplateGenerator.by(Property.class)
      .locale("fr-FR");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    // locale不存在时，DefinitionRegistry.get(name, locale) 仍基于original定义生成
    assertDoesNotThrow(() -> gen.to(baos));
  }

  // ── to(File) 文件写入失败 ──

  @Test
  @DisplayName("to(File) 无效路径应抛出DataReadException")
  void toFile_invalidPath_shouldThrowDataReadException() {
    DataTemplateGenerator<Property> gen = DataTemplateGenerator.by(Property.class);
    File invalidFile = new File("/nonexistent/deep/path/template.xlsx");

    DataReadException ex = assertThrows(DataReadException.class,
      () -> gen.to(invalidFile));
    assertTrue(ex.getMessage().contains("生成模板文件失败"),
      "异常消息应提示生成模板文件失败");
  }
}
