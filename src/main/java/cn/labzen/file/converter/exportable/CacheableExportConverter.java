//package cn.labzen.file.converter.exportable;
//
//import cn.labzen.algorithm.crypto.Digests;
//import cn.labzen.tool.util.Strings;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * 导出可缓存转换器基类
// * <p>
// * 仅支持导出方向的缓存转换器。子类实现 {@link #doConvertForExport} 即可。
// *
// * @author labzen
// */
//public abstract class CacheableExportConverter implements ExportableConverter {
//
//  private final Map<String, Object> cache = new ConcurrentHashMap<>();
//
//  @Override
//  public Object convertForExport(Object input, List<Object> arguments) {
//    String cacheKey = key(input, arguments);
//    return cache.computeIfAbsent(cacheKey, k -> doConvertForExport(input, arguments));
//  }
//
//  @Override
//  public boolean supportsExport(Class<?> sourceType) {
//    return true;
//  }
//
//  private String key(Object input, List<Object> arguments) {
//    return Strings.value(input, "") + "#" + Digests.blake3(arguments);
//  }
//
//  /**
//   * 执行实际的导出转换逻辑
//   *
//   * @param input     输入值
//   * @param arguments 配置参数
//   * @return 转换后的值
//   */
//  protected abstract Object doConvertForExport(Object input, List<Object> arguments);
//}
