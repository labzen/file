package cn.labzen.file.format.json;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.format.core.reader.AbstractDataFileReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
  protected Iterator<Map<String, String>> doRead(InputStream inputStream, DataDefinition definition) {
    try {
      JsonNode rootNode = OBJECT_MAPPER.readTree(inputStream);

      if (!rootNode.isArray()) {
        throw new IllegalArgumentException("JSON导入数据必须是数组格式");
      }

      List<Map<String, String>> dataRows = new ArrayList<>();
      for (JsonNode element : rootNode) {
        Map<String, String> rowData = new LinkedHashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = element.fields();
        while (fields.hasNext()) {
          Map.Entry<String, JsonNode> field = fields.next();
          String value = field.getValue().isNull() ? null : field.getValue().asText();
          rowData.put(field.getKey(), value);
        }
        dataRows.add(rowData);
      }

      return dataRows.iterator();
    } catch (Exception e) {
      throw new RuntimeException("JSON文件读取失败", e);
    }
  }
}
