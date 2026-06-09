package cn.labzen.file.util;

import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LocaledTextWithPlaceholder {

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

  private LocaledTextWithPlaceholder() {
  }


  /**
   * 解析文本中的 ${key} 占位符
   *
   * @param text 原始文本，可能包含 ${key}
   * @return 替换后的文本
   */
  public static String resolve(ResourceBundle resourceBundle, String text) {
    if (text == null || !text.contains("${")) {
      return text;
    }

    Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
    StringBuilder result = new StringBuilder();
    while (matcher.find()) {
      String key = matcher.group(1);
      String replacement = resourceBundle.getString(key);
      matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(result);
    return result.toString();
  }
}
