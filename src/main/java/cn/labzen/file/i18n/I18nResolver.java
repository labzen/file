package cn.labzen.file.i18n;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.TableColumn;
import cn.labzen.file.definition.bean.converter.Converter;
import cn.labzen.file.definition.bean.style.Font;
import cn.labzen.file.definition.bean.style.Style;
import cn.labzen.file.definition.bean.table.HeaderBuilder;
import cn.labzen.file.definition.bean.table.HeaderStructure;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 国际化占位符解析器
 * <p>
 * 将 {@link DataDefinition} 中所有 ${key} 占位符替换为 {@link I18nStoreProvider} 中对应 locale 的文本。
 * 解析过程会深拷贝原始定义，不影响注册中心中的模板定义。
 *
 * @author labzen
 */
public class I18nResolver {

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

  private final I18nStoreProvider i18NStoreProvider;

  public I18nResolver(I18nStoreProvider i18NStoreProvider) {
    this.i18NStoreProvider = i18NStoreProvider;
  }

  /**
   * 解析 DataDefinition 中的所有 ${key} 占位符
   *
   * @param template 模板定义（含 ${key} 占位符）
   * @param locale   目标语言标签
   * @return 解析后的新 DataDefinition 实例
   */
  public DataDefinition resolve(DataDefinition template, String locale) {
    DataDefinition resolved = copyDefinition(template);
    resolveDefinition(resolved, locale);
    return resolved;
  }

  private void resolveDefinition(DataDefinition definition, String locale) {
    // 解析 title
    definition.setTitle(resolveText(definition.getTitle(), locale));

    // 解析列定义
    if (definition.getColumns() != null) {
      for (TableColumn column : definition.getColumns().values()) {
        resolveColumn(column, locale);
      }
    }

    // 重建 HeaderStructure（header 文本可能已变更）
    if (definition.getColumns() != null) {
      HeaderStructure headerStructure = HeaderBuilder.build(
        definition.getColumns().values().stream().toList()
      );
      definition.setHeaders(headerStructure);
    }
  }

  private void resolveColumn(TableColumn column, String locale) {
    // header
    column.setHeader(resolveText(column.getHeader(), locale));

    // whenNull / whenBlank (继承自 GlobalColumn)
    column.setWhenNull(resolveText(column.getWhenNull(), locale));
    column.setWhenBlank(resolveText(column.getWhenBlank(), locale));

    // converter
    if (column.getConverter() != null) {
      resolveConverter(column.getConverter(), locale);
    }
  }

  private void resolveConverter(Converter converter, String locale) {
    // named 转换器中的 ${key}
    if (converter.getNamed() != null) {
      converter.setNamed(resolveText(converter.getNamed(), locale));
    }

    // mapping 中的 ${key}（只替换 value）
    if (converter.getMapping() != null) {
      Map<String, String> resolvedMapping = new LinkedHashMap<>();
      for (Map.Entry<String, String> entry : converter.getMapping().entrySet()) {
        resolvedMapping.put(entry.getKey(), resolveText(entry.getValue(), locale));
      }
      converter.setMapping(resolvedMapping);
    }
  }

  /**
   * 解析文本中的 ${key} 占位符
   *
   * @param text   原始文本，可能包含 ${key}
   * @param locale 目标语言标签
   * @return 替换后的文本
   */
  private String resolveText(String text, String locale) {
    if (text == null || !text.contains("${")) {
      return text;
    }

    Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
    StringBuilder result = new StringBuilder();
    while (matcher.find()) {
      String key = matcher.group(1);
      String replacement = i18NStoreProvider.getText(locale, key);
      matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(result);
    return result.toString();
  }

  // ===== 深拷贝方法 =====

  private DataDefinition copyDefinition(DataDefinition source) {
    DataDefinition copy = new DataDefinition();
    copy.setDomainName(source.getDomainName());
    copy.setFilename(source.getFilename());
    copy.setTitle(source.getTitle());
    copy.setHeaderStyle(copyStyle(source.getHeaderStyle()));
    copy.setColumnStyle(copyStyle(source.getColumnStyle()));
    copy.setHeaders(source.getHeaders());

    if (source.getColumns() != null) {
      Map<String, TableColumn> columnsCopy = new LinkedHashMap<>();
      source.getColumns().forEach((name, col) -> columnsCopy.put(name, copyTableColumn(col)));
      copy.setColumns(columnsCopy);
    }

    return copy;
  }

  private TableColumn copyTableColumn(TableColumn source) {
    TableColumn copy = new TableColumn();
    copy.setHeader(source.getHeader());
    copy.setWidth(source.getWidth());
    copy.setWhenNull(source.getWhenNull());
    copy.setWhenBlank(source.getWhenBlank());
    copy.setPrefix(source.getPrefix());
    copy.setSuffix(source.getSuffix());
    copy.setStyle(copyStyle(source.getStyle()));
    copy.setPattern(copyPattern(source.getPattern()));
    copy.setConverter(copyConverter(source.getConverter()));
    return copy;
  }

  private Style copyStyle(Style source) {
    if (source == null) {
      return null;
    }
    Style copy = new Style();
    copy.setAlign(source.getAlign());
    copy.setBackground(source.getBackground());
    copy.setWrapped(source.getWrapped());
    copy.setFont(copyFont(source.getFont()));
    return copy;
  }

  private Font copyFont(Font source) {
    if (source == null) {
      return null;
    }
    Font copy = new Font();
    copy.setFamily(source.getFamily());
    copy.setSize(source.getSize());
    copy.setColor(source.getColor());
    copy.setBold(source.getBold());
    copy.setItalic(source.getItalic());
    return copy;
  }

  private cn.labzen.file.definition.bean.converter.Pattern copyPattern(
    cn.labzen.file.definition.bean.converter.Pattern source) {
    if (source == null) {
      return null;
    }
    cn.labzen.file.definition.bean.converter.Pattern copy = new cn.labzen.file.definition.bean.converter.Pattern();
    copy.setDate(source.getDate());
    copy.setNumber(source.getNumber());
    return copy;
  }

  private Converter copyConverter(Converter source) {
    if (source == null) {
      return null;
    }
    Converter copy = new Converter();
    copy.setEnumerable(source.getEnumerable());
    copy.setNamed(source.getNamed());
    if (source.getMapping() != null) {
      copy.setMapping(new LinkedHashMap<>(source.getMapping()));
    }
    return copy;
  }
}
