package cn.labzen.file.cleanser.impl;

import cn.labzen.file.annotation.DataCleanser;
import cn.labzen.file.cleanser.Cleanser;

/**
 * 全角数字→半角数字
 * <p>
 * 如 １２３ → 123
 *
 * @author labzen
 */
@DataCleanser(name = "collapse-digits", priority = 60)
public class CollapseDigitsCleanser implements Cleanser {

  @Override
  public String cleanse(String input) {
    StringBuilder sb = new StringBuilder(input.length());
    for (char c : input.toCharArray()) {
      if (c >= '\uFF10' && c <= '\uFF19') {
        sb.append((char) (c - '\uFF10' + '0'));
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }
}
