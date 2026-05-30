//package cn.labzen.file.converter;
//
//import cn.labzen.file.annotation.DataConverter;
//import cn.labzen.file.definition.bean.DataDefinition;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.ServiceLoader;
//import java.util.function.BiConsumer;
//
//@Slf4j
//public abstract class ChainableConverterExecutor {
//
////  private static final Map<String, ChainableConverterExecutor> CHAIN_CACHE = Maps.newHashMap();
//
////  private final List<ConfiguredConverter> converters = Lists.newArrayList();
//
//  protected static void loadConverterInstances(BiConsumer<Converter, DataConverter> consumer) {
//    // 通过 SPI 机制加载所有 Converter 实现
//    ServiceLoader.load(Converter.class).forEach(converter -> {
//      Class<?> type = converter.getClass();
//      if (!type.isAnnotationPresent(DataConverter.class)) {
//        return;
//      }
//      DataConverter annotation = type.getAnnotation(DataConverter.class);
//      consumer.accept(converter, annotation);
//    });
//  }
//
//  protected ChainableConverterExecutor() {
//  }
//
//
//  private void createConfigured(String converterName, Object... args) {
//    if (!CONVERTER_INSTANCES.containsKey(converterName)) {
//      logger.error("不存在的转换器: {}", converterName);
//      return;
//    }
//
//    ConverterInstance instance = CONVERTER_INSTANCES.get(converterName);
//    ConfiguredConverter cc;
//    if (args.length == 1 && args[0] instanceof List<?> list) {
//      //noinspection unchecked
//      cc = new ConfiguredConverter(instance.priority(), instance.converter(), (List<Object>) list);
//    } else {
//      cc = new ConfiguredConverter(instance.priority(), instance.converter(), Arrays.stream(args).toList());
//    }
//    converters.add(cc);
//  }
//
//  /**
//   * 执行转换器
//   */
//  public abstract Object execute(Object input);
//
//  /**
//   * 为指定的 DataDefinition 构建转换器执行器映射（非缓存）
//   * <p>
//   * 用于 i18n 解析后的定义，每次调用都创建新的执行器实例，不影响全局缓存
//   *
//   * @param definition 数据定义
//   * @return 字段名 → 转换器执行器 的映射
//   */
//  public static Map<String, ChainableConverterExecutor> buildFor(DataDefinition definition) {
//    Map<String, ChainableConverterExecutor> executors = new java.util.HashMap<>();
//    definition.getColumns().forEach((columnName, column) ->
//      executors.put(columnName, new ChainableConverterExecutor(column))
//    );
//    return executors;
//  }
//
//  public static ChainableConverterExecutor get(String domainName, String columnName) {
//    String key = cacheKey(domainName, columnName);
//    return CHAIN_CACHE.get(key);
//  }
//
//  private static String cacheKey(String domainName, String columnName) {
//    return domainName + "@@" + columnName;
//  }
//
//  public static void clear() {
//    CHAIN_CACHE.clear();
//  }
//
//  record ConverterInstance(String name, int priority, Converter<?> converter) {
//  }
//
//  public record ConfiguredConverter(int priority, Converter<?> converter, List<Object> arguments) {
//  }
//}
