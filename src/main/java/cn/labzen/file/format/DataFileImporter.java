package cn.labzen.file.format;

import cn.labzen.file.definition.DefinitionRegistry;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.exception.DataReadException;
import cn.labzen.file.exception.DataWriteException;
import cn.labzen.file.format.core.reader.DataFileReader;
import cn.labzen.file.format.core.reader.process.ImportResult;
import cn.labzen.file.locale.FileResourceBundleLoader;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * 数据文件导入器
 * <p>
 * usage:
 * <pre>
 * ImportResult&lt;Property&gt; result = DataFileImporter.by(Property.class)
 *                                                 .as(FileFormat.EXCEL)
 *                                                 .locale("zh-CN")
 *                                                 .from(file);
 * </pre>
 *
 * @param <T> Bean类型
 * @author labzen
 */
@Slf4j
public final class DataFileImporter<T> {

  private static final Map<FileFormat, DataFileReader> READER_INSTANCES = new EnumMap<>(FileFormat.class);

  static {
    ServiceLoader.load(DataFileReader.class).forEach(reader -> {
      READER_INSTANCES.put(reader.format(), reader);
    });
  }

  private final Class<T> type;
  private final String name;
  private FileFormat format;
  private Locale locale;

  private DataFileImporter(Class<T> type) {
    this.type = type;
    this.name = type.getSimpleName();
    this.locale = FileResourceBundleLoader.DEFAULT_LOCALE;
  }

  /**
   * 创建导入器
   *
   * @param type Bean类型
   * @return 导入器实例
   */
  public static <T> DataFileImporter<T> by(Class<T> type) {
    return new DataFileImporter<>(type);
  }

  /**
   * 设置文件格式
   */
  public DataFileImporter<T> as(FileFormat format) {
    this.format = format;
    return this;
  }

  /**
   * 设置语言标签
   */
  public DataFileImporter<T> locale(String language) {
    this.locale = FileResourceBundleLoader.forLanguage(language);
    return this;
  }

  /**
   * 设置语言标签
   */
  public DataFileImporter<T> locale(Locale locale) {
    this.locale = locale;
    return this;
  }

  /**
   * 从文件导入
   */
  public ImportResult<T> from(File file) {
    try (InputStream is = new FileInputStream(file)) {
      return from(is);
    } catch (Exception e) {
      throw new DataReadException(e, "读取导入文件失败: {}", file.getAbsolutePath());
    }
  }

  /**
   * 从输入流导入
   */
  public ImportResult<T> from(InputStream inputStream) {
    DataDefinition definition = DefinitionRegistry.get(name, locale)
      .orElseThrow(() -> new DataWriteException("不支持的数据类型导出定义，请确认文件 {}.yml 确实存在并有效", name));
    if (!Objects.equals(type, definition.getDomainClass())) {
      throw new DataWriteException("数据定义文件 {}.yml 的 domain - [{}] 与导出数据类型 [{}] 不一致", name, definition.getDomain(), type.getName());
    }

    // 读取文件，获取行迭代器
    return getReader().read(definition, inputStream);
  }

  /**
   * 从文件路径导入
   */
  public ImportResult<T> from(String filePath) {
    return from(new File(filePath));
  }

  private DataFileReader getReader() {
    if (format == null) {
      throw new DataReadException("未设置导入文件格式，请调用 as() 方法");
    }

    DataFileReader reader = READER_INSTANCES.get(format);
    if (reader == null) {
      throw new DataReadException("不支持的导入文件格式: {}", format);
    }

    return reader;
  }
}
