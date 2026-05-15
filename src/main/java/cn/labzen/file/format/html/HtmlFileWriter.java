package cn.labzen.file.format.html;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.TableColumn;
import cn.labzen.file.definition.bean.style.Font;
import cn.labzen.file.definition.bean.style.Style;
import cn.labzen.file.definition.enums.Alignment;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.exception.DataWriteException;
import cn.labzen.file.format.AbstractDataFileWriter;
import cn.labzen.file.meta.FileConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
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

  /**
   * 默认表格容器宽度
   */
  private static final String DEFAULT_WIDTH = "100%";

  /**
   * 默认单元格内边距
   */
  private static final String DEFAULT_PADDING = "8px";

  @Override
  public @NonNull FileFormat format() {
    return FileFormat.HTML;
  }

  @Override
  public void initialize(@NonNull FileConfiguration configuration) {

  }

  @Override
  protected void generateContent(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull OutputStream outputStream) {
    List<Map<String, Object>> rows = extractRows(definition, data);
    Map<String, TableColumn> columns = definition.getColumns();
    String title = definition.getTitle();
    Style headerStyle = definition.getHeaderStyle();
    Style contentStyle = definition.getColumnStyle();

    try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
      // 写入 HTML 文档头部
      writeHtmlHead(writer, title);

      // 写入 CSS 样式
      writeStyles(writer, columns, headerStyle, contentStyle);

      // 写入 body 开始标签和标题
      writer.write("</head><body>");
      writer.write("<h1 class=\"page-title\">");
      writer.write(escapeHtml(title));
      writer.write("</h1>");

      // 写入表格开始标签
      writer.write("<div class=\"table-container\"><table class=\"data-table\">");

      // 写入表头
      writeTableHeader(writer, columns, headerStyle);

      // 写入数据行
      writeTableBody(writer, rows, columns, contentStyle);

      // 写入表格结束标签和 body 结束标签
      writer.write("</table></div></body></html>");

      writer.flush();
    } catch (IOException e) {
      throw new DataWriteException(e, "HTML 文件写入失败");
    }
  }

  /**
   * 写入 HTML 头部
   */
  private void writeHtmlHead(OutputStreamWriter writer, String title) throws IOException {
    writer.write("<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>");
    writer.write(escapeHtml(title));
    writer.write("</title>");
  }

  /**
   * 写入内联 CSS 样式
   */
  private void writeStyles(OutputStreamWriter writer, Map<String, TableColumn> columns,
                           Style headerStyle, Style contentStyle) throws IOException {
    writer.write("<style>");

    // 基础样式
    writer.write("*{box-sizing:border-box}body{font-family:Arial,'Microsoft YaHei',sans-serif;margin:20px;background:#f5f5f5}");
    writer.write(".page-title{color:#333;font-size:24px;font-weight:700;margin-bottom:20px;padding:10px 0;border-bottom:2px solid #007bff}");

    // 表格容器样式（支持滚动）
    writer.write(".table-container{overflow-x:auto;background:#fff;border-radius:8px;box-shadow:0 2px 12px rgba(0,0,0,.1);padding:15px}");

    // 表格基础样式
    writer.write(".data-table{width:");
    writer.write(DEFAULT_WIDTH);
    writer.write(";border-collapse:collapse;font-size:14px}");

    // 表头样式
    writer.write(".data-table th{background:");
    writer.write(headerStyle != null && headerStyle.getBackground() != null ? headerStyle.getBackground() : "#007bff");
    writer.write(";color:#fff;padding:");
    writer.write(DEFAULT_PADDING);
    writer.write(";text-align:center;font-weight:700;border:1px solid ");
    writer.write(getBorderColor(headerStyle));
    writer.write(";white-space:nowrap}");

    // 表头字体样式（会覆盖上面的 color）
    writeFontStyle(writer, ".data-table th", headerStyle);

    // 数据单元格样式（基础样式，不含字体颜色，由列样式决定）
    writer.write(".data-table td{padding:");
    writer.write(DEFAULT_PADDING);
    writer.write(";border:1px solid ");
    writer.write(getBorderColor(contentStyle));
    writer.write(";background:#fff;vertical-align:middle}");

    // 数据行斑马纹
    writer.write(".data-table tr:nth-child(even) td{background:#f8f9fa}");
    writer.write(".data-table tr:hover td{background:#e9ecef}");

    // 数据行字体样式（全局字体样式，会被列样式覆盖）
    writeFontStyle(writer, ".data-table td", contentStyle);

    // 按列生成样式（对齐、字体颜色等，会覆盖全局样式）
    writeColumnStyles(writer, columns);

    writer.write("</style>");
  }

  /**
   * 写入字体样式
   */
  private void writeFontStyle(OutputStreamWriter writer, String selector, Style style) throws IOException {
    if (style == null || style.getFont() == null) {
      return;
    }

    Font font = style.getFont();
    writer.write(selector);
    writer.write("{font-family:");
    writer.write(font.getFamily() != null ? "'" + font.getFamily() + "',sans-serif" : "Arial,'Microsoft YaHei',sans-serif");
    writer.write(";font-size:");
    writer.write(String.valueOf(font.getSize() != null ? font.getSize() : 14));
    writer.write("px;color:");
    writer.write(font.getColor() != null ? font.getColor() : "#333");
    writer.write("}");

    if (Boolean.TRUE.equals(font.getBold())) {
      writer.write(selector);
      writer.write("{font-weight:700}");
    }
    if (Boolean.TRUE.equals(font.getItalic())) {
      writer.write(selector);
      writer.write("{font-style:italic}");
    }
  }

  /**
   * 获取边框颜色
   */
  private String getBorderColor(Style style) {
    if (style != null && style.getBorder() != null && style.getBorder().getColor() != null) {
      return style.getBorder().getColor();
    }
    return "#dee2e6";
  }

  /**
   * 按列生成完整样式（对齐、字体颜色等）
   * <p>
   * 这些样式会覆盖全局样式，实现列级别的样式控制
   */
  private void writeColumnStyles(OutputStreamWriter writer, Map<String, TableColumn> columns) throws IOException {
    for (Map.Entry<String, TableColumn> entry : columns.entrySet()) {
      String fieldName = entry.getKey();
      TableColumn column = entry.getValue();
      Style columnStyle = column.getStyle();

      // 生成 td 样式
      writer.write(".data-table td.col-");
      writer.write(fieldName);
      writer.write("{text-align:");
      writer.write(alignmentToCssValue(getEffectiveAlign(column, Alignment.CENTER)));
      writer.write("}");

      // 如果列有字体颜色配置，生成字体样式
      if (columnStyle != null && columnStyle.getFont() != null && columnStyle.getFont().getColor() != null) {
        writer.write(".data-table td.col-");
        writer.write(fieldName);
        writer.write("{color:");
        writer.write(columnStyle.getFont().getColor());
        writer.write("}");
      }

      // 如果列有字体配置，生成字体族和大小
      if (columnStyle != null && columnStyle.getFont() != null) {
        Font font = columnStyle.getFont();
        if (font.getFamily() != null || font.getSize() != null) {
          writer.write(".data-table td.col-");
          writer.write(fieldName);
          writer.write("{font-family:");
          writer.write(font.getFamily() != null ? "'" + font.getFamily() + "',sans-serif" : "Arial,sans-serif");
          writer.write(";font-size:");
          writer.write(String.valueOf(font.getSize() != null ? font.getSize() : 14));
          writer.write("px}");
        }

        // 加粗和斜体
        if (Boolean.TRUE.equals(font.getBold())) {
          writer.write(".data-table td.col-");
          writer.write(fieldName);
          writer.write("{font-weight:700}");
        }
        if (Boolean.TRUE.equals(font.getItalic())) {
          writer.write(".data-table td.col-");
          writer.write(fieldName);
          writer.write("{font-style:italic}");
        }
      }

      // 生成 th 样式（表头对齐）
      writer.write(".data-table th.col-");
      writer.write(fieldName);
      writer.write("{text-align:");
      writer.write(alignmentToCssValue(getEffectiveAlign(column, Alignment.CENTER)));
      writer.write("}");
    }
  }

  /**
   * 获取有效的对齐方式
   */
  private Alignment getEffectiveAlign(TableColumn column, Alignment defaultAlign) {
    if (column == null || column.getStyle() == null || column.getStyle().getAlign() == null) {
      return defaultAlign;
    }
    return column.getStyle().getAlign();
  }

  /**
   * 将对齐枚举转换为 CSS 值
   */
  private String alignmentToCssValue(Alignment align) {
    if (align == null) {
      return "center";
    }
    return switch (align) {
      case LEFT -> "left";
      case RIGHT -> "right";
      default -> "center";
    };
  }

  /**
   * 写入表格表头（支持多级表头）
   */
  private void writeTableHeader(OutputStreamWriter writer, Map<String, TableColumn> columns, Style headerStyle) throws IOException {
    // 计算表头级别数
    int maxLevel = columns.values().stream()
      .mapToInt(TableColumn::getHeaderLevel)
      .max()
      .orElse(1);

    writer.write("<thead>");

    if (maxLevel > 1) {
      // 多级表头：逐级构建
      writeMultiLevelHeader(writer, columns, maxLevel);
    } else {
      // 单级表头：直接输出
      writer.write("<tr>");
      for (Map.Entry<String, TableColumn> entry : columns.entrySet()) {
        String fieldName = entry.getKey();
        TableColumn column = entry.getValue();

        writer.write("<th class=\"col-");
        writer.write(fieldName);
        writer.write("\"");

        // 处理换行样式
        if (column.getStyle() != null && column.getStyle().getWrapped() != null && !column.getStyle().getWrapped()) {
          writer.write(";white-space:nowrap");
        }

        writer.write(">");

        // 使用最低级别的表头作为标题
        if (column.getHeader() != null && !column.getHeader().isEmpty()) {
          writer.write(escapeHtml(column.getHeader().getLast()));
        }

        writer.write("</th>");
      }
      writer.write("</tr>");
    }

    writer.write("</thead>");
  }

  /**
   * 写入多级表头
   * <p>
   * 规则：
   * <ul>
   *   <li>每一列的 header 按照其级别数分配到不同行</li>
   *   <li>同一级别有相同 header 内容的列使用 colspan 合并</li>
   *   <li>某列在更高级别有 header 时，该单元格需要 rowspan 跨越</li>
   * </ul>
   * 例如：
   * <pre>
   * | 标识      | 属性值 | 创建时间 | 大小 |
   * | 属性名称 | 索引   |         |     |
   * </pre>
   */
  private void writeMultiLevelHeader(OutputStreamWriter writer, Map<String, TableColumn> columns, int maxLevel) throws IOException {
    List<String> fieldNames = new java.util.ArrayList<>(columns.keySet());
    int colCount = fieldNames.size();

    // 逐级输出表头行
    for (int level = 0; level < maxLevel; level++) {
      writer.write("<tr>");
      int colIndex = 0;

      while (colIndex < colCount) {
        String currentFieldName = fieldNames.get(colIndex);
        TableColumn currentCol = columns.get(currentFieldName);
        List<String> headers = currentCol.getHeader();
        int headerLevel = currentCol.getHeaderLevel();

        // 获取当前级别的 header
        String headerText = (headers != null && level < headers.size()) ? headers.get(level) : null;

        // 计算连续相同 header 的列数（只在当前级别有 header 时计算 colspan）
        int span = 1;
        if (headerText != null && !headerText.isEmpty()) {
          for (int nextIdx = colIndex + 1; nextIdx < colCount; nextIdx++) {
            String nextFieldName = fieldNames.get(nextIdx);
            TableColumn nextCol = columns.get(nextFieldName);
            List<String> nextHeaders = nextCol.getHeader();

            // 只合并：后续列在当前级别也有 header
            String nextHeaderText = (nextHeaders != null && level < nextHeaders.size()) ? nextHeaders.get(level) : null;
            if (nextHeaderText != null && !nextHeaderText.isEmpty() && headerText.equals(nextHeaderText)) {
              span++;
            } else {
              break;
            }
          }
        }

        // 如果当前级别没有 header 且下面没有级别（该列在更高级别有 header），跳过不输出
        if (headerText == null && (level + 1) >= headerLevel) {
          colIndex++;
          continue;
        }

        // 计算 rowspan：
        // 情况1：当前级别有 header 且下面还有级别需要跨越
        // 情况2：当前级别没有 header 但该列在更高级别有 header（需要跨越到该列有 header 的级别）
        int rowspan = 1;
        if (headerText != null && !headerText.isEmpty() && (level + 1) < headerLevel) {
          // 情况1：当前级别有 header，下面还有级别
          rowspan = headerLevel - level;
        } else if (headerText == null && level < headerLevel - 1) {
          // 情况2：当前级别没有 header，但该列在更高级别有 header
          // 计算需要跨越的行数
          rowspan = headerLevel - level;
        }

        // 如果既没有 headerText 也没有 rowspan，不需要输出 th
        if ((headerText == null || headerText.isEmpty()) && rowspan <= 1) {
          colIndex++;
          continue;
        }

        // 输出 th
        writer.write("<th class=\"col-");
        writer.write(currentFieldName);
        writer.write("\"");

        if (span > 1) {
          writer.write(" colspan=\"");
          writer.write(String.valueOf(span));
          writer.write("\"");
        }

        if (rowspan > 1) {
          writer.write(" rowspan=\"");
          writer.write(String.valueOf(rowspan));
          writer.write("\"");
        }

        // 处理换行样式
        if (currentCol.getStyle() != null && currentCol.getStyle().getWrapped() != null && !currentCol.getStyle().getWrapped()) {
          writer.write(" style=\"white-space:nowrap\"");
        }

        writer.write(">");
        if (headerText != null && !headerText.isEmpty()) {
          writer.write(escapeHtml(headerText));
        }
        writer.write("</th>");

        colIndex += span;
      }

      writer.write("</tr>");
    }
  }

  /**
   * 写入表格数据行
   */
  private void writeTableBody(OutputStreamWriter writer, List<Map<String, Object>> rows,
                              Map<String, TableColumn> columns, Style contentStyle) throws IOException {
    writer.write("<tbody>");

    for (Map<String, Object> row : rows) {
      writer.write("<tr>");

      for (String fieldName : columns.keySet()) {
        Object value = row.get(fieldName);
        TableColumn column = columns.get(fieldName);

        writer.write("<td class=\"col-");
        writer.write(fieldName);
        writer.write("\"");

        // 处理换行样式
        if (column.getStyle() != null && column.getStyle().getWrapped() != null && !column.getStyle().getWrapped()) {
          writer.write(" style=\"white-space:nowrap\"");
        }

        writer.write(">");

        // 写入单元格值，处理 null
        if (value != null) {
          writer.write(escapeHtml(String.valueOf(value)));
        }

        writer.write("</td>");
      }

      writer.write("</tr>");
    }

    writer.write("</tbody>");
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
