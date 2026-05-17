package cn.labzen.file.definition.bean.column;

import cn.labzen.file.definition.bean.style.Style;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 全局列默认值配置
 * <p>
 * 定义所有列的默认属性值，可被单独列配置覆盖
 *
 * @author labzen
 * @see Style
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalColumn {

  /**
   * 列宽
   */
  private Integer width = 10;

  /**
   * 当数据为 null 时使用的默认值
   */
  private String whenNull;

  /**
   * 当数据为空字符串时使用的默认值，仅适用于 String 类型
   */
  private String whenBlank;

  /**
   * 最终值的文本前缀
   */
  private String prefix;

  /**
   * 最终值的文本后缀
   */
  private String suffix;

  /**
   * 数据单元格样式
   */
  private Style style;
}
