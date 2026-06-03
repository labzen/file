package cn.labzen.file.format;

import cn.labzen.file.definition.DefinitionRegistry;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.exception.DataReadException;
import cn.labzen.file.exception.DataWriteException;
import cn.labzen.file.format.excel.ExcelTemplateGenerator;
import cn.labzen.file.i18n.I18nStoreHolder;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * 数据导入模板生成器 — Fluent API 入口
 * <p>
 * 使用方式：
 * <pre>
 * DataTemplateGenerator.by(Property.class)
 *     .locale("zh-CN")
 *     .to(outputStream);
 * </pre>
 * <p>
 * 模板仅支持 Excel 格式。
 *
 * @param <T> Bean类型
 * @author labzen
 */
@Slf4j
public final class DataTemplateGenerator<T> {

  //  private final Class<T> type;
//  private final DataDefinition definition;
  private final String name;
  private String locale;

  private DataTemplateGenerator(Class<T> type) {
    this.name = type.getSimpleName();
    if (!DefinitionRegistry.contains(name)) {
      throw new DataReadException("未找到类[{}]的数据定义", name);
    }

    this.locale = I18nStoreHolder.defaultLocale();
//    this.type = type;
//    this.definition = DefinitionRegistry.get(type.getSimpleName())
//      .orElseThrow(() -> new DataReadException("未找到类[{}]的数据定义", type.getSimpleName()));
  }

  /**
   * 创建模板生成器
   */
  public static <T> DataTemplateGenerator<T> by(Class<T> type) {
    return new DataTemplateGenerator<>(type);
  }

  /**
   * 设置语言标签
   */
  public DataTemplateGenerator<T> locale(String locale) {
    this.locale = locale;
    return this;
  }

  private String name() {
    String originalFilename = DefinitionRegistry.getDefinitionFilename(name).orElse("unknown");
    return originalFilename + "_template" + FileFormat.EXCEL.getExtension();
  }


  /**
   * 生成模板到输出流
   */
  public void to(OutputStream outputStream) {
    DefinitionRegistry.get(name, locale).ifPresent(definition ->
        new ExcelTemplateGenerator(definition, locale).generate(outputStream)
//      ExcelTemplateGenerator.generate(definition, locale, outputStream)
    );
  }

  /**
   * 生成模板到文件
   */
  public void to(File file) {
    try (OutputStream os = new FileOutputStream(file)) {
      to(os);
    } catch (Exception e) {
      throw new DataWriteException(e, "生成模板文件失败: {}", file.getAbsolutePath());
    }
  }

  /**
   * 生成模板到文件路径
   */
  public void to(String filePath) {
    to(new File(filePath));
  }
}
