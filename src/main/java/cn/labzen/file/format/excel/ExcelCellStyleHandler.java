package cn.labzen.file.format.excel;

import cn.labzen.file.definition.bean.column.TableColumn;
import cn.labzen.file.definition.bean.style.Style;
import cn.labzen.file.definition.enums.Alignment;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Excel 单元格样式处理器
 * <p>
 * 处理表头和数据单元格的样式设置：
 * <ul>
 *   <li>一级表头/非最低级表头存在合并时：居中对齐（水平和垂直居中）</li>
 *   <li>最低级表头：使用 column 定义中的 align 对齐方式</li>
 *   <li>数据单元格：使用 column 定义中的 align 对齐方式</li>
 * </ul>
 */
public final class ExcelCellStyleHandler {

  private final Map<String, TableColumn> columns;
  private final List<Integer> headerLevel;
  private final Style headerStyle;
  private final Style columnStyle;
  private final ExcelStyleBuilder styleBuilder;

  public ExcelCellStyleHandler(Map<String, TableColumn> columns, Style headerStyle, Style columnStyle,
                               org.apache.poi.ss.usermodel.Workbook workbook, List<Integer> headerLevel) {
    this.columns = columns;
    this.headerLevel = headerLevel;
    this.headerStyle = headerStyle;
    this.columnStyle = columnStyle;
    this.styleBuilder = new ExcelStyleBuilder(workbook);
  }

  /**
   * 应用样式到指定行
   *
   * @param sheet        工作表
   * @param rowIndex     行索引（0-based）
   * @param isHead       是否为表头行
   */
  public void applyStyles(Sheet sheet, int rowIndex, boolean isHead) {
    Row row = sheet.getRow(rowIndex);
    if (row == null) {
      return;
    }

    int colIndex = 0;
    for (Map.Entry<String, TableColumn> entry : columns.entrySet()) {
      TableColumn column = entry.getValue();
      Cell cell = row.getCell(colIndex);

      if (cell != null) {
        Style style = resolveStyle(sheet, rowIndex, colIndex, column, isHead);
        if (style != null) {
          org.apache.poi.ss.usermodel.CellStyle cellStyle = styleBuilder.build(style);
          if (cellStyle != null) {
            cell.setCellStyle(cellStyle);
          }
        }
      }
      colIndex++;
    }
  }

  /**
   * 解析单元格样式
   *
   * @param sheet    工作表
   * @param rowIndex 行索引
   * @param colIndex 列索引
   * @param column   列定义
   * @param isHead   是否为表头行
   * @return 单元格样式
   */
  private Style resolveStyle(Sheet sheet, int rowIndex, int colIndex, TableColumn column, boolean isHead) {
    if (isHead) {
      return resolveHeaderStyle(sheet, rowIndex, colIndex, column);
    } else {
      return resolveDataStyle(column);
    }
  }

  /**
   * 解析表头样式
   * <p>
   * 判断逻辑：
   * - 如果是一级表头行且单元格在横向合并区域中：居中
   * - 其他情况（多级表头的非一级行、单级表头列）：使用 column 定义的对齐方式
   */
  private Style resolveHeaderStyle(Sheet sheet, int rowIndex, int colIndex, TableColumn column) {
    // 获取当前列的表头级别
    int colLevel = getColumnHeaderLevel(colIndex);

    // 如果是一级表头行（第0行）且存在横向跨列合并，需要居中
    if (rowIndex == 0 && colLevel > 1) {
      // 检查是否存在横向合并（跨越多列）
      if (isInHorizontalMerge(sheet, rowIndex, colIndex)) {
        return createCenterStyle(headerStyle);
      }
    }

    // 其他情况：使用 column 定义的对齐方式（包括单级表头和多级表头的非一级行）
    return resolveDataStyle(column);
  }

  /**
   * 解析数据样式
   * <p>
   * 样式优先级：column.getStyle() > columnStyle > 默认值
   */
  private Style resolveDataStyle(TableColumn column) {
    Style style = new Style();

    // 背景色：优先使用列定义，否则使用 columnStyle，再否则使用默认白色
    String background = null;
    if (column.getStyle() != null) {
      background = column.getStyle().getBackground();
    }
    if (background == null || background.isEmpty()) {
      background = columnStyle != null ? columnStyle.getBackground() : null;
    }
    if (background == null || background.isEmpty()) {
      background = "#FFFFFF";  // 默认白色背景
    }
    style.setBackground(background);

    // 字体
    cn.labzen.file.definition.bean.style.Font font = null;
    if (column.getStyle() != null && column.getStyle().getFont() != null
        && column.getStyle().getFont().getColor() != null
        && !column.getStyle().getFont().getColor().isEmpty()) {
      font = column.getStyle().getFont();
    } else if (columnStyle != null && columnStyle.getFont() != null) {
      font = columnStyle.getFont();
    }
    if (font == null) {
      font = new cn.labzen.file.definition.bean.style.Font();
    }
    style.setFont(font);

    // 边框
    if (columnStyle != null && columnStyle.getBorder() != null) {
      style.setBorder(columnStyle.getBorder());
    }

    // 换行
    Boolean wrapped = null;
    if (column.getStyle() != null && column.getStyle().getWrapped() != null) {
      wrapped = column.getStyle().getWrapped();
    } else if (columnStyle != null) {
      wrapped = columnStyle.getWrapped();
    }
    if (wrapped == null) {
      wrapped = true;
    }
    style.setWrapped(wrapped);

    // 对齐方式
    Alignment align = null;
    if (column.getStyle() != null) {
      align = column.getStyle().getAlign();
    }
    if (align == null) {
      align = columnStyle != null ? columnStyle.getAlign() : null;
    }
    if (align == null) {
      align = Alignment.CENTER;  // 默认居中
    }
    style.setAlign(align);

    return style;
  }

  /**
   * 获取指定列的表头级别
   */
  private int getColumnHeaderLevel(int colIndex) {
    if (colIndex >= 0 && colIndex < headerLevel.size()) {
      return headerLevel.get(colIndex);
    }
    return 1;
  }

  /**
   * 检查单元格是否在横向合并区域中（跨越多列）
   * <p>
   * 如果单元格所在的合并区域跨越了多列，则认为是横向合并
   */
  private boolean isInHorizontalMerge(Sheet sheet, int rowIndex, int colIndex) {
    int numMergedRegions = sheet.getNumMergedRegions();
    for (int i = 0; i < numMergedRegions; i++) {
      CellRangeAddress region = sheet.getMergedRegion(i);
      if (region.isInRange(rowIndex, colIndex)) {
        // 如果合并区域跨越了多列（横向合并）
        if (region.getLastColumn() - region.getFirstColumn() > 0) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * 检查单元格是否被合并（包括横向和纵向）
   */
  private boolean isCellMerged(Sheet sheet, int rowIndex, int colIndex) {
    int numMergedRegions = sheet.getNumMergedRegions();
    for (int i = 0; i < numMergedRegions; i++) {
      CellRangeAddress region = sheet.getMergedRegion(i);
      if (region.isInRange(rowIndex, colIndex)) {
        // 如果当前单元格不是合并区域的起始单元格，则认为是合并单元格
        if (region.getFirstRow() != rowIndex || region.getFirstColumn() != colIndex) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * 创建居中样式（水平和垂直居中）
   */
  private Style createCenterStyle(Style baseStyle) {
    if (baseStyle == null) {
      return new Style();
    }
    Style centerStyle = new Style();
    centerStyle.setBackground(baseStyle.getBackground());
    centerStyle.setFont(baseStyle.getFont());
    centerStyle.setBorder(baseStyle.getBorder());
    centerStyle.setWrapped(baseStyle.getWrapped());
    centerStyle.setAlign(Alignment.CENTER);  // 居中
    return centerStyle;
  }

  /**
   * 根据对齐方式创建样式
   */
  private Style createAlignedStyle(Style baseStyle, Alignment alignment) {
    if (baseStyle == null) {
      Style style = new Style();
      style.setAlign(alignment);
      return style;
    }
    Style alignedStyle = new Style();
    alignedStyle.setBackground(baseStyle.getBackground());
    alignedStyle.setFont(baseStyle.getFont());
    alignedStyle.setBorder(baseStyle.getBorder());
    alignedStyle.setWrapped(baseStyle.getWrapped());
    alignedStyle.setAlign(alignment);
    return alignedStyle;
  }
}