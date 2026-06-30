package cn.labzen.file.format.core.reader.process;

import java.util.List;
import java.util.Map;

/**
 * 处理过的行数据
 * <p>
 * 表示一行数据在经过 清理→校验→转换→校验→Bean构建 全流程处理后的最终结果。
 * 本类不包含任何处理逻辑，所有加工过程由 {@link ImportProcessor} 负责。
 *
 * @param <T> domain bean 类型
 */
public record ProceedRow<T>(String sequence, Map<String, Object> data, List<FieldError> errors,
                            T instance) {

  public boolean success() {
    return errors.isEmpty();
  }

  public ImportFailure toFailure() {
    if (success()) {
      return null;
    }

    return new ImportFailure(sequence, data, errors);
  }
}
