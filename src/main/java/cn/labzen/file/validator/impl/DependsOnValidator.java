package cn.labzen.file.validator.impl;

import cn.labzen.file.annotation.DataValidator;
import cn.labzen.file.validator.ValidateContext;
import cn.labzen.file.validator.ValidateResult;
import cn.labzen.file.validator.Validator;
import cn.labzen.tool.util.Strings;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static cn.labzen.file.locale.LocaleKeys.IMPORT_VALIDATE_DEPENDS_ON;
import static cn.labzen.file.validator.Validator.DEPENDS_ON_NAME;
import static cn.labzen.file.validator.Validator.DEPENDS_ON_PRIORITY;

/**
 * 依赖校验器
 * <p>
 * 当指定字段有值时，当前字段必填
 * arguments[0..n] = 依赖的字段名列表
 *
 * @author labzen
 */
@DataValidator(name = DEPENDS_ON_NAME, priority = DEPENDS_ON_PRIORITY, execution = DataValidator.Execution.IMMEDIATE)
public class DependsOnValidator implements Validator {

  @Override
  public ValidateResult validate(@NonNull ValidateContext<?> context, @NonNull List<Object> arguments) {
    if (arguments.isEmpty()) {
      return ValidateResult.ok();
    }

    // 检查依赖字段，任意一个是否有值
    boolean anyDependentHasValue = false;
    for (Object arg : arguments) {
      String dependentFieldName = Strings.value(arg, "");
      if (Strings.isBlank(dependentFieldName)) {
        continue;
      }

      Object dependentValue = context.rawRowData().get(dependentFieldName);
      String dependentString = Strings.value(dependentValue, "");
      anyDependentHasValue = Strings.isNotBlank(dependentString);
      if (anyDependentHasValue) break;
    }

    if (!anyDependentHasValue) {
      // 依赖字段都没有值，跳过校验
      return ValidateResult.ok();
    }

    // 依赖字段有值，检查当前字段是否有值
    String input = Strings.value(context.value(), "");
    if (Strings.isBlank(input)) {
      return ValidateResult.fail(IMPORT_VALIDATE_DEPENDS_ON,
//        "当" + String.join(",", arguments.stream().map(Object::toString).toList()) + "有值时，" + context.headerText() + "不能为空", context.headerText(),
        arguments.stream().map(Object::toString).toList());
    }

    return ValidateResult.ok();
  }
}
