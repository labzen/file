package cn.labzen.file.exception;

import cn.labzen.meta.exception.LabzenRuntimeException;
import lombok.Getter;

/**
 * 配置文件加载异常
 * <p>
 * 当 YAML 配置文件加载、解析或校验失败时抛出此异常
 *
 * @author labzen
 */
public class DefinitionLoaderException extends LabzenRuntimeException {
//
//  /**
//   * 错误类型枚举
//   */
//  public enum ErrorType {
//    /**
//     * 文件未找到
//     */
//    FILE_NOT_FOUND,
//
//    /**
//     * classpath 资源未找到
//     */
//    RESOURCE_NOT_FOUND,
//
//    /**
//     * IO 错误
//     */
//    IO_ERROR,
//
//    /**
//     * 解析错误
//     */
//    PARSE_ERROR,
//
//    /**
//     * 校验错误
//     */
//    VALIDATION_ERROR
//  }
//
//  /**
//   * 获取错误类型
//   */
//  @Getter
//  private final ErrorType errorType;

//  /**
//   * 创建异常实例
//   *
//   * @param message   错误消息
//   * @param errorType 错误类型
//   */
//  public DefinitionLoaderException(String message, ErrorType errorType) {
//    super(message);
//    this.errorType = errorType;
//  }
//
//  /**
//   * 创建异常实例
//   *
//   * @param message   错误消息
//   * @param cause     原始异常
//   * @param errorType 错误类型
//   */
//  public DefinitionLoaderException(String message, Throwable cause, ErrorType errorType) {
//    super(message, cause);
//    this.errorType = errorType;
//  }

  public DefinitionLoaderException(String message, Object... args) {
    super(message, args);
  }
}
