package cn.labzen.file.format.excel;

import cn.labzen.file.definition.bean.column.TableColumn;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Map;

/**
 * Excel 列宽处理器
 */
public final class ExcelColumnWidthHandler {

  private static final int DEFAULT_WIDTH = 10;
  private static final int WIDTH_MULTIPLIER = 256;

  private final Map<String, TableColumn> columns;

  public ExcelColumnWidthHandler(Map<String, TableColumn> columns) {
    this.columns = columns;
  }

  public void applyColumnWidths(Sheet sheet) {
    int colIndex = 0;
    for (Map.Entry<String, TableColumn> entry : columns.entrySet()) {
      TableColumn column = entry.getValue();

      Integer width = column.getWidth();
      if (width == null || width <= 0) {
        width = DEFAULT_WIDTH;
      }

      sheet.setColumnWidth(colIndex, width * WIDTH_MULTIPLIER);
      colIndex++;
    }
  }
}