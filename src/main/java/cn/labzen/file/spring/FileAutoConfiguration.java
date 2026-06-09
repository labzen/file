package cn.labzen.file.spring;

import cn.labzen.file.definition.DefinitionLoader;
import cn.labzen.file.definition.DefinitionRegistry;
import cn.labzen.file.meta.FileConfiguration;
import cn.labzen.meta.Labzens;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * Labzen File 自动配置类
 * <p>
 * 在 Spring Boot 应用启动时，自动从 {@link FileConfiguration} 读取配置参数，
 * 创建 {@link DefinitionLoader} 并执行数据导出定义文件的加载与注册。
 */
@AutoConfiguration
@ConditionalOnClass(DefinitionLoader.class)
public class FileAutoConfiguration implements SmartInitializingSingleton, DisposableBean {

//  private ApplicationContext applicationContext;
//
//  @Override
//  public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
//    this.applicationContext = applicationContext;
//  }

  @Override
  public void afterSingletonsInstantiated() {
    FileConfiguration configuration = Labzens.configurationWith(FileConfiguration.class);
    loadDefinition(configuration);
//    initI18nStore(configuration);
  }

  @Override
  public void destroy() {
    DefinitionRegistry.clear();
  }

  /**
   * 加载数据定义文件
   */
  private void loadDefinition(FileConfiguration configuration) {
    String dataLocationPattern = configuration.dataDefinitionLocation();
    String globalDefinitionFilename = configuration.globalDefinitionFilename();

    DefinitionLoader loader = new DefinitionLoader(dataLocationPattern, globalDefinitionFilename);
    loader.load();
  }
//
//  /**
//   * 注册 I18nMessageSource
//   * <p>
//   * 优先从 Spring 容器中获取开发者提供的 {@link I18nMessageSource} 实现；
//   * 若未提供，则使用 {@link ManualI18nMessageSource}（基于内存的默认实现）。
//   */
//  private void initI18nStore(FileConfiguration configuration) {
//    I18nMessageSource messageSource = applicationContext.getBeanProvider(I18nMessageSource.class)
//      .getIfAvailable(ManualI18nMessageSource::new);
//    messageSource.setDefaultLocale(configuration.defaultLocale());
//    I18nMessageSourceHolder.register(messageSource);
//  }

//  /**
//   * 注册 I18nStore
//   * <p>
//   * 优先从 Spring 容器中获取开发者提供的 {@link I18nMessageSource} 实现；
//   * 若未提供，则使用 {@link ManualI18nMessageSource}（基于内存的默认实现）。
//   */
//  @Bean
//  public I18nMessageSource i18nMessageSource(ApplicationContext applicationContext) {
//    FileConfiguration config = Labzens.configurationWith(FileConfiguration.class);
//
//    // 尝试从 Spring 容器中获取开发者提供的 I18nMessageSource 实现
//    I18nMessageSource messageSource = applicationContext.getBeanProvider(I18nMessageSource.class)
//      .getIfAvailable(() -> new ManualI18nMessageSource(config.defaultLocale()));
//
//    // 注册到全局持有者，供非 Spring 管理的组件（如 SPI 加载的 Writer）使用
//    I18nMessageSourceHolder.register(messageSource);
//
//    return messageSource;
//  }
}
