package cn.labzen.file.i18n;

/**
 * 国际化文案仓库的全局持有者
 * <p>
 * 提供 {@link I18nStoreProvider} 的静态访问入口，供非 Spring 管理的组件（如通过 SPI 加载的 Writer）使用。
 * 在 Spring 上下文初始化时由 {@link cn.labzen.file.spring.FileAutoConfiguration} 注册。
 *
 * @author labzen
 */
public final class I18nStoreHolder {

  private static volatile I18nStoreProvider instance;

  private I18nStoreHolder() {
  }

  /**
   * 注册 I18nStore 实例
   *
   * @param store 国际化文案仓库
   */
  public static void register(I18nStoreProvider store) {
    instance = store;
  }

  /**
   * 获取 I18nStore 实例
   *
   * @return 国际化文案仓库，未注册时返回 null
   */
  public static I18nStoreProvider get() {
    return instance;
  }
}
