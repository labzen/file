package cn.labzen.file.cleanser.impl;

import cn.labzen.file.annotation.DataCleanser;
import cn.labzen.file.cleanser.Cleanser;

/**
 * 全角字母→半角字母
 * <p>
 * 如 ＡＢＣ → ABC
 *
 * @author labzen
 */
@DataCleanser(name = "collapse-letters", priority = 70)
public class CollapseLettersCleanser implements Cleanser {

  @Override
  public String cleanse(String input) {
    StringBuilder sb = new StringBuilder(input.length());
    for (char c : input.toCharArray()) {
      if (c >= '\uFF21' && c <= '\uFF3A') {
        // 全角大写 A-Z
        sb.append((char) (c - '\uFF21' + 'A'));
      } else if (c >= '\uFF41' && c <= '\uFF5A') {
        // 全角小写 a-z
        sb.append((char) (c - '\uFF41' + 'a'));
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }
}
