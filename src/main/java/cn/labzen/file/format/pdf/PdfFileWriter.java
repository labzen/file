package cn.labzen.file.format.pdf;

import cn.labzen.file.converter.ChainableConverterExecutor;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.TableColumn;
import cn.labzen.file.definition.bean.style.Font;
import cn.labzen.file.definition.bean.style.Style;
import cn.labzen.file.definition.enums.Alignment;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.exception.DataWriteException;
import cn.labzen.file.format.AbstractDataFileWriter;
import cn.labzen.file.meta.FileConfiguration;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PDF 文件写入器
 * <p>
 * 使用 iTextPDF 库实现 PDF 文件的生成，支持：
 * <ul>
 *   <li>多级表头</li>
 *   <li>单元格背景色</li>
 *   <li>文本对齐（居左、居中、居右）</li>
 * </ul>
 *
 * @param <T> 数据对象类型
 * @author labzen
 */
@Slf4j
public final class PdfFileWriter<T> extends AbstractDataFileWriter<T> {

  /**
   * 默认字体大小
   */
  private static final float DEFAULT_FONT_SIZE = 10f;

  /**
   * 表头字体大小
   */
  private static final float HEADER_FONT_SIZE = 11f;

  @Override
  public @NonNull FileFormat format() {
    return FileFormat.PDF;
  }

  @Override
  public void initialize(@NonNull FileConfiguration configuration) {

  }

  @Override
  protected void generateContent(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull OutputStream outputStream) {
    // 构建转换器链
    ChainableConverterExecutor.build(definition);

    List<Map<String, Object>> rows = extractRows(definition, data);
    Map<String, TableColumn> columns = definition.getColumns();
    int columnCount = columns.size();

    // 获取样式配置
    Style headerStyle = definition.getHeaderStyle();
    Style dataStyle = definition.getColumnStyle();

    try {
      // 获取字体（从缓存或自动解析，传入样式配置获取字体族名）
      FontResolver.FontInfo fontInfo = FontResolver.getFontInfo(dataStyle);
      PdfFont regularFont = fontInfo.regularFont();
      PdfFont boldFont = fontInfo.boldFont();

      // 创建 PDF 文档
      PdfWriter pdfWriter = new PdfWriter(outputStream);
      PdfDocument pdfDocument = new PdfDocument(pdfWriter);
      Document document = new Document(pdfDocument, PageSize.A4.rotate());  // 使用横向页面
      document.setMargins(20, 20, 20, 20);

      // 如果有标题，添加标题
      String title = definition.getTitle();
      if (title != null && !title.isEmpty()) {
        Paragraph titlePara = new Paragraph(title)
          .setFontSize(16f)
          .setFont(boldFont)
          .setTextAlignment(TextAlignment.CENTER)
          .setMarginBottom(15f);
        document.add(titlePara);
      }

      // 构建表头行列表和表头级别
      List<List<String>> headerRows = buildHeaderRows(columns);
      List<Integer> headerLevels = calculateHeaderLevel(columns);
      int headerRowCount = headerRows.size();

      // 计算列宽
      float[] columnWidths = new float[columnCount];
      int colIdx = 0;
      for (TableColumn column : columns.values()) {
        int width = column.getWidth() != null ? column.getWidth() : 15;
        columnWidths[colIdx] = width;
        colIdx++;
      }

      // 创建表格
      Table table = new Table(columnWidths);
      table.setWidth(UnitValue.createPercentValue(100));

      // 写入表头
      writeHeaderCells(table, columns, headerRows, headerLevels, headerStyle, boldFont);

      // 写入数据
      for (Map<String, Object> rowData : rows) {
        for (TableColumn column : columns.values()) {
          String fieldName = getFieldName(columns, column);
          Object value = rowData.get(fieldName);
          String text = value != null ? value.toString() : "";
          // 获取列级别的样式（优先使用列样式，否则使用全局样式）
          Style colStyle = getEffectiveStyle(column, dataStyle);
          // 获取该列对应的字体
          FontResolver.FontInfo colFontInfo = FontResolver.getFontInfo(colStyle);
          Cell cell = createDataCell(text, column, colStyle, colFontInfo.regularFont());
          table.addCell(cell);
        }
      }

      document.add(table);
      document.close();

    } catch (Exception e) {
      logger.error("PDF 生成异常: ", e);
      throw new DataWriteException(e, "PDF 文件写入失败");
    }
  }

  /**
   * 计算每个列的表头级别
   */
  private List<Integer> calculateHeaderLevel(Map<String, TableColumn> columns) {
    return columns.values().stream()
      .map(col -> col.getHeader() != null ? col.getHeader().size() : 1)
      .collect(java.util.stream.Collectors.toList());
  }

  /**
   * 写入表头单元格
   */
  private void writeHeaderCells(Table table, Map<String, TableColumn> columns,
                                List<List<String>> headerRows, List<Integer> headerLevels,
                                Style headerStyle, PdfFont boldFont) {
    int headerRowCount = headerRows.size();
    int columnCount = columns.size();

    // 写入所有表头单元格
    for (int rowIdx = 0; rowIdx < headerRowCount; rowIdx++) {
      List<String> rowHeaders = headerRows.get(rowIdx);
      for (int colIdx = 0; colIdx < columnCount; colIdx++) {
        String headerText = rowHeaders.get(colIdx);

        // 对于纵向合并：一级表头列只在第一行显示文本，后续行为空
        if (headerLevels.get(colIdx) == 1 && rowIdx > 0) {
          headerText = "";
        }

        Cell cell = createHeaderCell(headerText != null ? headerText : "", headerStyle, boldFont);
        table.addCell(cell);
      }
    }
  }

  /**
   * 构建多级表头行列表
   */
  private List<List<String>> buildHeaderRows(Map<String, TableColumn> columns) {
    int maxLevel = columns.values().stream()
      .mapToInt(col -> col.getHeader() != null ? col.getHeader().size() : 0)
      .max()
      .orElse(1);

    if (maxLevel == 0) {
      maxLevel = 1;
    }

    List<List<String>> headerRows = new ArrayList<>();

    for (int level = 0; level < maxLevel; level++) {
      List<String> levelHeaders = new ArrayList<>();
      for (TableColumn column : columns.values()) {
        List<String> header = column.getHeader();
        String headerText = "";

        if (header != null && !header.isEmpty()) {
          int headerCount = header.size();
          if (headerCount == 1) {
            if (level == 0) {
              headerText = header.getFirst();
            } else {
              headerText = "";
            }
          } else {
            int index = header.size() - maxLevel + level;
            if (index >= 0 && index < header.size()) {
              headerText = header.get(index);
            }
          }
        }
        levelHeaders.add(headerText);
      }
      headerRows.add(levelHeaders);
    }

    return headerRows;
  }

  /**
   * 创建表头单元格
   */
  private Cell createHeaderCell(String text, Style headerStyle, PdfFont boldFont) {
    Paragraph para = new Paragraph(text != null ? text : "")
      .setFontSize(HEADER_FONT_SIZE)
      .setFont(boldFont);

    Cell cell = new Cell()
      .add(para)
      .setTextAlignment(TextAlignment.CENTER)
      .setVerticalAlignment(VerticalAlignment.MIDDLE);

    if (headerStyle != null && headerStyle.getBackground() != null) {
      Color bgColor = parseColor(headerStyle.getBackground());
      if (bgColor != null) {
        cell.setBackgroundColor(bgColor);
      }
    }

    if (headerStyle != null && headerStyle.getFont() != null
      && headerStyle.getFont().getColor() != null) {
      Color fontColor = parseColor(headerStyle.getFont().getColor());
      if (fontColor != null) {
        cell.setFontColor(fontColor);
      }
    }

    return cell;
  }

  /**
   * 创建数据单元格
   */
  private Cell createDataCell(String text, TableColumn column, Style defaultStyle, PdfFont font) {
    Paragraph para = new Paragraph(text != null ? text : "")
      .setFontSize(DEFAULT_FONT_SIZE)
      .setFont(font);

    Cell cell = new Cell()
      .add(para)
      .setVerticalAlignment(VerticalAlignment.MIDDLE);

    // 获取对齐方式
    Alignment align = getAlignment(column, defaultStyle);
    cell.setTextAlignment(convertAlignment(align));

    // 设置背景色
    String bgColor = getBackgroundColor(column, defaultStyle);
    if (bgColor != null) {
      Color color = parseColor(bgColor);
      if (color != null) {
        cell.setBackgroundColor(color);
      }
    }

    // 设置字体颜色
    String fontColor = getFontColor(column, defaultStyle);
    if (fontColor != null) {
      Color color = parseColor(fontColor);
      if (color != null) {
        cell.setFontColor(color);
      }
    }

    return cell;
  }

  /**
   * 获取对齐方式
   */
  private Alignment getAlignment(TableColumn column, Style defaultStyle) {
    if (column.getStyle() != null && column.getStyle().getAlign() != null) {
      return column.getStyle().getAlign();
    }
    if (defaultStyle != null && defaultStyle.getAlign() != null) {
      return defaultStyle.getAlign();
    }
    return Alignment.CENTER;
  }

  /**
   * 获取背景色
   */
  private String getBackgroundColor(TableColumn column, Style defaultStyle) {
    if (column.getStyle() != null && column.getStyle().getBackground() != null) {
      return column.getStyle().getBackground();
    }
    if (defaultStyle != null && defaultStyle.getBackground() != null) {
      return defaultStyle.getBackground();
    }
    return "#FFFFFF";
  }

  /**
   * 获取有效的样式（合并列样式和全局样式，列样式优先）
   */
  private Style getEffectiveStyle(TableColumn column, Style defaultStyle) {
    Style columnStyle = column.getStyle();
    if (columnStyle == null) {
      return defaultStyle;
    }
    if (defaultStyle == null) {
      return columnStyle;
    }
    // 合并样式：列样式覆盖全局样式
    Style merged = new Style();
    merged.setAlign(columnStyle.getAlign() != null ? columnStyle.getAlign() : defaultStyle.getAlign());
    merged.setBackground(columnStyle.getBackground() != null ? columnStyle.getBackground() : defaultStyle.getBackground());
    Font defaultFont = defaultStyle.getFont();
    Font columnFont = columnStyle.getFont();
    if (columnFont != null) {
      Font mergedFont = new Font();
      mergedFont.setFamily(columnFont.getFamily() != null ? columnFont.getFamily() : (defaultFont != null ? defaultFont.getFamily() : null));
      mergedFont.setSize(columnFont.getSize() != null ? columnFont.getSize() : (defaultFont != null ? defaultFont.getSize() : null));
      mergedFont.setColor(columnFont.getColor() != null ? columnFont.getColor() : (defaultFont != null ? defaultFont.getColor() : null));
      mergedFont.setBold(columnFont.getBold() != null ? columnFont.getBold() : (defaultFont != null ? defaultFont.getBold() : null));
      mergedFont.setItalic(columnFont.getItalic() != null ? columnFont.getItalic() : (defaultFont != null ? defaultFont.getItalic() : null));
      merged.setFont(mergedFont);
    } else if (defaultFont != null) {
      merged.setFont(defaultFont);
    }
    merged.setBorder(columnStyle.getBorder() != null ? columnStyle.getBorder() : defaultStyle.getBorder());
    merged.setWrapped(columnStyle.getWrapped() != null ? columnStyle.getWrapped() : defaultStyle.getWrapped());
    return merged;
  }

  /**
   * 获取字体颜色
   */
  private String getFontColor(TableColumn column, Style defaultStyle) {
    if (column.getStyle() != null && column.getStyle().getFont() != null) {
      return column.getStyle().getFont().getColor();
    }
    if (defaultStyle != null && defaultStyle.getFont() != null) {
      return defaultStyle.getFont().getColor();
    }
    return null;
  }

  /**
   * 转换对齐方式
   */
  private TextAlignment convertAlignment(Alignment alignment) {
    if (alignment == null) {
      return TextAlignment.CENTER;
    }
    return switch (alignment) {
      case LEFT -> TextAlignment.LEFT;
      case RIGHT -> TextAlignment.RIGHT;
      case JUSTIFY, DISTRIBUTED -> TextAlignment.JUSTIFIED;
      default -> TextAlignment.CENTER;
    };
  }

  /**
   * 解析颜色字符串为 iText Color
   */
  private Color parseColor(String colorStr) {
    if (colorStr == null || colorStr.isEmpty()) {
      return null;
    }
    try {
      if (colorStr.startsWith("#")) {
        String hex = colorStr.substring(1);
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return new DeviceRgb(r, g, b);
      }
    } catch (Exception e) {
      System.out.println("警告: 无法解析颜色: " + colorStr);
    }
    return null;
  }

  /**
   * 获取字段名
   */
  private String getFieldName(Map<String, TableColumn> columns, TableColumn column) {
    for (Map.Entry<String, TableColumn> entry : columns.entrySet()) {
      if (entry.getValue() == column) {
        return entry.getKey();
      }
    }
    return null;
  }
}