package cn.labzen.file.format.md;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.Column;
import cn.labzen.file.definition.enums.Alignment;
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
 * Markdown 文件导出器
 * <p>
 * 实现 Markdown 表格格式文件的生成。
 * 结构：
 * <pre>
 * # 标题
 *
 * | 表头A | 表头B | 表头C | 表头D | 表头E |
 * |:---|---:|---|---|---:|
 * | 数据1A | 数据1B | 数据1C | 数据1D | 数据1E |
 * | 数据2A | 数据2B | 数据2C | 数据2D | 数据2E |
 * | 数据3A | 数据3B | 数据3C | 数据3D | 数据3E |
 * </pre>
 * 第一行为 title 作为 Markdown 大标题（# 标题），
 * 第二行为空行，
 * 后续为 Markdown 表格（标题行 + 分隔行 + 数据行）。
 * <p>
 * 对齐支持（基于列的 style.align 配置）：
 * <ul>
 *   <li>LEFT - 左对齐：|:---|</li>
 *   <li>RIGHT - 右对齐：|---:|
 *   <li>其他（CENTER 等）- 居中对齐：|:---:|
 * </ul>
 *
 * @param <T> 数据对象类型
 * @author labzen
 */
@Slf4j
public final class MarkdownFileWriter<T> extends AbstractDataFileWriter<T> {

  /**
   * 表格分隔符（前后都有空格）
   */
  private static final String TABLE_SEPARATOR = " | ";
  /**
   * 表格开始边框
   */
  private static final String TABLE_START = "| ";
  /**
   * 表格结束边框
   */
  private static final String TABLE_END = " |";

  @Override
  public @NonNull FileFormat format() {
    return FileFormat.MARKDOWN;
  }

  @Override
  public void initialize(@NonNull FileConfiguration configuration) {

  }

  @Override
  protected void exportContent(@Nonnull DataDefinition definition, @Nonnull List<Map<String, Object>> rows, @Nonnull OutputStream outputStream) {
    List<String> headers = definition.getHeaders().getLeafLevelHeaders();
    Map<String, Column> columns = definition.getColumns();
    String title = definition.getExportTitle();

    try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
      // 第一行：Markdown 大标题
      writer.write("# " + title + "\n\n");

      // 第二部分：表格标题行
      String headerLine = buildHeaderLine(headers);
      writer.write(headerLine + "\n");

      // 第三部分：表格分隔行（---|---|格式）
      String separatorLine = buildSeparatorLine(columns);
      writer.write(separatorLine + "\n");

      // 第四部分：数据行
      for (Map<String, Object> row : rows) {
        String dataLine = buildDataLine(row, columns);
        writer.write(dataLine + "\n");
      }

      writer.flush();
    } catch (IOException e) {
      throw new DataWriteException(e, "Markdown 文件导出失败");
    }
  }

  private String buildHeaderLine(List<String> headers) {
    String headerText = headers.stream()
      .map(header -> Strings.valueWhenBlank(header, "unknown-header"))
      .collect(Collectors.joining(TABLE_SEPARATOR));
    return TABLE_START + headerText + TABLE_END;
  }

  /**
   * 构建表格分隔行
   * <p>
   * 根据每个 column 的对齐方式生成不同的分隔符：
   * <ul>
   *   <li>LEFT - 左对齐：|:---|</li>
   *   <li>RIGHT - 右对齐：|---:|</li>
   *   <li>其他 - 居中对齐：|:---:|</li>
   * </ul>
   *
   * @param columns 列定义映射
   * @return 表格分隔行字符串（如 |:---|:---:|
   */
  private String buildSeparatorLine(Map<String, Column> columns) {
    String separators = columns.values().stream()
      .map(this::getAlignmentSeparator)
      .collect(Collectors.joining(TABLE_SEPARATOR));

    return TABLE_START + separators + TABLE_END;
  }

  /**
   * 根据对齐方式获取 Markdown 分隔符
   *
   * @param column 列定义
   * @return 分隔符（:---、---:、:---:）
   */
  private String getAlignmentSeparator(Column column) {
    if ( column.getExporting().getStyle() == null || column.getExporting().getStyle().getAlign() == null) {
      return ":---:"; // 默认居中对齐
    }

    Alignment align = column.getExporting().getStyle().getAlign();
    return switch (align) {
      case LEFT -> ":---";
      case RIGHT -> "---:";
      default -> ":---:"; // CENTER、HORIZONTAL 及其他默认居中对齐
    };
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
    String values = columns.keySet().stream()
      .map(key -> {
        Object value = row.get(key);
        return value != null ? String.valueOf(value) : "";
      })
      .collect(Collectors.joining(TABLE_SEPARATOR));

    return TABLE_START + values + TABLE_END;
  }
}
