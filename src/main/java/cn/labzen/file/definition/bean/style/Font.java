package cn.labzen.file.definition.bean.style;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字体样式定义
 * <p>
 * 用于定义单元格的字体属性，包括字体名称、大小、颜色、加粗、斜体等
 *
 * @author labzen
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Font {

  /**
   * 字体名称，如 Arial、Microsoft YaHei
   */
  private String family;

  /**
   * 字体大小，默认 13
   */
  private Integer size = 13;

  /**
   * 字体颜色，十六进制 RGB 格式，如 #FFFFFF
   */
  private String color;

  /**
   * 是否加粗
   */
  private Boolean bold = false;

  /**
   * 是否斜体
   */
  private Boolean italic = false;
}
