package cn.labzen.file.definition.enums;

/**
 * 定义支持的文件导出格式
 */
public enum FileFormat {

  /**
   * Excel 格式
   */
  EXCEL,

  /**
   * CSV 格式
   */
  CSV,

  /**
   * PDF 格式
   */
  PDF,

  /**
   * Markdown 格式
   */
  MARKDOWN,

  /**
   * HTML 格式
   */
  HTML,

  /**
   * JSON 格式
   */
  JSON,

  /**
   * YAML 格式
   */
  YAML,

  /**
   * XML 格式
   */
  XML,

  /**
   * 纯文本文件
   */
  TXT,

  /**
   * 无法识别类型，可兼容任何文件格式
   */
  UNKNOWN
}
