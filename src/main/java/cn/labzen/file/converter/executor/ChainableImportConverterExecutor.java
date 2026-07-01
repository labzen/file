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

  /**
   * 所有针对列的导入转换器链执行器，key: domain名+@@+列名称，value: 转换器链执行器
   */
  private static final Map<String, ChainableImportConverterExecutor> IMPORT_CHAIN_CACHE = Maps.newHashMap();

  public static synchronized void build(DataDefinition definition) {
    definition.getColumns().forEach((columnName, column) -> {
      String key = cacheKey(definition, columnName);
      IMPORT_CHAIN_CACHE.put(key, new ChainableImportConverterExecutor(column));
    });
  }

  public static void clear() {
    IMPORT_CHAIN_CACHE.clear();
  }

  ChainableImportConverterExecutor(Column column) {
    configImportConverter(column);
    sortByImportPriority();
  }

  private void configImportConverter(Column column) {
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

  public static ChainableImportConverterExecutor get(DataDefinition definition, String columnName) {
    return IMPORT_CHAIN_CACHE.get(cacheKey(definition, columnName));
  }
}
