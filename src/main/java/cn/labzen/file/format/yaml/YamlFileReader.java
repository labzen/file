package cn.labzen.file.format.yaml;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.format.core.reader.AbstractDataFileReader;
import cn.labzen.file.meta.FileConfiguration;
import com.google.common.collect.Lists;
import jakarta.annotation.Nonnull;
import org.jspecify.annotations.NonNull;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * YAML 文件读取器
 * <p>
 * 使用 SnakeYAML 解析YAML数组，每个对象为一行数据。
 *
 * @author labzen
 */
public class YamlFileReader extends AbstractDataFileReader {

  private Yaml yaml;

  @Override
  public FileFormat format() {
    return FileFormat.YAML;
  }

  @Override
  public void initialize(@NonNull FileConfiguration configuration) {
    yaml = YamlCreator.create(configuration.yamlPrettyFormat(), configuration.yamlKebabCaseEnabled());
  }

  @Override
  protected List<Map<String, String>> importContent(@NonNull InputStream inputStream) {
    try {
      Iterable<Object> documents = yaml.loadAll(inputStream);

      List<Map<String, String>> dataRows = new ArrayList<>();
      for (Object doc : documents) {
        if (doc instanceof List<?> list) {
//          for (Object item : list) {
//            if (item instanceof Map<?, ?> map) {
//              Map<String, String> rowData = new LinkedHashMap<>();
//              for (Map.Entry<?, ?> entry : map.entrySet()) {
//                String key = entry.getKey() != null ? entry.getKey().toString() : null;
//                String value = entry.getValue() != null ? entry.getValue().toString() : null;
//                if (key != null) {
//                  rowData.put(key, value);
//                }
//              }
//              dataRows.add(rowData);
//            }
//          }
          List<Map<String, String>> rows = readAsList(list);
          dataRows.addAll(rows);
        } else if (doc instanceof Map<?, ?> map) {
//          Map<String, String> rowData = new LinkedHashMap<>();
//          for (Map.Entry<?, ?> entry : map.entrySet()) {
//            String key = entry.getKey() != null ? entry.getKey().toString() : null;
//            String value = entry.getValue() != null ? entry.getValue().toString() : null;
//            if (key != null) {
//              rowData.put(key, value);
//            }
//          }
          Map<String, String> data = readAsMap(map);
          dataRows.add(data);
        }
      }

      return dataRows;
    } catch (Exception e) {
      throw new RuntimeException("YAML文件读取失败", e);
    }
  }

  private List<Map<String, String>> readAsList(List<?> list) {
    List<Map<String, String>> datum = Lists.newArrayList();
    for (Object item : list) {
      if (item instanceof Map<?, ?> map) {
        Map<String, String> data = readAsMap(map);
        datum.add(data);
      }
    }
    return datum;
  }

  private Map<String, String> readAsMap(Map<?, ?> map) {
    Map<String, String> data = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      String key = entry.getKey() != null ? entry.getKey().toString() : null;
      String value = entry.getValue() != null ? entry.getValue().toString() : null;
      if (key != null) {
        data.put(key, value);
      }
    }
    return data;
  }
}
