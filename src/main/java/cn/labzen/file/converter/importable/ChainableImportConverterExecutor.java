package cn.labzen.file.converter.importable;

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
 * 导入转换器链执行器
 * <p>
 * 按 priority 顺序链式执行 ImportableConverter，前一个输出作为后一个输入。
 * 供导入管线使用。
 *
 * @author labzen
 */
@Slf4j
public class ChainableImportConverterExecutor {

  /**
   * 所有导入转换器实例，key: 转换器名称，value: 转换器实例
   */
  private static final Map<String, ImportableConverter> IMPORT_CONVERTER_INSTANCES = Maps.newHashMap();
  /**
   * 所有针对列的导入转换器链执行器，key: domain名+@@+列名称，value: 转换器链执行器
   */
  private static final Map<String, ChainableImportConverterExecutor> IMPORT_CHAIN_CACHE = Maps.newHashMap();

  private static void loadConverterInstances() {
    // 通过 SPI 机制加载所有 Converter 实现
    ServiceLoader.load(Converter.class).forEach(converter -> {
      Class<?> type = converter.getClass();
      if (!type.isAnnotationPresent(DataConverter.class)) {
        return;
      }
      DataConverter annotation = type.getAnnotation(DataConverter.class);
      if (converter instanceof ImportableConverter ic) {
        IMPORT_CONVERTER_INSTANCES.put(annotation.name(), ic);
      }
    });
  }

  private static void clearConverterInstances() {
    IMPORT_CHAIN_CACHE.clear();
  }

  public static synchronized void build(DataDefinition definition) {
    loadConverterInstances();

    String domainName = definition.getDomainName();
    definition.getColumns().forEach((columnName, column) -> {
      String key = cacheKey(domainName, columnName);
      IMPORT_CHAIN_CACHE.put(key, new ChainableImportConverterExecutor(column));
    });

    clearConverterInstances();
  }

  private static String cacheKey(String domainName, String columnName) {
    return domainName + "@@" + columnName;
  }

  public static void clear() {
    IMPORT_CHAIN_CACHE.clear();
  }

  private final List<ConfiguredImportConverter> converters = Lists.newArrayList();

  ChainableImportConverterExecutor(Column column) {
    configImportConverter(column);
    sortConverter();
  }

  private void sortConverter() {
    converters.sort(Comparator.comparingInt(ConfiguredImportConverter::priority));
  }

  private void createConfigured(String converterName, Object... args) {
    ImportableConverter converter = IMPORT_CONVERTER_INSTANCES.get(converterName);
    if (converter == null) {
      logger.error("不存在的导入转换器: {}", converterName);
      return;
    }

    DataConverter annotation = converter.getClass().getAnnotation(DataConverter.class);
    ConfiguredImportConverter cc;
    if (args.length == 1 && args[0] instanceof List<?> list) {
      //noinspection unchecked
      cc = new ConfiguredImportConverter(annotation.priority(), converter, (List<Object>) list);
    } else {
      cc = new ConfiguredImportConverter(annotation.priority(), converter, Arrays.stream(args).toList());
    }
    converters.add(cc);
  }

  private void configImportConverter(Column column) {
    // mapping：importing 专属 > 共享 + reverseMapping
    Map<String, String> mapping = resolveImportMapping(column);
    if (mapping != null) {
      createConfigured(Converter.MAPPING_NAME, mapping);
    }

    // enumerable：importing 专属 > 共享 + reverseEnumerable
    String enumerable = resolveImportEnumerable(column);
    if (enumerable != null) {
      createConfigured(Converter.ENUM_NAME, enumerable);
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

    // importing.converter (方向专属)
    if (column.getImporting() != null && column.getImporting().getConverter() != null) {
      List<NamedConverterParser.MethodInvokeInfo> methodInvokeInfos =
        NamedConverterParser.parseMethod(column.getImporting().getConverter());
      methodInvokeInfos.forEach(mi -> createConfigured(mi.methodName(), mi.args()));
    }
  }

  private Map<String, String> resolveImportMapping(Column column) {
    if (column.getImporting() != null && column.getImporting().getMapping() != null) {
      return column.getImporting().getMapping();
    }
    return column.getMapping();
  }

  private String resolveImportEnumerable(Column column) {
    if (column.getImporting() != null && column.getImporting().getEnumerable() != null) {
      return column.getImporting().getEnumerable();
    }
    return column.getEnumerable();
  }

  public Object execute(Object input, Class<?> targetType) {
    Object latestValue = input;
    for (ConfiguredImportConverter cc : converters) {
      ImportableConverter converter = cc.converter();
      if (converter.supportsImport(targetType)) {
        latestValue = converter.convertForImport(latestValue, cc.arguments(), targetType);
      }
    }
    return latestValue;
  }

  /**
   * todo ???:???
   */
  public static Map<String, ChainableImportConverterExecutor> buildFor(DataDefinition definition) {
    Map<String, ChainableImportConverterExecutor> executors = new HashMap<>();
    definition.getColumns().forEach((columnName, column) ->
      executors.put(columnName, new ChainableImportConverterExecutor(column))
    );
    return executors;
  }

  public static ChainableImportConverterExecutor get(String domainName, String columnName) {
    return IMPORT_CHAIN_CACHE.get(cacheKey(domainName, columnName));
  }

//  public static Set<String> availableConverterNames() {
//    return IMPORT_CONVERTER_INSTANCES.keySet();
//  }

  record ConfiguredImportConverter(int priority, ImportableConverter converter, List<Object> arguments) {
  }
}
