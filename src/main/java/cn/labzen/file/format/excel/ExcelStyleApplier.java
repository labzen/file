package cn.labzen.file.format.excel;

import cn.labzen.file.definition.bean.column.TableColumn;
import cn.labzen.file.definition.bean.style.Font;
import cn.labzen.file.definition.bean.style.Style;
import cn.labzen.file.definition.enums.Alignment;
import cn.labzen.file.definition.enums.BorderWidth;
import com.google.common.collect.Maps;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Map;

/**
 * Excel 样式应用器
 * <p>
 * 合并原 {@code ExcelCellStyleHandler} 和 {@code ExcelStyleBuilder} 的职责，
 * 统一管理 CellStyle 的创建、缓存和应用。
 * <p>
 * 样式优先级（数据单元格）：
 * <ol>
 *   <li>列定义中的样式（column.getStyle()）</li>
 *   <li>全局默认列样式（defaultColumnStyle）</li>
 *   <li>系统默认值</li>
 * </ol>
 *
 * @author labzen
 */
public final class ExcelStyleApplier {

  private static final short DEFAULT_BORDER_COLOR = 8;
  private static final String DEFAULT_BACKGROUND = "#FFFFFF";

  private final Workbook workbook;
  private final ColorConvert colorConvert;
  private final Map<String, CellStyle> styleCache = Maps.newHashMap();

  public ExcelStyleApplier(Workbook workbook) {
    this.workbook = workbook;
    this.colorConvert = new ColorConvert();
  }

  /**
   * 应用表头样式到单元格
   *
   * @param cell        目标单元格
   * @param headerStyle 表头样式
   */
  public void applyHeaderStyle(Cell cell, Style headerStyle) {
    if (headerStyle == null) {
      return;
    }
    CellStyle cellStyle = buildCellStyle(headerStyle);
    if (cellStyle != null) {
      cell.setCellStyle(cellStyle);
    }
  }

  /**
   * 应用数据单元格样式
   * <p>
   * 按优先级合并列定义样式和全局默认样式。
   *
   * @param cell               目标单元格
   * @param column             列定义
   * @param defaultColumnStyle 全局默认列样式
   */
  public void applyDataStyle(Cell cell, TableColumn column, Style defaultColumnStyle) {
    Style style = resolveDataStyle(column, defaultColumnStyle);
    CellStyle cellStyle = buildCellStyle(style);
    if (cellStyle != null) {
      cell.setCellStyle(cellStyle);
    }
  }

  // ==================== CellStyle 构建与缓存 ====================

  private CellStyle buildCellStyle(Style style) {
    if (style == null) {
      return null;
    }
    String cacheKey = buildCacheKey(style);
    return styleCache.computeIfAbsent(cacheKey, k -> createCellStyle(style));
  }

  private String buildCacheKey(Style style) {
    return String.format("%s-%s-%s-%s-%s-%s",
      style.getAlign(),
      style.getBackground(),
      style.getWrapped(),
      style.getFont() != null ? style.getFont().getFamily() : "",
      style.getFont() != null ? style.getFont().getSize() : "",
      style.getFont() != null ? style.getFont().getColor() : "");
  }

  private CellStyle createCellStyle(Style style) {
    CellStyle cellStyle = workbook.createCellStyle();

    applyAlignment(cellStyle, style.getAlign());
    applyBackground(cellStyle, style.getBackground());

    if (style.getFont() != null) {
      applyFont(cellStyle, style.getFont());
    }

    if (Boolean.TRUE.equals(style.getWrapped())) {
      cellStyle.setWrapText(true);
    }

    return cellStyle;
  }

  // ==================== 样式属性应用 ====================

  private void applyAlignment(CellStyle cellStyle, Alignment alignment) {
    if (alignment == null) {
      alignment = Alignment.CENTER;
    }
    cellStyle.setAlignment(convertHorizontalAlignment(alignment));
    cellStyle.setVerticalAlignment(convertVerticalAlignment(alignment));
  }

  private HorizontalAlignment convertHorizontalAlignment(Alignment alignment) {
    return switch (alignment) {
      case LEFT -> HorizontalAlignment.LEFT;
      case RIGHT -> HorizontalAlignment.RIGHT;
      case JUSTIFY -> HorizontalAlignment.JUSTIFY;
      case DISTRIBUTED -> HorizontalAlignment.DISTRIBUTED;
      case FILL -> HorizontalAlignment.FILL;
      default -> HorizontalAlignment.CENTER;
    };
  }

  private VerticalAlignment convertVerticalAlignment(Alignment alignment) {
    return switch (alignment) {
      case TOP -> VerticalAlignment.TOP;
      case BOTTOM -> VerticalAlignment.BOTTOM;
      case JUSTIFY -> VerticalAlignment.JUSTIFY;
      case DISTRIBUTED -> VerticalAlignment.DISTRIBUTED;
      default -> VerticalAlignment.CENTER;
    };
  }

  private void applyBackground(CellStyle cellStyle, String background) {
    if (background == null || background.isEmpty()) {
      return;
    }
    if (workbook instanceof XSSFWorkbook && cellStyle instanceof XSSFCellStyle xssfCellStyle) {
      xssfCellStyle.setFillForegroundColor(colorConvert.getXssfColor(background));
      cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }
  }

  private BorderStyle convertBorderStyle(BorderWidth borderWidth) {
    if (borderWidth == null) {
      return BorderStyle.THIN;
    }
    return switch (borderWidth) {
      case MEDIUM -> BorderStyle.MEDIUM;
      case THICK -> BorderStyle.THICK;
      case DOUBLE -> BorderStyle.DOUBLE;
      default -> BorderStyle.THIN;
    };
  }

  private void applyFont(CellStyle cellStyle, Font fontConfig) {
    org.apache.poi.ss.usermodel.Font newFont = workbook.createFont();

    if (fontConfig.getFamily() != null) {
      newFont.setFontName(fontConfig.getFamily());
    }
    if (fontConfig.getSize() != null) {
      newFont.setFontHeightInPoints(fontConfig.getSize().shortValue());
    }
    if (Boolean.TRUE.equals(fontConfig.getBold())) {
      newFont.setBold(true);
    }
    if (Boolean.TRUE.equals(fontConfig.getItalic())) {
      newFont.setItalic(true);
    }

    if (fontConfig.getColor() != null && !fontConfig.getColor().isEmpty()
      && workbook instanceof XSSFWorkbook) {
      ((XSSFFont) newFont).setColor(colorConvert.getXssfColor(fontConfig.getColor()));
    }

    cellStyle.setFont(newFont);
  }

  // ==================== 数据样式解析 ====================

  /**
   * 按优先级解析数据单元格的最终样式
   * <p>
   * 优先级：column.getStyle() > defaultColumnStyle > 系统默认值
   */
  private Style resolveDataStyle(TableColumn column, Style defaultColumnStyle) {
    Style style = new Style();

    // 背景色
    style.setBackground(resolveBackground(column, defaultColumnStyle));

    // 字体
    style.setFont(resolveFont(column, defaultColumnStyle));

    // 自动换行
    style.setWrapped(resolveWrapped(column, defaultColumnStyle));

    // 对齐方式
    style.setAlign(resolveAlignment(column, defaultColumnStyle));

    return style;
  }

  private String resolveBackground(TableColumn column, Style defaultColumnStyle) {
    String background = null;
    if (column.getStyle() != null) {
      background = column.getStyle().getBackground();
    }
    if (background == null || background.isEmpty()) {
      background = defaultColumnStyle != null ? defaultColumnStyle.getBackground() : null;
    }
    if (background == null || background.isEmpty()) {
      background = DEFAULT_BACKGROUND;
    }
    return background;
  }

  private Font resolveFont(TableColumn column, Style defaultColumnStyle) {
    Font font = null;
    if (column.getStyle() != null && column.getStyle().getFont() != null
      && column.getStyle().getFont().getColor() != null
      && !column.getStyle().getFont().getColor().isEmpty()) {
      font = column.getStyle().getFont();
    } else if (defaultColumnStyle != null && defaultColumnStyle.getFont() != null) {
      font = defaultColumnStyle.getFont();
    }
    if (font == null) {
      font = new Font();
    }
    return font;
  }

  private boolean resolveWrapped(TableColumn column, Style defaultColumnStyle) {
    Boolean wrapped = null;
    if (column.getStyle() != null && column.getStyle().getWrapped() != null) {
      wrapped = column.getStyle().getWrapped();
    } else if (defaultColumnStyle != null) {
      wrapped = defaultColumnStyle.getWrapped();
    }
    return wrapped != null ? wrapped : true;
  }

  private Alignment resolveAlignment(TableColumn column, Style defaultColumnStyle) {
    Alignment align = null;
    if (column.getStyle() != null) {
      align = column.getStyle().getAlign();
    }
    if (align == null) {
      align = defaultColumnStyle != null ? defaultColumnStyle.getAlign() : null;
    }
    return align != null ? align : Alignment.CENTER;
  }
}
