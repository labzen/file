package cn.labzen.file.spring;

import cn.labzen.file.definition.DefinitionLoader;
import cn.labzen.file.meta.FileConfiguration;
import cn.labzen.meta.Labzens;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * Labzen File 自动配置类
 * <p>
 * 在 Spring Boot 应用启动时，自动从 {@link FileConfiguration} 读取配置参数，
 * 创建 {@link DefinitionLoader} 并执行数据导出定义文件的加载与注册。
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(DefinitionLoader.class)
public class FileAutoConfiguration {

  @PostConstruct
  void loadDefinitions() {
    FileConfiguration config = Labzens.configurationWith(FileConfiguration.class);

    String dataLocationPattern = config.dataDefinitionLocation();
    String globalDefinitionFilename = config.globalDefinitionFilename();

    logger.info("开始加载数据导出定义文件...");

    DefinitionLoader loader = new DefinitionLoader(dataLocationPattern, globalDefinitionFilename);
    loader.load();
  }
}
