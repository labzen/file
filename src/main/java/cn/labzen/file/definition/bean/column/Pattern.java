package cn.labzen.file.definition.bean.column;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据格式化模式定义
 * <p>
 * 用于定义日期和数值的格式化模式，pattern 遵循对应类型的标准格式语法
 *
 * @author labzen
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Deprecated
public class Pattern {

  /**
   * 日期格式 pattern，如 yyyy-MM-dd HH:mm:ss。
   * 仅适用于日期时间类型（java.util.Date, java.time.LocalDateTime 等）
   */
  private String date;

  /**
   * 数值格式 pattern，如 #,##0.00。
   * 仅适用于浮点类型或整数类型
   */
  private String number;
}
