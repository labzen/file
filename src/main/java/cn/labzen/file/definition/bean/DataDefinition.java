package cn.labzen.file.definition.bean;

import cn.labzen.file.definition.bean.column.Column;
import cn.labzen.file.definition.bean.scoped.TableExporting;
import cn.labzen.file.definition.bean.scoped.TableImporting;
import cn.labzen.file.definition.bean.style.Style;
import cn.labzen.file.definition.bean.table.HeaderStructure;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据定义根对象
 *
 * @author labzen
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataDefinition {

  /**
   * 域名（对应YAML文件名，也对应Java Bean类名）
   */
  private String domainName;

  /**
   * 导出文件名
   */
  private String filename;

  /**
   * 数据标题（Excel sheet名/PDF标题等）
   */
  private String title;

  /**
   * 单表通用表头样式
   */
  private Style headerStyle;

  /**
   * 单表通用单元格样式
   */
  private Style columnStyle;

  /**
   * 列定义（LinkedHashMap保持顺序，key=字段名）
   */
  private Map<String, Column> columns = new LinkedHashMap<>();

  /**
   * 列导出方向全局配置
   */
  private TableExporting exporting;

  /**
   * 列导入方向全局配置
   */
  private TableImporting importing;

  /**
   * 在运行时对数据定义构建后的计算出的表头结构
   */
  private HeaderStructure headers;

  /**
   * Mock数据（来自同名.mock.json文件，可能为null）
   */
  private List<Map<String, String>> mockData;
}
