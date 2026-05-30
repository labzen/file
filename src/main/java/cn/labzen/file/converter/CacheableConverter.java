//package cn.labzen.file.converter;
//
//import cn.labzen.algorithm.crypto.Digests;
//import cn.labzen.file.converter.exportable.CacheableExportConverter;
//import cn.labzen.file.converter.importable.ImportableConverter;
//import cn.labzen.tool.util.Strings;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * 双向可缓存转换器基类
// * <p>
// * 同时支持导出和导入方向的缓存转换器。
// * 子类实现 {@link #doConvertForExport} 和 {@link #doConvertForImport} 即可。
// *
// * @author labzen
// */
//public abstract class CacheableConverter extends CacheableExportConverter implements ImportableConverter {
//
//  private final Map<String, Object> importCache = new ConcurrentHashMap<>();
//
//  @Override
//  public Object convertForImport(Object input, List<Object> arguments, Class<?> targetType) {
//    String cacheKey = key(input, arguments) + "#" + targetType.getName();
//    return importCache.computeIfAbsent(cacheKey, k -> doConvertForImport(input, arguments, targetType));
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
