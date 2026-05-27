package cn.labzen.file.format;

import cn.labzen.file.converter.ChainableConverterExecutor;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.TableColumn;
import cn.labzen.file.exception.DataWriteException;
import cn.labzen.file.i18n.I18nResolver;
import cn.labzen.file.i18n.I18nStoreHolder;
import cn.labzen.file.i18n.I18nStoreProvider;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
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
   * @param executors  字段转换器映射，为 null 时使用全局缓存的转换器
   * @return 行数据列表
   */
  private List<Map<String, Object>> extractRows(@Nonnull DataDefinition definition,
                                                @Nonnull List<T> data,
                                                @Nullable Map<String, ChainableConverterExecutor> executors) {
    return data.stream().map(item -> extractRow(definition, item, executors)).collect(Collectors.toList());
  }

  /**
   * 将单个数据对象转换为行数据映射
   */
  private Map<String, Object> extractRow(@Nonnull DataDefinition definition,
                                         @Nonnull T item,
                                         @Nullable Map<String, ChainableConverterExecutor> executors) {
    Map<String, TableColumn> columns = definition.getColumns();
    Map<String, Object> result = new java.util.HashMap<>();
    for (String fieldName : columns.keySet()) {
      result.put(fieldName, extractFieldValue(definition.getDomainName(), fieldName, executors, item));
    }
    return result;
  }

  /**
   * 提取字段值
   */
  private Object extractFieldValue(String domainName,
                                   String fieldName,
                                   @Nullable Map<String, ChainableConverterExecutor> executors,
                                   @Nonnull T item) {
    try {
      Field field = item.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      Object value = field.get(item);

      ChainableConverterExecutor executor;
      if (executors != null) {
        executor = executors.get(fieldName);
      } else {
        executor = ChainableConverterExecutor.get(domainName, fieldName);
      }

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

//  // ===== 无 locale 的写入方法（保持向后兼容） =====
//
//  @Override
//  public void write(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull OutputStream outputStream) {
//    List<Map<String, Object>> rows = extractRows(definition, data, null);
//    generateContent(definition, rows, outputStream);
//  }
//
//  @Override
//  public void write(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull File file) {
//    if (file.isDirectory()) {
//      throw new DataWriteException("输出文件不能是目录: {}", file.getAbsolutePath());
//    }
//
//    try (OutputStream outputStream = createFileOutputStream(file)) {
//      write(definition, data, outputStream);
//    } catch (IOException e) {
//      throw new DataWriteException(e, "写入文件失败: {}", file.getAbsolutePath());
//    }
//  }
//
//  @Override
//  public void write(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull String filePath) {
//    write(definition, data, new File(filePath));
//  }
//
//  // ===== 支持 locale 的写入方法 =====

  @Override
  public void write(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull OutputStream outputStream, @Nullable String locale) {
    I18nStoreProvider store = I18nStoreHolder.get();
    I18nResolver resolver = new I18nResolver(store);
    DataDefinition resolved = resolver.resolve(definition, locale);
    Map<String, ChainableConverterExecutor> executors = ChainableConverterExecutor.buildFor(resolved);
    List<Map<String, Object>> rows = extractRows(resolved, data, executors);
    generateContent(resolved, rows, outputStream);
  }

  @Override
  public void write(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull File file, @Nullable String locale) {
    if (file.isDirectory()) {
      throw new DataWriteException("输出文件不能是目录: {}", file.getAbsolutePath());
    }

    try (OutputStream outputStream = createFileOutputStream(file)) {
      write(definition, data, outputStream, locale);
    } catch (IOException e) {
      throw new DataWriteException(e, "写入文件失败: {}", file.getAbsolutePath());
    }
  }

  @Override
  public void write(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull String filePath, @Nullable String locale) {
    write(definition, data, new File(filePath), locale);
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
