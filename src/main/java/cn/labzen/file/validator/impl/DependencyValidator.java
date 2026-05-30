package cn.labzen.file.validator.impl;

import cn.labzen.file.annotation.DataValidator;
import cn.labzen.file.validator.ValidateContext;
import cn.labzen.file.validator.ValidateResult;
import cn.labzen.file.validator.Validator;

import java.util.List;

/**
 * 依赖校验器
 * <p>
 * 当指定字段有值时，当前字段必填
 * arguments[0..n] = 依赖的字段名列表
 *
 * @author labzen
 */
@DataValidator(name = "dependency", priority = 40, execution = DataValidator.Execution.IMMEDIATE)
public class DependencyValidator implements Validator {

  @Override
  public ValidateResult validate(Object input, List<Object> arguments, ValidateContext context) {
    if (arguments.isEmpty()) {
      return ValidateResult.ok();
    }

    // 检查依赖字段是否有值
    boolean anyDependentHasValue = false;
    for (Object arg : arguments) {
      String dependentFieldName = arg.toString();
      Object dependentValue = context.currentRowData().get(dependentFieldName);
      if (dependentValue != null && !(dependentValue instanceof String s && s.isBlank())) {
        anyDependentHasValue = true;
        break;
      }
    }

    if (!anyDependentHasValue) {
      return ValidateResult.ok();
    }

    // 依赖字段有值，检查当前字段是否有值
    if (input == null || (input instanceof String s && s.isBlank())) {
      return ValidateResult.fail("import.validate.depends-on",
        "当" + String.join(",", arguments.stream().map(Object::toString).toList()) + "有值时，" + context.headerText() + "不能为空",
        context.headerText(), arguments.stream().map(Object::toString).toList());
    }

    return ValidateResult.ok();
  }
}
