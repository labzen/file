package cn.labzen.file.format.core.writer;

import cn.labzen.file.converter.exportable.ChainableExportConverterExecutor;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.Column;
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
 *
 * @param <T> 数据对象类型
 * @author labzen
 * @see DataFileWriter
 */
@Slf4j
public abstract class AbstractDataFileWriter<T> implements DataFileWriter<T> {

  private List<Map<String, Object>> extractRows(@Nonnull DataDefinition definition,
                                                @Nonnull List<T> data,
                                                @Nullable Map<String, ChainableExportConverterExecutor> executors) {
    return data.stream().map(item -> extractRow(definition, item, executors)).collect(Collectors.toList());
  }

  private Map<String, Object> extractRow(@Nonnull DataDefinition definition,
                                         @Nonnull T item,
                                         @Nullable Map<String, ChainableExportConverterExecutor> executors) {
    Map<String, Column> columns = definition.getColumns();
    Map<String, Object> result = new java.util.HashMap<>();
    for (String fieldName : columns.keySet()) {
      result.put(fieldName, extractFieldValue(definition.getDomainName(), fieldName, executors, item));
    }
    return result;
  }

  private Object extractFieldValue(String domainName,
                                   String fieldName,
                                   @Nullable Map<String, ChainableExportConverterExecutor> executors,
                                   @Nonnull T item) {
    try {
      Field field = item.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      Object value = field.get(item);

      ChainableExportConverterExecutor executor;
      if (executors != null) {
        executor = executors.get(fieldName);
      } else {
        executor = ChainableExportConverterExecutor.get(domainName, fieldName);
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

  @Override
  public void write(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull OutputStream outputStream, @Nullable String locale) {
    I18nStoreProvider store = I18nStoreHolder.get();
    I18nResolver resolver = new I18nResolver(store);
    DataDefinition resolved = resolver.resolve(definition, locale);
    Map<String, ChainableExportConverterExecutor> executors = ChainableExportConverterExecutor.buildFor(resolved);
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

  protected abstract void generateContent(@Nonnull DataDefinition definition,
                                          @Nonnull List<Map<String, Object>> rows,
                                          @Nonnull OutputStream outputStream);
}
