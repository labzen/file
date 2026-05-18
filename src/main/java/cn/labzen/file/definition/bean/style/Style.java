package cn.labzen.file.definition.bean.style;

import cn.labzen.file.definition.enums.Alignment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 样式定义
 * <p>
 * 用于定义单元格的完整样式，包括对齐方式、背景色、字体、边框、是否换行等。
 * 可用于全局样式（表头样式、数据样式）或单列覆盖样式
 *
 * @author labzen
 * @see Font
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Style {

  /**
   * 对齐方式，默认 CENTER（居中对齐）
   */
  private Alignment align = Alignment.CENTER;

  /**
   * 背景色，十六进制 RGB 格式，如 #FFFFFF
   */
  private String background = "#FFFFFF";

  /**
   * 字体样式
   */
  private Font font = new Font();

  /**
   * 是否自动换行，默认 true
   */
  private Boolean wrapped = true;
}
