package cn.labzen.file.format.core.reader.process;

import java.util.Map;

public record ProcessContext(String fieldName, String headerText, Map<String, Object> rawRowData, Object currentValue) {
}
