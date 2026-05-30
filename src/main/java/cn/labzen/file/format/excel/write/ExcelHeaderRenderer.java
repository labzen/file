package cn.labzen.file.format.excel.write;

import cn.labzen.file.definition.bean.style.Style;
import cn.labzen.file.definition.bean.table.HeaderCell;
import cn.labzen.file.definition.bean.table.HeaderStructure;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Excel 表头渲染器
 * <p>
 * 消费 {@link HeaderStructure}，将预计算好的表头结构渲染到 Excel Sheet 中，
 * 自动处理横向合并（colSpan）和纵向合并（rowSpan）。
 * <p>
 * 参考 {@link cn.labzen.file.format.pdf.PdfFileWriter#createTableHeader} 的设计思路，
 * 表头结构在定义阶段即由 {@link cn.labzen.file.definition.bean.table.HeaderBuilder} 计算完成，
 * 渲染器只需按结构直接输出，无需在写入阶段重新计算合并逻辑。
 *
 * @author labzen
 */
public final class ExcelHeaderRenderer {

  private final ExcelWorkbookContext context;

  public ExcelHeaderRenderer(ExcelWorkbookContext context) {
    this.context = context;
  }

  /**
   * 渲染表头到 Sheet
   *
   * @param headers     预计算的表头结构
   * @param headerStyle 表头样式
   * @return 表头所占行数
   */
  public int render(HeaderStructure headers, Style headerStyle) {
    Sheet sheet = context.sheet();
    int headerRowCount = headers.isSingleHeader() ? 1 : 2;

    // 渲染第一行表头
    Row firstRow = sheet.createRow(0);
    for (HeaderCell cell : headers.firstRow()) {
      renderHeaderCell(firstRow, cell, headerStyle, headerRowCount);
    }

    // 渲染第二行表头（仅多级表头时）
    if (!headers.isSingleHeader()) {
      Row secondRow = sheet.createRow(1);
      for (HeaderCell cell : headers.secondRow()) {
        renderHeaderCell(secondRow, cell, headerStyle, 1);
      }
    }

    return headerRowCount;
  }

  private void renderHeaderCell(Row row, HeaderCell headerCell, Style headerStyle, int totalHeaderRows) {
    Sheet sheet = context.sheet();
    int col = headerCell.index();

    Cell cell = row.createCell(col);
    cell.setCellValue(headerCell.text());
    context.styleApplier().applyHeaderStyle(cell, headerStyle);

    // 横向合并：同一行内跨越多列
    if (headerCell.colSpan() > 1) {
      int lastCol = col + headerCell.colSpan() - 1;
      sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), col, lastCol));
    }

    // 纵向合并：跨越多行（如单级列在多级表头中纵向合并）
    if (headerCell.rowSpan() > 1 && totalHeaderRows > 1) {
      int lastRow = row.getRowNum() + headerCell.rowSpan() - 1;
      sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), lastRow, col, col));
    }
  }
}
