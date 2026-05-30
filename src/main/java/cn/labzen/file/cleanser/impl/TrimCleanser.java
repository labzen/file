package cn.labzen.file.cleanser.impl;

import cn.labzen.file.annotation.DataCleanser;
import cn.labzen.file.cleanser.Cleanser;

/**
 * 去除首尾空白（半角+全角空格）
 *
 * @author labzen
 */
@DataCleanser(name = "trim", priority = 0)
public class TrimCleanser implements Cleanser {

  @Override
  public String cleanse(String input) {
    // 先替换全角空格为半角空格，再trim
    return input.replace('\u3000', ' ').trim();
  }
}
