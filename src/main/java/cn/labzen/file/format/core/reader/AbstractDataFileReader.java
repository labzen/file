package cn.labzen.file.format.core.reader;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.exception.DataReadException;
import cn.labzen.file.meta.FileConfiguration;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * 抽象数据文件读取器基类
 * <p>
 * 提供读取器的通用逻辑，子类只需实现具体的解析方法。
 *
 * @author labzen
 */
public abstract class AbstractDataFileReader implements DataFileReader {

  protected FileConfiguration configuration;

  @Override
  public void initialize(FileConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public Iterator<Map<String, String>> read(InputStream inputStream, DataDefinition definition) {
    try {
      return doRead(inputStream, definition);
    } catch (Exception e) {
      if (e instanceof DataReadException dre) {
        throw dre;
      }
      throw new DataReadException(e, "读取文件失败");
    }
  }

  /**
   * 子类实现具体的文件读取逻辑
   *
   * @param inputStream 文件输入流
   * @param definition  数据定义
   * @return 行迭代器
   */
  protected abstract Iterator<Map<String, String>> doRead(InputStream inputStream, DataDefinition definition);
}
