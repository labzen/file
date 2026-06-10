package cn.labzen.file.definition.bean;

import cn.labzen.file.definition.bean.column.GlobalExporting;
import cn.labzen.file.definition.bean.column.GlobalImporting;
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
 * width: 15
 * exporting-header-style:
 *   align: CENTER
 *   font:
 *     family: "Arial"
 *     size: 11
 *     bold: true
 *     italic: true
 *   wrapped: false
 * exporting-column-style:
 *   align: LEFT
 *   font:
 *     size: 12
 *     bold: false
 *     italic: false
 *     wrapped: true
 * importing:
 *   cleansing: [ 'trim', 'strip-html', 'strip-invisible' ]
 * exporting:
 *   when-null: ""
 *   when-blank: "-"
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
  private Style exportingHeaderStyle;

  /**
   * 全局单元格样式定义
   */
  private Style exportingColumnStyle;

  /**
   * 全局导出默认配置
   */
  private GlobalExporting exporting;

  /**
   * 全局导入默认配置
   */
  private GlobalImporting importing;

  /**
   * 全局默认列宽（导入导出共享）
   * <p>
   * 文件级别 dataDefinition.width 或 列级别 column.width 可覆盖此默认值
   */
  private Integer width;
}
