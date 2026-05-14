package cn.labzen.file.definition.bean.converter;

import cn.labzen.file.definition.enums.ConverterType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 数据转换器配置
 * <p>
 * 用于定义数据的转换规则，支持三种转换方式：
 * <ul>
 *   <li>{@link ConverterType#ENUM} - 枚举转换器：通过枚举类和方法将值转换为枚举描述</li>
 *   <li>{@link ConverterType#NAME} - 预置转换器：使用系统预置的转换器（如大写、小写、Trim 等）</li>
 *   <li>{@link ConverterType#MAPPING} - 值映射转换器：通过键值对映射表进行转换</li>
 * </ul>
 * <p>
 * 配置一种转换方式即可，多种配置时按优先级：ENUM > NAME > MAPPING
 *
 * @author labzen
 * @see ConverterType
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Converter {

  /**
   * 枚举转换配置
   * <p>
   * 格式为 枚举类全限定名#方法名，如 com.example.StatusEnum#getLabel
   */
  private String enumConverter;

  /**
   * 预置转换器名称
   * <p>
   * 如 uppercase、lowercase、trim 等，具体名称参考具体接口实现
   */
  private String namedConverter;

  /**
   * 值映射表
   * <p>
   * 格式为 原值:转换后值，如 {"1": "男", "2": "女"}
   */
  private Map<String, String> mapping;

//  /**
//   * 获取枚举转换器类名（不含方法名）
//   *
//   * @return 枚举类全限定名，如解析失败返回 null
//   */
//  public String getEnumClass() {
//    if (enumConverter == null || enumConverter.isBlank()) {
//      return null;
//    }
//    int hashIndex = enumConverter.indexOf('#');
//    if (hashIndex > 0) {
//      return enumConverter.substring(0, hashIndex);
//    }
//    return null;
//  }
//
//  /**
//   * 获取枚举转换器方法名
//   *
//   * @return 方法名，如解析失败返回 "toString"
//   */
//  public String getEnumMethod() {
//    if (enumConverter == null || enumConverter.isBlank()) {
//      return "toString";
//    }
//    int hashIndex = enumConverter.indexOf('#');
//    if (hashIndex > 0 && hashIndex < enumConverter.length() - 1) {
//      String method = enumConverter.substring(hashIndex + 1);
//      return method.isBlank() ? "toString" : method;
//    }
//    return "toString";
//  }
}
