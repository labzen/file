package cn.labzen.file.converter.exportable;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.Converter;
import cn.labzen.file.converter.util.NamedConverterParser;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.Column;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 导出转换器链执行器
 * <p>
 * 按 priority 顺序链式执行 ExportableConverter，前一个输出作为后一个输入。
 * 供导出管线使用。
 *
 * @author labzen
 */
@Slf4j
public class ChainableExportConverterExecutor {

  /**
   * 所有导出转换器实例，key: 转换器名称，value: 转换器实例
   */
  private static final Map<String, ExportableConverter<?>> EXPORT_CONVERTER_INSTANCES = Maps.newHashMap();
  /**
   * 所有针对列的导出转换器链执行器，key: domain名+@@+列名称，value: 转换器链执行器
   */
  private static final Map<String, ChainableExportConverterExecutor> EXPORT_CHAIN_CACHE = Maps.newHashMap();

  private static void loadConverterInstances() {
    // 通过 SPI 机制加载所有 Converter 实现
    ServiceLoader.load(Converter.class).forEach(converter -> {
      Class<?> type = converter.getClass();
      if (!type.isAnnotationPresent(DataConverter.class)) {
        return;
      }
      DataConverter annotation = type.getAnnotation(DataConverter.class);
      if (converter instanceof ExportableConverter<?> ec) {
        EXPORT_CONVERTER_INSTANCES.put(annotation.name(), ec);
      }
    });
  }

  private static void clearConverterInstances() {
    EXPORT_CONVERTER_INSTANCES.clear();
  }

  public static synchronized void build(DataDefinition definition) {
    loadConverterInstances();

    String domainName = definition.getDomainName();
    definition.getColumns().forEach((columnName, column) -> {
      String key = cacheKey(domainName, columnName);
      EXPORT_CHAIN_CACHE.put(key, new ChainableExportConverterExecutor(column));
    });

    clearConverterInstances();
  }

  private static String cacheKey(String domainName, String columnName) {
    return domainName + "@@" + columnName;
  }

  public static void clear() {
    EXPORT_CHAIN_CACHE.clear();
  }

  private final List<ConfiguredExportConverter> converters = Lists.newArrayList();

  ChainableExportConverterExecutor(Column column) {
    configExportConverter(column);
    sortConverter();
  }

  private void sortConverter() {
    converters.sort(Comparator.comparingInt(ConfiguredExportConverter::priority));
  }

  private void createConfigured(String converterName, Object... args) {
    ExportableConverter<?> converter = EXPORT_CONVERTER_INSTANCES.get(converterName);
    if (converter == null) {
      logger.error("不存在的导出转换器: {}", converterName);
      return;
    }

    DataConverter annotation = converter.getClass().getAnnotation(DataConverter.class);
    ConfiguredExportConverter cec;
    if (args.length == 1 && args[0] instanceof List<?> list) {
      //noinspection unchecked
      cec = new ConfiguredExportConverter(annotation.priority(), converter, (List<Object>) list);
    } else {
      cec = new ConfiguredExportConverter(annotation.priority(), converter, Arrays.stream(args).toList());
    }
    converters.add(cec);
  }

  private void configExportConverter(Column column) {
    // exporting 方向的配置
    if (column.getExporting() != null) {
      if (column.getExporting().getWhenNull() != null) {
        createConfigured(Converter.WHEN_NULL_NAME, column.getExporting().getWhenNull());
      }
      if (column.getExporting().getWhenBlank() != null) {
        createConfigured(Converter.WHEN_EMPTY_NAME, column.getExporting().getWhenBlank());
      }
      if (column.getExporting().getPrefix() != null) {
        createConfigured(Converter.PREFIX_NAME, column.getExporting().getPrefix());
      }
      if (column.getExporting().getSuffix() != null) {
        createConfigured(Converter.SUFFIX_NAME, column.getExporting().getSuffix());
      }
    }

    // pattern (共享)
    if (column.getPattern() != null) {
      if (column.getPattern().getDate() != null) {
        createConfigured(Converter.DATE_NAME, column.getPattern().getDate());
      }
      if (column.getPattern().getNumber() != null) {
        createConfigured(Converter.NUMBER_NAME, column.getPattern().getNumber());
      }
    }

    // mapping：exporting 专属 > 共享
    Map<String, String> mapping = resolveExportMapping(column);
    if (mapping != null) {
      createConfigured(Converter.MAPPING_NAME, mapping);
    }

    // enumerable：exporting 专属 > 共享
    String enumerable = resolveExportEnumerable(column);
    if (enumerable != null) {
      createConfigured(Converter.ENUM_NAME, enumerable);
    }

    // exporting.converter (方向专属)
    if (column.getExporting() != null && column.getExporting().getConverter() != null) {
      List<NamedConverterParser.MethodInvokeInfo> methodInvokeInfos =
        NamedConverterParser.parseMethod(column.getExporting().getConverter());
      methodInvokeInfos.forEach(mi -> createConfigured(mi.methodName(), mi.args()));
    }
  }

  private Map<String, String> resolveExportMapping(Column column) {
    if (column.getExporting() != null && column.getExporting().getMapping() != null) {
      return column.getExporting().getMapping();
    }
    return column.getMapping();
  }

  private String resolveExportEnumerable(Column column) {
    if (column.getExporting() != null && column.getExporting().getEnumerable() != null) {
      return column.getExporting().getEnumerable();
    }
    return column.getEnumerable();
  }

  public Object execute(Object input) {
    Object latestValue = input;
    for (ConfiguredExportConverter cec : converters) {
      ExportableConverter<?> converter = cec.converter();
      Class<?> sourceType = latestValue != null ? latestValue.getClass() : Object.class;
      if (converter.supportsExport(sourceType)) {
        latestValue = converter.convertForExport(latestValue, cec.arguments());
      }
    }
    return latestValue;
  }

  /**
   * todo ???:???
   */
  public static Map<String, ChainableExportConverterExecutor> buildFor(DataDefinition definition) {
    Map<String, ChainableExportConverterExecutor> executors = new HashMap<>();
    definition.getColumns().forEach((columnName, column) ->
      executors.put(columnName, new ChainableExportConverterExecutor(column))
    );
    return executors;
  }

  public static ChainableExportConverterExecutor get(String domainName, String columnName) {
    return EXPORT_CHAIN_CACHE.get(cacheKey(domainName, columnName));
  }

//  public static Set<String> availableConverterNames() {
//    return EXPORT_CONVERTER_INSTANCES.keySet();
//  }

  record ConfiguredExportConverter(int priority, ExportableConverter<?> converter, List<Object> arguments) {
  }
}
