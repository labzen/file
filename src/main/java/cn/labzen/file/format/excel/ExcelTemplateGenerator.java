package cn.labzen.file.format.excel;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.Column;
import cn.labzen.file.definition.bean.column.Importing;
import cn.labzen.file.exception.DataReadException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Excel 导入模板生成器
 * <p>
 * 生成包含以下结构的Excel模板文件：
 * <ul>
 *   <li>Row 1: 代码标识行（字段名），#列为HEADER，浅蓝背景</li>
 *   <li>Row 2: 人类阅读行（i18n表头文本 + 批注格式提示），#列为HEADER，浅蓝背景</li>
 *   <li>Row 3: 示例数据行（可选，来自mock.json），#列为MOCK，浅黄背景</li>
 *   <li>Row 4+: 用户数据区，#列为序号，白色背景</li>
 * </ul>
 *
 * @author labzen
 */
public class ExcelTemplateGenerator {

  private static final String MARKER_HEADER = "HEADER";
  private static final String MARKER_MOCK = "MOCK";

  // 颜色
  private static final short LIGHT_BLUE_INDEX = IndexedColors.PALE_BLUE.getIndex();
  private static final short LIGHT_YELLOW_INDEX = IndexedColors.LIGHT_YELLOW.getIndex();

  /**
   * 生成Excel模板
   */
  public static void generate(DataDefinition definition, String locale, OutputStream outputStream) {
    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet(definition.getTitle() != null ? definition.getTitle() : definition.getDomainName());

      // 创建样式
      CellStyle blueStyle = createBlueStyle(workbook);
      CellStyle yellowStyle = createYellowStyle(workbook);

      List<String> fieldNames = new ArrayList<>(definition.getColumns().keySet());
      int columnCount = fieldNames.size() + 1; // +1 for # column

      // ── Row 1: 代码标识行 ──
      Row row1 = sheet.createRow(0);
      Cell marker1 = row1.createCell(0);
      marker1.setCellValue(MARKER_HEADER);
      marker1.setCellStyle(blueStyle);

      for (int i = 0; i < fieldNames.size(); i++) {
        Cell cell = row1.createCell(i + 1);
        cell.setCellValue(fieldNames.get(i));
        cell.setCellStyle(blueStyle);

        // 添加批注：完整填写说明
        addHeaderComment(sheet, cell, definition.getColumns().get(fieldNames.get(i)), fieldNames.get(i));
      }

      // ── Row 2: 人类阅读行 + 格式提示批注 ──
      Row row2 = sheet.createRow(1);
      Cell marker2 = row2.createCell(0);
      marker2.setCellValue(MARKER_HEADER);
      marker2.setCellStyle(blueStyle);

      for (int i = 0; i < fieldNames.size(); i++) {
        Column column = definition.getColumns().get(fieldNames.get(i));
        Cell cell = row2.createCell(i + 1);
        String headerText = column.getHeader() != null ? column.getHeader() : fieldNames.get(i);
        cell.setCellValue(headerText);
        cell.setCellStyle(blueStyle);

        // 添加格式提示批注
        addHintComment(sheet, cell, column);
      }

      // ── Row 3: 示例数据行（可选）──
      if (definition.getMockData() != null && !definition.getMockData().isEmpty()) {
        for (int mockIdx = 0; mockIdx < definition.getMockData().size(); mockIdx++) {
          Map<String, String> mockRow = definition.getMockData().get(mockIdx);
          Row mockRowObj = sheet.createRow(2 + mockIdx);

          Cell mockMarker = mockRowObj.createCell(0);
          mockMarker.setCellValue(MARKER_MOCK);
          mockMarker.setCellStyle(yellowStyle);

          for (int i = 0; i < fieldNames.size(); i++) {
            Cell cell = mockRowObj.createCell(i + 1);
            String value = mockRow.get(fieldNames.get(i));
            cell.setCellValue(value != null ? value : "");
            cell.setCellStyle(yellowStyle);
          }

          // 添加MOCK批注（仅第一行mock数据）
          if (mockIdx == 0) {
            addComment(sheet, mockMarker, "示例数据，仅供参考，导入时将自动忽略");
          }
        }
      }

      // 设置列宽
      sheet.setColumnWidth(0, 3000); // # 列
      for (int i = 0; i < fieldNames.size(); i++) {
        sheet.setColumnWidth(i + 1, 5000);
      }

      // 为mapping列添加数据验证（下拉列表）
      int dataStartRow = definition.getMockData() != null && !definition.getMockData().isEmpty()
        ? 2 + definition.getMockData().size() : 2;
      addDataValidation(sheet, definition, fieldNames, dataStartRow);

      workbook.write(outputStream);
    } catch (Exception e) {
      throw new DataReadException(e, "生成Excel模板失败");
    }
  }

  private static CellStyle createBlueStyle(XSSFWorkbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.setFillForegroundColor(LIGHT_BLUE_INDEX);
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    Font font = workbook.createFont();
    font.setFontName("Arial");
    font.setFontHeightInPoints((short) 11);
    style.setFont(font);
    style.setLocked(true);
    return style;
  }

  private static CellStyle createYellowStyle(XSSFWorkbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.setFillForegroundColor(LIGHT_YELLOW_INDEX);
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    Font font = workbook.createFont();
    font.setFontName("Arial");
    font.setFontHeightInPoints((short) 11);
    style.setFont(font);
    return style;
  }

  private static void addHeaderComment(Sheet sheet, Cell cell, Column column, String fieldName) {
    StringBuilder sb = new StringBuilder();
    sb.append("【").append(column.getHeader() != null ? column.getHeader() : fieldName).append("】\n");

    // 类型
    Class<?> fieldType = null;
    try {
      fieldType = Class.class; // 简化，实际需要从Bean类获取
    } catch (Exception e) {
      // ignore
    }

    Importing importing = column.getImporting();
    if (importing != null) {
      if (importing.isRequired()) sb.append("必填：是\n");
      if (importing.getMaxLength() != null) sb.append("最大长度：").append(importing.getMaxLength()).append("字\n");
      if (importing.getMin() != null || importing.getMax() != null) {
        sb.append("范围：").append(importing.getMin() != null ? importing.getMin() : "-")
          .append("~").append(importing.getMax() != null ? importing.getMax() : "-").append("\n");
      }
    }

    if (column.getMapping() != null) {
      sb.append("允许值：\n");
      column.getMapping().forEach((k, v) -> sb.append("  ").append(k).append(" = ").append(v).append("\n"));
    }

    sb.append("\n代码标识行，请勿修改，否则导入将失败");
    addComment(sheet, cell, sb.toString());
  }

  private static void addHintComment(Sheet sheet, Cell cell, Column column) {
    Importing importing = column.getImporting();
    if (importing == null) return;

    List<String> hints = new ArrayList<>();
    if (importing.isRequired()) hints.add("*必填");
    else hints.add("选填");
    if (importing.getMaxLength() != null) hints.add("≤" + importing.getMaxLength() + "字");
    if (importing.getMin() != null || importing.getMax() != null) {
      hints.add((importing.getMin() != null ? importing.getMin() : "-") + "~" + (importing.getMax() != null ? importing.getMax() : "-"));
    }

    if (column.getMapping() != null) {
      List<String> mappingHints = new ArrayList<>();
      column.getMapping().forEach((k, v) -> mappingHints.add(k + "=" + v));
      hints.add(String.join("/", mappingHints));
    }

    if (!hints.isEmpty()) {
      addComment(sheet, cell, String.join(" | ", hints));
    }
  }

  private static void addComment(Sheet sheet, Cell cell, String text) {
    Drawing<?> drawing = sheet.createDrawingPatriarch();
    CreationHelper factory = sheet.getWorkbook().getCreationHelper();
    Comment comment = drawing.createCellComment(factory.createClientAnchor());
    comment.setString(factory.createRichTextString(text));
    cell.setCellComment(comment);
  }

  private static void addDataValidation(Sheet sheet, DataDefinition definition,
                                        List<String> fieldNames, int dataStartRow) {
    for (int i = 0; i < fieldNames.size(); i++) {
      Column column = definition.getColumns().get(fieldNames.get(i));
      if (column.getMapping() != null && !column.getMapping().isEmpty()) {
        String[] options = column.getMapping().values().toArray(new String[0]);
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(options);
        CellRangeAddressList addressList = new CellRangeAddressList(
          dataStartRow, 1000, i + 1, i + 1);
        DataValidation validation = validationHelper.createValidation(constraint, addressList);
        validation.setShowErrorBox(true);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        sheet.addValidationData(validation);
      }
    }
  }
}
