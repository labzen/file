package cn.labzen.file.definition.bean;

import cn.labzen.file.definition.bean.scoped.GlobalColumn;
import cn.labzen.file.definition.bean.style.Style;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 全局配置
 * <p>
 * 用于定义全局的样式默认值，可被单独配置文件中的配置覆盖。
 * 对应 YAML 配置文件中的根节点结构（仅包含 header 和 column）
 *
 * <p>配置示例：
 * <pre>
 * header:
 *   align: CENTER
 *   background: "#000000"
 *   font:
 *     family: "Arial"
 *     size: 11
 *     color: "#ffffff"
 *     bold: true
 *     italic: true
 *   border:
 *     color: "#ffffff"
 *     width: MEDIUM
 *   wrapped: false
 * column:
 *   width: 100
 *   when-null: "-"
 *   when-blank: "-"
 *   prefix: ""
 *   suffix: ""
 *   style:
 *     align: CENTER
 *     background: "#F0F0F0"
 * </pre>
 *
 * @author labzen
 * @see Style
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalDefinition {

  /**
   * 全局表头样式定义
   */
  private Style header;

  /**
   * 全局列（数据单元格）默认值定义
   */
  private GlobalColumn column;
}
