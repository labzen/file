package cn.labzen.file.cleanser.impl;

import cn.labzen.file.annotation.DataCleanser;
import cn.labzen.file.cleanser.Cleanser;

import java.util.Map;

/**
 * 智能引号→直引号
 * <p>
 * 替换各种语言中的弯引号、书名号等为标准直引号
 *
 * @author labzen
 */
@DataCleanser(name = "normalize-quotes", priority = 50)
public class NormalizeQuotesCleanser implements Cleanser {

  private static final Map<String, String> QUOTE_REPLACEMENTS = Map.ofEntries(
    Map.entry("\u201C", "\""),   // " → "
    Map.entry("\u201D", "\""),   // " → "
    Map.entry("\u2018", "'"),    // ' → '
    Map.entry("\u2019", "'"),    // ' → '
    Map.entry("\u300C", "\""),   // 「 → "
    Map.entry("\u300D", "\""),   // 」 → "
    Map.entry("\u300E", "\""),   // 『 → "
    Map.entry("\u300F", "\"")    // 』 → "
  );

  @Override
  public String cleanse(String input) {
    String result = input;
    for (Map.Entry<String, String> entry : QUOTE_REPLACEMENTS.entrySet()) {
      result = result.replace(entry.getKey(), entry.getValue());
    }
    return result;
  }
}
