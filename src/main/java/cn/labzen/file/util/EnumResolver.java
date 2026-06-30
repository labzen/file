package cn.labzen.file.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 通用枚举解析工具类
 *
 * <p>弥补 Java 标准库中枚举操作的不足，提供忽略大小写的查找、按属性反查等常用方法。
 * 开发者在使用 labzen-file 时可直接引用本类，无需自行编写枚举解析逻辑。
 *
 * <h3>典型用法</h3>
 * <pre>{@code
 * // 忽略大小写的 valueOf
 * StatusEnum s = EnumResolver.valueOfIgnoreCase(StatusEnum.class, "active");
 *
 * // 按 getter 反查（属性值 → 枚举常量）
 * Optional<StatusEnum> s = EnumResolver.findByGetter(StatusEnum.class, "getCode", "A");
 *
 * // 通过静态方法（如 fromCode）查找
 * Optional<StatusEnum> s = EnumResolver.fromMethod(StatusEnum.class, "fromCode", "A");
 * }</pre>
 *
 * @author labzen
 */
public final class EnumResolver {

  private EnumResolver() {
  }

  // ==================== valueOfIgnoreCase ====================

  /**
   * 忽略大小写的枚举查找。
   * <p>
   * 先尝试精确大写匹配，再尝试忽略大小写匹配。
   *
   * @param <E>      枚举类型
   * @param enumType 枚举类
   * @param name     枚举名称（不区分大小写）
   * @return 匹配的枚举常量
   * @throws IllegalArgumentException 未找到匹配的枚举常量
   */
  @SuppressWarnings("unchecked")
  public static <E extends Enum<E>> E valueOfIgnoreCase(Class<? extends Enum<?>> enumType, String name) {
    // 优先精确大写匹配（与标准 valueOf 一致）
    String upper = name.toUpperCase();
    for (Enum<?> constant : enumType.getEnumConstants()) {
      if (constant.name().equals(upper)) {
        return (E) constant;
      }
    }
    // 其次忽略大小写
    for (Enum<?> constant : enumType.getEnumConstants()) {
      if (constant.name().equalsIgnoreCase(name)) {
        return (E) constant;
      }
    }
    throw new IllegalArgumentException(
      "枚举[" + enumType.getName() + "]中不存在名称为[" + name + "]的常量，可用值：" + availableNames(enumType));
  }

  /**
   * 忽略大小写的枚举查找，返回 Optional 而非抛异常。
   */
  public static <E extends Enum<E>> Optional<E> valueOfIgnoreCaseOptional(Class<? extends Enum<?>> enumType, String name) {
    try {
      return Optional.of(valueOfIgnoreCase(enumType, name));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  // ==================== findByGetter ====================

  /**
   * 通过枚举实例的 getter 方法返回值反查枚举常量。
   * <p>
   * 适用于枚举定义了 {@code getCode()}/{@code getValue()} 等方法，且需要根据值反查枚举的场景。
   *
   * <pre>{@code
   * // 枚举定义：
   * enum StatusEnum {
   *     ACTIVE("A"), INACTIVE("I");
   *     String code;
   *     public String getCode() { return code; }
   * }
   *
   * // 按 code 反查：
   * Optional<StatusEnum> s = EnumResolver.findByGetter(StatusEnum.class, "getCode", "A");
   * // → Optional[ACTIVE]
   * }</pre>
   *
   * @param enumType   枚举类
   * @param getterName getter 方法名，如 "getCode"
   * @param value      要匹配的属性值
   * @return 匹配的枚举常量，未找到返回 Optional.empty()
   * @throws IllegalArgumentException getter 方法不存在或调用失败
   */
  public static <E extends Enum<E>> Optional<E> findByGetter(Class<E> enumType, String getterName, Object value) {
    Method getter;
    try {
      getter = enumType.getMethod(getterName);
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(
        "枚举[" + enumType.getName() + "]中不存在方法[" + getterName + "()]", e);
    }

    for (E constant : enumType.getEnumConstants()) {
      try {
        Object fieldValue = getter.invoke(constant);
        if (value == null ? fieldValue == null : value.equals(fieldValue)) {
          return Optional.of(constant);
        }
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new IllegalArgumentException(
          "调用枚举[" + enumType.getName() + "]的方法[" + getterName + "()]失败", e);
      }
    }
    return Optional.empty();
  }

  // ==================== fromMethod ====================

  /**
   * 通过枚举自身定义的静态工厂方法查找。
   * <p>
   * 适用于枚举定义了 {@code fromCode(String)} 等静态工厂方法的场景。
   * 方法返回值支持 {@link Optional}，会自动解包。
   *
   * <pre>{@code
   * // 枚举定义：
   * enum PriorityEnum {
   *     HIGH, MEDIUM, LOW;
   *     public static Optional<PriorityEnum> fromCode(String code) { ... }
   * }
   *
   * // 调用：
   * Optional<PriorityEnum> p = EnumResolver.fromMethod(PriorityEnum.class, "fromCode", "H");
   * }</pre>
   *
   * @param enumType   枚举类
   * @param methodName 静态方法名
   * @param value      要传入方法的字符串值
   * @return 方法返回值（自动解包 Optional），若方法返回 null/empty 则为 Optional.empty()
   * @throws IllegalArgumentException 方法不存在、调用失败或返回值类型不匹配
   */
  @SuppressWarnings("unchecked")
  public static <E extends Enum<E>> Optional<E> fromMethod(Class<E> enumType, String methodName, String value) {
    Method method;
    try {
      method = enumType.getMethod(methodName, String.class);
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(
        "枚举[" + enumType.getName() + "]中不存在静态方法[" + methodName + "(String)]", e);
    }

    Object rawResult;
    try {
      rawResult = method.invoke(null, value);
    } catch (InvocationTargetException e) {
      throw new IllegalArgumentException(
        "调用枚举[" + enumType.getName() + "]的静态方法[" + methodName + "(\"" + value + "\")]失败: "
          + e.getCause().getMessage(), e);
    } catch (Exception e) {
      throw new IllegalArgumentException(
        "调用枚举[" + enumType.getName() + "]的静态方法[" + methodName + "(\"" + value + "\")]异常", e);
    }

    // 解包 Optional
    if (rawResult instanceof Optional<?> opt) {
      return opt.map(o -> (E) o);
    }

    if (rawResult == null) {
      return Optional.empty();
    }
    return Optional.of((E) rawResult);
  }

  // ==================== 内部工具 ====================

  private static String availableNames(Class<? extends Enum<?>> enumType) {
    return Stream.of(enumType.getEnumConstants())
      .map(Enum::name)
      .collect(Collectors.joining(", ", "[", "]"));
  }
}
