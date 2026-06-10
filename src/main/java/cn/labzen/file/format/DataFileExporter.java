package cn.labzen.file.format;

import cn.labzen.file.definition.DefinitionRegistry;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.exception.DataReadException;
import cn.labzen.file.exception.DataWriteException;
import cn.labzen.file.format.core.writer.DataFileWriter;
import cn.labzen.file.locale.FileResourceBundleLoader;
import cn.labzen.file.meta.FileConfiguration;
import cn.labzen.file.util.DateTimeFormat;
import cn.labzen.meta.Labzens;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 数据文件生成器
 * <p>
 * usage:
 * <pre>
 * DataFileExporter.by(Property.class)
 *                 .with(properties)
 *                 .as(FileFormat.PDF)
 *                 .to(file);
 * </pre>
 *
 * @param <T> 数据对象类型
 * @author labzen
 */
public final class DataFileExporter<T> {

  /**
   * 文件格式到导出器构造器的映射
   */
  private static final Map<FileFormat, DataFileWriter<?>> WRITER_INSTANCES = new EnumMap<>(FileFormat.class);
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.get("yyyyMMddHHmmss");

//  private static final String DEFAULT_LOCALE;

  static {
    FileConfiguration configuration = Labzens.configurationWith(FileConfiguration.class);
    // 通过 SPI 机制加载所有 DataFileWriter 实现
    ServiceLoader.load(DataFileWriter.class).forEach(writer -> {
      writer.initialize(configuration);
      WRITER_INSTANCES.put(writer.format(), writer);
    });
  }

  private final Class<T> type;
  private final String name;
  private List<T> data;
  private FileFormat format;
  private String filename;
  private File file;
  private Locale locale;

  private DataFileExporter(Class<T> type) {
    this.type = type;
    this.name = type.getSimpleName();
    this.locale = FileResourceBundleLoader.DEFAULT_LOCALE;
  }

  /**
   * 创建一个数据文件生成器
   *
   * @param type 数据对象类型
   */
  public static <T> DataFileExporter<T> by(Class<T> type) {
    return new DataFileExporter<>(type);
  }

  /**
   * 设置数据集合
   */
  public DataFileExporter<T> with(List<T> data) {
    this.data = data;
    return this;
  }

  /**
   * 设置文件格式
   */
  public DataFileExporter<T> as(FileFormat format) {
    this.format = format;
    return this;
  }

  /**
   * 设置导出时的语言标签，用于国际化 ${key} 占位符的解析
   *
   * @param language 语言标签，如 zh-CN、en-US
   */
  public DataFileExporter<T> locale(String language) {
    this.locale = FileResourceBundleLoader.forLanguage(language);
    return this;
  }

  /**
   * 设置导出时的语言标签，用于国际化 ${key} 占位符的解析
   *
   * @param locale 地区
   */
  public DataFileExporter<T> locale(Locale locale) {
    this.locale = locale;
    return this;
  }

  /**
   * 设置文件名，默认格式 {定义的文件名}_{年月日时分秒}.{文件格式后缀}，例如 test_20250601120000.xlsx
   */
  public DataFileExporter<T> name() {
    String originalFilename = DefinitionRegistry.getDefinitionFilename(name).orElse("unknown");
    this.filename = originalFilename + "_" + DATE_TIME_FORMATTER.format(LocalDateTime.now()) + "." + format.getExtension();

    if (file != null) {
      // 重新设置文件路径
      folder(file.getParentFile());
    }
    return this;
  }

  /**
   * 设置文件夹路径，生成的文件将存储到这里，文件名默认由{@link #name()}方法指定
   */
  public DataFileExporter<T> folder(String path) {
    if (filename == null) {
      name();
    }

    this.file = new File(path, filename);
    return this;
  }

  /**
   * 设置文件夹路径，生成的文件将存储到这里，文件名默认由{@link #name()}方法指定
   */
  public DataFileExporter<T> folder(File folder) {
    if (filename == null) {
      name();
    }

    this.file = new File(folder, filename);
    return this;
  }

  /**
   * 生成数据文件到指定文件路径
   * <p>
   * 该方法会忽略{@link #name()}和{@link #folder(File)}/{@link #folder(String)}方法（假如在此之前调用过）
   *
   * @param filePath 文件路径
   */
  public void to(String filePath) {
    to(new File(filePath));
  }

  /**
   * 将数据到指定输出流中
   * <p>
   * 该方法会忽略{@link #name()}和{@link #folder(File)}/{@link #folder(String)}方法（假如在此之前调用过）
   *
   * @param outputStream 输出流
   */
  public void to(OutputStream outputStream) {
    DataDefinition definition = DefinitionRegistry.get(name, locale)
      .orElseThrow(() -> new DataWriteException("不支持的数据类型导出定义，请确认文件 {}.yml 确实存在并有效", name));
    if (!Objects.equals(type, definition.getDomainClass())) {
      throw new DataWriteException("数据定义文件 {}.yml 的 domain - [{}] 与导出数据类型 [{}] 不一致", name, definition.getDomain(), type.getName());
    }

    getWriter().write(definition, data, outputStream);
  }

  /**
   * 生成数据文件
   * <p>
   * 该方法会忽略{@link #name()}和{@link #folder(File)}/{@link #folder(String)}方法（假如在此之前调用过）
   *
   * @param file 文件
   */
  public void to(File file) {
    try (OutputStream os = new FileOutputStream(file)) {
      to(os);
    } catch (Exception e) {
      throw new DataReadException(e, "导出数据文件失败: {}", file.getAbsolutePath());
    }
  }

  /**
   * 开始生成文件，文件输出位置由{@link #name()}和{@link #folder(File)}/{@link #folder(String)}方法指定。假如没有调用过，则会失败
   */
  public File to() {
    if (file == null) {
      throw new IllegalStateException("文件输出位置未指定，请先调用 name() 和 folder() 方法指定文件输出位置");
    }

    to(file);
    return file;
  }

  /**
   * 根据文件格式获取对应的导出器
   *
   * @return 对应的导出器实例
   * @throws IllegalArgumentException 如果格式不支持
   */
  private DataFileWriter<T> getWriter() {
    if (format == null) {
      throw new DataWriteException("未设置导出文件格式，请调用 as() 方法");
    }

    DataFileWriter<?> writer = WRITER_INSTANCES.get(format);
    if (writer == null) {
      throw new DataWriteException("不支持的导出文件格式: {}", format);
    }
    //noinspection unchecked
    return (DataFileWriter<T>) writer;
  }

}
