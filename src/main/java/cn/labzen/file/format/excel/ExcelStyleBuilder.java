package cn.labzen.file.format.excel;

import cn.labzen.file.definition.bean.style.Border;
import cn.labzen.file.definition.bean.style.Font;
import cn.labzen.file.definition.bean.style.Style;
import cn.labzen.file.definition.enums.Alignment;
import cn.labzen.file.definition.enums.BorderWidth;
import com.google.common.collect.Maps;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Map;

/**
 * Excel 样式构建器
 */
public final class ExcelStyleBuilder {

  private static final short DEFAULT_BORDER_COLOR = 8;

  private final Workbook workbook;
  private final ColorConvert colorConvert;
  private final Map<String, CellStyle> styleCache = Maps.newHashMap();

  public ExcelStyleBuilder(Workbook workbook) {
    this.workbook = workbook;
    this.colorConvert = new ColorConvert();
  }

  public CellStyle build(Style style) {
    if (style == null) {
      return null;
    }

    String cacheKey = buildCacheKey(style);
    return styleCache.computeIfAbsent(cacheKey, k -> createCellStyle(style));
  }

  private String buildCacheKey(Style style) {
    return String.format("%s-%s-%s-%s-%s-%s-%s",
      style.getAlign(),
      style.getBackground(),
      style.getWrapped(),
      style.getFont() != null ? style.getFont().getFamily() : "",
      style.getFont() != null ? style.getFont().getSize() : "",
      style.getFont() != null ? style.getFont().getColor() : "",
      style.getBorder() != null ? style.getBorder().getWidth() : "");
  }

  private CellStyle createCellStyle(Style style) {
    CellStyle cellStyle = workbook.createCellStyle();

    applyAlignment(cellStyle, style.getAlign());
    applyBackground(cellStyle, style.getBackground());

    if (style.getBorder() != null) {
      applyBorder(cellStyle, style.getBorder());
    }

    if (style.getFont() != null) {
      applyFont(cellStyle, style.getFont());
    }

    if (style.getWrapped() != null && style.getWrapped()) {
      cellStyle.setWrapText(true);
    }

    return cellStyle;
  }

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
      cellStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
    }
  }

  private void applyBorder(CellStyle cellStyle, Border border) {
    BorderStyle borderStyle = convertBorderStyle(border.getWidth());
    cellStyle.setBorderTop(borderStyle);
    cellStyle.setBorderRight(borderStyle);
    cellStyle.setBorderBottom(borderStyle);
    cellStyle.setBorderLeft(borderStyle);
    cellStyle.setTopBorderColor(DEFAULT_BORDER_COLOR);
    cellStyle.setRightBorderColor(DEFAULT_BORDER_COLOR);
    cellStyle.setBottomBorderColor(DEFAULT_BORDER_COLOR);
    cellStyle.setLeftBorderColor(DEFAULT_BORDER_COLOR);
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
    if (fontConfig.getBold() != null && fontConfig.getBold()) {
      newFont.setBold(true);
    }
    if (fontConfig.getItalic() != null && fontConfig.getItalic()) {
      newFont.setItalic(true);
    }

    if (fontConfig.getColor() != null && !fontConfig.getColor().isEmpty()) {
      if (workbook instanceof XSSFWorkbook) {
        org.apache.poi.xssf.usermodel.XSSFFont xssfFont = (org.apache.poi.xssf.usermodel.XSSFFont) newFont;
        xssfFont.setColor(colorConvert.getXssfColor(fontConfig.getColor()));
      }
    }

    cellStyle.setFont(newFont);
  }
}