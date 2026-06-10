package cn.labzen.file.format.xml;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.exception.DataWriteException;
import cn.labzen.file.format.core.writer.AbstractDataFileWriter;
import cn.labzen.file.meta.FileConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * XML 文件导出器
 * <p>
 * 实现 XML 格式文件的生成，输出格式为顶级数组类型。
 * 数据对象的属性键使用 columns 的 key 值（字段名），而不是 header 的定义内容。
 * 不包含表头信息。
 * <p>
 * XML 结构示例：
 * <pre>
 * &lt;domain-name title="标题"&gt;
 *   &lt;record&gt;
 *     &lt;name&gt;...&lt;/name&gt;
 *     &lt;value&gt;...&lt;/value&gt;
 *   &lt;/record&gt;
 * &lt;/property&gt;
 * </pre>
 *
 * @param <T> 数据对象类型
 * @author labzen
 */
@Slf4j
public final class XmlFileWriter<T> extends AbstractDataFileWriter<T> {

  private static final String DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

  @Override
  public @NonNull FileFormat format() {
    return FileFormat.XML;
  }

  @Override
  public void initialize(@NonNull FileConfiguration configuration) {
    // do nothing
  }

  @Override
  protected void exportContent(@Nonnull DataDefinition definition, @Nonnull List<Map<String, Object>> rows, @Nonnull OutputStream outputStream) {
    String filename = definition.getExportFilename();
    String title = definition.getExportTitle();

    try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
      String content = buildXml(filename, title, rows);
      writer.write(content);
      writer.flush();
    } catch (IOException e) {
      throw new DataWriteException(e, "XML 文件导出失败");
    }
  }

  /**
   * 构建 XML 字符串
   *
   * @param filename 根节点名称
   * @param title    根节点 title 属性
   * @param rows     数据行列表
   * @return XML 字符串
   */
  private String buildXml(String filename, String title, List<Map<String, Object>> rows) {
    StringBuilder sb = new StringBuilder();

    // 构建根节点，包含 title 属性
    sb.append(DECLARATION);
    sb.append("<").append(filename).append(" title=\"").append(title).append("\">\n");

    // 构建 record 节点
    for (Map<String, Object> row : rows) {
      sb.append("  <record>\n");
      for (Map.Entry<String, Object> entry : row.entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();

        sb.append("    <").append(key).append(">");
        if (value != null) {
          sb.append(escapeXml(String.valueOf(value)));
        }
        sb.append("</").append(key).append(">\n");
      }
      sb.append("  </record>\n");
    }

    // 关闭根节点
    sb.append("</").append(filename).append(">\n");

    return sb.toString();
  }

  /**
   * XML 特殊字符转义
   *
   * @param value 原始值
   * @return 转义后的值
   */
  private String escapeXml(String value) {
    if (value == null) {
      return "";
    }
    return value
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("'", "&apos;");
  }
}