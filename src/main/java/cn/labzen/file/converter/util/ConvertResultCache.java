package cn.labzen.file.converter.util;

import cn.labzen.algorithm.crypto.Digests;
import cn.labzen.tool.util.Strings;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 转换结果缓存器
 * <p>
 * 用于缓存转换器的转换结果，以避免重复计算。
 *
 * @author labzen
 */
public final class ConvertResultCache {

  private static final Cache<String, Object> CACHE = Caffeine.newBuilder()
    .maximumSize(10000)
    .expireAfterAccess(60, TimeUnit.SECONDS)
    .build();

  private ConvertResultCache() {
  }

  public static <T> T computeExportValueIfAbsent(Object input, List<Object> arguments, Supplier<Object> supplier) {
    String key = Strings.value(input, "") + "#ex#" + Digests.blake3(arguments);
    //noinspection unchecked
    return (T) CACHE.get(key, k -> supplier.get());
  }

  public static <T> T computeImportValueIfAbsent(Object input, List<Object> arguments, Supplier<Object> supplier) {
    String key = Strings.value(input, "") + "#im#" + Digests.blake3(arguments);
    //noinspection unchecked
    return (T) CACHE.get(key, k -> supplier.get());
  }
}
