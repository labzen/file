package cn.labzen.file.format.txt;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.Column;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.exception.DataWriteException;
import cn.labzen.file.format.core.writer.AbstractDataFileWriter;
import cn.labzen.file.meta.FileConfiguration;
import cn.labzen.tool.util.Strings;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 纯文本文件写入器
 * <p>
 * 实现简单的文本表格格式文件生成，默认使用4个空格分隔列。
 * 结构：
 * <pre>
 * 表头A    表头B    表头C    表头D    表头E
 * 数据1A    数据1B    数据1C    数据1D    数据1E
 * 数据2A    数据2B    数据2C    数据2D    数据2E
 * 数据3A    数据3B    数据3C    数据3D    数据3E
 * </pre>
 * 第一行为标题行（header 内容），后续行为数据行。
 *
 * @param <T> 数据对象类型
 * @author labzen
 */
@Slf4j
public final class TxtFileWriter<T> extends AbstractDataFileWriter<T> {

  /**
   * 用于分隔列
   */
  public static final String DEFAULT_SEPARATOR = "    ";
  private String separator = DEFAULT_SEPARATOR;

  @Override
  public @NonNull FileFormat format() {
    return FileFormat.TXT;
  }

  @Override
  public void initialize(@NonNull FileConfiguration configuration) {
    this.separator = Strings.valueWhenEmpty(configuration.txtSeparator(), DEFAULT_SEPARATOR);
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  protected void generateContent(@Nonnull DataDefinition definition, @Nonnull List<Map<String, Object>> rows, @Nonnull OutputStream outputStream) {
    List<String> headers = definition.getHeaders().getLeafLevelHeaders();
    Map<String, Column> columns = definition.getColumns();

    try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
      // 第一行：标题行（使用 header 内容）
      // TXT 不支持多级表头，只取最低级别的表头（最后一个元素）
      String headerLine = buildHeaderLine(headers);
      writer.write(headerLine + "\n");

      // 后续行：数据行
      for (Map<String, Object> row : rows) {
        String dataLine = buildDataLine(row, columns);
        writer.write(dataLine + "\n");
      }

      writer.flush();
    } catch (IOException e) {
      throw new DataWriteException(e, "TXT 文件写入失败");
    }
  }

  /**
   * 构建标题行
   * <p>
   * 按 columns 的顺序，使用 header 的最后一个值作为列标题（取最低级别的表头）
   *
   * @param headers 列定义映射
   * @return 标题行字符串
   */
  private String buildHeaderLine(List<String> headers) {
    return headers.stream()
      .map(header -> Strings.valueWhenBlank(header, "unknown-header"))
      .collect(Collectors.joining(separator));
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
  private String buildDataLine(Map<String, Object> row, Map<String, Column> columns) {
    return columns.keySet().stream()
      .map(key -> {
        Object value = row.get(key);
        return Strings.value(value, "");
      })
      .collect(Collectors.joining(separator));
  }
}
