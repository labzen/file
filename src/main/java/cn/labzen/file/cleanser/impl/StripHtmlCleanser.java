package cn.labzen.file.cleanser.impl;

import cn.labzen.file.annotation.DataCleanser;
import cn.labzen.file.cleanser.Cleanser;

/**
 * 去除HTML标签
 * <p>
 * 用于用户从网页复制粘贴的场景
 *
 * @author labzen
 */
@DataCleanser(name = "strip-html", priority = 40)
public class StripHtmlCleanser implements Cleanser {

  private static final String HTML_TAG_PATTERN = "<[^>]+>";

  @Override
  public String cleanse(String input) {
    return input.replaceAll(HTML_TAG_PATTERN, "");
  }
}
