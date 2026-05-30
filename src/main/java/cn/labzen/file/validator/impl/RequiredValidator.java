package cn.labzen.file.validator.impl;

import cn.labzen.file.annotation.DataValidator;
import cn.labzen.file.validator.ValidateContext;
import cn.labzen.file.validator.ValidateResult;
import cn.labzen.file.validator.Validator;

import java.util.List;

/**
 * 必填校验器
 * <p>
 * null、空字符串、纯空白均视为未填
 *
 * @author labzen
 */
@DataValidator(name = "required", priority = 0, execution = DataValidator.Execution.IMMEDIATE)
public class RequiredValidator implements Validator {

  @Override
  public ValidateResult validate(Object input, List<Object> arguments, ValidateContext context) {
    if (input == null) {
      return ValidateResult.fail("import.validate.required",
        context.headerText() + "不能为空", context.headerText());
    }
    if (input instanceof String s && s.isBlank()) {
      return ValidateResult.fail("import.validate.required",
        context.headerText() + "不能为空", context.headerText());
    }
    return ValidateResult.ok();
  }
}
