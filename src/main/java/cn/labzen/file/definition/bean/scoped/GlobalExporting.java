package cn.labzen.file.definition.bean.scoped;

import lombok.Data;

/**
 * 导出方向表内全局定义
 *
 * @author labzen
 */
@Data
public class GlobalExporting {

  /**
   * null时默认值
   */
  private String whenNull;

  /**
   * 空字符串时默认值
   */
  private String whenBlank;
}
