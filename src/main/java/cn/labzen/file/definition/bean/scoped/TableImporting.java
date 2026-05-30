package cn.labzen.file.definition.bean.scoped;

import lombok.Data;

import java.util.List;

/**
 * 导入方向全局定义
 *
 * @author labzen
 */
@Data
public class TableImporting {

  /**
   * 必填
   */
  private boolean required = true;

  /**
   * 清理器列表
   */
  private List<String> cleansing;
}
