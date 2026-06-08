package cn.labzen.file.format.excel;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.exception.DataWriteException;
import cn.labzen.file.format.core.writer.AbstractDataFileWriter;
import cn.labzen.file.format.excel.write.ExcelWorkbookContext;
import cn.labzen.file.meta.FileConfiguration;
import cn.labzen.tool.util.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jspecify.annotations.NonNull;

import jakarta.annotation.Nonnull;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Excel 文件导出器
 * <p>
 * 使用 Apache POI 库实现 Excel（.xlsx）文件的生成，支持：
 * <ul>
 *   <li>多级表头（通过 {@link cn.labzen.file.definition.bean.table.HeaderStructure} 预计算合并信息）</li>
 *   <li>单元格背景色、字体、边框、对齐方式</li>
 *   <li>列宽配置</li>
 *   <li>样式缓存，避免重复创建 CellStyle</li>
 * </ul>
 * <p>
 * 类结构设计参考 {@link cn.labzen.file.format.pdf.PdfFileWriter} 的协调者模式：
 * 本类作为协调者，将表头渲染、数据渲染、样式应用等职责委托给专门的渲染器，
 * 各渲染器通过 {@link ExcelWorkbookContext} 共享工作簿上下文。
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
    // do nothing
  }

  @Override
  protected void exportContent(@Nonnull DataDefinition definition, @Nonnull List<Map<String, Object>> rows, @Nonnull OutputStream outputStream) {
    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
      ExcelWorkbookContext context = new ExcelWorkbookContext(workbook, resolveSheetName(definition), definition.getColumns());

      // 渲染表头
      int headerRowCount = context.createHeaderRenderer().render(definition.getHeaders(), definition.getExportingHeaderStyle());

      // 渲染数据行
      context.createDataRenderer().render(rows, headerRowCount, definition.getExportingColumnStyle());

      // 导出输出流
      workbook.write(outputStream);

    } catch (Exception e) {
      throw new DataWriteException(e, "Excel 文件导出失败");
    }
  }

  /**
   * 解析 Sheet 名称
   * <p>
   * 优先使用数据定义的 title，若为空则使用默认名称 "data"。
   * Excel Sheet 名称有长度和字符限制，这里做简单处理：
   * 截断至 31 字符（Excel 最大限制），并替换非法字符。
   *
   * @param definition 数据定义
   * @return 合法的 Sheet 名称
   */
  private String resolveSheetName(DataDefinition definition) {
    String title = Strings.valueWhenBlank(definition.getTitle(), "data");

    // Excel sheet 名称最大 31 字符，不能包含 : \ / ? * [ ]
    String sanitized = title.replaceAll("[:\\\\/?*\\[\\]]", "_").trim();
    if (sanitized.length() > 31) {
      sanitized = sanitized.substring(0, 31);
    }
    return sanitized.isEmpty() ? "data" : sanitized;
  }
}
