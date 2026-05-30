//package cn.labzen.file.converter.importable;
//
//import cn.labzen.algorithm.crypto.Digests;
//import cn.labzen.tool.util.Strings;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * 导入可缓存转换器基类
// * <p>
// * 仅支持导入方向的缓存转换器。子类实现 {@link #doConvertForImport} 即可。
// *
// * @author labzen
// */
//public abstract class CacheableImportConverter implements ImportableConverter {
//
//  private final Map<String, Object> cache = new ConcurrentHashMap<>();
//
//  @Override
//  public Object convertForImport(Object input, List<Object> arguments, Class<?> targetType) {
//    String cacheKey = key(input, arguments);
//    return cache.computeIfAbsent(cacheKey, k -> doConvertForImport(input, arguments, targetType));
//  }
//
//  @Override
//  public boolean supportsImport(Class<?> targetType) {
//    return true;
//  }
//
//  private String key(Object input, List<Object> arguments) {
//    return Strings.value(input, "") + "#" + Digests.blake3(arguments);
//  }
//
//  /**
//   * 执行实际的导入转换逻辑
//   *
//   * @param input      输入值
//   * @param arguments  配置参数
//   * @param targetType 目标Java类型
//   * @return 转换后的值
//   */
//  protected abstract Object doConvertForImport(Object input, List<Object> arguments, Class<?> targetType);
//}
