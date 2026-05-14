package cn.labzen.file.format.json;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.exception.DataWriteException;
import cn.labzen.file.format.AbstractDataFileWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * JSON 文件写入器
 * <p>
 * 实现 JSON 格式文件的生成，输出格式为顶级数组类型。
 * 数据对象的属性键使用 columns 的 key 值（字段名），而不是 header 的定义内容。
 * 不包含表头信息。
 *
 * @param <T> 数据对象类型
 * @author labzen
 */
@Slf4j
public final class JsonFileWriter<T> extends AbstractDataFileWriter<T> {

  /**
   * Jackson ObjectMapper 实例，用于 JSON 序列化
   */
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
    .enable(SerializationFeature.INDENT_OUTPUT)
    .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

  @Override
  public @NonNull FileFormat format() {
    return FileFormat.JSON;
  }

  @Override
  protected void generateContent(@Nonnull DataDefinition definition, @Nonnull List<T> data, @Nonnull OutputStream outputStream) {
    if (data.isEmpty()) {
      throw new DataWriteException("数据集合不能为空");
    }

    List<Map<String, Object>> rows = extractRows(definition, data);

    try {
      OBJECT_MAPPER.writeValue(outputStream, rows);
      outputStream.flush();
    } catch (IOException e) {
      throw new DataWriteException(e, "JSON 文件写入失败");
    }
  }
}