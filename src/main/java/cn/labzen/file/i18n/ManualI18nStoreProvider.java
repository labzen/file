package cn.labzen.file.i18n;

import cn.labzen.tool.util.Strings;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的国际化文案仓库实现（内存存储）
 * <p>
 * 提供基于内存的简单存储，适用于无需持久化的场景或测试环境。
 * 生产环境建议实现 {@link I18nStoreProvider} 接口，从数据库等外部源加载文案。
 *
 * @author labzen
 */
public class ManualI18nStoreProvider implements I18nStoreProvider {

  /**
   * locale → (key → text)
   */
  private final Map<String, Map<String, String>> store = new ConcurrentHashMap<>();

  @Setter
  @Getter
  private String defaultLocale;

  @Override
  public String getText(String locale, String key) {
    // 精准匹配指定 locale
    Map<String, String> keyMap = store.get(locale);
    if (keyMap != null) {
      String text = keyMap.get(key);
      if (text != null) {
        return text;
      }
    }

    // 回退至默认 locale
    keyMap = store.get(defaultLocale);
    if (keyMap != null) {
      String text = keyMap.get(key);
      if (text != null) {
        return text;
      }
    }

    return key;
  }

  @Override
  public String getText(String locale, String key, Object... args) {
    String text = getText(locale, key);
    text = Strings.format(text, args);
    return text;
  }

  @Override
  public String getText(String locale, String key, List<Object> args) {
    String text = getText(locale, key);
    text = Strings.format(text, args);
    return text;
  }

  //  public String defaultLocale() {
//    return defaultLocale;
//  }

  public void put(String locale, String key, String text) {
    store.computeIfAbsent(locale, l -> new ConcurrentHashMap<>()).put(key, text);
  }

  public void putAll(String locale, Map<String, String> textMap) {
    store.computeIfAbsent(locale, l -> new ConcurrentHashMap<>()).putAll(textMap);
  }
}
