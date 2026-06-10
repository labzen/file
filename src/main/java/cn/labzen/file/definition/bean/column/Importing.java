package cn.labzen.file.definition.bean.column;

import cn.labzen.file.definition.bean.column.constraint.DateRange;
import cn.labzen.file.definition.bean.column.constraint.LengthRange;
import cn.labzen.file.definition.bean.column.constraint.NumericRange;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 导入列定义
 * <p>
 * 包含仅用于导入方向的列配置
 *
 * @author labzen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Importing extends GlobalImporting {

  /**
   * 最小长度
   */
  private Integer minLength;

  /**
   * 最大长度
   */
  private Integer maxLength;

  /**
   * 批次内唯一
   */
  private Boolean unique;

  /**
   * 依赖字段列表（当这些字段有值时，当前字段必填）
   */
  private List<String> dependsOn;

  /**
   * 最小值（数值/日期，字符串表示）
   */
  private String min;

  /**
   * 最大值（数值/日期，字符串表示）
   */
  private String max;

  /**
   * 导入专属映射（覆盖共享层mapping）
   * <p>
   * key=存储值，value=导入值
   */
  private Map<String, String> mapping;

  /**
   * 导入专属枚举（覆盖共享层enumerable）
   */
  private String enumerable;

  /**
   * 导入专属预制转换器
   * <p>
   * 如 uppercase、lowercase, bool 等，具体名称参考具体接口实现
   */
  private String converter;

  private LengthRange lengthRange;
  private NumericRange numericRange;
  private DateRange dateRange;

}
