package cn.labzen.file.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 校验器注解
 * <p>
 * 用于标识一个校验器类，并定义校验器的名称、执行优先级和执行时机。
 * 校验器在导入管线中分为即时执行和延后执行两种模式。
 *
 * @author labzen
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DataValidator {

  /**
   * 校验器名称
   */
  String name();

  /**
   * 校验器执行优先级，数字越小优先级越高
   */
  int priority();

  /**
   * 校验器执行时机
   */
  Execution execution() default Execution.IMMEDIATE;

  /**
   * 校验器执行时机枚举
   */
  enum Execution {
    /**
     * 即时执行 — 在校验阶段逐行执行
     */
    IMMEDIATE,

    /**
     * 延后执行 — 在所有行处理完成后执行（如唯一性校验）
     */
    DEFERRED
  }
}
