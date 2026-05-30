package cn.labzen.file.cleanser.impl;

import cn.labzen.file.annotation.DataCleanser;
import cn.labzen.file.cleanser.Cleanser;

/**
 * 规范化空白字符
 * <p>
 * 全角空格→半角空格，多个连续空白→单个空格
 *
 * @author labzen
 */
@DataCleanser(name = "normalize-whitespace", priority = 20)
public class NormalizeWhitespaceCleanser implements Cleanser {

  @Override
  public String cleanse(String input) {
    // 全角空格→半角空格
    String result = input.replace('\u3000', ' ');
    // 多个连续空白→单个空格
    result = result.replaceAll("\\s+", " ");
    return result;
  }
}
