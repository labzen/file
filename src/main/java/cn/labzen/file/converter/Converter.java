package cn.labzen.file.converter;

import cn.labzen.file.exception.DataConvertException;

import java.util.List;

/**
 * 转换器接口
 * <p>
 * 定义数据转换的统一接口，支持泛型指定输入和输出类型。
 * 所有转换器实现类都应实现此接口。
 *
 * @param <O> 输出类型
 * @author labzen
 */
public interface Converter<O> {

  String WHEN_NULL_NAME = "innate#when-null";
  int WHEN_NULL_PRIORITY = 100;
  String WHEN_EMPTY_NAME = "innate#when-empty";
  int WHEN_EMPTY_PRIORITY = 110;
  String DATE_NAME = "innate#date";
  int DATE_PRIORITY = 200;
  String NUMBER_NAME = "innate#number";
  int NUMBER_PRIORITY = 210;
  String MAPPING_NAME = "innate#mapping";
  int MAPPING_PRIORITY = 300;
  String ENUM_NAME = "innate#mapping";
  int ENUM_PRIORITY = 310;
  String PREFIX_NAME = "innate#prefix";
  int PREFIX_PRIORITY = 900;
  String SUFFIX_NAME = "innate#suffix";
  int SUFFIX_PRIORITY = 910;

  String DESENSITIZE_NAME = "desensitize";
  int DESENSITIZE_PRIORITY = 400;
  String TRUNCATE_NAME = "truncate";
  int TRUNCATE_PRIORITY = 400;
  String BOOL_NAME = "bool";
  int BOOL_PRIORITY = 400;

  /**
   * 执行转换
   *
   * @param input  输入值
   * @return 转换后的值
   * @throws DataConvertException 如果转换失败
   */
  O convert(Object input, List<Object> argument) throws DataConvertException;

  /**
   * 检查是否支持指定的数据类型
   *
   * @param type 数据类型
   * @return 是否支持
   */
  boolean supports(Class<?> type);
}
