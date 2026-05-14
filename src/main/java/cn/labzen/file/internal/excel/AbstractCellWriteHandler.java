package cn.labzen.file.internal.excel;

import cn.labzen.file.definition.bean.Column;
import cn.labzen.file.definition.bean.Schema;
import com.alibaba.excel.write.handler.CellWriteHandler;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;

@RequiredArgsConstructor
public abstract class AbstractCellWriteHandler implements CellWriteHandler {

  protected final Schema schema;

  protected Column findColumn(Cell cell) {
    int columnIndex = cell.getColumnIndex();

    Column currentColumn = null;
    for (Column column : schema.getColumns()) {
      if (column.getIndex() == columnIndex) {
        currentColumn = column;
        break;
      }
    }

    return currentColumn;
  }
}
