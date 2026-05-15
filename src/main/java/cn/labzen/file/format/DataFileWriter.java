package cn.labzen.file.format;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.meta.FileConfiguration;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.OutputStream;
import java.util.List;

/**
 * 数据文件写入器接口
 * <p>
 * 定义文件生成的核心接口，提供统一的文件写入操作入口。
 * 支持多种文件格式的生成，通过 {@link FileFormat} 枚举进行格式区分。
 *
 * @param <T> 数据对象类型
 * @author labzen
 */
public interface DataFileWriter<T> {

  /**
   * 获取写入器对应的文件格式
   *
   * @return 文件格式枚举
   */
  @Nonnull
  FileFormat format();

  /**
   * 初始化写入器
   *
   * @param configuration 文件配置
   */
  void initialize(@Nonnull FileConfiguration configuration);

  /**
   * 写入数据到指定文件路径
   *
   * @param definition 数据定义配置
   * @param data       数据集合
   * @param filePath   输出文件路径
   */
  void write(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull String filePath);

  /**
   * 写入数据到指定输出流
   * <p>
   * 此方法主要用于支持 Web 下载等场景，不关闭输出流
   *
   * @param definition   数据定义配置
   * @param data         数据集合
   * @param outputStream 输出流
   */
  void write(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull OutputStream outputStream);

  /**
   * 写入数据到指定文件
   *
   * @param definition 数据定义配置
   * @param data       数据集合
   * @param file       输出文件
   */
  void write(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull File file);
}
