package cn.labzen.file.converter.executor;

import cn.labzen.file.converter.Converter;
import cn.labzen.file.converter.ImportableConverter;
import cn.labzen.file.converter.util.NamedConverterParser;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.Column;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 导入转换器链执行器
 * <p>
 * 按 priority 顺序链式执行 ImportableConverter，前一个输出作为后一个输入。
 * 供导入管线使用。
 *
 * @author labzen
 */
@Slf4j
public class ChainableImportConverterExecutor extends ChainableExecutor<ImportableConverter> {

//  /**
//   * 所有导入转换器实例，key: 转换器名称，value: 转换器实例
//   */
//  private static final Map<String, ImportableConverter> IMPORT_CONVERTER_INSTANCES = Maps.newHashMap();
  /**
   * 所有针对列的导入转换器链执行器，key: domain名+@@+列名称，value: 转换器链执行器
   */
  private static final Map<String, ChainableImportConverterExecutor> IMPORT_CHAIN_CACHE = Maps.newHashMap();

//  private static void loadConverterInstances() {
//    // 通过 SPI 机制加载所有 Converter 实现
//    ServiceLoader.load(Converter.class).forEach(converter -> {
//      Class<?> type = converter.getClass();
//      if (!type.isAnnotationPresent(DataConverter.class)) {
//        return;
//      }
//      DataConverter annotation = type.getAnnotation(DataConverter.class);
//      if (converter instanceof ImportableConverter ic) {
//        IMPORT_CONVERTER_INSTANCES.put(annotation.name(), ic);
//      }
//    });
//  }

//  private static void clearConverterInstances() {
//    IMPORT_CHAIN_CACHE.clear();
//  }

  public static synchronized void build(DataDefinition definition) {
//    loadConverterInstances();

//    String domainName = definition.getDomainName();
    definition.getColumns().forEach((columnName, column) -> {
      String key = cacheKey(definition, columnName);
      IMPORT_CHAIN_CACHE.put(key, new ChainableImportConverterExecutor(column));
    });

//    clearConverterInstances();
  }

//  private static String cacheKey(String domainName, String columnName) {
//    return domainName + "@@" + columnName;
//  }

  public static void clear() {
    IMPORT_CHAIN_CACHE.clear();
  }

//  private final List<ConfiguredConverter<ExportableConverter<?>>> converters = Lists.newArrayList();

  ChainableImportConverterExecutor(Column column) {
    configImportConverter(column);
    sortConverter();
  }

//  private void sortConverter() {
//    converters.sort(Comparator.comparingInt(ConfiguredImportConverter::priority));
//  }

//  @SuppressWarnings("unchecked")
//  private void createConfigured(String converterName, Object... args) {
//    ConverterInstance<ImportableConverter> converterInstance = ConverterInstanceSupplier.get(converterName);
//    if (converterInstance == null) {
//      return;
//    }
//
////    DataConverter annotation = converter.getClass().getAnnotation(DataConverter.class);
////    ConfiguredImportConverter cc;
//    if (args.length == 1 && args[0] instanceof List<?> list) {
////      cc = new ConfiguredImportConverter(annotation.priority(), converter, (List<Object>) list);
//      converters.add(new ConfiguredConverter<>(converterInstance, (List<Object>) list));
//    } else {
////      cc = new ConfiguredImportConverter(annotation.priority(), converter, Arrays.stream(args).toList());
//      converters.add(new ConfiguredConverter<>(converterInstance, Arrays.stream(args).toList()));
//    }

  /// /    converters.add(cc);
//  }
  private void configImportConverter(Column column) {
    // mapping：importing 专属
//    Map<String, String> mapping = resolveImportMapping(column);
    if (column.getImporting() != null) {
      if (column.getImporting().getMapping() != null) {
        createConfigured(Converter.MAPPING_NAME, column.getImporting().getMapping());
      }
      // enumerable：importing 专属
      if (column.getImporting().getEnumerable() != null) {
        createConfigured(Converter.ENUM_NAME, column.getImporting().getEnumerable());
      }
      // importing.converter (方向专属)
      if (column.getImporting().getConverter() != null) {
        List<NamedConverterParser.MethodInvokeInfo> methodInvokeInfos =
          NamedConverterParser.parseMethod(column.getImporting().getConverter());
        methodInvokeInfos.forEach(mi -> createConfigured(mi.methodName(), mi.args()));
      }
    }

    // pattern (共享)
    if (column.getPatternDate() != null) {
      createConfigured(Converter.DATE_NAME, column.getPatternDate());
    }
    if (column.getPatternNumber() != null) {
      createConfigured(Converter.NUMBER_NAME, column.getPatternNumber());
    }
  }

//  private Map<String, String> resolveImportMapping(Column column) {
//    if (column.getImporting() != null && column.getImporting().getMapping() != null) {
//      return column.getImporting().getMapping();
//    }
//    return column.getMapping();
//  }

//  private String resolveImportEnumerable(Column column) {
//    if (column.getImporting() != null && column.getImporting().getEnumerable() != null) {
//      return column.getImporting().getEnumerable();
//    }
//    return column.getEnumerable();
//  }

  public Object execute(Object input, Class<?> targetType) {
    Object latestValue = input;
    for (ConfiguredConverter<ImportableConverter> cc : converters) {
      ImportableConverter converter = cc.instance().converter();
      if (converter.supportsImport(targetType)) {
        latestValue = converter.convertForImport(latestValue, cc.arguments(), targetType);
      }
    }
    return latestValue;
  }

//  /**
//   * todo ???:???
//   */
//  public static Map<String, ChainableImportConverterExecutor> buildFor(DataDefinition definition) {
//    Map<String, ChainableImportConverterExecutor> executors = new HashMap<>();
//    definition.getColumns().forEach((columnName, column) ->
//      executors.put(columnName, new ChainableImportConverterExecutor(column))
//    );
//    return executors;
//  }

  public static ChainableImportConverterExecutor get(DataDefinition definition, String columnName) {
    return IMPORT_CHAIN_CACHE.get(cacheKey(definition, columnName));
  }

//  public static Set<String> availableConverterNames() {
//    return IMPORT_CONVERTER_INSTANCES.keySet();
//  }

//  record ConfiguredImportConverter(int priority, ImportableConverter converter, List<Object> arguments) {
//  }
}
