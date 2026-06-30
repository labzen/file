package cn.labzen.file.format.core.reader.process;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
   * 成功的 Bean 实例集合（含行号）
   * <p>
   * 通过 {@link PositionedData} 绑定领域 Bean 与其在原始文件中的行号，
   * 使下游入库阶段能精确报告错误所在行。
   *
   * @see #getPayloads() 仅获取领域 Bean 列表（不关心行号时使用）
   */
  private final List<PositionedData<T>> data;

  /**
   * 失败详情
   */
  private final List<ImportFailure> failures;

  public boolean hasFailure() {
    return failureCount > 0;
  }

  /**
   * 获取带行号的全量数据列表。
   */
  public List<PositionedData<T>> getData() {
    return Collections.unmodifiableList(data);
  }

  /**
   * 仅获取领域 Bean 列表（不含行号信息），过滤掉失败行（payload 为 null 的记录）。
   * <p>
   * 用于不关心 Excel 行号的场景，与旧版 {@code getData()} 行为一致。
   */
  public List<T> getPayloads() {
    return data.stream()
      .map(PositionedData::payload)
      .filter(Objects::nonNull)
      .toList();
  }

  /**
   * 获取失败详情列表。
   */
  public List<ImportFailure> getFailures() {
    return Collections.unmodifiableList(failures);
  }
}
