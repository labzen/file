package cn.labzen.file.converter.executor;

import cn.labzen.file.converter.Converter;

/**
 * 转换器实例 + 方向差异化优先级
 *
 * @param <T>             转换器类型
 * @param converter        转换器实例
 * @param exportPriority   导出方向优先级（数字越小越先执行）
 * @param importPriority   导入方向优先级（数字越小越先执行）
 */
record ConverterInstance<T extends Converter>(T converter, int exportPriority, int importPriority) {
}
