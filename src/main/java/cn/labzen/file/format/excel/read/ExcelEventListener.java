package cn.labzen.file.format.excel.read;

import cn.labzen.tool.util.Strings;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.apache.fesod.sheet.context.AnalysisContext;
import org.apache.fesod.sheet.event.AnalysisEventListener;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.labzen.file.format.core.reader.DataFileReader.SEQUENCE_KEY;

@RequiredArgsConstructor
public class ExcelEventListener extends AnalysisEventListener<Map<Integer, Object>> {

  private static final String MARKER_COLUMN_CODE = "CODE";
  private static final String MARKER_COLUMN_HINT = "HINT";
  private static final String MARKER_COLUMN_MOCK = "MOCK";

  private volatile boolean headerParsed = false;
  private volatile boolean firstHintParsed = false;
  private final Map<Integer, String> columnMapping = Maps.newHashMap();

  private final List<Map<String, String>> rowsData;

  @Override
  public void invoke(Map<Integer, Object> data, AnalysisContext analysisContext) {
    String marker = Strings.value(data.get(0), "");

    if (!headerParsed) {
      // 优先识别 CODE 行，其次兼容 HEADER 行
      if (MARKER_COLUMN_CODE.equals(marker)) {
        headerParsed = true;
        for (Map.Entry<Integer, Object> entry : data.entrySet()) {
          Integer columnIndex = entry.getKey();
          if (columnIndex == 0) continue; // 跳过 # 列

          String code = Strings.value(data.get(columnIndex), "");
          if (Strings.isNotBlank(code)) {
            columnMapping.put(columnIndex, code.trim());
          }
        }
      }
      return;
    }

    // 已解析过表头，跳过标记行（CODE/HINT/MOCK）
    if (MARKER_COLUMN_CODE.equals(marker) || MARKER_COLUMN_MOCK.equals(marker)) {
      return;
    }
    // 兼容两级表头的第二行跳过
    if (!firstHintParsed && MARKER_COLUMN_HINT.equals(marker)) {
      firstHintParsed = true;
      return;
    } else if (firstHintParsed && Strings.isBlank(marker)) {
      return;
    }

    // 数据行：转换为 字段名→字符串值 的映射
    Map<String, String> rowData = new LinkedHashMap<>();
    for (Map.Entry<Integer, Object> entry : data.entrySet()) {
      Integer columnIndex = entry.getKey();

      if (columnIndex == 0) {
        // 记录序号
        rowData.put(SEQUENCE_KEY, Strings.value(entry.getValue(), ""));
        continue;
      }

      String fieldName = columnMapping.get(columnIndex);
      if (fieldName != null) {
        String value = Strings.value(entry.getValue(), null);
        rowData.put(fieldName, value);
      }
    }

    if (!rowData.isEmpty()) {
      rowsData.add(rowData);
    }
  }

  @Override
  public void doAfterAllAnalysed(AnalysisContext analysisContext) {
    // 读取完成，可以做一些后续处理
//    System.out.println("读取完成，共读取 " + rowsData.size() + " 行数据");
  }
}
