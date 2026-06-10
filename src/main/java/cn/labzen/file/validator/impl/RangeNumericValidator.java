package cn.labzen.file.validator.impl;

import cn.labzen.file.annotation.DataValidator;
import cn.labzen.file.validator.ValidateContext;
import cn.labzen.file.validator.ValidateResult;
import cn.labzen.file.validator.Validator;
import cn.labzen.tool.util.Collections;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.util.List;

import static cn.labzen.file.locale.LocaleKeys.*;
import static cn.labzen.file.validator.Validator.RANGE_NUMERIC_NAME;
import static cn.labzen.file.validator.Validator.RANGE_NUMERIC_PRIORITY;

/**
 * 范围校验器（后校验，转换后执行）
 * <p>
 * arguments[0] = min值, arguments[1] = max值
 * 支持数值类型的范围校验
 *
 * @author labzen
 */
@DataValidator(name = RANGE_NUMERIC_NAME, priority = RANGE_NUMERIC_PRIORITY, execution = DataValidator.Execution.DEFERRED)
public class RangeNumericValidator implements Validator {

  @Override
  public ValidateResult validate(@NonNull ValidateContext<?> context, @NonNull List<Object> arguments) {
    if (context.value() == null) {
      return ValidateResult.ok();
    }

    Number number;
    if (context.value() instanceof Number num) {
      number = num;
    } else {
      return ValidateResult.fail(IMPORT_VALIDATE_NUMERIC_WRONG_TYPE, context.value());
    }

    BigDecimal min = null, max = null;
    Object minObj = Collections.safeGet(arguments, 0, null);
    if (minObj instanceof Number mumMin) {
      min = toBigDecimal(mumMin);
    }
    Object maxObj = Collections.safeGet(arguments, 1, null);
    if (maxObj instanceof Number mumMax) {
      max = toBigDecimal(mumMax);
    }

    BigDecimal decimalValue = toBigDecimal(number);

    if (min != null && decimalValue.compareTo(min) < 0) {
      return ValidateResult.fail(IMPORT_VALIDATE_NUMERIC_MIN, min, max);
    }

    if (max != null && decimalValue.compareTo(max) > 0) {
      return ValidateResult.fail(IMPORT_VALIDATE_NUMERIC_MAX, min, max);
    }

    return ValidateResult.ok();
  }

  private BigDecimal toBigDecimal(Number value) {
    if (value instanceof BigDecimal bd) return bd;
    return new BigDecimal(value.toString());
  }
}
