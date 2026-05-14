package cn.labzen.file.exception;

import cn.labzen.meta.exception.LabzenRuntimeException;

/**
 * 文件写入异常
 * <p>
 * 当文件生成过程中发生错误时抛出此异常
 *
 * @author labzen
 */
public class DataWriteException extends LabzenRuntimeException {

  /**
   * 构造器
   *
   * @param message 异常消息
   */
  public DataWriteException(String message) {
    super(message);
  }

  /**
   * 构造器
   *
   * @param cause   原因异常
   * @param message 占位符消息
   * @param args    参数
   */
  public DataWriteException(Throwable cause, String message, Object... args) {
    super(cause, message, args);
  }
}
