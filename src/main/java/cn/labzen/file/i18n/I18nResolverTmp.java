//package cn.labzen.file.i18n;
//
//import cn.labzen.file.definition.bean.DataDefinition;
//import cn.labzen.file.definition.bean.column.Column;
//import cn.labzen.file.definition.bean.column.Exporting;
//import cn.labzen.file.definition.bean.column.Importing;
//import cn.labzen.file.definition.bean.scoped.GlobalExporting;
//import cn.labzen.file.definition.bean.scoped.GlobalImporting;
//import cn.labzen.file.definition.bean.style.Font;
//import cn.labzen.file.definition.bean.style.Style;
//
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.Map;
//import java.util.regex.Matcher;
//
///**
// * 国际化占位符解析器
// * <p>
// * 将 {@link DataDefinition} 中所有 ${key} 占位符替换为 {@link I18nStoreProvider} 中对应 locale 的文本。
// * 解析过程会深拷贝原始定义，不影响注册中心中的模板定义。
// *
// * @author labzen
// */
//public class I18nResolverTmp {
//
//  private static final java.util.regex.Pattern PLACEHOLDER_PATTERN =
//    java.util.regex.Pattern.compile("\\$\\{([^}]+)}");
//
//  private final I18nStoreProvider i18NStoreProvider;
//
//  public I18nResolverTmp(I18nStoreProvider i18NStoreProvider) {
//    this.i18NStoreProvider = i18NStoreProvider;
//  }
//
//  /**
//   * 解析 DataDefinition 中的所有 ${key} 占位符
//   *
//   * @param template 模板定义（含 ${key} 占位符）
//   * @param locale   目标语言标签
//   * @return 解析后的新 DataDefinition 实例
//   */
//  public DataDefinition resolve(DataDefinition template, String locale) {
//    DataDefinition resolved = copyDefinition(template);
//    resolveDefinition(resolved, locale);
//    return resolved;
//  }
//
//  private void resolveDefinition(DataDefinition definition, String locale) {
//    // 解析 title
//    definition.setTitle(resolveText(definition.getTitle(), locale));
//
//    // 解析列定义
//    if (definition.getColumns() != null) {
//      for (Column column : definition.getColumns().values()) {
//        resolveColumn(column, locale);
//      }
//    }
//
//    // 重建 HeaderStructure（header 文本可能已变更）
//    if (definition.getColumns() != null) {
////      HeaderStructure headerStructure = HeaderBuilder.build(
////        definition.getColumns().values().stream().toList()
////      );
////      definition.setHeaders(headerStructure);
//    }
//  }
//
//  private void resolveColumn(Column column, String locale) {
//    // header
//    column.setHeader(resolveText(column.getHeader(), locale));
//
//    // 共享 mapping 中的 ${key}（只替换 value）
//    resolveMapping(column.getMapping(), locale);
//
//    // 共享 enumerable（不包含 ${key}，但保持一致性仍解析）
//    column.setEnumerable(resolveText(column.getEnumerable(), locale));
//
//    // 导出配置
//    if (column.getExporting() != null) {
//      resolveExporting(column.getExporting(), locale);
//    }
//
//    // 导入配置
//    if (column.getImporting() != null) {
//      resolveImporting(column.getImporting(), locale);
//    }
//  }
//
//  private void resolveExporting(Exporting exporting, String locale) {
//    // 继承自 TableExporting 的共享属性
//    exporting.setWhenNull(resolveText(exporting.getWhenNull(), locale));
//    exporting.setWhenBlank(resolveText(exporting.getWhenBlank(), locale));
//
//    // 列级专属属性
//    exporting.setPrefix(resolveText(exporting.getPrefix(), locale));
//    exporting.setSuffix(resolveText(exporting.getSuffix(), locale));
//    resolveMapping(exporting.getMapping(), locale);
//    exporting.setEnumerable(resolveText(exporting.getEnumerable(), locale));
//    exporting.setConverter(resolveText(exporting.getConverter(), locale));
//  }
//
//  private void resolveImporting(Importing importing, String locale) {
//    // 列级专属属性（含 ${key} 的文本字段）
//    resolveMapping(importing.getMapping(), locale);
//    importing.setEnumerable(resolveText(importing.getEnumerable(), locale));
//    importing.setConverter(resolveText(importing.getConverter(), locale));
//  }
//
//  /**
//   * 解析映射表中的 ${key} 占位符（只替换 value）
//   */
//  private void resolveMapping(Map<String, String> mapping, String locale) {
//    if (mapping == null) {
//      return;
//    }
//    Map<String, String> resolved = new LinkedHashMap<>();
//    for (Map.Entry<String, String> entry : mapping.entrySet()) {
//      resolved.put(entry.getKey(), resolveText(entry.getValue(), locale));
//    }
//    mapping.clear();
//    mapping.putAll(resolved);
//  }
//
//  /**
//   * 解析文本中的 ${key} 占位符
//   *
//   * @param text   原始文本，可能包含 ${key}
//   * @param locale 目标语言标签
//   * @return 替换后的文本
//   */
//  private String resolveText(String text, String locale) {
//    if (text == null || !text.contains("${")) {
//      return text;
//    }
//
//    Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
//    StringBuilder result = new StringBuilder();
//    while (matcher.find()) {
//      String key = matcher.group(1);
//      String replacement = i18NStoreProvider.getText(locale, key);
//      matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
//    }
//    matcher.appendTail(result);
//    return result.toString();
//  }
//
//  // ===== 深拷贝方法 =====
//
//  private DataDefinition copyDefinition(DataDefinition source) {
//    DataDefinition copy = new DataDefinition();
//    copy.setDomainName(source.getDomainName());
//    copy.setFilename(source.getFilename());
//    copy.setTitle(source.getTitle());
//    copy.setExportingHeaderStyle(copyStyle(source.getExportingHeaderStyle()));
//    copy.setExportingColumnStyle(copyStyle(source.getExportingColumnStyle()));
//    copy.setHeaders(source.getHeaders());
//    copy.setExporting(copyTableExporting(source.getExporting()));
//    copy.setImporting(copyTableImporting(source.getImporting()));
//
//    if (source.getColumns() != null) {
//      Map<String, Column> columnsCopy = new LinkedHashMap<>();
//      source.getColumns().forEach((name, col) -> columnsCopy.put(name, copyColumn(col)));
//      copy.setColumns(columnsCopy);
//    }
//
//    return copy;
//  }
//
//  private Column copyColumn(Column source) {
//    Column copy = new Column();
//    copy.setHeader(source.getHeader());
//    copy.getExporting().setWidth(source.getExporting().getWidth());
//    copy.getExporting().setStyle(copyStyle(source.getExporting().getStyle()));
////    copy.setPattern(copyPattern(source.getPattern()));
//    copy.setPatternDate(source.getPatternDate());
//    copy.setPatternNumber(source.getPatternNumber());
//
//    // 共享 mapping
//    if (source.getMapping() != null) {
//      copy.setMapping(new LinkedHashMap<>(source.getMapping()));
//    }
//
//    // 共享 enumerable
//    copy.setEnumerable(source.getEnumerable());
//
//    // 导出/导入配置
//    copy.setExporting(copyExporting(source.getExporting()));
//    copy.setImporting(copyImporting(source.getImporting()));
//
//    return copy;
//  }
//
//  private Exporting copyExporting(Exporting source) {
//    if (source == null) {
//      return null;
//    }
//    Exporting copy = new Exporting();
//    copy.setWhenNull(source.getWhenNull());
//    copy.setWhenBlank(source.getWhenBlank());
//    copy.setPrefix(source.getPrefix());
//    copy.setSuffix(source.getSuffix());
//    if (source.getMapping() != null) {
//      copy.setMapping(new LinkedHashMap<>(source.getMapping()));
//    }
//    copy.setEnumerable(source.getEnumerable());
//    copy.setConverter(source.getConverter());
//    return copy;
//  }
//
//  private Importing copyImporting(Importing source) {
//    if (source == null) {
//      return null;
//    }
//    Importing copy = new Importing();
//    copy.setRequired(source.getRequired());
//    if (source.getCleansing() != null) {
//      copy.setCleansing(new ArrayList<>(source.getCleansing()));
//    }
//    copy.setMinLength(source.getMinLength());
//    copy.setMaxLength(source.getMaxLength());
//    copy.setUnique(source.getUnique());
//    if (source.getDependsOn() != null) {
//      copy.setDependsOn(new ArrayList<>(source.getDependsOn()));
//    }
//    copy.setMin(source.getMin());
//    copy.setMax(source.getMax());
//    if (source.getMapping() != null) {
//      copy.setMapping(new LinkedHashMap<>(source.getMapping()));
//    }
//    copy.setEnumerable(source.getEnumerable());
//    copy.setConverter(source.getConverter());
//    return copy;
//  }
//
//  private GlobalExporting copyTableExporting(
//    GlobalExporting source) {
//    if (source == null) {
//      return null;
//    }
//    GlobalExporting copy =
//      new GlobalExporting();
//    copy.setWhenNull(source.getWhenNull());
//    copy.setWhenBlank(source.getWhenBlank());
//    return copy;
//  }
//
//  private GlobalImporting copyTableImporting(
//    GlobalImporting source) {
//    if (source == null) {
//      return null;
//    }
//    GlobalImporting copy =
//      new GlobalImporting();
//    copy.setRequired(source.getRequired());
//    if (source.getCleansing() != null) {
//      copy.setCleansing(new ArrayList<>(source.getCleansing()));
//    }
//    return copy;
//  }
//
//  private Style copyStyle(Style source) {
//    if (source == null) {
//      return null;
//    }
//    Style copy = new Style();
//    copy.setAlign(source.getAlign());
//    copy.setBackground(source.getBackground());
//    copy.setWrapped(source.getWrapped());
//    copy.setFont(copyFont(source.getFont()));
//    return copy;
//  }
//
//  private Font copyFont(Font source) {
//    if (source == null) {
//      return null;
//    }
//    Font copy = new Font();
//    copy.setFamily(source.getFamily());
//    copy.setSize(source.getSize());
//    copy.setColor(source.getColor());
//    copy.setBold(source.getBold());
//    copy.setItalic(source.getItalic());
//    return copy;
//  }
//
////  private Pattern copyPattern(Pattern source) {
////    if (source == null) {
////      return null;
////    }
////    Pattern copy = new Pattern();
////    copy.setDate(source.getDate());
////    copy.setNumber(source.getNumber());
////    return copy;
////  }
//}
