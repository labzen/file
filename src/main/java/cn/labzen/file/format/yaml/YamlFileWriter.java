package cn.labzen.file.format.yaml;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.exception.DataWriteException;
import cn.labzen.file.format.core.writer.AbstractDataFileWriter;
import cn.labzen.file.meta.FileConfiguration;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * YAML 文件导出器
 * <p>
 * 实现 YAML 格式文件的生成，输出格式为顶级数组类型。
 * 数据对象的属性键使用 columns 的 key 值（字段名），而不是 header 的定义内容。
 * 不包含表头信息。
 *
 * @param <T> 数据对象类型
 * @author labzen
 */
@Slf4j
public final class YamlFileWriter<T> extends AbstractDataFileWriter<T> {

  /**
   * SnakeYaml Yaml 实例，用于 YAML 序列化
   */
  private Yaml yaml;

  @Override
  public @NonNull FileFormat format() {
    return FileFormat.YAML;
  }

  @Override
  public void initialize(@NonNull FileConfiguration configuration) {
    yaml = YamlCreator.create(configuration.yamlPrettyFormat(), configuration.yamlKebabCaseEnabled());
  }

  @Override
  protected void exportContent(@Nonnull DataDefinition definition, @Nonnull List<Map<String, Object>> rows, @Nonnull OutputStream outputStream) {
    try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
      yaml.dump(rows, writer);
      writer.flush();
    } catch (IOException e) {
      throw new DataWriteException(e, "YAML 文件导出失败");
    }
  }
}