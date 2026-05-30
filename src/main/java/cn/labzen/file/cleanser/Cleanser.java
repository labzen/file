package cn.labzen.file.cleanser;

/**
 * 清理器接口
 * <p>
 * 在导入管线中最先执行，用于对原始字符串进行归一化处理。
 * 输入和输出均为字符串，语义不变但格式规范化。
 *
 * @author labzen
 */
public interface Cleanser {

  /**
   * 清理输入字符串
   *
   * @param input 原始输入，不会为null（框架保证）
   * @return 清理后的字符串，允许返回空字符串，不允许返回null
   */
  String cleanse(String input);
}
