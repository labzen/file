package cn.labzen.file.converter;

/**
 * 转换器标记接口 — 所有转换器的父类型
 * <p>
 * 按三个区段组织：
 * <ol>
 *   <li>名称 — 全部 13 个内置转换器的名称常量</li>
 *   <li>导出优先级 — 支持导出的转换器按执行顺序排列（数字小先执行）</li>
 *   <li>导入优先级 — 支持导入的转换器按执行顺序排列（数字小先执行）</li>
 * </ol>
 *
 * <h3>导出链执行顺序</h3>
 * <pre>
 * when-null(100) → when-empty(110) → date(200) → number(210) →
 * enum(290) → mapping(320) → bool/desensitize/truncate(400) →
 * prefix(900) → suffix(910)
 * </pre>
 *
 * <h3>导入链执行顺序</h3>
 * <pre>
 * date(200) → number(210) → mapping(300) → enum(320) →
 * bool(400) → uppercase(500) → lowercase(510)
 * </pre>
 *
 * @author labzen
 */
public interface Converter {


  // 单方向转换器在不适用的方向上使用此占位值
  int UNUSED_PRIORITY = 0;

  // ================================================================
  // 一、转换器名称
  // ================================================================

  // ── 双向（导出+导入）──
  String DATE_NAME = "innate#date";
  String NUMBER_NAME = "innate#number";
  String MAPPING_NAME = "innate#mapping";
  String ENUM_NAME = "innate#enum";
  String BOOL_NAME = "bool";

  // ── 仅导出 ──
  String WHEN_NULL_NAME = "innate#when-null";
  String WHEN_EMPTY_NAME = "innate#when-empty";
  String DESENSITIZE_NAME = "desensitize";
  String TRUNCATE_NAME = "truncate";
  String PREFIX_NAME = "innate#prefix";
  String SUFFIX_NAME = "innate#suffix";

  // ── 仅导入 ──
  String UPPERCASE_NAME = "uppercase";
  String LOWERCASE_NAME = "lowercase";

  // ================================================================
  // 二、导出优先级（数字小 → 先执行）
  // ================================================================

  int WHEN_NULL_EXPORT_PRIORITY = 100;
  int WHEN_EMPTY_EXPORT_PRIORITY = 110;
  int DATE_EXPORT_PRIORITY = 200;
  int NUMBER_EXPORT_PRIORITY = 210;

  int ENUM_EXPORT_PRIORITY = 290;
  int MAPPING_EXPORT_PRIORITY = 320;

  int BOOL_EXPORT_PRIORITY = 400;
  int DESENSITIZE_EXPORT_PRIORITY = 400;
  int TRUNCATE_EXPORT_PRIORITY = 400;

  int PREFIX_EXPORT_PRIORITY = 900;
  int SUFFIX_EXPORT_PRIORITY = 910;

  // ================================================================
  // 三、导入优先级（数字小 → 先执行）
  // ================================================================

  int DATE_IMPORT_PRIORITY = 200;
  int NUMBER_IMPORT_PRIORITY = 210;

  int MAPPING_IMPORT_PRIORITY = 300;
  int ENUM_IMPORT_PRIORITY = 320;

  int BOOL_IMPORT_PRIORITY = 400;

  int UPPERCASE_IMPORT_PRIORITY = 500;
  int LOWERCASE_IMPORT_PRIORITY = 510;
}
