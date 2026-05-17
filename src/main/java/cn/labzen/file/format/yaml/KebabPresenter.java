package cn.labzen.file.format.yaml;

import cn.labzen.tool.util.Strings;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Kebab 命名的 YAML 节点生成器
 */
public class KebabPresenter extends Representer {

  public KebabPresenter(DumperOptions options) {
    super(options);
  }

  @Override
  protected Node representMapping(Tag tag, Map<?, ?> mapping, DumperOptions.FlowStyle flowStyle) {
    Map<Object, Object> kebabMapping = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : mapping.entrySet()) {
      Object key = entry.getKey();
      if (key instanceof String k) {
        key = Strings.kebabCase(k);
      }
      kebabMapping.put(key, entry.getValue());
    }
    return super.representMapping(tag, kebabMapping, flowStyle);
  }
}
