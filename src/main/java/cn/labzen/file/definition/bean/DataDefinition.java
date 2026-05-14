package cn.labzen.file.definition.bean;

import cn.labzen.file.definition.bean.column.TableColumn;
import cn.labzen.file.definition.bean.style.Style;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 数据导出配置根对象
 * <p>
 * 定义一个表的数据导出时的完整配置，包括文件名、标题、全局样式和列定义。
 * 对应 YAML 配置文件中的根节点结构
 *
 * <p>配置示例：
 * <pre>
 * filename: user-export
 * title: 用户信息导出
 * global-style:
 *   header:
 *     align: CENTER
 *     background: "#000000"
 *     font:
 *       family: "Arial"
 *       size: 11
 *       color: "#ffffff"
 *       bold: true
 *       italic: true
 *     border:
 *       color: "#ffffff"
 *       width: MEDIUM
 *     wrapped: false
 *   body:
 *     align: CENTER
 * columns:
 *   username:
 *     header:
 *       - "基本信息"
 *       - "用户名"
 *     index: 0
 *     width: 100
 *     when-null: "匿名"
 *     converter:
 *       mapping:
 *         "1": "男"
 *         "2": "女"
 * </pre>
 *
 * @author labzen
 * @see TableColumn
 * @see Style
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataDefinition {

  /**
   * 表映射类名
   */
  private String domainName;

  /**
   * 导出文件名
   */
  private String filename;

  /**
   * 数据标题
   * <p>
   * 用于显示在表头上方：
   * <ul>
   *   <li>Excel - 作为 sheet 名</li>
   *   <li>CSV - 忽略</li>
   *   <li>其他文件格式 - 作为数据表格上方的标题</li>
   * </ul>
   */
  private String title;

  /**
   * 单表数据的通用表头样式定义
   */
  private Style headerStyle;

  /**
   * 单表数据的通用单元格样式定义
   */
  private Style columnStyle;

  /**
   * 列定义集合
   * <p>
   * key 为表字段名映射的类属性名，value 为列配置
   * <p>
   * 例如：表字段名为 parent_code，映射的属性名为 parentCode，使用 parentCode 作为 key
   */
  private Map<String, TableColumn> columns = new LinkedHashMap<>();
}
