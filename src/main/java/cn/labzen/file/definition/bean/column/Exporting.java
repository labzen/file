package cn.labzen.file.definition.bean.column;

import cn.labzen.file.definition.bean.scoped.GlobalExporting;
import cn.labzen.file.definition.bean.style.Style;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 导出列定义
 * <p>
 * 包含仅用于导出方向的列配置
 *
 * @author labzen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Exporting extends GlobalExporting {

  /**
   * 前缀
   */
  private String prefix;

  /**
   * 后缀
   */
  private String suffix;

  /**
   * 导出专属映射（覆盖共享层mapping）
   * <p>
   * key=存储值，value=展示值
   */
  private Map<String, String> mapping;

  /**
   * 导出专属枚举（覆盖共享层enumerable）
   * <p>
   * 格式为 枚举类全限定名#方法名，如 com.example.StatusEnum#getLabel
   */
  private String enumerable;

  /**
   * 导出专属预制转换器
   * <p>
   * 如 desensitize、truncate、bool 等，具体名称参考具体接口实现
   */
  private String converter;

  /**
   * 导出文件样式
   */
  private Style style;
}
