package cn.labzen.file.definition.bean.column;

import cn.labzen.file.definition.bean.converter.Converter;
import cn.labzen.file.definition.bean.converter.Pattern;
import cn.labzen.file.definition.bean.style.Style;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 列定义
 * <p>
 * 用于定义单个表字段在导出时的配置，包括表头标题、列宽、默认值、数据格式、数据转换器和样式覆盖等。
 * 每个 ColumnDefinition 对应 YAML 配置文件中 columns 节点下的一个字段配置
 *
 * @author labzen
 * @see Converter
 * @see Pattern
 * @see Style
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableColumn extends GlobalColumn {

  /**
   * 表头标题列表，支持多级表头（CSV文件不支持）
   * <p>
   * 数组长度表示表头级别，示例：
   * <ul>
   *   <li>["姓名"] - 一级表头</li>
   *   <li>["基本信息", "姓名"] - 二级表头</li>
   *   <li>["基本信息", "级别一", "姓名"] - 三级表头</li>
   * </ul>
   */
  private List<String> header = new ArrayList<>();

  /**
   * 列索引，从 0 开始，越小越靠左
   * <p>
   * 不需指定，会按照 YAML 文件中定义的顺序显示列
   */
  private Integer index;

  /**
   * 数据格式化模式配置
   */
  private Pattern pattern = new Pattern();

  /**
   * 数据转换器配置
   */
  private Converter converter = new Converter();

  /**
   * 获取第一级表头标题
   *
   * @return 第一级表头，如无配置返回 null
   */
  public String getFirstHeader() {
    if (header == null || header.isEmpty()) {
      return null;
    }
    return header.getFirst();
  }

  /**
   * 获取表头级别数（多级表头深度）
   *
   * @return 表头级别数
   */
  public int getHeaderLevel() {
    if (header == null) {
      return 0;
    }
    return header.size();
  }
}
