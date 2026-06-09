package cn.labzen.file.definition;

import cn.labzen.file.converter.executor.ChainableExportConverterExecutor;
import cn.labzen.file.converter.executor.ChainableImportConverterExecutor;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.locale.LocaledDefinitionResolver;
import jakarta.annotation.Nonnull;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置注册中心
 * <p>
 * 存储所有加载并合并后的数据配置实例。
 * 提供统一的配置访问入口。
 * 转换器链的构建和缓存由各自的 ChainableExecutor 独立管理。
 *
 * @author labzen
 * @see DataDefinition
 */
public final class DefinitionRegistry {

  private static final Map<String, DataDefinition> ORIGINAL_DEFINITION_MAP = new ConcurrentHashMap<>();
  private static final Map<String, DataDefinition> LOCALIZED_DEFINITION_MAP = new ConcurrentHashMap<>();

  private DefinitionRegistry() {
  }

  /**
   * 注册配置
   *
   * @param name       配置名称
   * @param definition 数据配置对象
   */
  static void register(@Nonnull String name, @Nonnull DataDefinition definition) {
    if (name.isBlank()) {
      throw new IllegalArgumentException("配置名称不能为空");
    }

    ORIGINAL_DEFINITION_MAP.put(name, definition);
  }

  /**
   * 根据名称获取原始配置
   *
   * @param name 配置名称
   * @return 配置对象，如果不存在返回 Optional.empty()
   */
  public static Optional<DataDefinition> get(String name) {
    return Optional.ofNullable(ORIGINAL_DEFINITION_MAP.get(name));
  }

  /**
   * 根据名称和区域获取配置
   */
  public static Optional<DataDefinition> get(String name, Locale locale) {
    return get(name).map(original -> {
      String key = name + "#[" + locale + "]";
      return LOCALIZED_DEFINITION_MAP.computeIfAbsent(key, k -> localize(original, locale));
    });
  }

  private static DataDefinition localize(@Nonnull DataDefinition original, Locale locale) {
    LocaledDefinitionResolver resolver = new LocaledDefinitionResolver(original, locale);
//    I18nResolver i18nResolver = new I18nResolver(original, locale);
    DataDefinition localizedDefinition = resolver.resolve();

//    List<Column> columns = localizedDefinition.getColumns().values().stream().toList();
//    HeaderStructure headerStructure = HeaderBuilder.build(localizedDefinition);
//    localizedDefinition.setHeaders(headerStructure);
    localizedDefinition.pretreatment();

    // 构建导出和导入转换器链
    ChainableExportConverterExecutor.build(localizedDefinition);
    ChainableImportConverterExecutor.build(localizedDefinition);

    return localizedDefinition;
  }

  public static Optional<String> getDefinitionFilename(String name) {
    return get(name).map(DataDefinition::getFilename);
  }

  /**
   * 检查配置是否存在
   *
   * @param name 配置名称
   * @return 是否存在
   */
  public static boolean contains(String name) {
    return ORIGINAL_DEFINITION_MAP.containsKey(name);
  }

  /**
   * 获取所有配置名称
   *
   * @return 配置名称列表
   */
  public static Set<String> getNames() {
    return new java.util.LinkedHashSet<>(ORIGINAL_DEFINITION_MAP.keySet());
  }

  /**
   * 获取配置数量
   *
   * @return 配置数量
   */
  public static int size() {
    return ORIGINAL_DEFINITION_MAP.size();
  }

  /**
   * 判断是否为空
   *
   * @return 是否为空
   */
  public static boolean isEmpty() {
    return ORIGINAL_DEFINITION_MAP.isEmpty();
  }

  /**
   * 清除所有配置
   */
  public static void clear() {
    ORIGINAL_DEFINITION_MAP.clear();
    ChainableExportConverterExecutor.clear();
    ChainableImportConverterExecutor.clear();
  }
}
