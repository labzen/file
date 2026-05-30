package cn.labzen.file.exception;

import cn.labzen.meta.exception.LabzenRuntimeException;

/**
 * 文件读取异常
 * <p>
 * 当文件生成过程中发生错误时抛出此异常
 *
 * @author labzen
 */
public class DataReadException extends LabzenRuntimeException {

  /**
   * 构造器
   *
   * @param message 异常消息
   */
  public DataReadException(String message) {
    super(message);
  }

  /**
   * 构造器
   *
   * @param message 占位符消息
   * @param args    参数
   */
  public DataReadException(String message, Object... args) {
    super(message, args);
  }

  /**
   * 构造器
   *
   * @param cause   原因异常
   * @param message 占位符消息
   * @param args    参数
   */
  public DataReadException(Throwable cause, String message, Object... args) {
    super(cause, message, args);
  }
}
