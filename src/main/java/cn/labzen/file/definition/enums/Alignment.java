package cn.labzen.file.definition.enums;

/**
 * 对齐方式枚举
 * <p>
 * 用于定义单元格内容在水平和垂直方向上的对齐方式
 *
 * @author labzen
 */
public enum Alignment {

  /**
   * 默认水平对齐
   */
  HORIZONTAL,

  /**
   * 居中对齐（水平和垂直居中）
   */
  CENTER,

  /**
   * 水平左对齐
   */
  LEFT,

  /**
   * 水平右对齐
   */
  RIGHT,

  /**
   * 垂直上对齐
   */
  TOP,

  /**
   * 垂直下对齐
   */
  BOTTOM,

  /**
   * 水平填充对齐
   */
  FILL,

  /**
   * 水平和垂直两端对齐
   */
  JUSTIFY,

  /**
   * 水平和垂直分散对齐
   */
  DISTRIBUTED
}
