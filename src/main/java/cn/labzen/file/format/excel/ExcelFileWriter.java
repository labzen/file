package cn.labzen.file.format.excel;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.TableColumn;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.exception.DataWriteException;
import cn.labzen.file.format.AbstractDataFileWriter;
import cn.labzen.file.meta.FileConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Excel 文件写入器
 *
 * @param <T> 数据对象类型
 * @author labzen
 */
@Slf4j
public final class ExcelFileWriter<T> extends AbstractDataFileWriter<T> {

  @Override
  public @NonNull FileFormat format() {
    return FileFormat.EXCEL;
  }

  @Override
  public void initialize(@NonNull FileConfiguration configuration) {

  }

  @Override
  protected void generateContent(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull OutputStream outputStream) {
    List<Map<String, Object>> rows = extractRows(definition, data);
    Map<String, TableColumn> columns = definition.getColumns();

    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("data");

      // 构建表头
      List<List<String>> head = buildHeaderList(columns);
      int headerRowCount = head.size();
      List<Integer> headerLevel = calculateHeaderLevel(columns);

      // 写入表头
      for (int rowIdx = 0; rowIdx < headerRowCount; rowIdx++) {
        Row row = sheet.createRow(rowIdx);
        List<String> rowHeaders = head.get(rowIdx);
        for (int colIdx = 0; colIdx < rowHeaders.size(); colIdx++) {
          Cell cell = row.createCell(colIdx);
          cell.setCellValue(rowHeaders.get(colIdx));
        }
      }

      // 应用多级表头合并
      HeaderMergeHandler mergeHandler = new HeaderMergeHandler(columns, headerLevel);
      mergeHandler.applyMerges(sheet);

      // 写入数据
//      int startDataRow = headerRowCount;
      for (int rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
        Row row = sheet.createRow(headerRowCount + rowIdx);
        Map<String, Object> rowData = rows.get(rowIdx);

        int colIdx = 0;
        for (String colKey : columns.keySet()) {
          Cell cell = row.createCell(colIdx);
          Object value = rowData.get(colKey);
          if (value != null) {
            cell.setCellValue(value.toString());
          }
          colIdx++;
        }
      }

      // 应用列宽
      ExcelColumnWidthHandler columnWidthHandler = new ExcelColumnWidthHandler(columns);
      columnWidthHandler.applyColumnWidths(sheet);

      // 应用样式
      ExcelCellStyleHandler cellStyleHandler = new ExcelCellStyleHandler(
        columns, definition.getHeaderStyle(), definition.getColumnStyle(), workbook, headerLevel);

      // 表头样式
      for (int rowIdx = 0; rowIdx < headerRowCount; rowIdx++) {
        cellStyleHandler.applyStyles(sheet, rowIdx, true);
      }

      // 数据样式
      for (int rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
        cellStyleHandler.applyStyles(sheet, headerRowCount + rowIdx, false);
      }

      // 写入文件
      workbook.write(outputStream);

    } catch (Exception e) {
      throw new DataWriteException(e, "Excel 文件写入失败");
    }
  }

  private List<List<String>> buildHeaderList(Map<String, TableColumn> columns) {
    int maxLevel = columns.values().stream()
      .mapToInt(col -> col.getHeader() != null ? col.getHeader().size() : 0)
      .max()
      .orElse(1);

    List<List<String>> headers = new ArrayList<>();

    for (int level = 0; level < maxLevel; level++) {
      final int currentLevel = level;
      List<String> levelHeaders = new ArrayList<>();
      int colIdx = 0;
      for (TableColumn column : columns.values()) {
        List<String> header = column.getHeader();
        String headerText = "";

        if (header != null && !header.isEmpty()) {
          int headerCount = header.size();
          if (headerCount == 1) {
            // 只有一级表头：文字放在第一行（纵向合并后只显示第一行）
            if (currentLevel == 0) {
              headerText = header.get(0);
            }
          } else {
            // 多级表头：正常逻辑
            int index = header.size() - maxLevel + currentLevel;
            if (index >= 0 && index < header.size()) {
              headerText = header.get(index);
            }
          }
        }
        levelHeaders.add(headerText);
        colIdx++;
      }
      headers.add(levelHeaders);
    }

    return headers;
  }

  /**
   * 计算每个列的表头级别
   * <p>
   *
   * @return 每个列的表头级别列表，级别从1开始
   */
  private List<Integer> calculateHeaderLevel(Map<String, TableColumn> columns) {
    return columns.values().stream()
      .map(col -> col.getHeader() != null ? col.getHeader().size() : 1)
      .collect(Collectors.toList());
  }
}