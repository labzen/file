package cn.labzen.file.locale;

import cn.labzen.tool.util.Strings;

import java.util.List;
import java.util.ResourceBundle;

public abstract class FormattableResourceBundle extends ResourceBundle {

  public String getString(String key, Object... args) {
    String text = getString(key);
    if (args != null && args.length > 0 && !text.equals(key)) {
      return Strings.format(text, args);
    }
    return text;
  }

  public String getString(String key, List<Object> args) {
    String text = getString(key);
    if (args != null && !args.isEmpty() && !text.equals(key)) {
      return Strings.format(text, args);
    }
    return text;
  }
}
