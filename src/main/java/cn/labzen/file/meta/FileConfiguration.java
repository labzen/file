package cn.labzen.file.meta;

import cn.labzen.file.format.csv.CsvFileWriter;
import cn.labzen.file.format.txt.TxtFileWriter;
import cn.labzen.meta.configuration.annotation.Configured;
import cn.labzen.meta.configuration.annotation.Item;

@Configured(namespace = "file")
public interface FileConfiguration {

  /**
   * 数据导出规则定义文件的位置，默认值（classpath*:data-export/&ast;&ast;/&ast;.yml）在 resources/data-export/ 下的所有yml文件
   */
  @Item(path = "data-definition-location", required = false, defaultValue = "classpath*:data-export/**/*.yml")
  String dataDefinitionLocation();

  /**
   * 全局定义文件名称，默认值（classpath:data-export/__global__.yml）
   */
  @Item(path = "global-definition-name", required = false, defaultValue = "classpath:data-export/__global__.yml")
  String globalDefinitionFilename();

  @Deprecated
  @Item(path = "font-family", required = false, defaultValue = "auto")
  String defaultFontFamily();

  /**
   * Txt文件中分隔每列表头、数据的分隔符，默认4个空格
   */
  @Item(path = "txt.separator", required = false, defaultValue = TxtFileWriter.DEFAULT_SEPARATOR)
  String txtSeparator();

  /**
   * CSV文件中的分隔符，默认英文逗号
   */
  @Item(path = "csv.separator", required = false, defaultValue = CsvFileWriter.DEFAULT_DELIMITER)
  String csvDelimiter();

  /**
   * CSV文件中的引用字符，默认英文双引号
   */
  @Item(path = "csv.quote", required = false, defaultValue = CsvFileWriter.DEFAULT_QUOTE)
  String csvQuote();

  /**
   * JSON文件的内容格式化输出，默认true
   */
  @Item(path = "json.pretty", required = false, defaultValue = "true")
  boolean jsonPrettyFormat();

  /**
   * YAML文件的内容格式化输出，默认true
   */
  @Item(path = "yaml.pretty", required = false, defaultValue = "true")
  boolean yamlPrettyFormat();
}
