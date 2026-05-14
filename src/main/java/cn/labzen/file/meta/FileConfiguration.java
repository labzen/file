package cn.labzen.file.meta;

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


  @Item(path = "font-family", required = false, defaultValue = "auto")
  String defaultFontFamily();
}
