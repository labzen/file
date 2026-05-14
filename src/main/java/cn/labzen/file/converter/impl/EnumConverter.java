package cn.labzen.file.converter.impl;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.CacheableConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.tool.util.Strings;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 枚举转换器
 * <p>
 * 将枚举值转换为字符串表示
 * <ul>
 *   <li>输入支持: String</li>
 *   <li>输出: String</li>
 * </ul>
 *
 * @author labzen
 */
@Slf4j
@DataConverter(name = Converter.ENUM_NAME, priority = Converter.ENUM_PRIORITY)
public class EnumConverter extends CacheableConverter<String> {

  private static final Map<String, EnumInfo> ENUM_CACHE = new ConcurrentHashMap<>();
  private static final Pattern PATTERN = Pattern.compile("^(([a-zA-Z_][a-zA-Z0-9_]*\\.)*([A-Z_$][a-zA-Z0-9_$]*))#([a-zA-Z_$][a-zA-Z0-9_$]*)$");

  @Override
  public boolean supports(Class<?> type) {
    return String.class.isAssignableFrom(type);
  }

  @Override
  protected String doConvert(Object input, List<Object> arguments) {
    if (input == null) {
      return null;
    }

    String enumClassAndMethodName = Strings.value(arguments.getFirst(), "");
    EnumInfo enumInfo = ENUM_CACHE.computeIfAbsent(enumClassAndMethodName, k -> {
      Matcher matcher = PATTERN.matcher(enumClassAndMethodName);
      if (!matcher.matches()) {
        return EnumInfo.EMPTY;
      }

      String className = matcher.group(1);
      String methodName = matcher.group(4);
      try {
        Class<?> clazz = Class.forName(className);
        if (!clazz.isEnum()) {
          logger.error("类 [{}] 不是枚举类", className);
          return EnumInfo.EMPTY;
        }
        Method method = clazz.getMethod(methodName);
        //noinspection unchecked
        return new EnumInfo((Class<? extends Enum<?>>) clazz, method);
      } catch (ClassNotFoundException e) {
        logger.error("枚举类 [{}] 不存在", className);
        return EnumInfo.EMPTY;
      } catch (NoSuchMethodException e) {
        logger.error("枚举类方法 [{}#{}()] 不存在", className, methodName);
        return EnumInfo.EMPTY;
      }
    });

    if (enumInfo == EnumInfo.EMPTY) {
      return "convert-enum-failed";
    }

    // 忽略大小写查找枚举实例
    Enum<?> enumConstant = findEnumConstant(enumInfo.type(), input.toString());
    if (enumConstant == null) {
      return "convert-enum-failed";
    }

    try {
      Object result = enumInfo.method().invoke(enumConstant);
      return result != null ? result.toString() : "";
    } catch (Exception e) {
      return "convert-enum-failed";
    }
  }

  private Enum<?> findEnumConstant(Class<? extends Enum<?>> enumType, String input) {
    // 先尝试大写匹配
    String upperInput = input.toUpperCase();
    for (Enum<?> constant : enumType.getEnumConstants()) {
      if (constant.name().equals(upperInput)) {
        return constant;
      }
    }
    // 再尝试忽略大小写匹配
    for (Enum<?> constant : enumType.getEnumConstants()) {
      if (constant.name().equalsIgnoreCase(input)) {
        return constant;
      }
    }
    return null;
  }

  private record EnumInfo(Class<? extends Enum<?>> type, Method method) {

    static final EnumInfo EMPTY = new EnumInfo(null, null);
  }
}
