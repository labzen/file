package cn.labzen.file.format.core.reader;

import cn.labzen.file.definition.DefinitionRegistry;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.exception.DataReadException;
import cn.labzen.file.format.excel.ExcelFileReader;
import cn.labzen.file.format.json.JsonFileReader;
import cn.labzen.file.format.xml.XmlFileReader;
import cn.labzen.file.format.yaml.YamlFileReader;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * 数据文件导入器 — Fluent API 入口
 * <p>
 * 使用方式：
 * <pre>
 * ImportResult&lt;Property&gt; result = DataFileImporter.by(Property.class)
 *     .as(FileFormat.EXCEL)
 *     .locale("zh-CN")
 *     .from(new File("import.xlsx"));
 * </pre>
 *
 * @param <T> Bean类型
 * @author labzen
 */
@Slf4j
public final class DataFileImporter<T> {

  private static final Map<FileFormat, DataFileReader> READER_INSTANCES;

  static {
    READER_INSTANCES = new java.util.HashMap<>();
    registerReader(new ExcelFileReader());
    registerReader(new JsonFileReader());
    registerReader(new XmlFileReader());
    registerReader(new YamlFileReader());
  }

  private final Class<T> type;
  private final DataDefinition definition;
  private FileFormat format;
  private String locale;

  private DataFileImporter(Class<T> type) {
    this.type = type;
    this.definition = DefinitionRegistry.get(type.getSimpleName())
      .orElseThrow(() -> new DataReadException("未找到类[{}]的数据定义", type.getSimpleName()));
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
  public DataFileImporter<T> locale(String locale) {
    this.locale = locale;
    return this;
  }

  /**
   * 从文件导入
   */
  public ImportResult<T> from(File file) {
    try (InputStream is = new FileInputStream(file)) {
      return doImport(is);
    } catch (Exception e) {
      if (e instanceof DataReadException dre) throw dre;
      throw new DataReadException(e, "读取导入文件失败: {}", file.getAbsolutePath());
    }
  }

  /**
   * 从输入流导入
   */
  public ImportResult<T> from(InputStream inputStream) {
    return doImport(inputStream);
  }

  /**
   * 从文件路径导入
   */
  public ImportResult<T> from(String filePath) {
    return from(new File(filePath));
  }

  // ── 内部方法 ──

  private ImportResult<T> doImport(InputStream inputStream) {
    if (format == null) {
      throw new DataReadException("未设置导入文件格式，请调用 as() 方法");
    }

    DataFileReader reader = READER_INSTANCES.get(format);
    if (reader == null) {
      throw new DataReadException("不支持的导入文件格式: {}", format);
    }

    // 读取文件，获取行迭代器
    Iterator<Map<String, String>> rowIterator = reader.read(inputStream, definition);

    // 创建导入管线并逐行处理
    ImportPipeline<T> pipeline = new ImportPipeline<>(definition, type, locale);
    int rowIndex = 1;
    while (rowIterator.hasNext()) {
      Map<String, String> rowData = rowIterator.next();
      pipeline.processRow(rowIndex, rowData);
      rowIndex++;
    }

    // 执行延后校验
    pipeline.executeDeferredValidation();

    return pipeline.buildResult();
  }

  private static void registerReader(DataFileReader reader) {
    READER_INSTANCES.put(reader.format(), reader);
  }
}
