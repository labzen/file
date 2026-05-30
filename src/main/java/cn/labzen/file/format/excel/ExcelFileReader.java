package cn.labzen.file.format.excel;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.format.core.reader.AbstractDataFileReader;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.io.InputStream;
import java.util.*;

/**
 * Excel 文件读取器
 * <p>
 * 使用 EasyExcel（Fesod）流式读取Excel文件。
 * 通过 # 列标记识别行类型（HEADER/MOCK/数据行），
 * 通过 Row 1 的代码标识行匹配列字段。
 *
 * @author labzen
 */
public class ExcelFileReader extends AbstractDataFileReader {

  private static final String MARKER_COLUMN_HEADER = "HEADER";
  private static final String MARKER_COLUMN_MOCK = "MOCK";

  @Override
  public FileFormat format() {
    return FileFormat.EXCEL;
  }

  @Override
  protected Iterator<Map<String, String>> doRead(InputStream inputStream, DataDefinition definition) {
    List<Map<String, String>> dataRows = new ArrayList<>();
    Map<Integer, String> columnMapping = new LinkedHashMap<>();
    boolean headerFound = false;

    // 使用 EasyExcel 读取
    EasyExcel.read(inputStream, new AnalysisEventListener<Map<Integer, String>>() {
      private boolean headerParsed = false;

      @Override
      public void invoke(Map<Integer, String> data, AnalysisContext context) {
        // 检查 # 列（第0列）的值来判断行类型
        String marker = data.get(0);

        if (!headerParsed) {
          // 寻找第一个 HEADER 行作为代码标识行
          if (MARKER_COLUMN_HEADER.equals(marker)) {
            headerParsed = true;
            // 解析列映射：列索引 → 字段名
            for (Map.Entry<Integer, String> entry : data.entrySet()) {
              if (entry.getKey() == 0) continue; // 跳过 # 列
              if (entry.getValue() != null && !entry.getValue().isBlank()) {
                columnMapping.put(entry.getKey(), entry.getValue().trim());
              }
            }
          }
          return;
        }

        // 已解析过表头，跳过 HEADER 和 MOCK 行
        if (MARKER_COLUMN_HEADER.equals(marker) || MARKER_COLUMN_MOCK.equals(marker)) {
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
