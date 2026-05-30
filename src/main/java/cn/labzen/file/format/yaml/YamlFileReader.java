package cn.labzen.file.format.yaml;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.format.core.reader.AbstractDataFileReader;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;

/**
 * YAML 文件读取器
 * <p>
 * 使用 SnakeYAML 解析YAML数组，每个对象为一行数据。
 *
 * @author labzen
 */
public class YamlFileReader extends AbstractDataFileReader {

  @Override
  public FileFormat format() {
    return FileFormat.YAML;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Iterator<Map<String, String>> doRead(InputStream inputStream, DataDefinition definition) {
    try {
      Yaml yaml = new Yaml();
      Iterable<Object> documents = yaml.loadAll(inputStream);

      List<Map<String, String>> dataRows = new ArrayList<>();
      for (Object doc : documents) {
        if (doc instanceof List<?> list) {
          for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
              Map<String, String> rowData = new LinkedHashMap<>();
              for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = entry.getKey() != null ? entry.getKey().toString() : null;
                String value = entry.getValue() != null ? entry.getValue().toString() : null;
                if (key != null) {
                  rowData.put(key, value);
                }
              }
              dataRows.add(rowData);
            }
          }
        } else if (doc instanceof Map<?, ?> map) {
          Map<String, String> rowData = new LinkedHashMap<>();
          for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey() != null ? entry.getKey().toString() : null;
            String value = entry.getValue() != null ? entry.getValue().toString() : null;
            if (key != null) {
              rowData.put(key, value);
            }
          }
          dataRows.add(rowData);
        }
      }

      return dataRows.iterator();
    } catch (Exception e) {
      throw new RuntimeException("YAML文件读取失败", e);
    }
  }
}
