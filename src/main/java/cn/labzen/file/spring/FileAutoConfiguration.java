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

  @Override
  public void afterSingletonsInstantiated() {
    FileConfiguration configuration = Labzens.configurationWith(FileConfiguration.class);
    loadDefinition(configuration);
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
}
