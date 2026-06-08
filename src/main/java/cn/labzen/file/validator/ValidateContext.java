package cn.labzen.file.validator;

import cn.labzen.file.format.core.reader.process.ProceedRow;

import java.util.List;
import java.util.Map;

/**
 * 校验上下文
 * <p>
 * 提供校验器执行时所需的环境信息
 *
 * @author labzen
 */
public record ValidateContext<T>(String fieldName, Object value, Map<String, String> rawRowData,
                                 List<ProceedRow<T>> proceedRows) {

}
