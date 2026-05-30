package cn.labzen.file.converter;

import cn.labzen.file.converter.exportable.ExportableConverter;
import cn.labzen.file.converter.importable.ImportableConverter;

/**
 * 转换器标记接口 — 所有转换器的父类型
 * <p>
 * 定义内置转换器的名称和优先级常量。
 * 具体的转换方向由 {@link ExportableConverter} 和 {@link ImportableConverter} 分别定义。
 *
 * @author labzen
 */
public interface Converter {

  // ── 导出+导入 双向转换器 ──
  String WHEN_NULL_NAME = "innate#when-null";
  int WHEN_NULL_PRIORITY = 100;
  String WHEN_EMPTY_NAME = "innate#when-empty";
  int WHEN_EMPTY_PRIORITY = 110;
  String NUMBER_NAME = "innate#number";
  int NUMBER_PRIORITY = 210;
  String MAPPING_NAME = "innate#mapping";
  int MAPPING_PRIORITY = 300;
  String ENUM_NAME = "innate#enum";
  int ENUM_PRIORITY = 310;
  String BOOL_NAME = "bool";
  int BOOL_PRIORITY = 400;

  // ── 仅导出转换器 ──
  String DATE_NAME = "innate#date";
  int DATE_PRIORITY = 200;
  String DESENSITIZE_NAME = "desensitize";
  int DESENSITIZE_PRIORITY = 400;
  String TRUNCATE_NAME = "truncate";
  int TRUNCATE_PRIORITY = 400;
  String PREFIX_NAME = "innate#prefix";
  int PREFIX_PRIORITY = 900;
  String SUFFIX_NAME = "innate#suffix";
  int SUFFIX_PRIORITY = 910;

  // ── 仅导入转换器 ──
  String UPPERCASE_NAME = "uppercase";
  int UPPERCASE_PRIORITY = 500;
  String LOWERCASE_NAME = "lowercase";
  int LOWERCASE_PRIORITY = 510;
//  String DATE_NAME = "date";
//  int DATE_PRIORITY = 520;
//  String LOCAL_DATE_NAME = "local_date";
//  int LOCAL_DATE_PRIORITY = 520;
//  String LOCAL_TIME_NAME = "local_time";
//  int LOCAL_TIME_PRIORITY = 520;
//  String LOCAL_DATETIME_NAME = "local_datetime";
//  int LOCAL_DATETIME_PRIORITY = 520;
}
