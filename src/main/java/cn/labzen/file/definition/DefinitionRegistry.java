package cn.labzen.file.definition;

import cn.labzen.file.converter.ChainableConverterExecutor;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.table.HeaderStructure;

import jakarta.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置注册中心
 * <p>
 * 存储所有加载并合并后的表数据导出配置实例。
 * 提供统一的配置访问入口
 *
 * @author labzen
 * @see DataDefinition
 */
public final class DefinitionRegistry {

  /**
   * 配置存储映射
   * key: 配置名称（如 User -> user-export.yml）
   * value: 合并后的配置对象
   */
  private static final Map<String, DataDefinition> DEFINITION_MAP = new ConcurrentHashMap<>();

  private DefinitionRegistry() {
  }

  /**
   * 注册配置
   *
   * @param name       配置名称
   * @param definition 数据配置对象
   */
  static void register(@Nonnull String name, @Nonnull DataDefinition definition/*, @Nonnull HeaderStructure headerStructure*/) {
    if (name.isBlank()) {
      throw new IllegalArgumentException("配置名称不能为空");
    }

    ChainableConverterExecutor.build(definition);
    DEFINITION_MAP.put(name, definition);
  }

  /**
   * 根据名称获取配置
   *
   * @param name 配置名称
   * @return 配置对象，如果不存在返回 Optional.empty()
   */
  public static Optional<DataDefinition> get(String name) {
    return Optional.ofNullable(DEFINITION_MAP.get(name));
  }

  /**
   * 检查配置是否存在
   *
   * @param name 配置名称
   * @return 是否存在
   */
  public static boolean contains(String name) {
    return DEFINITION_MAP.containsKey(name);
  }

  /**
   * 获取所有配置名称
   *
   * @return 配置名称列表
   */
  public static Set<String> getNames() {
    return new java.util.LinkedHashSet<>(DEFINITION_MAP.keySet());
  }

  /**
   * 获取配置数量
   *
   * @return 配置数量
   */
  public static int size() {
    return DEFINITION_MAP.size();
  }

  /**
   * 判断是否为空
   *
   * @return 是否为空
   */
  public static boolean isEmpty() {
    return DEFINITION_MAP.isEmpty();
  }

  /**
   * 清除所有配置
   */
  public static void clear() {
    DEFINITION_MAP.clear();
    ChainableConverterExecutor.clear();
  }
}
