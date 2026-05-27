package cn.labzen.file.i18n;

/**
 * 国际化文案仓库接口
 * <p>
 * 定义国际化文本的存取契约。开发者需实现此接口以提供国际化内容。来为导出文件时提供国际化文案的支撑能力。
 */
public interface I18nStoreProvider {

  /**
   * 获取国际化文本
   *
   * @param locale 语言标签，如 zh-CN、en-US
   * @param key    文案键，对应 YAML 中 ${key} 的 key
   * @return 对应的文本；若精准匹配不到，返回默认 locale 的文本；若仍无，返回 key 本身
   */
  String getText(String locale, String key);

  /**
   * 设置默认语言标签
   *
   * @param defaultLocale 默认语言标签，如 zh-CN
   */
  void setDefaultLocale(String defaultLocale);
}
