package cn.labzen.file.converter.executor;

import cn.labzen.file.converter.Converter;
import cn.labzen.file.converter.ExportableConverter;
import cn.labzen.file.converter.util.NamedConverterParser;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.Column;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 导出转换器链执行器
 * <p>
 * 按 priority 顺序链式执行 ExportableConverter，前一个输出作为后一个输入。
 * 供导出管线使用。
 *
 * @author labzen
 */
@Slf4j
public class ChainableExportConverterExecutor extends ChainableExecutor<ExportableConverter<?>> {

  /**
   * 所有针对列的导出转换器链执行器，key: domain名+@@+列名称，value: 转换器链执行器
   */
  private static final Map<String, ChainableExportConverterExecutor> EXPORT_CHAIN_CACHE = Maps.newHashMap();

  public static synchronized void build(DataDefinition definition) {

    definition.getColumns().forEach((columnName, column) -> {
      String key = cacheKey(definition, columnName);
      EXPORT_CHAIN_CACHE.put(key, new ChainableExportConverterExecutor(column));
    });

  }

  public static void clear() {
    EXPORT_CHAIN_CACHE.clear();
  }

  ChainableExportConverterExecutor(Column column) {
    configExportConverter(column);
    sortConverter();
  }

  private void configExportConverter(Column column) {
    // exporting 方向的配置
    if (column.getExporting() != null) {
      // mapping：exporting 专属
      if (column.getExporting().getMapping() != null) {
        createConfigured(Converter.MAPPING_NAME, column.getExporting().getMapping());
      }
      // enumerable：exporting 专属
      if (column.getExporting().getEnumerable() != null) {
        createConfigured(Converter.ENUM_NAME, column.getExporting().getEnumerable());
      }
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
      // exporting.converter (方向专属)
      if (column.getExporting().getConverter() != null) {
        List<NamedConverterParser.MethodInvokeInfo> methodInvokeInfos =
          NamedConverterParser.parseMethod(column.getExporting().getConverter());
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

  public Object execute(Object input) {
    Object latestValue = input;
    for (ConfiguredConverter<ExportableConverter<?>> cc : converters) {
      ExportableConverter<?> converter = cc.instance().converter();
      Class<?> sourceType = latestValue != null ? latestValue.getClass() : Object.class;
      if (converter.supportsExport(sourceType)) {
        latestValue = converter.convertForExport(latestValue, cc.arguments());
      }
    }
    return latestValue;
  }

  public static ChainableExportConverterExecutor get(DataDefinition definition, String columnName) {
    return EXPORT_CHAIN_CACHE.get(cacheKey(definition, columnName));
  }
}
