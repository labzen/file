package cn.labzen.file.format.yaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

final class YamlCreator {

  static Yaml create(boolean prettyFormat, boolean kebabCaseEnabled) {
    DumperOptions dumperOptions = new DumperOptions();
    dumperOptions.setPrettyFlow(prettyFormat);

    Representer representer;
    if (kebabCaseEnabled) {
      representer = new KebabPresenter(dumperOptions);
    } else {
      representer = new Representer(dumperOptions);
    }

    return new Yaml(representer, dumperOptions);
  }
}
