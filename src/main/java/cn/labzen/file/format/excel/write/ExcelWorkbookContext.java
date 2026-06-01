package cn.labzen.file.format.excel.write;

import cn.labzen.file.definition.bean.column.Column;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Map;

/**
 * Excel 工作簿上下文
 * <p>
 * 封装 POI 的 Workbook 和 Sheet 实例，以及列定义、样式应用器等共享资源，
 * 为表头渲染器和数据渲染器提供统一的上下文环境。
 *
 * @author labzen
 */
public final class ExcelWorkbookContext {

  private static final int DEFAULT_COLUMN_WIDTH = 10;
  private static final int WIDTH_UNIT = 256;

  private final Workbook workbook;
  private final Sheet sheet;
  private final Map<String, Column> columns;
  private final ExcelStyleApplier styleApplier;

  public ExcelWorkbookContext(Workbook workbook, String sheetName, Map<String, Column> columns) {
    this.workbook = workbook;
    this.sheet = workbook.createSheet(sheetName);
    this.columns = columns;
    this.styleApplier = new ExcelStyleApplier(workbook);
    applyColumnWidths();
  }

  public Workbook workbook() {
    return workbook;
  }

  public Sheet sheet() {
    return sheet;
  }

  public Map<String, Column> columns() {
    return columns;
  }

  public ExcelStyleApplier styleApplier() {
    return styleApplier;
  }

  public ExcelHeaderRenderer createHeaderRenderer() {
    return new ExcelHeaderRenderer(this);
  }

  public ExcelDataRenderer createDataRenderer() {
    return new ExcelDataRenderer(this);
  }

  /**
   * 应用列宽配置
   * <p>
   * 按列定义中的 width 值设置每列宽度，未配置时使用默认值。
   */
  private void applyColumnWidths() {
    int colIndex = 0;
    for (Column column : columns.values()) {
      int width = (column.getExporting().getWidth() != null && column.getExporting().getWidth() > 0)
        ? column.getExporting().getWidth()
        : DEFAULT_COLUMN_WIDTH;
      sheet.setColumnWidth(colIndex, width * WIDTH_UNIT);
      colIndex++;
    }
  }
}
