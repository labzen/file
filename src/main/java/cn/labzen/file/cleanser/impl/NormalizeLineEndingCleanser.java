package cn.labzen.file.cleanser.impl;

import cn.labzen.file.annotation.DataCleanser;
import cn.labzen.file.cleanser.Cleanser;

/**
 * 规范化换行符
 * <p>
 * \r\n 和 \r → \n
 *
 * @author labzen
 */
@DataCleanser(name = "normalize-line-ending", priority = 30)
public class NormalizeLineEndingCleanser implements Cleanser {

  @Override
  public String cleanse(String input) {
    return input.replace("\r\n", "\n").replace("\r", "\n");
  }
}
