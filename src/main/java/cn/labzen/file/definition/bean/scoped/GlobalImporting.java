package cn.labzen.file.definition.bean.scoped;

import lombok.Data;

import java.util.List;

/**
 * 导入方向全局定义
 *
 * @author labzen
 */
@Data
public class GlobalImporting {

  /**
   * 必填
   */
  private Boolean require = false;

  /**
   * 清理器列表
   */
  private List<String> cleansing;
}
