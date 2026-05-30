package cn.labzen.file.format.core.reader;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.meta.FileConfiguration;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * 数据文件读取器接口
 * <p>
 * 定义从不同格式文件中读取数据的统一接口。
 * 每种支持的文件格式（Excel、JSON、XML、YAML）各有一个实现。
 *
 * @author labzen
 */
public interface DataFileReader {

  /**
   * 支持的文件格式
   */
  FileFormat format();

  /**
   * 初始化配置
   */
  void initialize(FileConfiguration configuration);

  /**
   * 流式读取文件，返回行迭代器
   *
   * @param inputStream   文件输入流
   * @param definition    数据定义（用于Excel读取时识别列）
   * @return 迭代器，每个元素为 字段名→字符串值 的映射
   */
  Iterator<Map<String, String>> read(InputStream inputStream, DataDefinition definition);
}
