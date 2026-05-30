package cn.labzen.file.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 清理器注解
 * <p>
 * 用于标识一个清理器类，并定义清理器的名称和执行优先级。
 * 清理器在导入管线中最先执行，用于对原始字符串进行归一化处理。
 *
 * @author labzen
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DataCleanser {

  /**
   * 清理器名称
   */
  String name();

  /**
   * 清理器执行优先级，数字越小优先级越高
   */
  int priority();
}
