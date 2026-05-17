package cn.labzen.file.definition.bean.style;

import cn.labzen.file.definition.enums.BorderWidth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 边框样式定义
 * <p>
 * 用于定义单元格的边框属性，包括边框颜色和宽度
 *
 * @author labzen
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
// 不打算要边框的配置了，没啥意义
@Deprecated
public class Border {

  /**
   * 边框颜色，十六进制 RGB 格式，如 #FFFFFF
   */
  private String color;

  /**
   * 边框宽度，默认 THIN（细边框）
   */
  private BorderWidth width = BorderWidth.THIN;
}
