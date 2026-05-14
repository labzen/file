package cn.labzen.file.format.csv;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.TableColumn;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.exception.DataWriteException;
import cn.labzen.file.format.AbstractDataFileWriter;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CSV 文件写入器
 * <p>
 * 实现逗号分隔值文件的生成
 *
 * @param <T> 数据对象类型
 * @author labzen
 */
public final class CsvFileWriter<T> extends AbstractDataFileWriter<T> {

  /**
   * 默认分隔符
   */
  private static final char DEFAULT_DELIMITER = ',';

  /**
   * 默认引用字符
   */
  private static final char DEFAULT_QUOTE = '"';

  @Override
  public @NonNull FileFormat format() {
    return FileFormat.CSV;
  }

  @Override
  protected void generateContent(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull OutputStream outputStream) {
    if (data.isEmpty()) {
      throw new DataWriteException("数据集合不能为空");
    }

    LinkedHashMap<String, TableColumn> columns = new LinkedHashMap<>(definition.getColumns());
    List<List<String>> headers = extractHeaders(definition);
    List<Map<String, Object>> rows = extractRows(definition, data);

    try (Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), DEFAULT_BUFFER_SIZE)) {
      // 写入 UTF-8 BOM，Excel 能正确识别中文
      writer.write("\uFEFF");

      // 写入表头行
      writeHeaderRow(writer, headers, columns);
      // 写入数据行
      writeDataRows(writer, rows, columns);

      writer.flush();
    } catch (IOException e) {
      throw new DataWriteException(e, "CSV 文件写入失败");
    }
  }

  /**
   * 写入表头行
   */
  private void writeHeaderRow(Writer writer, List<List<String>> headers, LinkedHashMap<String, TableColumn> columns) throws IOException {
    int colIndex = 0;
    for (Map.Entry<String, TableColumn> entry : columns.entrySet()) {
      TableColumn column = entry.getValue();
      List<String> header = column.getHeader();
      String headerText = (header != null && !header.isEmpty()) ? header.getLast() : entry.getKey();

      if (colIndex > 0) {
        writer.write(DEFAULT_DELIMITER);
      }
      writer.write(escapeField(headerText));
      colIndex++;
    }
    writer.write("\n");
  }

  /**
   * 写入数据行
   */
  private void writeDataRows(Writer writer, List<Map<String, Object>> rows, LinkedHashMap<String, TableColumn> columns) throws IOException {
    for (Map<String, Object> row : rows) {
      int colIndex = 0;
      for (Map.Entry<String, TableColumn> entry : columns.entrySet()) {
        String fieldName = entry.getKey();
        Object value = row.get(fieldName);

        if (colIndex > 0) {
          writer.write(DEFAULT_DELIMITER);
        }
        writer.write(escapeField(value));
        colIndex++;
      }
      writer.write("\n");
    }
  }

  /**
   * 转义 CSV 字段
   * <p>
   * 规则：
   * <ul>
   *   <li>如果字段包含逗号、引号或换行符，需要用引号包裹</li>
   *   <li>字段内的引号需要加倍转义</li>
   * </ul>
   */
  private String escapeField(Object value) {
    if (value == null) {
      return "";
    }

    String text = value.toString();
    boolean needsQuoting = text.contains(String.valueOf(DEFAULT_DELIMITER))
      || text.contains(String.valueOf(DEFAULT_QUOTE))
      || text.contains("\n")
      || text.contains("\r");


    if (needsQuoting) {
      return DEFAULT_QUOTE + text.replace(String.valueOf(DEFAULT_QUOTE), String.valueOf(DEFAULT_QUOTE) + DEFAULT_QUOTE) + DEFAULT_QUOTE;
    }
    return text;
  }
}
