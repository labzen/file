package cn.labzen.file.converter;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.TableColumn;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class ChainableConverterExecutor {

  private static final Map<String, ConverterInstance> CONVERTER_INSTANCES = Maps.newHashMap();
  private static final Map<String, ChainableConverterExecutor> CHAIN_CACHE = Maps.newHashMap();

  static {
    // 通过 SPI 机制加载所有 Converter 实现
    ServiceLoader.load(Converter.class).forEach(converter -> {
      @SuppressWarnings("unchecked")
      Class<? extends Converter<?>> type = (Class<? extends Converter<?>>) converter.getClass();
      if (!type.isAnnotationPresent(DataConverter.class)) {
        return;
      }
      DataConverter annotation = type.getAnnotation(DataConverter.class);
      String name = annotation.name();
      int priority = annotation.priority();
      CONVERTER_INSTANCES.put(name, new ConverterInstance(name, priority, converter));
    });
  }

  private final List<ConfiguredConverter> converters = Lists.newArrayList();

  ChainableConverterExecutor(TableColumn column) {
    configConverter(column);
    sortConverter();
  }

  private void sortConverter() {
    converters.sort(Comparator.comparingInt(ConfiguredConverter::priority));
  }

  private void createConfigured(String converterName, Object... args) {
    if (!CONVERTER_INSTANCES.containsKey(converterName)) {
      logger.error("不存在的转换器: {}", converterName);
      return;
    }

    ConverterInstance instance = CONVERTER_INSTANCES.get(converterName);
    ConfiguredConverter cc;
    if (args.length == 1 && args[0] instanceof List<?> list) {
      //noinspection unchecked
      cc = new ConfiguredConverter(instance.priority(), instance.converter(), (List<Object>) list);
    } else {
      cc = new ConfiguredConverter(instance.priority(), instance.converter(), Arrays.stream(args).toList());
    }
    converters.add(cc);
  }

  private void configConverter(TableColumn column) {
    if (column.getWhenNull() != null) {
      createConfigured(Converter.WHEN_NULL_NAME, column.getWhenNull());
    }
    if (column.getWhenBlank() != null) {
      createConfigured(Converter.WHEN_EMPTY_NAME, column.getWhenBlank());
    }
    if (column.getPattern() != null) {
      if (column.getPattern().getDate() != null) {
        createConfigured(Converter.DATE_NAME, column.getPattern().getDate());
      }
      if (column.getPattern().getNumber() != null) {
        createConfigured(Converter.NUMBER_NAME, column.getPattern().getNumber());
      }
    }
    if (column.getConverter() != null) {
      if (column.getConverter().getMapping() != null) {
        createConfigured(Converter.MAPPING_NAME, column.getConverter().getMapping());
      }
      if (column.getConverter().getEnumerable() != null) {
        createConfigured(Converter.ENUM_NAME, column.getConverter().getEnumerable());
      }
      String namedConverter = column.getConverter().getNamed();
      if (namedConverter != null) {
        List<NamedConverterParser.MethodInvokeInfo> methodInvokeInfos = NamedConverterParser.parseMethod(namedConverter);
        methodInvokeInfos.forEach(mi -> createConfigured(mi.methodName(), mi.args()));
      }
    }
    if (column.getPrefix() != null) {
      createConfigured(Converter.PREFIX_NAME, column.getPrefix());
    }
    if (column.getSuffix() != null) {
      createConfigured(Converter.SUFFIX_NAME, column.getSuffix());
    }
  }

  public Object execute(Object input) {
    Object latestValue = input;
    for (ConfiguredConverter cc : converters) {
      Converter<?> converter = cc.converter();
      if (converter.supports(latestValue != null ? latestValue.getClass() : Object.class)) {
        latestValue = converter.convert(latestValue, cc.arguments());
      }
    }
    return latestValue;
  }

  public static void build(DataDefinition definition) {
    String domainName = definition.getDomainName();
    definition.getColumns().forEach((columnName, column) -> {
      String key = cacheKey(domainName, columnName);
      CHAIN_CACHE.put(key, new ChainableConverterExecutor(column));
    });
  }

  /**
   * 为指定的 DataDefinition 构建转换器执行器映射（非缓存）
   * <p>
   * 用于 i18n 解析后的定义，每次调用都创建新的执行器实例，不影响全局缓存
   *
   * @param definition 数据定义
   * @return 字段名 → 转换器执行器 的映射
   */
  public static Map<String, ChainableConverterExecutor> buildFor(DataDefinition definition) {
    Map<String, ChainableConverterExecutor> executors = new java.util.HashMap<>();
    definition.getColumns().forEach((columnName, column) ->
      executors.put(columnName, new ChainableConverterExecutor(column))
    );
    return executors;
  }

  public static ChainableConverterExecutor get(String domainName, String columnName) {
    String key = cacheKey(domainName, columnName);
    return CHAIN_CACHE.get(key);
  }

  private static String cacheKey(String domainName, String columnName) {
    return domainName + "@@" + columnName;
  }

  public static void clear() {
    CHAIN_CACHE.clear();
  }

  record ConverterInstance(String name, int priority, Converter<?> converter) {
  }

  public record ConfiguredConverter(int priority, Converter<?> converter, List<Object> arguments) {
  }
}
