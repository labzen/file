package cn.labzen.file.format.core.reader.process;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 导入失败详情
 *
 * @author labzen
 */
@Data
@AllArgsConstructor
public class ImportFailure {

  /**
   * 行号 = Excel中#列的序号，与用户看到的一致
   * <p>
   * 如果是其他格式，则为 1-N 的序号
   */
  private final String sequence;

  /**
   * 原始行数据（字段名→原始字符串值）
   */
  private final Map<String, Object> rowData;

  /**
   * 该行的所有错误
   */
  private final List<FieldError> errors;
}
