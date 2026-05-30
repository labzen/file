package cn.labzen.file.format.core.reader;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 字段级错误
 *
 * @author labzen
 */
@Data
@AllArgsConstructor
public class FieldError {

  /**
   * 字段名
   */
  private final String fieldName;

  /**
   * 人类可读的表头名
   */
  private final String headerText;

  /**
   * 原始值
   */
  private final String cellValue;

  /**
   * 失败阶段
   */
  private final ImportPhase phase;

  /**
   * 人类可读的错误信息（已国际化）
   */
  private final String errorMessage;
}
