package cn.labzen.file.validator.impl;

import cn.labzen.file.annotation.DataValidator;
import cn.labzen.file.validator.ValidateContext;
import cn.labzen.file.validator.ValidateResult;
import cn.labzen.file.validator.Validator;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 正则格式校验器
 * <p>
 * arguments[0] = 正则表达式字符串
 *
 * @author labzen
 */
@DataValidator(name = "pattern", priority = 20, execution = DataValidator.Execution.IMMEDIATE)
public class PatternValidator implements Validator {

  @Override
  public ValidateResult validate(Object input, List<Object> arguments, ValidateContext context) {
    if (input == null || arguments.isEmpty()) {
      return ValidateResult.ok();
    }

    String value = input.toString();
    if (value.isBlank()) {
      return ValidateResult.ok();
    }

    String regex = arguments.getFirst().toString();
    if (!Pattern.matches(regex, value)) {
      return ValidateResult.fail("import.validate.pattern",
        context.headerText() + "格式不正确",
        context.headerText());
    }

    return ValidateResult.ok();
  }
}
