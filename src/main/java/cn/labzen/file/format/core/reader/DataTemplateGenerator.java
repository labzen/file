package cn.labzen.file.format.core.reader;

import cn.labzen.file.definition.DefinitionRegistry;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.exception.DataReadException;
import cn.labzen.file.format.excel.ExcelTemplateGenerator;
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
  private final DataDefinition definition;
  private String locale;

  private DataTemplateGenerator(Class<T> type) {
//    this.type = type;
    this.definition = DefinitionRegistry.get(type.getSimpleName())
      .orElseThrow(() -> new DataReadException("未找到类[{}]的数据定义", type.getSimpleName()));
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

  /**
   * 生成模板到输出流
   */
  public void to(OutputStream outputStream) {
    ExcelTemplateGenerator.generate(definition, locale, outputStream);
  }

  /**
   * 生成模板到文件
   */
  public void to(File file) {
    try (OutputStream os = new FileOutputStream(file)) {
      to(os);
    } catch (Exception e) {
      throw new DataReadException(e, "生成模板文件失败: {}", file.getAbsolutePath());
    }
  }

  /**
   * 生成模板到文件路径
   */
  public void to(String filePath) {
    to(new File(filePath));
  }
}
