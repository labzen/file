package cn.labzen.file.format.core.reader;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.exception.DataReadException;
import cn.labzen.file.exception.DataWriteException;
import cn.labzen.file.format.core.reader.process.ImportProcessor;
import cn.labzen.file.format.core.reader.process.ImportResult;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 抽象数据文件读取器基类
 * <p>
 * 提供读取器的通用逻辑，子类只需实现具体的解析方法。
 *
 * @author labzen
 */
@Slf4j
public abstract class AbstractDataFileReader implements DataFileReader {

  @Override
  public final <T> ImportResult<T> read(@NonNull DataDefinition definition, @NonNull InputStream inputStream) {
    try {
      List<Map<String, String>> rowsData = importContent(inputStream);
      ImportProcessor<T> pipeline = new ImportProcessor<>(definition);
      return pipeline.process(rowsData);
//      return rowsData;
    } catch (Exception e) {
      if (e instanceof DataReadException dre) {
        throw dre;
      }
      throw new DataReadException(e, "读取文件失败");
    }
  }

  @Override
  public final <T> ImportResult<T> read(@NonNull DataDefinition definition, @NonNull File file) {
    if (!file.isFile()) {
      throw new DataReadException("导入文件不是文件: {}", file.getAbsolutePath());
    }

    try (InputStream inputStream = new FileInputStream(file)) {
      return read(definition, inputStream);
    } catch (IOException e) {
      throw new DataWriteException(e, "读取文件失败: {}", file.getAbsolutePath());
    }
  }

  @Override
  public final <T> ImportResult<T> read(@NonNull DataDefinition definition, @NonNull String filePath) {
    return read(definition, new File(filePath));
  }

  /**
   * 子类实现具体的文件读取逻辑
   *
   * @param inputStream 文件输入流
   * @return 行迭代器
   */
  protected abstract List<Map<String, String>> importContent(@Nonnull InputStream inputStream);
}
