package cn.labzen.file.validator.impl;

import cn.labzen.file.annotation.DataValidator;
import cn.labzen.file.format.core.reader.process.ProceedRow;
import cn.labzen.file.validator.ValidateContext;
import cn.labzen.file.validator.ValidateResult;
import cn.labzen.file.validator.Validator;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;

import static cn.labzen.file.locale.LocaleKeys.IMPORT_VALIDATE_UNIQUE;
import static cn.labzen.file.validator.Validator.UNIQUE_NAME;
import static cn.labzen.file.validator.Validator.UNIQUE_PRIORITY;

/**
 * 唯一性校验器（延后执行）
 * <p>
 * arguments[0] = 该列所有已收集的值列表
 * <p>
 * 此校验器在所有行处理完成后执行，检查批次内是否存在重复值
 *
 * @author labzen
 */
@DataValidator(name = UNIQUE_NAME, priority = UNIQUE_PRIORITY, execution = DataValidator.Execution.DEFERRED)
public class UniqueValidator implements Validator {

  @Override
  public ValidateResult validate(@NonNull ValidateContext<?> context, @NonNull List<Object> arguments) {
    Object input = context.value();
    if (input == null) {
      return ValidateResult.ok();
    }

    // arguments中应包含所有行的该列原始值，由 ImportProcessor 在延后校验时提供
    // 此处仅校验当前值在rawRowData集合中的出现次数
    String fieldName = context.fieldName();
    boolean found = false;
    for (ProceedRow<?> row : context.proceedRows()) {
      if (Objects.equals(input, row.getData().get(fieldName))) {
        found = true;
        break;
      }
    }

    if (found) {
      return ValidateResult.fail(IMPORT_VALIDATE_UNIQUE);
    }

    return ValidateResult.ok();
  }
}
