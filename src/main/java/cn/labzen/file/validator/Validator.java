package cn.labzen.file.validator;

import java.util.List;

/**
 * 校验器接口
 * <p>
 * 在导入管线中执行校验。校验器分为即时执行（逐行）和延后执行（全量完成后）两种模式。
 *
 * @author labzen
 */
public interface Validator {

  /**
   * 执行校验
   *
   * @param input     待校验的值
   * @param arguments 校验参数（来自YAML配置）
   * @param context   校验上下文
   * @return 校验结果，null表示通过
   */
  ValidateResult validate(Object input, List<Object> arguments, ValidateContext context);
}
