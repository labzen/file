package cn.labzen.file.format.core.reader;

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
   */
  private final int rowIndex;

  /**
   * 原始行数据（字段名→原始字符串值）
   */
  private final Map<String, String> rowData;

  /**
   * 该行的所有错误
   */
  private final List<FieldError> errors;
}
