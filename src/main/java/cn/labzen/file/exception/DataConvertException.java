package cn.labzen.file.exception;

import cn.labzen.meta.exception.LabzenRuntimeException;

/**
 * 转换器异常类
 * <p>
 * 当数据转换过程中发生错误时抛出此异常
 *
 * @author labzen
 */
public class DataConvertException extends LabzenRuntimeException {

  /**
   * 构造器
   *
   * @param message 异常消息
   */
  public DataConvertException(String message) {
    super(message);
  }


  /**
   * 构造器
   *
   * @param message 占位符消息
   * @param args    参数
   */
  public DataConvertException(String message, Object... args) {
    super(message, args);
  }

  /**
   * 构造器
   *
   * @param cause   原因异常
   * @param message 占位符消息
   * @param args    参数
   */
  public DataConvertException(Throwable cause, String message, Object... args) {
    super(cause, message, args);
  }
}
