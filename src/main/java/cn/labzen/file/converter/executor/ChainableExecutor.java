package cn.labzen.file.converter.executor;

import cn.labzen.file.converter.Converter;
import cn.labzen.file.definition.bean.DataDefinition;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public abstract class ChainableExecutor<C extends Converter> {

  final List<ConfiguredConverter<C>> converters = Lists.newArrayList();

  protected static String cacheKey(DataDefinition definition, String columnName) {
    return definition.getName() + "#" + definition.getLocale() + "@@" + columnName;
  }

  /**
   * 按导出方向优先级排序
   */
  protected void sortByExportPriority() {
    converters.sort(Comparator.comparingInt(v -> v.instance().exportPriority()));
  }

  /**
   * 按导入方向优先级排序
   */
  protected void sortByImportPriority() {
    converters.sort(Comparator.comparingInt(v -> v.instance().importPriority()));
  }

  @SuppressWarnings("unchecked")
  protected void createConfigured(String converterName, Object... args) {
    ConverterInstance<C> converterInstance = ConverterInstanceSupplier.get(converterName);
    if (converterInstance == null) {
      return;
    }

    if (args.length == 1 && args[0] instanceof List<?> list) {
      converters.add(new ConfiguredConverter<>(converterInstance, (List<Object>) list));
    } else {
      converters.add(new ConfiguredConverter<>(converterInstance, Arrays.stream(args).toList()));
    }
  }
}
