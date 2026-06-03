package cn.labzen.file.validator.impl;

import cn.labzen.file.annotation.DataValidator;
import cn.labzen.file.validator.ValidateContext;
import cn.labzen.file.validator.ValidateResult;
import cn.labzen.file.validator.Validator;

import java.util.List;

/**
 * 长度校验器
 * <p>
 * 校验字符串长度范围。arguments[0] = minLength, arguments[1] = maxLength
 *
 * @author labzen
 */
@DataValidator(name = "length", priority = 10, execution = DataValidator.Execution.IMMEDIATE)
public class LengthValidator implements Validator {

  @Override
  public ValidateResult validate(Object input, List<Object> arguments, ValidateContext context) {
    if (input == null) {
      return ValidateResult.ok();
    }

    String value = input.toString();
    int len = value.length();

    Integer minLength = !arguments.isEmpty() ? toInt(arguments.get(0)) : null;
    Integer maxLength = arguments.size() > 1 ? toInt(arguments.get(1)) : null;

    if (minLength != null && len < minLength) {
      return ValidateResult.fail("import.validate.min-length",
        context.headerText() + "长度不能少于" + minLength + "个字符",
        context.headerText(), minLength);
    }
    if (maxLength != null && len > maxLength) {
      return ValidateResult.fail("import.validate.max-length",
        context.headerText() + "长度不能超过" + maxLength + "个字符",
        context.headerText(), maxLength);
    }

    return ValidateResult.ok();
  }

  private Integer toInt(Object obj) {
    if (obj == null) return null;
    if (obj instanceof Number n) return n.intValue();
    try {
      return Integer.parseInt(obj.toString());
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
