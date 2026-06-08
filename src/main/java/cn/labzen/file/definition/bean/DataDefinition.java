package cn.labzen.file.definition.bean;

import cn.labzen.file.definition.bean.column.Column;
import cn.labzen.file.definition.bean.column.Importing;
import cn.labzen.file.definition.bean.column.constraint.DateRange;
import cn.labzen.file.definition.bean.column.constraint.LengthRange;
import cn.labzen.file.definition.bean.column.constraint.NumericRange;
import cn.labzen.file.definition.bean.scoped.GlobalExporting;
import cn.labzen.file.definition.bean.scoped.GlobalImporting;
import cn.labzen.file.definition.bean.style.Style;
import cn.labzen.file.definition.bean.table.HeaderBuilder;
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
   * 域映射类FQCN
   */
  private String domain;

  private Class<?> domainClass;

  /**
   * 语言
   */
  private String locale;

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
  private Style exportingHeaderStyle;

  /**
   * 单表通用单元格样式
   */
  private Style exportingColumnStyle;

  /**
   * 列定义（LinkedHashMap保持顺序，key=字段名）
   */
  private Map<String, Column> columns = new LinkedHashMap<>();

  /**
   * 列导出方向全局配置
   */
  private GlobalExporting exporting;

  /**
   * 列导入方向全局配置
   */
  private GlobalImporting importing;

  /**
   * 全局默认列宽（导入导出共享）
   * <p>
   * 列级别 column.width 可覆盖此默认值
   */
  private Integer width;

  /**
   * 在运行时对数据定义构建后的计算出的表头结构
   */
  private HeaderStructure headers;

  /**
   * Mock数据（来自同名.mock.json文件，可能为null）
   */
  private List<Map<String, String>> mockData;

  public void pretreatment() {
    this.headers = HeaderBuilder.build(this);

    for (Column column : columns.values()) {
      Importing ipt = column.getImporting();
      if (ipt == null) {
        continue;
      }

      ipt.setLengthRange(LengthRange.get(ipt));
      ipt.setDateRange(DateRange.get(ipt, column.getPatternDate()));
      ipt.setNumericRange(NumericRange.get(ipt));
    }
  }
}
