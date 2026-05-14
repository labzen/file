package cn.labzen.file.definition.enums;

/**
 * 数据转换器类型枚举
 * <p>
 * 定义支持的数据转换器类型
 *
 * @author labzen
 */
public enum ConverterType {

  /**
   * 枚举转换器 - 通过枚举类和方法将值转换为枚举描述
   */
  ENUM,

  /**
   * 预置转换器 - 使用系统预置的转换器（如大写、小写、 Trim 等）
   */
  NAME,

  /**
   * 值映射转换器 - 通过键值对映射表进行转换
   */
  MAPPING
}
