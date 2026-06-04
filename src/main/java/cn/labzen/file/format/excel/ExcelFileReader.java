package cn.labzen.file.format.excel;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.format.core.reader.AbstractDataFileReader;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.context.AnalysisContext;
import org.apache.fesod.sheet.event.AnalysisEventListener;

import java.io.InputStream;
import java.util.*;

/**
 * Excel 文件读取器
 * <p>
 * 使用 EasyExcel（Fesod）流式读取Excel文件。
 * 通过 # 列标记识别行类型，支持以下标记：
 * <ul>
 *   <li>CODE — 代码标识行（字段名），由模板生成器产生</li>
 *   <li>HEADER — 旧版代码标识行（向后兼容）</li>
 *   <li>HINT — 人类阅读行（i18n表头文本），跳过</li>
 *   <li>MOCK — 示例数据行，跳过</li>
 *   <li>数字/空 — 用户数据行，读取</li>
 * </ul>
 *
 * @author labzen
 */
public class ExcelFileReader extends AbstractDataFileReader {

  private static final String MARKER_COLUMN_CODE = "CODE";
  //  private static final String MARKER_COLUMN_HEADER = "HEADER";
  private static final String MARKER_COLUMN_HINT = "HINT";
  private static final String MARKER_COLUMN_MOCK = "MOCK";

  @Override
  public FileFormat format() {
    return FileFormat.EXCEL;
  }

  @Override
  protected Iterator<Map<String, String>> doRead(InputStream inputStream, DataDefinition definition) {
    List<Map<String, String>> dataRows = new ArrayList<>();
    Map<Integer, String> columnMapping = new LinkedHashMap<>();

    FesodSheet.read(inputStream, new AnalysisEventListener<Map<Integer, String>>() {
      private boolean headerParsed = false;

      @Override
      public void invoke(Map<Integer, String> data, AnalysisContext context) {
        String marker = data.get(0);
        if (marker != null) {
          marker = marker.trim();
        }

        if (!headerParsed) {
          // 优先识别 CODE 行，其次兼容 HEADER 行
          if (MARKER_COLUMN_CODE.equals(marker)) {
            headerParsed = true;
            for (Map.Entry<Integer, String> entry : data.entrySet()) {
              if (entry.getKey() == 0) continue; // 跳过 # 列
              if (entry.getValue() != null && !entry.getValue().isBlank()) {
                columnMapping.put(entry.getKey(), entry.getValue().trim());
              }
            }
          }
          return;
        }

        // 已解析过表头，跳过标记行（CODE/HEADER/HINT/MOCK）
        if (MARKER_COLUMN_CODE.equals(marker) || MARKER_COLUMN_HINT.equals(marker) || MARKER_COLUMN_MOCK.equals(marker)) {
          return;
        }

        // 数据行：转换为 字段名→字符串值 的映射
        Map<String, String> rowData = new LinkedHashMap<>();
        for (Map.Entry<Integer, String> entry : data.entrySet()) {
          String fieldName = columnMapping.get(entry.getKey());
          if (fieldName != null) {
            rowData.put(fieldName, entry.getValue() != null ? entry.getValue().trim() : null);
          }
        }

        if (!rowData.isEmpty()) {
          dataRows.add(rowData);
        }
      }

      @Override
      public void doAfterAllAnalysed(AnalysisContext context) {
        // 读取完成
      }
    }).sheet().doRead();

    return dataRows.iterator();
  }
}
