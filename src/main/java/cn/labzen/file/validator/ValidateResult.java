package cn.labzen.file.validator;

import lombok.Getter;

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

  private ValidateResult(String errorI18nCode, Object[] errorArgs) {
    this.errorI18nCode = errorI18nCode;
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
}
