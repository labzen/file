package cn.labzen.file.format.excel;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.Column;
import cn.labzen.file.definition.bean.column.Importing;
import cn.labzen.file.definition.bean.column.constraint.DateRange;
import cn.labzen.file.definition.bean.column.constraint.LengthRange;
import cn.labzen.file.definition.bean.column.constraint.NumericRange;
import cn.labzen.file.definition.bean.table.HeaderCell;
import cn.labzen.file.definition.bean.table.HeaderStructure;
import cn.labzen.file.exception.DataReadException;
import cn.labzen.file.exception.DataWriteException;
import cn.labzen.file.locale.FileResourceBundleLoader;
import cn.labzen.file.locale.FormattableResourceBundle;
import cn.labzen.file.util.LocaledTextWithPlaceholder;
import cn.labzen.tool.util.Collections;
import cn.labzen.tool.util.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DataValidationConstraint.OperatorType;
import org.apache.poi.ss.usermodel.DataValidationConstraint.ValidationType;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.labzen.file.locale.LocaleKeys.*;

/**
 * Excel 导入模板生成器
 * <p>
 * 生成包含以下结构的Excel模板文件：
 * <ul>
 *   <li>Row 1: 代码标识行（字段名），#列为CODE，浅蓝背景</li>
 *   <li>Row 2~N: 人类阅读行（i18n表头文本 + 批注格式提示），#列为HINT，浅蓝背景
 *       <br>单级表头占1行，多级表头占2行（含合并单元格）</li>
 *   <li>Row N+1+: 示例数据行（可选，来自mock.json），#列为MOCK，浅黄背景</li>
 *   <li>最后一部分: 用户数据区，#列为序号，白色背景</li>
 * </ul>
 *
 * @author labzen
 */
@Slf4j
public final class ExcelTemplateGenerator {

  private static final int PREPARE_ROW_NUMBER = 1000;
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

  //  private final I18nMessageSource i18nMessageSource;
  private final DataDefinition definition;
  //  private final Locale locale;
  private final FormattableResourceBundle resourceBundle;
  private Sheet sheet;
  private DataValidationHelper validationHelper;

  private String constraintBoxTitle;
  private String constraintBoxMessage;

  public ExcelTemplateGenerator(DataDefinition definition, Locale locale) {
    this.definition = definition;
//    this.locale = locale;
    this.resourceBundle = FileResourceBundleLoader.load(locale);
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

    List<Column> columns = List.copyOf(definition.getColumns().values());

    int columnIndex;
    int rowIndex;

    // ── Row 1: 代码标识行 (CODE) ── 扁平结构，每列一个单元格
    rowIndex = 0;
    Row columnCodeRow = sheet.createRow(rowIndex);
    Cell columnCodeMarkCell = columnCodeRow.createCell(0);
    columnCodeMarkCell.setCellValue(MARKER_TEXT_CODE);
    columnCodeMarkCell.setCellStyle(headerMarkStyle);
    String columnCodeComment = resourceBundle.getString(TEMPLATE_MARKER_CODE_COMMENT);
    addComment(columnCodeMarkCell, columnCodeComment);

    columnIndex = 1;
    for (Column column : columns) {
      Cell cell = columnCodeRow.createCell(columnIndex);
      cell.setCellValue(column.getFieldName());
      cell.setCellStyle(headerStyle);

      addComment(cell, columnCodeComment);

      columnIndex++;
    }

    // ── Row 2~3: 人类阅读行 (HINT) ── 支持多级表头渲染
    rowIndex++;
    Row columnHintRow = sheet.createRow(rowIndex);
    Cell columnHintMarkCell = columnHintRow.createCell(0);
    columnHintMarkCell.setCellValue(MARKER_TEXT_HINT);
    columnHintMarkCell.setCellStyle(headerMarkStyle);
    addComment(columnHintMarkCell, resourceBundle.getString(TEMPLATE_MARKER_HINT_COMMENT));

    HeaderStructure headers = definition.getHeaders();
    boolean singleHeader = headers.isSingleHeader();
    // 渲染 HINT 第一行（含合并单元格）
    for (HeaderCell hc : headers.firstRow()) {
      int col = hc.index() + 1; // 偏移1列（#标记列）
      Cell cell = columnHintRow.createCell(col);
      cell.setCellValue(hc.text());
      cell.setCellStyle(headerStyle);

      // 横向合并（colSpan > 1）
      if (hc.colSpan() > 1) {
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, col, col + hc.colSpan() - 1));
      }
      // 纵向合并（单级列在多级表头中 rowSpan=2）
      if (!singleHeader && hc.rowSpan() > 1) {
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex + 1, col, col));
      }

      // 叶子节点（单级表头 或 纵向合并的列）→ 添加导入提示批注
      if (singleHeader || hc.rowSpan() > 1) {
        Column column = columns.get(hc.index());
        addHintComment(cell, column);
      }
    }

    // 多级表头时：#列纵向合并 + 渲染 HINT 第二行
    if (!singleHeader) {
      // #标记列
      rowIndex++;
      sheet.addMergedRegion(new CellRangeAddress(rowIndex - 1, rowIndex, 0, 0));

      Row hintSecondRow = sheet.createRow(2);
      for (HeaderCell hc : headers.secondRow()) {
        int col = hc.index() + 1;
        Cell cell = hintSecondRow.createCell(col);
        cell.setCellValue(hc.text());
        cell.setCellStyle(headerStyle);

        Column column = columns.get(hc.index());
        addHintComment(cell, column);
      }
    }

    // ── Row 3~4+: 示例数据行（MOCK） ── 可选
    CellStyle mockStyle = createHeaderCellStyle(LIGHT_YELLOW_INDEX);
    CellStyle mockMarkStyle = createMarkCellStyle(LIGHT_YELLOW_INDEX);

    rowIndex++; // CODE行 + HINT行数
    List<Map<String, String>> mockData = definition.getMockData();
    if (!Collections.isNullOrEmpty(mockData)) {
      for (Map<String, String> mock : mockData) {
        Row mockRow = sheet.createRow(rowIndex);
        Cell mockMarkCell = mockRow.createCell(0);
        mockMarkCell.setCellValue(MARKER_MOCK);
        mockMarkCell.setCellStyle(mockMarkStyle);
        addComment(mockMarkCell, resourceBundle.getString(TEMPLATE_MARKER_MOCK_COMMENT));

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

    // ── 冻结前几行 ──

    sheet.createFreezePane(1, rowIndex);

    // ── 列宽 + 数据验证 ──
    sheet.setColumnWidth(0, 3000); // # 列
    columnIndex = 1;
    for (Column column : columns) {
      // 设置单元格默认格式
      setupCellFormat(columnIndex, column);

      try {
        constraintBoxTitle = resourceBundle.getString(TEMPLATE_CONSTRAINT_BOX_TITLE);
        constraintBoxMessage = resourceBundle.getString(TEMPLATE_CONSTRAINT_BOX_MESSAGE);

        Importing importing = column.getImporting();
        if (importing != null) {
          LengthRange lengthRange = importing.getLengthRange();
          if (lengthRange != null) {
            setupCellLengthValidation(rowIndex, columnIndex, lengthRange);
          }

          NumericRange numericRange = importing.getNumericRange();
          if (numericRange != null) {
            setupCellNumberValidation(rowIndex, columnIndex, numericRange, column.getFieldType());
          }

          DateRange dateRange = importing.getDateRange();
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

    // ── 初始化序号列 ──
    String firstColumn = "B";
    String lastColumn = calculateColumn(firstColumn, columns.size());
    // 设置第一列 #序号：从最后一条MOCK行之后开始，递增生成 PREPARE_ROW_NUMBER 条
    for (int i = 0; i < PREPARE_ROW_NUMBER; i++) {
      Row row = sheet.createRow(rowIndex + i);
      Cell indexCell = row.createCell(0);
      int cr = rowIndex + i + 1;
      String formula = String.format("IF(COUNTA(%s%d:%s%d)=0,\"\",ROW()-%d)", firstColumn, cr, lastColumn, cr, rowIndex);
      indexCell.setCellFormula(formula);
    }

    sheet.protectSheet("");
  }

  private String calculateColumn(String startColumn, int columnSize) {
    int start = CellReference.convertColStringToIndex(startColumn);
    int last = start + columnSize - 1;
    return CellReference.convertNumToColString(last);
  }

  private void setupCellFormat(int columnIndex, Column column) {
    Class<?> fieldType = column.getFieldType();
    if (fieldType == null) {
      return;
    }

    Integer width = column.getWidth();
    sheet.setColumnWidth(columnIndex, width * 256);

    DataFormat dataFormat = sheet.getWorkbook().createDataFormat();
    CellStyle formatStyle = sheet.getWorkbook().createCellStyle();

    boolean isDateType = Date.class.isAssignableFrom(fieldType)
      || LocalDate.class.isAssignableFrom(fieldType)
      || LocalDateTime.class.isAssignableFrom(fieldType)
      || LocalTime.class.isAssignableFrom(fieldType)
      || java.sql.Date.class.isAssignableFrom(fieldType)
      || java.sql.Timestamp.class.isAssignableFrom(fieldType);
    boolean isNumberType = Number.class.isAssignableFrom(fieldType);
    boolean isDecimalType = Double.class.isAssignableFrom(fieldType)
      || Float.class.isAssignableFrom(fieldType)
      || BigDecimal.class.isAssignableFrom(fieldType);

    if (isDateType) {
      // 日期类型：根据 column.patternDate 设置格式
      if (Strings.isNotBlank(column.getPatternDate())) {
        formatStyle.setDataFormat(dataFormat.getFormat(column.getPatternDate()));
      }
    } else if (isNumberType) {
      // 数值类型：根据 column.patternNumber 设置格式，若无则根据类型推断
      if (Strings.isNotBlank(column.getPatternNumber())) {
        formatStyle.setDataFormat(dataFormat.getFormat(column.getPatternNumber()));
      } else {
        formatStyle.setDataFormat(dataFormat.getFormat(isDecimalType ? "0.00" : "0"));
      }
    }

    formatStyle.setLocked(false);
    // 对数据区域的列设置格式（整列）
    sheet.setDefaultColumnStyle(columnIndex, formatStyle);
  }

  private void setupCellValidation(int rowIndex, int columnIndex, DataValidationConstraint constraint) {
    CellRangeAddressList addressList = new CellRangeAddressList(rowIndex, PREPARE_ROW_NUMBER, columnIndex, columnIndex);
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

    if (Date.class.isAssignableFrom(fieldType)
      || LocalDateTime.class.isAssignableFrom(fieldType)
      || LocalDate.class.isAssignableFrom(fieldType)
      || java.sql.Date.class.isAssignableFrom(fieldType)
      || Timestamp.class.isAssignableFrom(fieldType)) {
      // dateFormat 参数在 XSSF 中已被忽略，需使用 Excel 日期序列号确保 Excel 可正确识别
      String min = toExcelDateValue(dateRange.min());
      String max = toExcelDateValue(dateRange.max());
      if (Strings.isBlank(min)) {
        constraint = validationHelper.createDateConstraint(OperatorType.LESS_OR_EQUAL, max, null, null);
      } else if (Strings.isBlank(max)) {
        constraint = validationHelper.createDateConstraint(OperatorType.GREATER_OR_EQUAL, min, null, null);
      } else {
        constraint = validationHelper.createDateConstraint(OperatorType.BETWEEN, min, max, null);
      }
    } else if (LocalTime.class.isAssignableFrom(fieldType)) {
      // 时间类型：转为 Excel 时间序列号（一天中的比例值）
      String min = toExcelTimeValue(dateRange.min());
      String max = toExcelTimeValue(dateRange.max());
      if (Strings.isBlank(min)) {
        constraint = validationHelper.createTimeConstraint(OperatorType.LESS_OR_EQUAL, max, null);
      } else if (Strings.isBlank(max)) {
        constraint = validationHelper.createTimeConstraint(OperatorType.GREATER_OR_EQUAL, min, null);
      } else {
        constraint = validationHelper.createTimeConstraint(OperatorType.BETWEEN, min, max);
      }
    } else {
      throw new DataWriteException("对非日期类型的字段设置了max或min约束");
    }
    setupCellValidation(rowIndex, columnIndex, constraint);
  }

  /**
   * 将 LocalDateTime 转换为 Excel 日期序列号字符串
   * <p>
   * Excel 日期序列号是从 1900-01-01 起的天数（数值型），
   * 传给 createDateConstraint 可避免日期字符串格式因 locale 不同导致 Excel 无法解析的问题
   */
  private String toExcelDateValue(LocalDateTime dateTime) {
    if (dateTime == null) {
      return "";
    }
    Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    double excelDate = DateUtil.getExcelDate(date);
    return String.valueOf(excelDate);
  }

  /**
   * 将 LocalDateTime 转换为 Excel 时间序列号字符串
   * <p>
   * Excel 时间序列号是一天中的比例值（0.0 ~ 1.0），
   * 例如 12:00 = 0.5, 18:00 = 0.75
   */
  private String toExcelTimeValue(LocalDateTime dateTime) {
    if (dateTime == null) {
      return "";
    }
    // 直接从时/分/秒计算比例值，避免基准日期序列号带来的精度问题
    int hour = dateTime.getHour();
    int minute = dateTime.getMinute();
    int second = dateTime.getSecond();
    double fraction = (hour * 3600.0 + minute * 60.0 + second) / 86400.0;
    return String.valueOf(fraction);
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
      if (importing.getRequire()) sb.append(resourceBundle.getString(TEMPLATE_HINT_REQUIRED_VALUE)).append(CR);
      if (importing.getMaxLength() != null)
        sb.append(resourceBundle.getString(TEMPLATE_HINT_MAX_LENGTH, importing.getMaxLength())).append(CR);
      if (importing.getMinLength() != null)
        sb.append(resourceBundle.getString(TEMPLATE_HINT_MIN_LENGTH, importing.getMinLength())).append(CR);
      if (importing.getMax() != null)
        sb.append(resourceBundle.getString(TEMPLATE_HINT_MAX_NUMBER, importing.getMax())).append(CR);
      if (importing.getMin() != null)
        sb.append(resourceBundle.getString(TEMPLATE_HINT_MIN_NUMBER, importing.getMin())).append(CR);
      if (importing.getDependsOn() != null) {
        String dependsText = String.join(", ", importing.getDependsOn());
        sb.append(resourceBundle.getString(TEMPLATE_HINT_DEPENDS_ON, dependsText)).append(CR);
      }

      // 方案C：使用 importing 专属 mapping，而非共享 mapping
      Map<String, String> mapping = importing.getMapping();
      if (mapping != null) {
        sb.append(resourceBundle.getString(TEMPLATE_HINT_OPTIONS)).append(CR);
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
    anchor.setCol2(cell.getColumnIndex() + 2);
    anchor.setRow1(cell.getRowIndex());
    anchor.setDx1(30);
    anchor.setDy2(30);
    anchor.setDx1(10000);
    anchor.setDy2(10000);
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
    style.setVerticalAlignment(VerticalAlignment.CENTER);
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

  private String resolveText(String text) {
    return LocaledTextWithPlaceholder.resolve(resourceBundle, text);
  }
}
