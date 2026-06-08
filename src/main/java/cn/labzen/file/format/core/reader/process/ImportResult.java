package cn.labzen.file.format.core.reader.process;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 导入结果
 *
 * @param <T> Bean类型
 * @author labzen
 */
@Data
@AllArgsConstructor
public class ImportResult<T> {

  private final Class<?> type;

  /**
   * 总数据行数（不含表头/提示/示例）
   */
  private final int totalRows;

  /**
   * 成功行数
   */
  private final int successCount;

  /**
   * 失败行数
   */
  private final int failureCount;

  /**
   * 成功的Bean实例集合
   */
  private final List<T> data;

  /**
   * 失败详情
   */
  private final List<ImportFailure> failures;

  public boolean hasFailure() {
    return failureCount > 0;
  }

  public List<T> getData() {
    return Collections.unmodifiableList(data);
  }

  public List<ImportFailure> getFailures() {
    return Collections.unmodifiableList(failures);
  }
}
