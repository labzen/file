package cn.labzen.file.converter;

import cn.labzen.file.converter.util.ConvertResultCache;
import cn.labzen.file.exception.DataConvertException;

import java.util.List;

/**
 * 导入转换器接口 — 用户输入文本 → Bean字段值
 * <p>
 * 在导入管线中按优先级链式执行，将用户输入的字符串转换为Bean字段所需的类型。
 * 转换失败等同于校验失败（如反向映射找不到key、枚举匹配不到等）。
 *
 * @param <I> 转换后将导入的类型
 * @author labzen
 */
public interface ImportableConverter extends Converter {

  /**
   * 执行导入转换
   *
   * @param input     用户输入的字符串值
   * @param arguments 转换参数（来自YAML配置）
   * @return 转换后的值
   * @throws DataConvertException 如果转换失败
   */
  default Object convertForImport(Object input, List<Object> arguments, Class<?> targetType) throws DataConvertException {
    return ConvertResultCache.computeImportValueIfAbsent(input, arguments, () -> doConvertForImport(input, arguments, targetType));
  }

  /**
   * 判断是否能将值转换为此目标Java类型
   *
   * @param targetType 目标Java类型
   * @return 是否支持
   */
  boolean supportsImport(Class<?> targetType);

  Object doConvertForImport(Object input, List<Object> arguments, Class<?> targetType) throws DataConvertException;
}
