package cn.labzen.file.format.core.writer;

import cn.labzen.file.converter.executor.ChainableExportConverterExecutor;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.Column;
import cn.labzen.file.exception.DataWriteException;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据文件导出器抽象基类
 * <p>
 * 提供文件导出的通用逻辑，子类只需实现具体的格式生成方法。
 *
 * @param <T> 数据对象类型
 * @author labzen
 * @see DataFileWriter
 */
@Slf4j
public abstract class AbstractDataFileWriter<T> implements DataFileWriter<T> {

  private List<Map<String, Object>> extractRows(@Nonnull DataDefinition definition, @Nonnull List<T> data) {
    return data.stream().map(item -> extractRow(definition, item)).collect(Collectors.toList());
  }

  private Map<String, Object> extractRow(@Nonnull DataDefinition definition, @Nonnull T item) {
    Map<String, Column> columns = definition.getColumns();
    Map<String, Object> result = new java.util.HashMap<>();
    for (String fieldName : columns.keySet()) {
      result.put(fieldName, extractFieldValue(definition, fieldName, item));
    }
    return result;
  }

  private Object extractFieldValue(DataDefinition definition,
                                   String fieldName,
                                   @Nonnull T item) {
    try {
      Field field = item.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      Object value = field.get(item);

      // todo 改为事先获取所有的转换器
      ChainableExportConverterExecutor executor = ChainableExportConverterExecutor.get(definition, fieldName);
//      if (executors != null) {
//        executor = executors.get(fieldName);
//      } else {
//        executor = ChainableExportConverterExecutor.get(definition, fieldName);
//      }

      if (executor != null) {
        return executor.execute(value);
      } else {
        logger.warn("未找到字段转换器 [{} - {}]", definition.getName(), fieldName);
        return "failed";
      }
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new DataWriteException(e, "无法访问字段 - {}#{}", definition.getName(), fieldName);
    }
  }

  private FileOutputStream createFileOutputStream(File file) throws FileNotFoundException {
    File parent = file.getParentFile();
    if (parent != null && !parent.exists()) {
      boolean make = parent.mkdirs();
      if (!make) {
        logger.warn("创建文件夹失败: {}", parent.getAbsolutePath());
      }
    }
    return new FileOutputStream(file);
  }

  @Override
  public final void write(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull OutputStream outputStream) {
//    I18nMessageSource messageSource = I18nMessageSourceHolder.get();
//    I18nResolver resolver = new I18nResolver(store);
//    DataDefinition resolved = resolver.resolve(definition, locale);
//    Map<String, ChainableExportConverterExecutor> executors = ChainableExportConverterExecutor.get(definition, locale);
    List<Map<String, Object>> rows = extractRows(definition, data);
    exportContent(definition, rows, outputStream);
  }

  @Override
  public final void write(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull File file) {
    if (!file.isFile()) {
      throw new DataWriteException("导出文件不是文件: {}", file.getAbsolutePath());
    }

    try (OutputStream outputStream = createFileOutputStream(file)) {
      write(definition, data, outputStream);
    } catch (IOException e) {
      throw new DataWriteException(e, "导出文件失败: {}", file.getAbsolutePath());
    }
  }

  @Override
  public final void write(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull String filePath) {
    write(definition, data, new File(filePath));
  }

  protected abstract void exportContent(@Nonnull DataDefinition definition,
                                        @Nonnull List<Map<String, Object>> rows,
                                        @Nonnull OutputStream outputStream);
}
