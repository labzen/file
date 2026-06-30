package cn.labzen.file.format.core.reader.process;

/**
 * 带位置信息的导入数据行。
 * <p>
 * {@link #sequence} 记录该行数据在原始文件中的序号（如 Excel 行号），
 * {@link #payload} 为解析后的领域 Bean。失败行 payload 为 null。
 * <p>
 * 通过 {@link ImportResult} 向下游传递，使入库等后续阶段能精确报告错误所在行号。
 *
 * @param <T>      领域 Bean 类型
 * @param sequence 原始文件行号（如 Excel 中 # 列的值）
 * @param payload  解析后的领域 Bean 实例（失败行为 null）
 */
public record PositionedData<T>(String sequence, T payload) {
}
