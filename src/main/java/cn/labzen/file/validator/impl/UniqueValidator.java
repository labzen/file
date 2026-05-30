package cn.labzen.file.validator.impl;

import cn.labzen.file.annotation.DataValidator;
import cn.labzen.file.validator.ValidateContext;
import cn.labzen.file.validator.ValidateResult;
import cn.labzen.file.validator.Validator;

import java.util.List;

/**
 * 唯一性校验器（延后执行）
 * <p>
 * arguments[0] = 该列所有已收集的值列表
 * <p>
 * 此校验器在所有行处理完成后执行，检查批次内是否存在重复值
 *
 * @author labzen
 */
@DataValidator(name = "unique", priority = 30, execution = DataValidator.Execution.DEFERRED)
public class UniqueValidator implements Validator {

  @Override
  public ValidateResult validate(Object input, List<Object> arguments, ValidateContext context) {
    if (input == null) {
      return ValidateResult.ok();
    }

    String value = input.toString();
    // arguments中应包含所有行的该列原始值，由 ImportPipeline 在延后校验时提供
    // 此处仅校验当前值在rawRowData集合中的出现次数
    long count = context.rawRowData().values().stream()
      .filter(v -> v != null && v.equals(value))
      .count();

    if (count > 1) {
      return ValidateResult.fail("import.validate.unique",
        context.headerText() + "在导入数据中存在重复",
        context.headerText());
    }

    return ValidateResult.ok();
  }
}
