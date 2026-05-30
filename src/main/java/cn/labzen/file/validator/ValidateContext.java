package cn.labzen.file.validator;

import java.util.Map;

/**
 * 校验上下文
 * <p>
 * 提供校验器执行时所需的环境信息
 *
 * @author labzen
 */
public record ValidateContext(int rowIndex, String fieldName, String headerText, Map<String, Object> currentRowData,
                              Map<String, String> rawRowData, String locale) {

}
