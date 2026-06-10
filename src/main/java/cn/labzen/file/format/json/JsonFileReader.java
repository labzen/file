package cn.labzen.file.format.json;

import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.format.core.reader.AbstractDataFileReader;
import cn.labzen.file.meta.FileConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import java.io.InputStream;
import java.util.*;

/**
 * JSON 文件读取器
 * <p>
 * 使用 Jackson 流式读取 JSON 数组，每个对象为一行数据。
 *
 * @author labzen
 */
public class JsonFileReader extends AbstractDataFileReader {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public FileFormat format() {
    return FileFormat.JSON;
  }

  @Override
  public void initialize(@NonNull FileConfiguration configuration) {
    // do nothing
  }

  @Override
  protected List<Map<String, String>> importContent(@NonNull InputStream inputStream) {
    try {
      JsonNode rootNode = OBJECT_MAPPER.readTree(inputStream);

      if (!rootNode.isArray()) {
        throw new IllegalArgumentException("JSON导入数据必须是数组格式");
      }

      List<Map<String, String>> dataRows = new ArrayList<>();
      for (JsonNode element : rootNode) {
        Map<String, String> rowData = new LinkedHashMap<>();
        Set<Map.Entry<String, JsonNode>> properties = element.properties();
        for (Map.Entry<String, JsonNode> propertyEntry : properties) {
          String value = propertyEntry.getValue().isNull() ? null : propertyEntry.getValue().asText();
          rowData.put(propertyEntry.getKey(), value);
        }

        dataRows.add(rowData);
      }

      return dataRows;
    } catch (Exception e) {
      throw new RuntimeException("JSON文件读取失败", e);
    }
  }
}
