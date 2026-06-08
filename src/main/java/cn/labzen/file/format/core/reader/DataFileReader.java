package cn.labzen.file.format.core.reader;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.format.core.reader.process.ImportResult;
import cn.labzen.file.meta.FileConfiguration;
import jakarta.annotation.Nonnull;

import java.io.File;
import java.io.InputStream;

/**
 * 数据文件读取器接口
 * <p>
 * 定义从不同格式文件中读取数据的统一接口。
 * 每种支持的文件格式（Excel、JSON、XML、YAML）各有一个实现。
 *
 * @author labzen
 */
public interface DataFileReader {

  String SEQUENCE_KEY = ":__row-sequence__:";

  /**
   * 支持的文件格式
   */
  FileFormat format();

  /**
   * 初始化导入器
   *
   * @param configuration 文件配置
   */
  void initialize(@Nonnull FileConfiguration configuration);

  /**
   * 流式读取文件，返回行迭代器
   *
   * @param definition  数据定义（用于Excel读取时识别列）
   * @param inputStream 文件输入流
   * @return 迭代器，每个元素为 字段名→字符串值 的映射
   */
  <T> ImportResult<T> read(@Nonnull DataDefinition definition, @Nonnull InputStream inputStream);

  /**
   * 读取文件，返回行列表
   *
   * @param definition 数据定义（用于Excel读取时识别列）
   * @param file       文件
   * @return 行列表，每个元素为 字段名→字符串值 的映射
   */
  <T> ImportResult<T> read(@Nonnull DataDefinition definition, @Nonnull File file);

  /**
   * 读取文件，返回行列表
   *
   * @param definition 数据定义（用于Excel读取时识别列）
   * @param filePath   文件路径
   * @return 行列表，每个元素为 字段名→字符串值 的映射
   */
  <T> ImportResult<T> read(@Nonnull DataDefinition definition, @Nonnull String filePath);
}
