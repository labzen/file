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

import static cn.labzen.file.locale.LocaleKeys.*;
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
}
