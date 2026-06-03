package cn.labzen.file.i18n;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.Column;
import cn.labzen.file.definition.bean.column.Exporting;
import cn.labzen.file.definition.bean.column.Importing;
import cn.labzen.tool.util.Strings;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class I18nResolver {

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final DataDefinition definition;
  private final String locale;
  private final I18nStoreProvider i18NStore;

  public I18nResolver(DataDefinition definition, String locale) {
    this.definition = copy(definition);
    this.definition.setLocale(locale);
    this.locale = locale;
    this.i18NStore = I18nStoreHolder.get();
  }

  private DataDefinition copy(DataDefinition definition) {
    return OBJECT_MAPPER.convertValue(definition, DataDefinition.class);
  }

  public DataDefinition resolve() {
    definition.setTitle(resolveText(definition.getTitle()));

    // 解析列定义
    if (definition.getColumns() != null) {
      for (Column column : definition.getColumns().values()) {
        resolveColumn(column);
      }
    }

    return definition;
  }

  private void resolveColumn(Column column) {
    // header
    column.setHeader(resolveText(column.getHeader()));
    // pattern - date
    column.setPatternDate(resolveText(column.getPatternDate()));
    // pattern - number
    column.setPatternNumber(resolveText(column.getPatternNumber()));

    // 共享 enumerable（不包含 ${key}，但保持一致性仍解析）
    if (Strings.isNotBlank(column.getEnumerable())) {
      column.setEnumerable(resolveText(column.getEnumerable()));
    }
    // 共享 mapping 中的 ${key}（只替换 value）
    resolveMapping(column.getMapping());

    // 导出配置
    if (column.getExporting() != null) {
      resolveExporting(column.getExporting());
    }

    // 导入配置
    if (column.getImporting() != null) {
      resolveImporting(column.getImporting());
    }
  }

  /**
   * 解析映射表中的 ${key} 占位符（只替换 value）
   */
  private void resolveMapping(Map<String, String> mapping) {
    if (mapping == null) {
      return;
    }

    Map<String, String> resolved = new LinkedHashMap<>();
    for (Map.Entry<String, String> entry : mapping.entrySet()) {
      resolved.put(entry.getKey(), resolveText(entry.getValue()));
    }
    mapping.clear();
    mapping.putAll(resolved);
  }


  private void resolveExporting(Exporting exporting) {
    // 继承自 TableExporting 的共享属性
    exporting.setWhenNull(resolveText(exporting.getWhenNull()));
    exporting.setWhenBlank(resolveText(exporting.getWhenBlank()));

    // 列级专属属性
    exporting.setPrefix(resolveText(exporting.getPrefix()));
    exporting.setSuffix(resolveText(exporting.getSuffix()));
    exporting.setConverter(resolveText(exporting.getConverter()));
    exporting.setEnumerable(resolveText(exporting.getEnumerable()));
    resolveMapping(exporting.getMapping());
  }


  private void resolveImporting(Importing importing) {
    // 继承自 TableExporting 的共享属性
    importing.setConverter(resolveText(importing.getConverter()));
    importing.setEnumerable(resolveText(importing.getEnumerable()));
    resolveMapping(importing.getMapping());
  }

  /**
   * 解析文本中的 ${key} 占位符
   *
   * @param text 原始文本，可能包含 ${key}
   * @return 替换后的文本
   */
  @SuppressWarnings("DuplicatedCode")
  private String resolveText(String text) {
    if (text == null || !text.contains("${")) {
      return text;
    }

    Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
    StringBuilder result = new StringBuilder();
    while (matcher.find()) {
      String key = matcher.group(1);
      String replacement = i18NStore.getText(locale, key);
      matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(result);
    return result.toString();
  }
}
