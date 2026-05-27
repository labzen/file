package cn.labzen.file.format.csv;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.TableColumn;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.exception.DataWriteException;
import cn.labzen.file.format.AbstractDataFileWriter;
import cn.labzen.file.meta.FileConfiguration;
import cn.labzen.tool.util.Strings;
import org.jspecify.annotations.NonNull;

import jakarta.annotation.Nonnull;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
  public static final String DEFAULT_DELIMITER = ",";
  /**
   * 默认引用字符
   */
  public static final String DEFAULT_QUOTE = "\"";
  private String delimiter;
  private String quote;

  @Override
  public @NonNull FileFormat format() {
    return FileFormat.CSV;
  }

  @Override
  public void initialize(@NonNull FileConfiguration configuration) {
    this.delimiter = Strings.valueWhenEmpty(configuration.csvDelimiter(), DEFAULT_DELIMITER);
    this.quote = Strings.valueWhenEmpty(configuration.csvQuote(), DEFAULT_QUOTE);
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  protected void generateContent(@Nonnull DataDefinition definition, @Nonnull List<Map<String, Object>> rows, @Nonnull OutputStream outputStream) {
    LinkedHashMap<String, TableColumn> columns = new LinkedHashMap<>(definition.getColumns());
    List<String> headers = definition.getHeaders().getLeafLevelHeaders();

    try (Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
      // 写入 UTF-8 BOM，Excel 能正确识别中文
      writer.write("\uFEFF");

      // 写入表头行
      // CSV 不支持多级表头，只取最低级别的表头（最后一个元素）
      String headerLine = buildHeaderLine(headers);
      writer.write(headerLine + "\n");

      // 写入数据行
      for (Map<String, Object> row : rows) {
        String dataLine = buildDataLine(row, columns);
        writer.write(dataLine + "\n");
      }

      writer.flush();
    } catch (IOException e) {
      throw new DataWriteException(e, "CSV 文件写入失败");
    }
  }

  private String buildHeaderLine(List<String> headers) {
    return headers.stream()
      .map(header -> Strings.valueWhenBlank(header, "unknown-header"))
      .collect(Collectors.joining(delimiter));
  }

  /**
   * 构建数据行
   * <p>
   * 按 columns 的顺序，提取 row 中对应字段的值
   *
   * @param row     数据行映射
   * @param columns 列定义映射
   * @return 数据行字符串
   */
  private String buildDataLine(Map<String, Object> row, Map<String, TableColumn> columns) {
    return columns.keySet().stream()
      .map(key -> {
        Object value = row.get(key);
        return escapeField(value);
      })
      .collect(Collectors.joining(delimiter));
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
    boolean needsQuoting = Strings.containsAny(text, delimiter, quote, "\n", "\r");
    if (needsQuoting) {
      return quote + text.replace(quote, quote + quote) + quote;
    }
    return text;
  }
}
