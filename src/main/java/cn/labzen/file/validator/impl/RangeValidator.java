package cn.labzen.file.validator.impl;

import cn.labzen.file.annotation.DataValidator;
import cn.labzen.file.validator.ValidateContext;
import cn.labzen.file.validator.ValidateResult;
import cn.labzen.file.validator.Validator;

import java.math.BigDecimal;
import java.util.List;

/**
 * 范围校验器（后校验，转换后执行）
 * <p>
 * arguments[0] = min值, arguments[1] = max值
 * 支持数值和日期类型的范围校验
 *
 * @author labzen
 */
@DataValidator(name = "range", priority = 100, execution = DataValidator.Execution.IMMEDIATE)
public class RangeValidator implements Validator {

  @Override
  public ValidateResult validate(Object input, List<Object> arguments, ValidateContext context) {
    if (input == null) {
      return ValidateResult.ok();
    }

    if (arguments.size() < 2) {
      return ValidateResult.ok();
    }

    String minStr = arguments.get(0) != null ? arguments.get(0).toString() : null;
    String maxStr = arguments.get(1) != null ? arguments.get(1).toString() : null;

    if (input instanceof Number) {
      return validateNumberRange((Number) input, minStr, maxStr, context);
    }

    if (input instanceof Comparable<?>) {
      //noinspection unchecked
      return validateComparableRange((Comparable<Object>) input, minStr, maxStr, context);
    }

    return ValidateResult.ok();
  }

  private ValidateResult validateNumberRange(Number value, String minStr, String maxStr, ValidateContext context) {
    BigDecimal decimalValue = toBigDecimal(value);

    if (minStr != null && !minStr.isBlank()) {
      BigDecimal min = new BigDecimal(minStr);
      if (decimalValue.compareTo(min) < 0) {
        return ValidateResult.fail("import.validate.range",
          context.headerText() + "必须在" + minStr + "到" + maxStr + "之间",
          context.headerText(), minStr, maxStr);
      }
    }

    if (maxStr != null && !maxStr.isBlank()) {
      BigDecimal max = new BigDecimal(maxStr);
      if (decimalValue.compareTo(max) > 0) {
        return ValidateResult.fail("import.validate.range",
          context.headerText() + "必须在" + minStr + "到" + maxStr + "之间",
          context.headerText(), minStr, maxStr);
      }
    }

    return ValidateResult.ok();
  }

  private ValidateResult validateComparableRange(Comparable<Object> value, String minStr, String maxStr, ValidateContext context) {
    if (minStr != null && !minStr.isBlank()) {
      if (value.compareTo((Object) minStr) < 0) {
        return ValidateResult.fail("import.validate.range",
          context.headerText() + "必须在" + minStr + "到" + maxStr + "之间",
          context.headerText(), minStr, maxStr);
      }
    }

    if (maxStr != null && !maxStr.isBlank()) {
      if (value.compareTo((Object) maxStr) > 0) {
        return ValidateResult.fail("import.validate.range",
          context.headerText() + "必须在" + minStr + "到" + maxStr + "之间",
          context.headerText(), minStr, maxStr);
      }
    }

    return ValidateResult.ok();
  }

  private BigDecimal toBigDecimal(Number value) {
    if (value instanceof BigDecimal bd) return bd;
    return new BigDecimal(value.toString());
  }
}
