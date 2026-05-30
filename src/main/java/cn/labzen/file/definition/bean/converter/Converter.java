//package cn.labzen.file.definition.bean.converter;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.util.Map;
//
///**
// * 数据转换器配置
// * <p>
// * 用于定义数据的转换规则，支持三种转换方式：
// * <ul>
// *   <li>枚举转换器：通过枚举类和方法将值转换为枚举描述</li>
// *   <li>预置转换器：使用系统预置的转换器（如大写、小写、Trim 等）</li>
// *   <li>值映射转换器：通过键值对映射表进行转换</li>
// * </ul>
// * <p>
// * 配置一种转换方式即可，多种配置时按优先级：ENUM > NAME > MAPPING
// *
// * @author labzen
// */
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class Converter {
//
//  /**
//   * 枚举转换配置
//   * <p>
//   * 格式为 枚举类全限定名#方法名，如 com.example.StatusEnum#getLabel
//   */
//  private String enumerable;
//
//  /**
//   * 预置转换器名称
//   * <p>
//   * 如 uppercase、lowercase、trim 等，具体名称参考具体接口实现
//   */
//  private String named;
//
//  /**
//   * 值映射表
//   * <p>
//   * 格式为 原值:转换后值，如 {"1": "男", "2": "女"}
//   */
//  private Map<String, String> mapping;
//}
