package cn.labzen.file.validator;

import lombok.Getter;

import java.util.Arrays;

/**
 * 校验结果
 * <p>
 * null 表示校验通过，非null表示校验失败。
 *
 * @author labzen
 */
@Getter
public class ValidateResult {

  private final String errorCode;
  private final Object[] errorArgs;
  private final String defaultMessage;

  private ValidateResult(String errorCode, String defaultMessage, Object[] errorArgs) {
    this.errorCode = errorCode;
    this.defaultMessage = defaultMessage;
    this.errorArgs = errorArgs;
  }

  /**
   * 校验通过
   */
  public static ValidateResult ok() {
    return null;
  }

  /**
   * 校验失败
   *
   * @param errorCode     国际化key
   * @param defaultMessage 默认错误信息
   * @param errorArgs      国际化参数
   */
  public static ValidateResult fail(String errorCode, String defaultMessage, Object... errorArgs) {
    return new ValidateResult(errorCode, defaultMessage, errorArgs);
  }

  @Override
  public String toString() {
    return "ValidateResult{errorCode='" + errorCode + "', args=" + Arrays.toString(errorArgs) + "}";
  }
}
