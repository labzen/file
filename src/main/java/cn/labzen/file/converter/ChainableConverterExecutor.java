package cn.labzen.file.converter;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.TableColumn;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ChainableConverterExecutor {

  // 方法匹配
  private static final Pattern METHOD_PATTERN = Pattern.compile("\\s*([a-zA-Z_$][\\w$]*)\\s*\\((.*?)\\)\\s*(?:;|$)");
  // 参数匹配
  private static final Pattern ARG_PATTERN = Pattern.compile("\\s*(?:\"([^\"]*)\"|([^,\\s][^,]*))\\s*(?:,|$)");
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
      if (column.getConverter().getEnumConverter() != null) {
        createConfigured(Converter.ENUM_NAME, column.getConverter().getEnumConverter());
      }
      String namedConverter = column.getConverter().getNamedConverter();
      if (namedConverter != null) {
        List<MethodInvokeInfo> methodInvokeInfos = parseMethod(namedConverter);
        methodInvokeInfos.forEach(mi -> createConfigured(mi.methodName, mi.args));
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

  private List<MethodInvokeInfo> parseMethod(String methodsText) {
    Matcher matcher = METHOD_PATTERN.matcher(methodsText);
    if (!matcher.matches()) {
      logger.warn("数据文件导出转换器配置 [{}] 有错误，无法保证正确执行转换", methodsText);
    }

    List<MethodInvokeInfo> result = Lists.newArrayList();
    while (matcher.find()) {
      // 方法名
      String methodName = matcher.group(1);
      // 参数原始字符串
      String argsText = matcher.group(2);

      List<String> argsList = parseArgs(argsText);

      result.add(new MethodInvokeInfo(methodName, argsList));
    }

    return result;
  }

  private List<String> parseArgs(String argsText) {
    if (argsText == null || argsText.isBlank()) {
      return Collections.emptyList();
    }

    List<String> result = new ArrayList<>();
    Matcher argMatcher = ARG_PATTERN.matcher(argsText);

    while (argMatcher.find()) {
      String strValue = argMatcher.group(1);
      result.add(Objects.requireNonNullElseGet(strValue, () -> argMatcher.group(2).trim()));
    }

    return result;
  }

  private record MethodInvokeInfo(String methodName, List<String> args) {
  }

  public static void build(DataDefinition definition) {
    String domainName = definition.getDomainName();
    definition.getColumns().forEach((columnName, column) -> {
      String key = cacheKey(domainName, columnName);
      CHAIN_CACHE.put(key, new ChainableConverterExecutor(column));
    });
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
