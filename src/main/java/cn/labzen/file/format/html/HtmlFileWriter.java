package cn.labzen.file.format.html;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.Column;
import cn.labzen.file.definition.bean.style.Font;
import cn.labzen.file.definition.bean.style.Style;
import cn.labzen.file.definition.bean.table.HeaderCell;
import cn.labzen.file.definition.bean.table.HeaderStructure;
import cn.labzen.file.definition.enums.Alignment;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.exception.DataWriteException;
import cn.labzen.file.format.core.writer.AbstractDataFileWriter;
import cn.labzen.file.meta.FileConfiguration;
import cn.labzen.tool.util.Strings;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * HTML 文件写入器
 * <p>
 * 实现 HTML 表格格式文件的生成。
 * 特性：
 * <ul>
 *   <li>所有 CSS 样式内联在 HTML 文件的 &lt;style&gt; 标签中，不产生外部文件</li>
 *   <li>支持多级表头（colspan/rowspan）</li>
 *   <li>支持单元格样式（对齐、字体、背景色、边框）</li>
 *   <li>表格可滚动，支持固定表头</li>
 * </ul>
 *
 * @param <T> 数据对象类型
 * @author labzen
 */
@Slf4j
public final class HtmlFileWriter<T> extends AbstractDataFileWriter<T> {

  private static final String DEFAULT_FONT_COLOR = "#333";
  private static final String HTML_TEMPLATE = """
    <!DOCTYPE html>
    <html lang="zh-CN">
      <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>%s</title>
        <style>%s</style>
      </head>
      <body>
        <h1 class="page-title">%s</h1>
        <div class="table-container">
          <table class="data-table">
            <thead>
              %s
            </thead>
            <tbody>
              %s
            </tbody>
          </table>
        </div>
      </body>
    </html>
    """;
  private static final String CSS_BASIC = "*{box-sizing:border-box}body{font-family:Arial,'Microsoft YaHei',sans-serif;margin:20px;background:#f5f5f5}"
    + ".page-title{color:" + DEFAULT_FONT_COLOR + ";font-size:24px;font-weight:700;margin-bottom:20px;padding:10px 0;border-bottom:2px solid #007bff}"
    // 表格容器样式（支持滚动）
    + ".table-container{overflow-x:auto;background:#fff;border-radius:8px;box-shadow:0 2px 12px rgba(0,0,0,.1);padding:15px}"
    // 表格基础样式
    + ".data-table{width:100%;border-collapse:collapse;font-size:13px}"
    // 数据行斑马纹
    + ".data-table tr:nth-child(even) td{background:#f8f9fa}.data-table tr:hover td{background:#e9ecef}";
  private static final String CSS_TABLE_HEADER_TEMPLATE = ".data-table th{background:%s;padding:8px;text-align:center;border:1px solid #dadada;white-space:nowrap;%s}";
  private static final String CSS_TABLE_HEADER_COLUMN_TEMPLATE = ".data-table th.col-%s{text-align:%s;%s}";
  private static final String CSS_TABLE_BODY_TEMPLATE = ".data-table td{padding:8px;text-align:center;border:1px solid #dadada;vertical-align:middle;word-break:%s;%s}";
  private static final String CSS_TABLE_BODY_COLUMN_TEMPLATE = ".data-table td.col-%s{text-align:%s;width:%s;%s}";
  private static final String CSS_FONT_TEMPLATE = "font-family:%s,sans-serif;font-size:%s;color:%s;font-weight:%s;font-style:%s";
  private static final String CSS_DEFAULT_FONT_STYLE = "font-family:'Arial','Microsoft YaHei',sans-serif;font-size:13px;color:" + DEFAULT_FONT_COLOR;

  @Override
  public @NonNull FileFormat format() {
    return FileFormat.HTML;
  }

  @Override
  public void initialize(@NonNull FileConfiguration configuration) {

  }

  @Override
  protected void generateContent(@Nonnull DataDefinition definition, @Nonnull List<Map<String, Object>> rows, @Nonnull OutputStream outputStream) {
    Map<String, Column> columns = definition.getColumns();
    HeaderStructure headers = definition.getHeaders();
    String title = escapeHtml(definition.getTitle());
    Style headerStyle = definition.getExportingHeaderStyle();
    Style contentStyle = definition.getExportingColumnStyle();

    try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
      // 生成 CSS 样式
      String style = buildStyles(headerStyle, contentStyle, columns);

      // 生成表头标签
      String headerTags = buildHeaderTags(headers);

      // 生成数据行标签
      String bodyTags = buildBodyTags(rows, columns);

      String html = HTML_TEMPLATE.formatted(title, style, title, headerTags, bodyTags);
      writer.write(html);
      writer.flush();
    } catch (IOException e) {
      throw new DataWriteException(e, "HTML 文件写入失败");
    }
  }

  private String buildStyles(Style headerStyle, Style contentStyle, Map<String, Column> columns) {
    // 表格头公共样式
    String headerFontStyle = safeConvertFontStyle(headerStyle.getFont());
    String cssOfHeader = CSS_TABLE_HEADER_TEMPLATE.formatted(
      headerStyle.getBackground(),
      headerFontStyle
    );

    // 表格内容公共样式
    String bodyTextWrap = contentStyle.getWrapped() ? "break-word" : "nowrap";
    String bodyFontStyle = safeConvertFontStyle(contentStyle.getFont());
    String cssOfBody = CSS_TABLE_BODY_TEMPLATE.formatted(bodyTextWrap, bodyFontStyle);

    int allColumnsWidth = columns.values().stream().mapToInt(value -> value.getExporting().getWidth()).sum();

    // 表格各列样式（头和内容）
    StringBuilder cssOfColumns = new StringBuilder();
    columns.forEach((columnName, column) -> {
      String columnTextAlign = safeConvertAlignmentStyle(column.getExporting().getStyle().getAlign());
      String columnFontStyle = safeConvertFontStyle(column.getExporting().getStyle().getFont());
      String width = String.format("%.1f%%", (column.getExporting().getWidth() + 0.0) / allColumnsWidth * 100);
      String cssOfHeaderColumn = CSS_TABLE_HEADER_COLUMN_TEMPLATE.formatted(columnName, columnTextAlign, columnFontStyle);
      String cssOfBodyColumn = CSS_TABLE_BODY_COLUMN_TEMPLATE.formatted(columnName, columnTextAlign, width, columnFontStyle);
      cssOfColumns.append(cssOfHeaderColumn).append(cssOfBodyColumn);
    });

    return CSS_BASIC + cssOfHeader + cssOfBody + cssOfColumns;
  }

  private String safeConvertAlignmentStyle(Alignment alignment) {
    if (alignment == null) {
      return "center";
    }

    return switch (alignment) {
      case LEFT -> "left";
      case RIGHT -> "right";
      case JUSTIFY -> "justify";
      default -> "center";
    };
  }

  private String safeConvertFontStyle(Font font) {
    if (font == null) {
      return CSS_DEFAULT_FONT_STYLE;
    }

    String family = Strings.value(font.getFamily(), "Microsoft YaHei");
    if (family.contains(" ")) {
      family = "'" + family + "'";
    }
    family = "Arial," + family + ",sans-serif";
    String color = Strings.valueWhenBlank(font.getColor(), DEFAULT_FONT_COLOR);
    String weight = font.getBold() ? "bold" : "normal";
    String italic = font.getItalic() ? "italic" : "normal";
    return CSS_FONT_TEMPLATE.formatted(family, font.getSize() + "px", color, weight, italic);
  }

  private String buildHeaderTags(HeaderStructure structure) {
    StringBuilder headerTags = new StringBuilder("<tr>\n");
    for (HeaderCell cell : structure.firstRow()) {
      headerTags.append("<th");
      if (cell.colSpan() > 1) {
        headerTags.append(" colspan='").append(cell.colSpan()).append("'");
      }
      if (cell.rowSpan() > 1) {
        headerTags.append(" rowspan='").append(cell.rowSpan()).append("'");
      }
      headerTags.append(">").append(cell.text()).append("</th>\n");
    }
    headerTags.append("</tr>\n");

    if (!structure.isSingleHeader()) {
      headerTags.append("<tr>\n");

      for (HeaderCell cell : structure.secondRow()) {
        headerTags.append("<th>").append(cell.text()).append("</th>\n");
      }

      headerTags.append("</tr>\n");
    }
    return headerTags.toString();
  }

  private String buildBodyTags(List<Map<String, Object>> rows, Map<String, Column> columns) {
    StringBuilder bodyTags = new StringBuilder();
    for (Map<String, Object> row : rows) {
      bodyTags.append("<tr>\n");

      for (String key : columns.keySet()) {
        Object value = row.get(key);

        bodyTags.append("<td class=\"col-")
          .append(key)
          .append("\">")
          .append(escapeHtml(Strings.value(value, "")))
          .append("</td>");
      }

      bodyTags.append("</tr>\n");
    }
    return bodyTags.toString();
  }

  /**
   * HTML 实体转义
   */
  private String escapeHtml(String text) {
    if (text == null) {
      return "";
    }
    return text
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("'", "&#39;");
  }
}
