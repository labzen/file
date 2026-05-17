package cn.labzen.file.definition.bean.table;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public record HeaderStructure(boolean isSingleHeader, List<HeaderCell> firstRow, List<HeaderCell> secondRow) {

  public List<String> getLeafLevelHeaders() {
    if (isSingleHeader) {
      return firstRow.stream().map(HeaderCell::text).toList();
    }

    Map<Integer, String> ordered = new TreeMap<>();
    // 第一行中 rowspan=2 的属于叶子节点
    for (HeaderCell cell : firstRow) {
      if (cell.rowSpan() > 1) {
        ordered.put(cell.index(), cell.text());
      }
    }

    // 第二行全部是叶子节点
    for (HeaderCell cell : secondRow) {
      ordered.put(cell.index(), cell.text());
    }

    return List.copyOf(ordered.values());
  }
}
