package cn.labzen.file.format.txt;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.TableColumn;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.exception.DataWriteException;
import cn.labzen.file.format.AbstractDataFileWriter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
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
 * 实现简单的文本表格格式文件生成，使用制表符（Tab）分隔列。
 * 结构：
 * <pre>
 * 属性名称\t属性值\t索引\t创建时间\t大小
 * 系统配置\tdebug=true\t1\t2026-05-12\t1024.5
 * 数据库连接\tjdbc:mysql://...\t2\t2026-05-11\t2048.75
 * </pre>
 * 第一行为标题行（header 内容），后续行为数据行。
 *
 * @param <T> 数据对象类型
 * @author labzen
 */
@Slf4j
public final class TxtFileWriter<T> extends AbstractDataFileWriter<T> {

  /**
   * 制表符，用于分隔列
   */
  private static final String TAB_SEPARATOR = "\t";

  @Override
  public @NonNull FileFormat format() {
    return FileFormat.TXT;
  }

  @Override
  protected void generateContent(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull OutputStream outputStream) {
    if (data.isEmpty()) {
      throw new DataWriteException("数据集合不能为空");
    }

    List<Map<String, Object>> rows = extractRows(definition, data);
    Map<String, TableColumn> columns = definition.getColumns();

    try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
      // 第一行：标题行（使用 header 内容）
      String headerLine = buildHeaderLine(columns);
      writer.write(headerLine);
      writer.write("\n");

      // 后续行：数据行
      for (Map<String, Object> row : rows) {
        String dataLine = buildDataLine(row, columns);
        writer.write(dataLine);
        writer.write("\n");
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
   * @param columns 列定义映射
   * @return 标题行字符串
   */
  private String buildHeaderLine(Map<String, TableColumn> columns) {
    return columns.values().stream()
      .map(col -> {
        List<String> header = col.getHeader();
        // TXT 不支持多级表头，只取最低级别的表头（最后一个元素）
        return header != null && !header.isEmpty() ? header.getLast() : "";
      })
      .collect(Collectors.joining(TAB_SEPARATOR));
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
        return value != null ? String.valueOf(value) : "";
      })
      .collect(Collectors.joining(TAB_SEPARATOR));
  }
}
