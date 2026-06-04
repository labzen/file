package cn.labzen.file.definition.bean.column;

import lombok.Data;

import java.util.Map;

/**
 * 列定义（继承全局列默认值）
 *
 * @author labzen
 */
@Data
public class Column {

  private String fieldName;
  private Class<?> fieldType;
  private boolean validated;

  /**
   * 列宽（导入导出共享）
   */
  private Integer width = 10;

  /**
   * 表头标题（支持二级，如 "成绩:-:语文"）
   */
  private String header;

  /**
   * 日期格式 pattern，如 yyyy-MM-dd HH:mm:ss。 仅适用于日期时间类型（java.util.Date, java.time.LocalDateTime 等）
   */
  private String patternDate;
  /**
   * 数值格式 pattern，如 #,##0.00。 仅适用于浮点类型或整数类型
   */
  private String patternNumber;

  /**
   * 共享映射（导入导出均可使用）
   * <p>
   * key=存储值（Bean字段值/数据库值），value=展示值/导入值（用户看到/录入的文本）
   * <p>
   * 导出时：如果字段值在key中存在，则使用映射value，否则无法转换
   * <p>
   * 导入时：如果导入值在value中存在，则使用映射key；否则转换失败
   */
  private Map<String, String> mapping;

  /**
   * 共享枚举（导入导出均可使用）
   * <p>
   * 格式为 枚举类全限定名#方法名，如 com.example.StatusEnum#getLabel
   */
  private String enumerable;

  /**
   * 导出列默认配置
   */
  private Exporting exporting;

  /**
   * 导入列默认配置
   */
  private Importing importing;
}
