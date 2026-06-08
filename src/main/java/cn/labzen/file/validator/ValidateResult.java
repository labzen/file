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

  private final String errorI18nCode;
  private final Object[] errorArgs;
//  private final String defaultMessage;

  private ValidateResult(String errorI18nCode, Object[] errorArgs) {
    this.errorI18nCode = errorI18nCode;
//    this.defaultMessage = defaultMessage;
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
   * @param errorCode      国际化key
   * @param defaultMessage 默认错误信息
   * @param errorArgs      国际化参数
   */
  public static ValidateResult fail(String errorCode, Object... errorArgs) {
    return new ValidateResult(errorCode, errorArgs);
  }

//  @Override
//  public String toString() {
//    return "ValidateResult{errorCode='" + errorI18nCode + "', args=" + Arrays.toString(errorArgs) + "}";
//  }
}
