package cn.labzen.file.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 转换器注解
 * <p>
 * 用于标识一个转换器类，并定义转换器的名称和执行优先级
 *
 * @author labzen
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DataConverter {

  /**
   * 转换器名称
   */
  String name();

  /**
   * 转换器执行优先级，数字越小优先级越高
   */
  int priority();
}
