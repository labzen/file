package cn.labzen.file.validator;

import cn.labzen.file.annotation.DataValidator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 校验器链执行器
 * <p>
 * 按 priority 顺序执行 Validator，收集所有失败结果。
 * 支持即时执行（IMMEDIATE）和延后执行（DEFERRED）两种模式。
 *
 * @author labzen
 */
@Slf4j
public class ChainableValidatorExecutor {

  private static final Map<String, ValidatorInstance> VALIDATOR_INSTANCES = Maps.newHashMap();

  static {
    ServiceLoader.load(Validator.class).forEach(validator -> {
      Class<?> type = validator.getClass();
      if (!type.isAnnotationPresent(DataValidator.class)) {
        return;
      }
      DataValidator annotation = type.getAnnotation(DataValidator.class);
      VALIDATOR_INSTANCES.put(annotation.name(),
        new ValidatorInstance(annotation.name(), annotation.priority(), annotation.execution(), validator));
    });
  }

  private final List<ConfiguredValidator> immediateValidators = Lists.newArrayList();
  private final List<ConfiguredValidator> deferredValidators = Lists.newArrayList();

  public ChainableValidatorExecutor() {
  }

  public boolean hasValidator() {
    return !immediateValidators.isEmpty() || !deferredValidators.isEmpty();
  }

  public void addValidator(String name, List<Object> arguments) {
    ValidatorInstance instance = VALIDATOR_INSTANCES.get(name);
    if (instance == null) {
      logger.error("不存在的校验器: {}", name);
      return;
    }

    ConfiguredValidator cv = new ConfiguredValidator(instance.priority(), instance.validator(), arguments);
    if (instance.execution() == DataValidator.Execution.IMMEDIATE) {
      immediateValidators.add(cv);
    } else {
      deferredValidators.add(cv);
    }
  }

  /**
   * 排序校验器链
   */
  public void sort() {
    immediateValidators.sort(Comparator.comparingInt(ConfiguredValidator::priority));
    deferredValidators.sort(Comparator.comparingInt(ConfiguredValidator::priority));
  }

  /**
   * 执行即时校验（逐行）
   *
   * @return 所有校验失败的结果，空列表表示全部通过
   */
  public List<ValidateResult> executeImmediate(ValidateContext context) {
    return execute(immediateValidators, context);
//    List<ValidateResult> failures = new ArrayList<>();
//    for (ConfiguredValidator cv : immediateValidators) {
//      ValidateResult result = cv.validator().validate(input, cv.arguments(), context);
//      if (result != null) {
//        failures.add(result);
//      }
//    }
//    return failures;
  }

  /**
   * 执行延后校验（全量完成后）
   *
   * @return 所有校验失败的结果，空列表表示全部通过
   */
  public List<ValidateResult> executeDeferred(ValidateContext context) {
    return execute(deferredValidators, context);
//    List<ValidateResult> failures = new ArrayList<>();
//    for (ConfiguredValidator cv : deferredValidators) {
//      ValidateResult result = cv.validator().validate(input, cv.arguments(), context);
//      if (result != null) {
//        failures.add(result);
//      }
//    }
//    return failures;
  }

  private List<ValidateResult> execute(List<ConfiguredValidator> validators, ValidateContext context) {
    List<ValidateResult> failures = new ArrayList<>();
    for (ConfiguredValidator cv : validators) {
      ValidateResult result = cv.validator().validate(context, cv.arguments());
      if (result != null) {
        failures.add(result);
      }
    }
    return failures;
  }

//  public boolean hasDeferredValidators() {
//    return !deferredValidators.isEmpty();
//  }
//
//  public static Set<String> availableValidatorNames() {
//    return VALIDATOR_INSTANCES.keySet();
//  }

  record ValidatorInstance(String name, int priority, DataValidator.Execution execution, Validator validator) {
  }

  record ConfiguredValidator(int priority, Validator validator, List<Object> arguments) {
  }
}
