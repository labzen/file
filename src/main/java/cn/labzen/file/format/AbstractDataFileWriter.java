package cn.labzen.file.format;

import cn.labzen.file.converter.ChainableConverterExecutor;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.TableColumn;
import cn.labzen.file.exception.DataWriteException;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.io.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据文件写入器抽象基类
 * <p>
 * 提供文件写入的通用逻辑，子类只需实现具体的格式生成方法。
 * 包含数据验证、输出流管理等通用操作。
 *
 * @param <T> 数据对象类型
 * @author labzen
 * @see DataFileWriter
 */
@Slf4j
public abstract class AbstractDataFileWriter<T> implements DataFileWriter<T> {

  /**
   * 将数据对象转换为行数据映射
   * <p>
   * key: 字段名（属性名），value: 字段值
   *
   * @param definition 数据定义
   * @param data       数据集合
   * @return 行数据列表
   */
  private List<Map<String, Object>> extractRows(@Nonnull DataDefinition definition, @Nonnull List<T> data) {
    return data.stream().map(item -> extractRow(definition, item)).collect(Collectors.toList());
  }

  /**
   * 将单个数据对象转换为行数据映射
   *
   * @param definition 数据定义
   * @param item       单条数据
   * @return 字段名到字段值的映射
   */
  private Map<String, Object> extractRow(@Nonnull DataDefinition definition, @Nonnull T item) {
    Map<String, TableColumn> columns = definition.getColumns();
    Map<String, Object> result = new java.util.HashMap<>();
    for (String fieldName : columns.keySet()) {
      result.put(fieldName, extractFieldValue(definition.getDomainName(), fieldName, columns.get(fieldName), item));
    }
    return result;
  }

  /**
   * 提取字段值
   *
   * @param fieldName 字段名
   * @param column    列定义
   * @param item      数据对象
   * @return 字段值
   */
  private Object extractFieldValue(String domainName, String fieldName, TableColumn column, @Nonnull T item) {
    try {
      Field field = item.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      Object value = field.get(item);

      ChainableConverterExecutor executor = ChainableConverterExecutor.get(domainName, fieldName);
      if (executor != null) {
        return executor.execute(value);
      } else {
        logger.warn("未找到字段转换器 [{} - {}]", domainName, fieldName);
        return "failed";
      }
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new DataWriteException(e, "无法访问字段 - {}#{}", domainName, fieldName);
    }
  }

  /**
   * 创建字节输出流
   *
   * @param file 输出文件
   * @return 字节输出流
   */
  protected FileOutputStream createFileOutputStream(File file) throws FileNotFoundException {
    File parent = file.getParentFile();
    if (parent != null && !parent.exists()) {
      boolean make = parent.mkdirs();
      if (!make) {
        logger.warn("创建文件夹失败: {}", parent.getAbsolutePath());
      }
    }
    return new FileOutputStream(file);
  }

  /**
   * 写入数据到输出流
   * <p>
   * 模板方法实现，调用 generateContent 生成具体格式内容
   *
   * @param definition   数据定义
   * @param data         数据集合
   * @param outputStream 输出流
   */
  @Override
  public void write(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull OutputStream outputStream) {
    List<Map<String, Object>> rows = extractRows(definition, data);
    generateContent(definition, rows, outputStream);
  }

  /**
   * 写入数据到文件
   * <p>
   * 模板方法，定义通用的写入流程
   *
   * @param definition 数据定义
   * @param data       数据集合
   * @param file       输出文件
   */
  @Override
  public void write(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull File file) {
    try (OutputStream outputStream = createFileOutputStream(file)) {
      write(definition, data, outputStream);
    } catch (IOException e) {
      throw new DataWriteException(e, "写入文件失败: {}", file.getAbsolutePath());
    }
  }

  /**
   * 写入数据到文件路径
   * <p>
   * 模板方法，定义通用的写入流程
   *
   * @param definition 数据定义
   * @param data       数据集合
   * @param filePath   输出文件路径
   */
  @Override
  public void write(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull String filePath) {
    write(definition, data, new File(filePath));
  }

  /**
   * 生成文件内容的抽象方法
   * <p>
   * 子类实现此方法定义具体的格式生成逻辑
   *
   * @param definition   数据定义
   * @param rows         数据集合
   * @param outputStream 输出流
   */
  protected abstract void generateContent(@Nonnull DataDefinition definition,
                                          @Nonnull List<Map<String, Object>> rows,
                                          @Nonnull OutputStream outputStream);
}
