package cn.labzen.file.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 转换器注解
 * <p>
 * 标识一个转换器类，定义转换器的名称和导入/导出两个方向的执行优先级。
 * 数字越小优先级越高，在转换链中越先执行。
 *
 * <pre>{@code
 * // 简单场景：导入导出优先级一致
 * @DataConverter(name = "bool", exportPriority = 400, importPriority = 400)
 *
 * // 方向差异场景：mapping 在导入时优先于 enum，导出时反之
 * @DataConverter(name = "innate#mapping", exportPriority = 320, importPriority = 300)
 * }</pre>
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
   * 导出方向的执行优先级，数字越小优先级越高（越先执行）
   */
  int exportPriority();

  /**
   * 导入方向的执行优先级，数字越小优先级越高（越先执行）
   */
  int importPriority();
}
