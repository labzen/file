package cn.labzen.file.validator.impl;

import cn.labzen.file.annotation.DataValidator;
import cn.labzen.file.validator.ValidateContext;
import cn.labzen.file.validator.ValidateResult;
import cn.labzen.file.validator.Validator;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static cn.labzen.file.locale.LocaleKeys.IMPORT_VALIDATE_REQUIRE;
import static cn.labzen.file.validator.Validator.REQUIRE_NAME;
import static cn.labzen.file.validator.Validator.REQUIRE_PRIORITY;

/**
 * 必填校验器
 * <p>
 * null、空字符串、纯空白均视为未填
 *
 * @author labzen
 */
@DataValidator(name = REQUIRE_NAME, priority = REQUIRE_PRIORITY, execution = DataValidator.Execution.DEFERRED)
public class RequiredValidator implements Validator {

  @Override
  public ValidateResult validate(@NonNull ValidateContext<?> context, @NonNull List<Object> arguments) {
    if (context.value() == null) {
      return ValidateResult.fail(IMPORT_VALIDATE_REQUIRE);
    }
    if (context.value() instanceof String str && str.isBlank()) {
      return ValidateResult.fail(IMPORT_VALIDATE_REQUIRE);
    }
    return ValidateResult.ok();
  }
}
