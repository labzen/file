package cn.labzen.file.format;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.meta.FileConfiguration;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

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

//  /**
//   * 写入数据到指定文件路径
//   *
//   * @param definition 数据定义配置
//   * @param data       数据集合
//   * @param filePath   输出文件路径
//   */
//  void write(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull String filePath);
//
//  /**
//   * 写入数据到指定输出流
//   * <p>
//   * 此方法主要用于支持 Web 下载等场景，不关闭输出流
//   *
//   * @param definition   数据定义配置
//   * @param data         数据集合
//   * @param outputStream 输出流
//   */
//  void write(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull OutputStream outputStream);
//
//  /**
//   * 写入数据到指定文件
//   *
//   * @param definition 数据定义配置
//   * @param data       数据集合
//   * @param file       输出文件
//   */
//  void write(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull File file);

  /**
   * 写入数据到指定输出流，支持国际化
   * <p>
   * 指定 locale 后，会先将 DataDefinition 中的 ${key} 占位符解析为对应语言的文本，再执行导出。
   *
   * @param definition   数据定义配置（模板，含 ${key} 占位符）
   * @param data         数据集合
   * @param outputStream 输出流
   * @param locale       语言标签，如 zh-CN、en-US；为 null 时不做国际化解析
   */
  void write(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull OutputStream outputStream, @Nullable String locale);

  /**
   * 写入数据到指定文件，支持国际化
   *
   * @param definition 数据定义配置
   * @param data       数据集合
   * @param file       输出文件
   * @param locale     语言标签；为 null 时不做国际化解析
   */
  void write(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull File file, @Nullable String locale);

  /**
   * 写入数据到指定文件路径，支持国际化
   *
   * @param definition 数据定义配置
   * @param data       数据集合
   * @param filePath   输出文件路径
   * @param locale     语言标签；为 null 时不做国际化解析
   */
  void write(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull String filePath, @Nullable String locale);
}
