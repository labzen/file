package cn.labzen.file.format.excel;

import cn.labzen.file.definition.bean.column.TableColumn;
import cn.labzen.file.definition.bean.style.Style;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.List;
import java.util.Map;

/**
 * Excel 数据行渲染器
 * <p>
 * 负责将数据行写入 Sheet，按列定义顺序填充单元格，并为每个单元格应用对应列的样式。
 *
 * @author labzen
 */
public final class ExcelDataRenderer {

  private final ExcelWorkbookContext context;

  public ExcelDataRenderer(ExcelWorkbookContext context) {
    this.context = context;
  }

  /**
   * 渲染数据行
   *
   * @param rows               数据行列表
   * @param startRow           起始行索引（表头之后的行）
   * @param defaultColumnStyle 默认列样式
   */
  public void render(List<Map<String, Object>> rows, int startRow, Style defaultColumnStyle) {
    Sheet sheet = context.sheet();

    for (int i = 0; i < rows.size(); i++) {
      Row row = sheet.createRow(startRow + i);
      renderRow(row, rows.get(i), defaultColumnStyle);
    }
  }

  private void renderRow(Row row, Map<String, Object> rowData, Style defaultColumnStyle) {
    int colIndex = 0;
    for (Map.Entry<String, TableColumn> entry : context.columns().entrySet()) {
      String fieldName = entry.getKey();
      TableColumn column = entry.getValue();

      Cell cell = row.createCell(colIndex);
      Object value = rowData.get(fieldName);
      if (value != null) {
        cell.setCellValue(value.toString());
      }

      context.styleApplier().applyDataStyle(cell, column, defaultColumnStyle);
      colIndex++;
    }
  }
}
