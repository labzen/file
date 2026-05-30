package cn.labzen.file.definition.bean.scoped;

import cn.labzen.file.definition.bean.style.Style;
import lombok.Data;

/**
 * 全局列默认值
 *
 * @author labzen
 */
@Data
public class GlobalColumn {

  /**
   * 列宽
   */
  private Integer width = 10;
  /**
   * 导出文件样式
   */
  private Style style;
}
