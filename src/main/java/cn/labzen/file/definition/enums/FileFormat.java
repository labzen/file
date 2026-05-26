package cn.labzen.file.definition.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 定义支持的文件导出格式
 */
@RequiredArgsConstructor
public enum FileFormat {

  /**
   * Excel 格式
   */
  EXCEL(".xlsx"),

  /**
   * CSV 格式
   */
  CSV(".csv"),

  /**
   * PDF 格式
   */
  PDF(".pdf"),

  /**
   * Markdown 格式
   */
  MARKDOWN(".md"),

  /**
   * HTML 格式
   */
  HTML(".html"),

  /**
   * JSON 格式
   */
  JSON(".json"),

  /**
   * YAML 格式
   */
  YAML(".yml"),

  /**
   * XML 格式
   */
  XML(".xml"),

  /**
   * 纯文本文件
   */
  TXT(".txt"),

  /**
   * 无法识别类型，可兼容任何文件格式
   */
  UNKNOWN("");

  @Getter
  private final String extension;
}
