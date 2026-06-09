package cn.labzen.file.validator.impl;

import cn.labzen.file.annotation.DataValidator;
import cn.labzen.file.validator.ValidateContext;
import cn.labzen.file.validator.ValidateResult;
import cn.labzen.file.validator.Validator;
import cn.labzen.tool.util.Collections;
import cn.labzen.tool.util.Strings;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static cn.labzen.file.locale.LocaleKeys.IMPORT_VALIDATE_LENGTH_MAX;
import static cn.labzen.file.locale.LocaleKeys.IMPORT_VALIDATE_LENGTH_MIN;
import static cn.labzen.file.validator.Validator.RANGE_LENGTH_NAME;
import static cn.labzen.file.validator.Validator.RANGE_LENGTH_PRIORITY;

/**
 * 长度校验器
 * <p>
 * 校验字符串长度范围。arguments[0] = minLength, arguments[1] = maxLength
 *
 * @author labzen
 */
@DataValidator(name = RANGE_LENGTH_NAME, priority = RANGE_LENGTH_PRIORITY, execution = DataValidator.Execution.IMMEDIATE)
public class RangeLengthValidator implements Validator {

  @Override
  public ValidateResult validate(@NonNull ValidateContext<?> context, @NonNull List<Object> arguments) {
    String input = Strings.value(context.value(), "");
    if (Strings.isBlank(input)) {
      return ValidateResult.ok();
    }

    Integer min = null, max = null;
    Object minObj = Collections.safeGet(arguments, 0, null);
    if (minObj instanceof Number mumMin) {
      min = toInt(mumMin);
    }
    Object maxObj = Collections.safeGet(arguments, 1, null);
    if (maxObj instanceof Number mumMax) {
      max = toInt(mumMax);
    }

    int len = input.length();
    if (min != null && len < min) {
      return ValidateResult.fail(IMPORT_VALIDATE_LENGTH_MIN,
//        context.headerText() + "长度不能少于" + minLength + "个字符", context.headerText(),
        min);
    }
    if (max != null && len > max) {
      return ValidateResult.fail(IMPORT_VALIDATE_LENGTH_MAX,
//        context.headerText() + "长度不能超过" + maxLength + "个字符", context.headerText(),
        max);
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
