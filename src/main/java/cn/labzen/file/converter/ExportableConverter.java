package cn.labzen.file.converter;

import cn.labzen.file.converter.util.ConvertResultCache;
import cn.labzen.file.exception.DataConvertException;

import java.util.List;

/**
 * 导出转换器接口 — Bean字段值 → 展示文本
 * <p>
 * 在导出管线中按优先级链式执行，将Java字段的原始值转换为适合展示的字符串。
 *
 * @param <O> 转换后导出的类型
 * @author labzen
 */
public interface ExportableConverter<O> extends Converter {

  /**
   * 执行导出转换
   *
   * @param input     Bean字段的原始值
   * @param arguments 转换参数（来自YAML配置）
   * @return 转换后的值
   * @throws DataConvertException 如果转换失败
   */
  default O convertForExport(Object input, List<Object> arguments) throws DataConvertException {
    return ConvertResultCache.computeExportValueIfAbsent(input, arguments, () -> doConvertForExport(input, arguments));
  }

  /**
   * 判断是否能处理来自此Java类型的值
   *
   * @param sourceType 输入值的Java类型
   * @return 是否支持
   */
  boolean supportsExport(Class<?> sourceType);

  /**
   * 执行实际转换逻辑
   *
   * @param input     输入值
   * @param arguments 配置参数
   * @return 转换后的值
   * @throws DataConvertException 如果转换失败
   */
  O doConvertForExport(Object input, List<Object> arguments) throws DataConvertException;
}
