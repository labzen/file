package cn.labzen.file.format;

import cn.labzen.file.definition.DefinitionRegistry;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.meta.FileConfiguration;
import cn.labzen.meta.Labzens;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * 数据文件生成器
 * <p>
 * usage:
 * <pre>
 * DataFileGenerator.by(Property.class)
 *                  .with(properties)
 *                  .as(FileFormat.PDF)
 *                  .to(file);
 * </pre>
 *
 * @param <T> 数据对象类型
 */
public final class DataFileGenerator<T> {

  /**
   * 文件格式到写入器构造器的映射
   */
  private static final Map<FileFormat, DataFileWriter<?>> WRITER_INSTANCES = new EnumMap<>(FileFormat.class);

  static {
    FileConfiguration configuration = Labzens.configurationWith(FileConfiguration.class);
    // 通过 SPI 机制加载所有 DataFileWriter 实现
    ServiceLoader.load(DataFileWriter.class).forEach(writer -> {
      writer.initialize(configuration);
      WRITER_INSTANCES.put(writer.format(), writer);
    });
  }

  private final DataDefinition definition;
  private List<T> data;
  private FileFormat format;

  private DataFileGenerator(Class<T> type) {
    String name = type.getSimpleName();
    this.definition = DefinitionRegistry.get(name)
      .orElseThrow(() -> new IllegalArgumentException("不支持的数据类型导出定义，请确认文件 [" + type + ".yml] 确实存在并有效"));
  }

  /**
   * 创建一个数据文件生成器
   *
   * @param type 数据对象类型
   */
  public static <T> DataFileGenerator<T> by(Class<T> type) {
    return new DataFileGenerator<>(type);
  }

  /**
   * 设置数据集合
   */
  public DataFileGenerator<T> with(List<T> data) {
    this.data = data;
    return this;
  }

  /**
   * 设置文件格式
   */
  public DataFileGenerator<T> as(FileFormat format) {
    this.format = format;
    return this;
  }

  /**
   * 生成数据文件到指定文件路径
   *
   * @param filePath 文件路径
   */
  public void to(String filePath) {
    getWriter(format).write(definition, data, filePath);
  }

  /**
   * 将数据到指定输出流中
   *
   * @param outputStream 输出流
   */
  public void to(OutputStream outputStream) {
    getWriter(format).write(definition, data, outputStream);
  }

  /**
   * 生成数据文件
   *
   * @param file 文件
   */
  public void to(File file) {
    getWriter(format).write(definition, data, file);
  }

  /**
   * 根据文件格式获取对应的写入器
   *
   * @param format 文件格式枚举
   * @return 对应的写入器实例
   * @throws IllegalArgumentException 如果格式不支持
   */
  private DataFileWriter<T> getWriter(@Nonnull FileFormat format) {
    DataFileWriter<?> writer = WRITER_INSTANCES.get(format);
    if (writer == null) {
      throw new IllegalArgumentException("不支持的文件格式: " + format);
    }
    //noinspection unchecked
    return (DataFileWriter<T>) writer;
  }

}
