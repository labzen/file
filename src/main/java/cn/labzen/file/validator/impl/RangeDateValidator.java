package cn.labzen.file.validator.impl;

import cn.labzen.file.annotation.DataValidator;
import cn.labzen.file.validator.ValidateContext;
import cn.labzen.file.validator.ValidateResult;
import cn.labzen.file.validator.Validator;
import cn.labzen.tool.util.Collections;
import cn.labzen.tool.util.DateTimes;
import org.jspecify.annotations.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import static cn.labzen.file.i18n.internal.Internal18nKeys.*;
import static cn.labzen.file.validator.Validator.RANGE_DATE_NAME;
import static cn.labzen.file.validator.Validator.RANGE_DATE_PRIORITY;

/**
 * 范围校验器（后校验，转换后执行）
 * <p>
 * arguments[0] = min值, arguments[1] = max值
 * 支持日期类型的范围校验
 *
 * @author labzen
 */
@DataValidator(name = RANGE_DATE_NAME, priority = RANGE_DATE_PRIORITY, execution = DataValidator.Execution.DEFERRED)
public class RangeDateValidator implements Validator {

  @Override
  public ValidateResult validate(@NonNull ValidateContext<?> context, @NonNull List<Object> arguments) {
    if (context.value() == null) {
      return ValidateResult.ok();
    }

    LocalDateTime input, min = null, max = null;
    Object minObj = Collections.safeGet(arguments, 0, null);
    if (minObj instanceof LocalDateTime minLdt) {
      min = minLdt;
    }
    Object maxObj = Collections.safeGet(arguments, 1, null);
    if (maxObj instanceof LocalDateTime maxLdt) {
      max = maxLdt;
    }

    input = switch (context.value()) {
      case java.sql.Date sqlDate -> sqlDate.toLocalDate().atStartOfDay();
      case java.sql.Time sqlTime -> DateTimes.toLocalDateTime(sqlTime);
      case java.sql.Timestamp sqlTimestamp -> DateTimes.toLocalDateTime(sqlTimestamp);
      case Date date -> DateTimes.toLocalDateTime(date);
      case LocalDateTime ldt -> ldt;
      case LocalDate ld -> ld.atStartOfDay();
      case LocalTime lt -> {
        if (min != null) min = LocalDateTime.of(LocalDate.now(), lt);
        if (max != null) max = LocalDateTime.of(LocalDate.now(), lt);
        yield LocalDateTime.of(LocalDate.now(), lt);
      }
      default -> null;
    };

    if (input == null) {
      return ValidateResult.fail(IMPORT_VALIDATE_DATE_WRONG_TYPE, context.value());
    }

    if (min != null && min.isAfter(input)) {
      return ValidateResult.fail(IMPORT_VALIDATE_DATE_MIN, min, max);
    }
    if (max != null && max.isBefore(input)) {
      return ValidateResult.fail(IMPORT_VALIDATE_DATE_MAX, min, max);
    }

    return ValidateResult.ok();
  }

//  private ValidateResult validateNumberRange(Number value, String minStr, String maxStr, ValidateContext context) {
//    BigDecimal decimalValue = toBigDecimal(value);
//
//    if (minStr != null && !minStr.isBlank()) {
//      BigDecimal min = new BigDecimal(minStr);
//      if (decimalValue.compareTo(min) < 0) {
//        return ValidateResult.fail("import.validate.range",
//          context.headerText() + "必须在" + minStr + "到" + maxStr + "之间",
//          context.headerText(), minStr, maxStr);
//      }
//    }
//
//    if (maxStr != null && !maxStr.isBlank()) {
//      BigDecimal max = new BigDecimal(maxStr);
//      if (decimalValue.compareTo(max) > 0) {
//        return ValidateResult.fail("import.validate.range",
//          context.headerText() + "必须在" + minStr + "到" + maxStr + "之间",
//          context.headerText(), minStr, maxStr);
//      }
//    }
//
//    return ValidateResult.ok();
//  }

//  private ValidateResult validateComparableRange(Comparable<Object> value, String minStr, String maxStr, ValidateContext context) {
//    if (minStr != null && !minStr.isBlank()) {
//      if (value.compareTo((Object) minStr) < 0) {
//        return ValidateResult.fail("import.validate.range",
//          context.headerText() + "必须在" + minStr + "到" + maxStr + "之间",
//          context.headerText(), minStr, maxStr);
//      }
//    }
//
//    if (maxStr != null && !maxStr.isBlank()) {
//      if (value.compareTo((Object) maxStr) > 0) {
//        return ValidateResult.fail("import.validate.range",
//          context.headerText() + "必须在" + minStr + "到" + maxStr + "之间",
//          context.headerText(), minStr, maxStr);
//      }
//    }
//
//    return ValidateResult.ok();
//  }

//  private BigDecimal toBigDecimal(Number value) {
//    if (value instanceof BigDecimal bd) return bd;
//    return new BigDecimal(value.toString());
//  }
}
