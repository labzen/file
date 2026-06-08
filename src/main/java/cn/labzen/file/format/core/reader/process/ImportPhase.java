package cn.labzen.file.format.core.reader.process;

/**
 * 导入阶段枚举
 *
 * @author labzen
 */
public enum ImportPhase {
  /**
   * 清理阶段
   */
  CLEANSE,
  /**
   * 预校验阶段（必填、长度、格式、依赖）
   */
  VALIDATE,
  /**
   * 转换阶段（类型转换、反向映射、反向枚举——含隐式校验）
   */
  CONVERT,
  /**
   * 后校验阶段（数值/日期范围）
   */
  POST_VALIDATE,
  /**
   * Bean构建阶段
   */
  CONSTRUCT
}
