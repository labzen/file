package cn.labzen.file.cleanser.impl;

import cn.labzen.file.annotation.DataCleanser;
import cn.labzen.file.cleanser.Cleanser;

/**
 * 去除不可见Unicode字符
 * <p>
 * 包括：零宽空格(U+200B)、软连字符(U+00AD)、BOM(U+FEFF)、
 * 左右标记(U+200E/U+200F)、零宽连接符(U+200D)、零宽非连接符(U+200C)等
 *
 * @author labzen
 */
@DataCleanser(name = "strip-invisible", priority = 10)
public class StripInvisibleCleanser implements Cleanser {

  private static final String INVISIBLE_CHARS_PATTERN =
    "[\\u200B\\u200C\\u200D\\u200E\\u200F\\u00AD\\uFEFF\\u2060\\u180E]";

  @Override
  public String cleanse(String input) {
    return input.replaceAll(INVISIBLE_CHARS_PATTERN, "");
  }
}
