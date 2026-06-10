package cn.labzen.file.format.excel;

import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.format.core.reader.AbstractDataFileReader;
import cn.labzen.file.format.excel.read.ExcelEventListener;
import cn.labzen.file.meta.FileConfiguration;
import org.apache.fesod.sheet.FesodSheet;
import org.jspecify.annotations.NonNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Excel 文件读取器
 * <p>
 * 使用 EasyExcel（Fesod）流式读取Excel文件。
 * 通过 # 列标记识别行类型，支持以下标记：
 * <ul>
 *   <li>CODE — 代码标识行（字段名），由模板生成器产生</li>
 *   <li>HINT — 人类阅读行（i18n表头文本），跳过</li>
 *   <li>MOCK — 示例数据行，跳过</li>
 *   <li>数字/空 — 用户数据行，读取</li>
 * </ul>
 *
 * @author labzen
 */
public class ExcelFileReader extends AbstractDataFileReader {

  @Override
  public FileFormat format() {
    return FileFormat.EXCEL;
  }

  @Override
  public void initialize(@NonNull FileConfiguration configuration) {
    // do nothing
  }

  @Override
  protected List<Map<String, String>> importContent(@NonNull InputStream inputStream) {
    List<Map<String, String>> dataRows = new ArrayList<>();
    ExcelEventListener listener = new ExcelEventListener(dataRows);
    FesodSheet.read(inputStream, listener).sheet().headRowNumber(0).doReadSync();
    return dataRows;
  }
}
