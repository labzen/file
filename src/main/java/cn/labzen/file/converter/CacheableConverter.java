package cn.labzen.file.converter;

import cn.labzen.file.exception.DataConvertException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持缓存的抽象转换器
 * <p>
 * 提供通用的缓存机制，子类只需实现具体的转换逻辑
 *
 * @param <I> 输入类型
 * @param <O> 输出类型
 * @param <C> 配置参数类型
 * @author labzen
 */
public abstract class CacheableConverter<O> implements Converter<O> {

  /**
   * 缓存映射：key = 输入值.toString() + 配置哈希，value = 转换结果
   */
  private final Map<String, O> cache = new ConcurrentHashMap<>();

  @Override
  public O convert(Object input, List<Object> argument) throws DataConvertException {
    String cacheKey = key(input, argument);
    return cache.computeIfAbsent(cacheKey, k -> doConvert(input, argument));
  }

  private String key(Object input, List<Object> argument) {
    return input != null ? input.toString() + argument.hashCode() : "null";
  }

  /**
   * 执行实际转换逻辑
   *
   * @param input    输入值
   * @param argument 配置参数
   * @return 转换后的值
   * @throws DataConvertException 如果转换失败
   */
  protected abstract O doConvert(Object input, List<Object> argument) throws DataConvertException;
}
