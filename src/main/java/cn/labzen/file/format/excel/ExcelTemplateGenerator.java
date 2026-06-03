package cn.labzen.file.format.excel;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.Column;
import cn.labzen.file.definition.bean.column.Importing;
import cn.labzen.file.definition.bean.column.constraint.DateRange;
import cn.labzen.file.definition.bean.column.constraint.LengthRange;
import cn.labzen.file.definition.bean.column.constraint.NumericRange;
import cn.labzen.file.definition.bean.table.HeaderBuilder;
import cn.labzen.file.exception.DataReadException;
import cn.labzen.file.exception.DataWriteException;
import cn.labzen.file.format.excel.template.ExcelTemplateI18nDefaultStore;
import cn.labzen.file.i18n.I18nStoreProvider;
import cn.labzen.tool.util.Collections;
import cn.labzen.tool.util.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DataValidationConstraint.OperatorType;
import org.apache.poi.ss.usermodel.DataValidationConstraint.ValidationType;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.labzen.file.format.excel.template.ExcelTemplateI18nKeys.*;

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
@Slf4j
public final class ExcelTemplateGenerator {

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

  private static final String MARKER_TEXT_CODE = "CODE";
  private static final String MARKER_TEXT_HINT = "HINT";
  private static final String MARKER_MOCK = "MOCK";

  // 颜色
  private static final short LIGHT_BLUE_INDEX = IndexedColors.PALE_BLUE.getIndex();
  private static final short LIGHT_YELLOW_INDEX = IndexedColors.LIGHT_YELLOW.getIndex();
  private static final short BLACK_INDEX = IndexedColors.BLACK.getIndex();
  private static final short GREY_INDEX = IndexedColors.GREY_50_PERCENT.getIndex();

  private static final String CR = "\n";

  private static final int VALIDATION_CONSTRAINT_ERROR = DataValidation.ErrorStyle.WARNING;

  private final I18nStoreProvider store = new ExcelTemplateI18nDefaultStore();
  private final DataDefinition definition;
  private final String locale;
  private Sheet sheet;
  private DataValidationHelper validationHelper;

  private String constraintBoxTitle;
  private String constraintBoxMessage;

  public ExcelTemplateGenerator(DataDefinition definition, String locale) {
    this.definition = definition;
    this.locale = locale;
  }

  /**
   * 生成Excel模板
   */
  public void generate(OutputStream outputStream) {
    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
      String sheetTitle = definition.getTitle() != null ? definition.getTitle() : definition.getDomainName();
      this.sheet = workbook.createSheet(sheetTitle);
      this.validationHelper = sheet.getDataValidationHelper();
      doGenerate(sheet, definition);
      workbook.write(outputStream);
    } catch (Exception e) {
      throw new DataReadException(e, "生成Excel模板失败");
    }
  }

  private void doGenerate(Sheet sheet, DataDefinition definition) {
    CellStyle headerStyle = createHeaderCellStyle(LIGHT_BLUE_INDEX);
    CellStyle headerMarkStyle = createMarkCellStyle(LIGHT_BLUE_INDEX);

    int columnIndex;
    Collection<Column> columns = definition.getColumns().values();
//    List<String> fieldNames = definition.getColumns().keySet().stream().toList();

    // ── Row 1: 代码标识行 ──
    Row columnCodeRow = sheet.createRow(0);
    Cell columnCodeMarkCell = columnCodeRow.createCell(0);
    columnCodeMarkCell.setCellValue(MARKER_TEXT_CODE);
    columnCodeMarkCell.setCellStyle(headerMarkStyle);
    String commentText = store.getText(locale, MARKER_CODE_COMMENT);
    addComment(columnCodeMarkCell, commentText);

    columnIndex = 1;
    for (Column column : columns) {
      Cell cell = columnCodeRow.createCell(columnIndex);
      cell.setCellValue(column.getFieldName());
      cell.setCellStyle(headerStyle);

      addComment(cell, commentText);

      columnIndex++;
    }

    // ── Row 2: 人类阅读行 + 格式提示批注 ──
    Row columnHintRow = sheet.createRow(1);
    Cell columnHintMarkCell = columnHintRow.createCell(0);
    columnHintMarkCell.setCellValue(MARKER_TEXT_HINT);
    columnHintMarkCell.setCellStyle(headerMarkStyle);
    addComment(columnHintMarkCell, store.getText(locale, MARKER_HINT_COMMENT));

    columnIndex = 1;
    for (Column column : columns) {
      Cell cell = columnHintRow.createCell(columnIndex);
      List<String> headerTexts = HeaderBuilder.headerTexts(column);
      cell.setCellValue(headerTexts.getLast());
      cell.setCellStyle(headerStyle);

      addHintComment(cell, column);

      columnIndex++;
    }

    // ── Row 3+: 示例数据行（可选）──
    CellStyle mockStyle = createHeaderCellStyle(LIGHT_YELLOW_INDEX);
    CellStyle mockMarkStyle = createMarkCellStyle(LIGHT_YELLOW_INDEX);

    int rowIndex = 2;
    List<Map<String, String>> mockData = definition.getMockData();
    if (!Collections.isNullOrEmpty(mockData)) {
      for (Map<String, String> mock : mockData) {
        Row mockRow = sheet.createRow(rowIndex);
        Cell mockMarkCell = mockRow.createCell(0);
        mockMarkCell.setCellValue(MARKER_MOCK);
        mockMarkCell.setCellStyle(mockMarkStyle);

        addComment(mockMarkCell, store.getText(locale, MARKER_MOCK_COMMENT));

        columnIndex = 1;
        for (Column column : columns) {
          String name = column.getFieldName();
          String value = Strings.value(mock.get(name), "");
          Cell mockCell = mockRow.createCell(columnIndex);
          mockCell.setCellValue(value);
          mockCell.setCellStyle(mockStyle);

          columnIndex++;
        }

        rowIndex++;
      }
    }

    // 设置列宽，设置数据验证（下拉列表）
    sheet.setColumnWidth(0, 3000); // # 列
    columnIndex = 1;
    for (Column column : columns) {
      Integer width = column.getExporting().getWidth();
      sheet.setColumnWidth(columnIndex, width * 256);

      try {
        constraintBoxTitle = store.getText(locale, CONSTRAINT_BOX_TITLE);
        constraintBoxMessage = store.getText(locale, CONSTRAINT_BOX_MESSAGE);

        Importing importing = column.getImporting();
        if (importing != null) {
          LengthRange lengthRange = LengthRange.get(importing);
          if (lengthRange != null) {
            setupCellLengthValidation(rowIndex, columnIndex, lengthRange);
          }

          NumericRange numericRange = NumericRange.get(importing);
          if (numericRange != null) {
            setupCellNumberValidation(rowIndex, columnIndex, numericRange, column.getFieldType());
          }

          DateRange dateRange = DateRange.get(importing, column.getPatternDate());
          if (dateRange != null) {
            setupCellDateValidation(rowIndex, columnIndex, dateRange, column.getFieldType());
          }

          if (importing.getMapping() != null) {
            setupCellOptionsValidation(rowIndex, columnIndex, importing.getMapping());
          }
        }
      } catch (Exception e) {
        logger.warn("对列 [{}] 设置约束时产生问题：{}", column.getFieldName(), e.getMessage());
      }

      columnIndex++;
    }
  }

  private void setupCellValidation(int rowIndex, int columnIndex, DataValidationConstraint constraint) {
    CellRangeAddressList addressList = new CellRangeAddressList(rowIndex, 10000, columnIndex, columnIndex);
    DataValidation validation = validationHelper.createValidation(constraint, addressList);
    validation.setShowErrorBox(true);
    validation.createErrorBox(constraintBoxTitle, constraintBoxMessage);
    validation.setErrorStyle(VALIDATION_CONSTRAINT_ERROR);
    sheet.addValidationData(validation);
  }

  private void setupCellLengthValidation(int rowIndex, int columnIndex, LengthRange lengthRange) {
    DataValidationConstraint constraint;
    String min = Strings.value(lengthRange.min(), "");
    String max = Strings.value(lengthRange.max(), "");
    if (Strings.isBlank(min)) {
      constraint = validationHelper.createTextLengthConstraint(OperatorType.LESS_OR_EQUAL, min, max);
    } else if (Strings.isBlank(max)) {
      constraint = validationHelper.createTextLengthConstraint(OperatorType.GREATER_OR_EQUAL, min, max);
    } else {
      constraint = validationHelper.createTextLengthConstraint(OperatorType.BETWEEN, min, max);
    }

    setupCellValidation(rowIndex, columnIndex, constraint);
  }

  private void setupCellNumberValidation(int rowIndex, int columnIndex, NumericRange numericRange, Class<?> fieldType) {
    int validationType;
    if (Integer.class.isAssignableFrom(fieldType) || Long.class.isAssignableFrom(fieldType) || Short.class.isAssignableFrom(fieldType)) {
      validationType = ValidationType.INTEGER;
    } else if (Double.class.isAssignableFrom(fieldType) || Float.class.isAssignableFrom(fieldType)) {
      validationType = ValidationType.DECIMAL;
    } else if (Number.class.isAssignableFrom(fieldType)) {
      validationType = ValidationType.DECIMAL;
    } else {
      throw new DataWriteException("对非数字类型的字段设置了max或min约束");
    }

    DataValidationConstraint constraint;
    String min = numericRange.min();
    String max = numericRange.max();
    if (Strings.isBlank(min)) {
      constraint = validationHelper.createNumericConstraint(validationType, OperatorType.LESS_OR_EQUAL, max, null);
    } else if (Strings.isBlank(max)) {
      constraint = validationHelper.createNumericConstraint(validationType, OperatorType.GREATER_OR_EQUAL, min, null);
    } else {
      constraint = validationHelper.createNumericConstraint(validationType, OperatorType.BETWEEN, min, max);
    }

    setupCellValidation(rowIndex, columnIndex, constraint);
  }

  private void setupCellDateValidation(int rowIndex, int columnIndex, DateRange dateRange, Class<?> fieldType) {
    DataValidationConstraint constraint;
    String min = dateRange.min();
    String max = dateRange.max();
    if (Date.class.isAssignableFrom(fieldType) || LocalDateTime.class.isAssignableFrom(fieldType) || LocalDate.class.isAssignableFrom(fieldType)) {
      if (Strings.isBlank(min)) {
        constraint = validationHelper.createDateConstraint(OperatorType.LESS_OR_EQUAL, max, null, dateRange.pattern());
      } else if (Strings.isBlank(max)) {
        constraint = validationHelper.createDateConstraint(OperatorType.GREATER_OR_EQUAL, min, null, dateRange.pattern());
      } else {
        constraint = validationHelper.createDateConstraint(OperatorType.BETWEEN, dateRange.min(), dateRange.max(), dateRange.pattern());
      }
    } else if (LocalTime.class.isAssignableFrom(fieldType)) {
      if (Strings.isBlank(min)) {
        constraint = validationHelper.createTimeConstraint(OperatorType.LESS_OR_EQUAL, max, null);
      } else if (Strings.isBlank(max)) {
        constraint = validationHelper.createTimeConstraint(OperatorType.GREATER_OR_EQUAL, min, null);
      } else {
        constraint = validationHelper.createTimeConstraint(OperatorType.BETWEEN, dateRange.min(), dateRange.max());
      }
    } else {
      throw new DataWriteException("对非日期类型的字段设置了max或min约束");
    }
    setupCellValidation(rowIndex, columnIndex, constraint);
  }

  private void setupCellOptionsValidation(int rowIndex, int columnIndex, Map<String, String> mapping) {
    String[] options = mapping.values().toArray(new String[0]);
    DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(options);
    setupCellValidation(rowIndex, columnIndex, constraint);
  }

  private void addHintComment(Cell cell, Column column) {
    StringBuilder sb = new StringBuilder();
    sb.append("[").append(column.getFieldName()).append("]\n");

    Importing importing = column.getImporting();
    if (importing != null) {
      if (importing.getRequired()) sb.append(store.getText(locale, HINT_REQUIRED_VALUE)).append(CR);
      if (importing.getMaxLength() != null)
        sb.append(store.getText(locale, HINT_MAX_LENGTH, importing.getMaxLength())).append(CR);
      if (importing.getMinLength() != null)
        sb.append(store.getText(locale, HINT_MIN_LENGTH, importing.getMinLength())).append(CR);
      if (importing.getMax() != null) sb.append(store.getText(locale, HINT_MAX_NUMBER, importing.getMax())).append(CR);
      if (importing.getMin() != null) sb.append(store.getText(locale, HINT_MIN_NUMBER, importing.getMin())).append(CR);
      if (importing.getDependsOn() != null) {
        String dependsText = String.join(", ", importing.getDependsOn());
        sb.append(store.getText(locale, HINT_DEPENDS_ON, dependsText)).append(CR);
      }
    }

    if (column.getImporting() != null) {
      Map<String, String> mapping = column.getMapping();
      if (mapping != null) {
        sb.append(store.getText(locale, HINT_OPTIONS)).append(CR);
        String optionsText = mapping.values().stream().map(v -> "- " + v).collect(Collectors.joining(CR));
        sb.append(optionsText);
      }
    }

    addComment(cell, sb.toString());
  }

  private void addComment(Cell cell, String text) {
    Drawing<?> drawing = sheet.createDrawingPatriarch();
    CreationHelper factory = sheet.getWorkbook().getCreationHelper();
    ClientAnchor anchor = factory.createClientAnchor();
    anchor.setCol1(cell.getColumnIndex());
    anchor.setRow1(cell.getRowIndex());
    Comment comment = drawing.createCellComment(anchor);
    comment.setString(factory.createRichTextString(text));
    cell.setCellComment(comment);
  }

  private CellStyle createHeaderCellStyle(short fillColor) {
    CellStyle style = createCellStyle(fillColor);

    Font font = createHeaderFont();
    style.setFont(font);
    style.setLocked(true);

    return style;
  }

  private CellStyle createMarkCellStyle(short fillColor) {
    CellStyle style = createCellStyle(fillColor);

    Font font = createMarkFont();
    style.setFont(font);
    style.setLocked(true);

    return style;
  }

  private CellStyle createCellStyle(short fillColor) {
    CellStyle style = sheet.getWorkbook().createCellStyle();
    style.setFillForegroundColor(fillColor);
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setBorderTop(BorderStyle.THIN);
    style.setTopBorderColor(BLACK_INDEX);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBottomBorderColor(BLACK_INDEX);
    style.setBorderLeft(BorderStyle.THIN);
    style.setLeftBorderColor(BLACK_INDEX);
    style.setBorderRight(BorderStyle.THIN);
    style.setRightBorderColor(BLACK_INDEX);
    style.setAlignment(HorizontalAlignment.CENTER);
    return style;
  }

  private Font createHeaderFont() {
    Font font = sheet.getWorkbook().createFont();
    font.setFontName("Arial");
    font.setFontHeightInPoints((short) 11);
    font.setColor(BLACK_INDEX);
    font.setBold(true);
    return font;
  }

  private Font createMarkFont() {
    Font font = sheet.getWorkbook().createFont();
    font.setFontName("Arial");
    font.setFontHeightInPoints((short) 11);
    font.setColor(GREY_INDEX);
    font.setBold(true);
    return font;
  }

  /**
   * 解析文本中的 ${key} 占位符
   *
   * @param text 原始文本，可能包含 ${key}
   * @return 替换后的文本
   */
  @SuppressWarnings("DuplicatedCode")
  private String resolveText(String text) {
    if (text == null || !text.contains("${")) {
      return text;
    }

    Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
    StringBuilder result = new StringBuilder();
    while (matcher.find()) {
      String key = matcher.group(1);
      String replacement = store.getText(locale, key);
      matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(result);
    return result.toString();
  }

//  /**
//   * 生成Excel模板
//   */
//  public static void generate(DataDefinition definition, String locale, OutputStream outputStream) {
//    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
//      Sheet sheet = workbook.createSheet(definition.getTitle() != null ? definition.getTitle() : definition.getDomainName());
//
//      // 创建样式
//      CellStyle blueStyle = createBlueStyle(workbook);
//      CellStyle yellowStyle = createYellowStyle(workbook);
//
//      List<String> fieldNames = new ArrayList<>(definition.getColumns().keySet());
//      int columnCount = fieldNames.size() + 1; // +1 for # column
//
//      // ── Row 1: 代码标识行 ──
//      Row row1 = sheet.createRow(0);
//      Cell marker1 = row1.createCell(0);
//      marker1.setCellValue(MARKER_TEXT_CODE);
//      marker1.setCellStyle(blueStyle);
//
//      for (int i = 0; i < fieldNames.size(); i++) {
//        Cell cell = row1.createCell(i + 1);
//        cell.setCellValue(fieldNames.get(i));
//        cell.setCellStyle(blueStyle);
//
//        // 添加批注：完整填写说明
//        addHeaderComment(sheet, cell, definition.getColumns().get(fieldNames.get(i)), fieldNames.get(i));
//      }
//
//      // ── Row 2: 人类阅读行 + 格式提示批注 ──
//      Row row2 = sheet.createRow(1);
//      Cell marker2 = row2.createCell(0);
//      marker2.setCellValue(MARKER_TEXT_CODE);
//      marker2.setCellStyle(blueStyle);
//
//      for (int i = 0; i < fieldNames.size(); i++) {
//        Column column = definition.getColumns().get(fieldNames.get(i));
//        Cell cell = row2.createCell(i + 1);
//        String headerText = column.getHeader() != null ? column.getHeader() : fieldNames.get(i);
//        cell.setCellValue(headerText);
//        cell.setCellStyle(blueStyle);
//
//        // 添加格式提示批注
//        addHintComment(sheet, cell, column);
//      }
//
//      // ── Row 3: 示例数据行（可选）──
//      if (definition.getMockData() != null && !definition.getMockData().isEmpty()) {
//        for (int mockIdx = 0; mockIdx < definition.getMockData().size(); mockIdx++) {
//          Map<String, String> mockRow = definition.getMockData().get(mockIdx);
//          Row mockRowObj = sheet.createRow(2 + mockIdx);
//
//          Cell mockMarker = mockRowObj.createCell(0);
//          mockMarker.setCellValue(MARKER_MOCK);
//          mockMarker.setCellStyle(yellowStyle);
//
//          for (int i = 0; i < fieldNames.size(); i++) {
//            Cell cell = mockRowObj.createCell(i + 1);
//            String value = mockRow.get(fieldNames.get(i));
//            cell.setCellValue(value != null ? value : "");
//            cell.setCellStyle(yellowStyle);
//          }
//
//          // 添加MOCK批注（仅第一行mock数据）
//          if (mockIdx == 0) {
//            addComment(sheet, mockMarker, "示例数据，仅供参考，导入时将自动忽略");
//          }
//        }
//      }
//
//      // 设置列宽
//      sheet.setColumnWidth(0, 3000); // # 列
//      for (int i = 0; i < fieldNames.size(); i++) {
//        sheet.setColumnWidth(i + 1, 5000);
//      }
//
//      // 为mapping列添加数据验证（下拉列表）
//      int dataStartRow = definition.getMockData() != null && !definition.getMockData().isEmpty()
//        ? 2 + definition.getMockData().size() : 2;
//      addDataValidation(sheet, definition, fieldNames, dataStartRow);
//
//      workbook.write(outputStream);
//    } catch (Exception e) {
//      throw new DataReadException(e, "生成Excel模板失败");
//    }
//  }
//
//  private static CellStyle createBlueStyle(XSSFWorkbook workbook) {
//    CellStyle style = workbook.createCellStyle();
//    style.setFillForegroundColor(LIGHT_BLUE_INDEX);
//    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//    Font font = workbook.createFont();
//    font.setFontName("Arial");
//    font.setFontHeightInPoints((short) 11);
//    style.setFont(font);
//    style.setLocked(true);
//    return style;
//  }
//
//  private static CellStyle createYellowStyle(XSSFWorkbook workbook) {
//    CellStyle style = workbook.createCellStyle();
//    style.setFillForegroundColor(LIGHT_YELLOW_INDEX);
//    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//    Font font = workbook.createFont();
//    font.setFontName("Arial");
//    font.setFontHeightInPoints((short) 11);
//    style.setFont(font);
//    return style;
//  }
//
//  private static void addHeaderComment(Sheet sheet, Cell cell, Column column, String fieldName) {
//    StringBuilder sb = new StringBuilder();
//    sb.append("【").append(column.getHeader() != null ? column.getHeader() : fieldName).append("】\n");
//
//    // 类型
//    Class<?> fieldType = null;
//    try {
//      fieldType = Class.class; // 简化，实际需要从Bean类获取
//    } catch (Exception e) {
//      // ignore
//    }
//
//    Importing importing = column.getImporting();
//    if (importing != null) {
//      if (importing.getRequired()) sb.append("必填：是\n");
//      if (importing.getMaxLength() != null) sb.append("最大长度：").append(importing.getMaxLength()).append("字\n");
//      if (importing.getMin() != null || importing.getMax() != null) {
//        sb.append("范围：").append(importing.getMin() != null ? importing.getMin() : "-")
//          .append("~").append(importing.getMax() != null ? importing.getMax() : "-").append("\n");
//      }
//    }
//
//    if (column.getMapping() != null) {
//      sb.append("允许值：\n");
//      column.getMapping().forEach((k, v) -> sb.append("  ").append(k).append(" = ").append(v).append("\n"));
//    }
//
//    sb.append("\n代码标识行，请勿修改，否则导入将失败");
//    addComment(sheet, cell, sb.toString());
//  }
//
//  private static void addHintComment(Sheet sheet, Cell cell, Column column) {
//    Importing importing = column.getImporting();
//    if (importing == null) return;
//
//    List<String> hints = new ArrayList<>();
//    if (importing.getRequired()) hints.add("*必填");
//    else hints.add("选填");
//    if (importing.getMaxLength() != null) hints.add("≤" + importing.getMaxLength() + "字");
//    if (importing.getMin() != null || importing.getMax() != null) {
//      hints.add((importing.getMin() != null ? importing.getMin() : "-") + "~" + (importing.getMax() != null ? importing.getMax() : "-"));
//    }
//
//    if (column.getMapping() != null) {
//      List<String> mappingHints = new ArrayList<>();
//      column.getMapping().forEach((k, v) -> mappingHints.add(k + "=" + v));
//      hints.add(String.join("/", mappingHints));
//    }
//
//    if (!hints.isEmpty()) {
//      addComment(sheet, cell, String.join(" | ", hints));
//    }
//  }
//
//  private static void addComment(Sheet sheet, Cell cell, String text) {
//    Drawing<?> drawing = sheet.createDrawingPatriarch();
//    CreationHelper factory = sheet.getWorkbook().getCreationHelper();
//    Comment comment = drawing.createCellComment(factory.createClientAnchor());
//    comment.setString(factory.createRichTextString(text));
//    cell.setCellComment(comment);
//  }
//
//  private static void addDataValidation(Sheet sheet, DataDefinition definition,
//                                        List<String> fieldNames, int dataStartRow) {
//    for (int i = 0; i < fieldNames.size(); i++) {
//      Column column = definition.getColumns().get(fieldNames.get(i));
//      if (column.getMapping() != null && !column.getMapping().isEmpty()) {
//        String[] options = column.getMapping().values().toArray(new String[0]);
//        DataValidationHelper validationHelper = sheet.getDataValidationHelper();
//        DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(options);
//        CellRangeAddressList addressList = new CellRangeAddressList(
//          dataStartRow, 1000, i + 1, i + 1);
//        DataValidation validation = validationHelper.createValidation(constraint, addressList);
//        validation.setShowErrorBox(true);
//        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
//        sheet.addValidationData(validation);
//      }
//    }
//  }
}
