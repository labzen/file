package cn.labzen.file.format.pdf;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.Column;
import cn.labzen.file.definition.bean.style.Style;
import cn.labzen.file.definition.bean.head.HeaderCell;
import cn.labzen.file.definition.bean.head.HeaderStructure;
import cn.labzen.file.definition.enums.Alignment;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.exception.DataWriteException;
import cn.labzen.file.format.core.writer.AbstractDataFileWriter;
import cn.labzen.file.meta.FileConfiguration;
import cn.labzen.tool.util.Strings;
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
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * PDF 文件导出器
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

  private static final Color DATA_ROW_DEFAULT_BACKGROUND = new DeviceRgb(250, 250, 250);
  private static final Color DATA_ROW_HIGHLIGHT_BACKGROUND = new DeviceRgb(148, 149, 150);

  private float reductionFactor = 1.5f;

  @Override
  public @NonNull FileFormat format() {
    return FileFormat.PDF;
  }

  @Override
  public void initialize(@NonNull FileConfiguration configuration) {
    reductionFactor = configuration.pdfFontReductionFactor();
  }

  @Override
  protected void exportContent(@Nonnull DataDefinition definition, @Nonnull List<Map<String, Object>> rows, @Nonnull OutputStream outputStream) {
    Map<String, Column> columns = definition.getColumns();
    int columnCount = columns.size();

    // 获取样式配置
    Style headerStyle = definition.getExportingHeaderStyle();
    Style dataStyle = definition.getExportingColumnStyle();
    HeaderStructure headers = definition.getHeaders();

    PdfWriter pdfWriter = null;
    PdfDocument pdfDocument = null;
    Document document = null;

    try {
      // 获取字体（从缓存或自动解析，传入样式配置获取字体族名）
      FontResolver.FontInfo fontInfo = FontResolver.getFontInfo(dataStyle);
      PdfFont regularFont = fontInfo.regularFont();
      PdfFont boldFont = fontInfo.boldFont();

      // 创建 PDF 文档
      pdfWriter = new PdfWriter(outputStream);
      pdfDocument = new PdfDocument(pdfWriter);
      document = new Document(pdfDocument, PageSize.A4.rotate());  // 使用横向页面
      document.setMargins(20, 20, 20, 20);

      // 添加标题
      Paragraph titleParagraph = buildTitle(definition, boldFont);
      document.add(titleParagraph);

      // 计算列宽
      float[] columnWidths = new float[columnCount];
      int idx = 0;
      for (Column column : columns.values()) {
        columnWidths[idx++] = column.getWidth() != null ? column.getWidth() : 10;
      }

      // 创建表格
      Table table = new Table(columnWidths);
      table.setWidth(UnitValue.createPercentValue(100));

      // 导出表头
      createTableHeader(table, headers, headerStyle, boldFont);

      // 导出数据
      createTableBody(table, rows, columns, regularFont);

      document.add(table);
    } catch (Exception e) {
      logger.error("PDF 生成异常: ", e);
      throw new DataWriteException(e, "PDF 文件导出失败");
    } finally {
      if (document != null) {
        document.close();
      }
      if (pdfDocument != null) {
        pdfDocument.close();
      }
      if (pdfWriter != null) {
        try {
          pdfWriter.close();
        } catch (IOException e) {
          logger.error("PDF 导出器关闭异常: ", e);
        }
      }
    }
  }

  /**
   * 构建标题
   */
  private Paragraph buildTitle(@NonNull DataDefinition definition, @Nonnull PdfFont font) {
    String title = Strings.value(definition.getExportTitle(), "Unknown Title");

    return new Paragraph(title)
      .setFontSize(18)
      .setFont(font)
      .setTextAlignment(TextAlignment.CENTER)
      .setMarginBottom(16f);
  }

  private void createTableHeader(@NonNull Table table, @NonNull HeaderStructure headers, Style headerStyle, @NonNull PdfFont font) {
    String background = headerStyle.getBackground();
    for (HeaderCell hc : headers.firstRow()) {
      Cell cell = buildTableHeaderCell(hc, headerStyle, font, background);
      table.addHeaderCell(cell);
    }

    if (!headers.isSingleHeader()) {
      for (HeaderCell hc : headers.secondRow()) {
        Cell cell = buildTableHeaderCell(hc, headerStyle, font, background);
        table.addHeaderCell(cell);
      }
    }
  }

  private Cell buildTableHeaderCell(HeaderCell hc, Style headerStyle, PdfFont font, String background) {
    Alignment alignment = headerStyle.getAlign();

    Cell cell = new Cell(hc.rowSpan(), hc.colSpan());
    Paragraph paragraph = new Paragraph(hc.text());
    paragraph.setFont(font);
    paragraph.setFontSize(headerStyle.getFont().getSize() / reductionFactor);
    paragraph.setFontColor(parseColor(headerStyle.getFont().getColor()));
    cell.add(paragraph);
    cell.setBackgroundColor(parseColor(background));
    cell.setTextAlignment(safeConvertTextAlignment(alignment));
    cell.setVerticalAlignment(safeConvertVerticalAlignment(alignment));

    return cell;
  }

  private void createTableBody(@NonNull Table table,
                               @NonNull List<Map<String, Object>> rows,
                               @Nonnull Map<String, Column> columns,
                               @Nonnull PdfFont font) {
    int index = 0;
    for (Map<String, Object> row : rows) {
      createTableBodyRow(table, row, columns, font, index++);
    }
  }

  private void createTableBodyRow(@NonNull Table table,
                                  @Nonnull Map<String, Object> row,
                                  @Nonnull Map<String, Column> columns,
                                  @Nonnull PdfFont font,
                                  int rowIndex) {
    for (Map.Entry<String, Column> entry : columns.entrySet()) {
      String fieldName = entry.getKey();
      Column column = entry.getValue();

      Object value = row.get(fieldName);
      String text = Strings.value(value, "");
      Cell cell = buildTableBodyCell(text, column, font, rowIndex);
      table.addCell(cell);
    }
  }

  private Cell buildTableBodyCell(String text, Column column, PdfFont font, int rowIndex) {
    boolean highlight = rowIndex % 2 == 1;
    Alignment alignment = column.getExporting().getStyle().getAlign();

    Cell cell = new Cell();
    Paragraph paragraph = new Paragraph(text);
    paragraph.setFont(font);
    paragraph.setFontSize(column.getExporting().getStyle().getFont().getSize() / reductionFactor);
    paragraph.setFontColor(parseColor(column.getExporting().getStyle().getFont().getColor()));
    cell.add(paragraph);
    cell.setBackgroundColor(highlight ? DATA_ROW_HIGHLIGHT_BACKGROUND : DATA_ROW_DEFAULT_BACKGROUND);
    cell.setTextAlignment(safeConvertTextAlignment(alignment));
    cell.setVerticalAlignment(safeConvertVerticalAlignment(alignment));

    return cell;
  }

  private TextAlignment safeConvertTextAlignment(Alignment alignment) {
    if (alignment == null) {
      return TextAlignment.CENTER;
    }

    return switch (alignment) {
      case LEFT -> TextAlignment.LEFT;
      case CENTER -> TextAlignment.CENTER;
      case RIGHT -> TextAlignment.RIGHT;
      case JUSTIFY -> TextAlignment.JUSTIFIED;
      default -> TextAlignment.CENTER;
    };
  }

  private VerticalAlignment safeConvertVerticalAlignment(Alignment alignment) {
    if (alignment == null) {
      return VerticalAlignment.MIDDLE;
    }

    return switch (alignment) {
      case TOP -> VerticalAlignment.TOP;
      case CENTER -> VerticalAlignment.MIDDLE;
      case BOTTOM -> VerticalAlignment.BOTTOM;
      default -> VerticalAlignment.MIDDLE;
    };
  }

  /**
   * 解析颜色字符串为 iText Color
   */
  private Color parseColor(String color) {
    if (color == null || color.isEmpty()) {
      return null;
    }
    try {
      if (color.startsWith("#")) {
        String hex = color.substring(1);
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return new DeviceRgb(r, g, b);
      }
    } catch (Exception e) {
      System.out.println("警告: 无法解析颜色: " + color);
    }
    return null;
  }
}