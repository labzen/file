package cn.labzen.file.converter.executor;

import cn.labzen.file.annotation.DataConverter;
import cn.labzen.file.converter.Converter;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.ServiceLoader;

@Slf4j
public final class ConverterInstanceSupplier {

  /**
   * 所有转换器实例，key: 转换器名称，value: 转换器实例
   */
  private static final Map<String, ConverterInstance<Converter>> CONVERTER_INSTANCES = Maps.newHashMap();

  public static synchronized void init() {
    CONVERTER_INSTANCES.clear();

    ServiceLoader.load(Converter.class).forEach(converter -> {
      Class<?> type = converter.getClass();
      if (!type.isAnnotationPresent(DataConverter.class)) {
        return;
      }
      DataConverter annotation = type.getAnnotation(DataConverter.class);
      int priority = annotation.priority();

      ConverterInstance<Converter> instance = new ConverterInstance<>(converter, priority);

      CONVERTER_INSTANCES.put(annotation.name(), instance);
    });
  }

  static <T extends Converter> ConverterInstance<T> get(String name) {
    //noinspection unchecked
    ConverterInstance<T> instance = (ConverterInstance<T>) CONVERTER_INSTANCES.get(name);
    if (instance == null) {
      logger.error("不存在的导入转换器: {}", name);
    }
    return instance;
  }
}
