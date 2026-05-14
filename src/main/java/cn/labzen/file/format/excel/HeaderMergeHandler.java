package cn.labzen.file.format.excel;

import cn.labzen.file.definition.bean.column.TableColumn;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.*;

/**
 * Excel 多级表头合并处理器
 * <p>
 * 合并规则：
 * <ul>
 *   <li>具有相同一级标题的单元格横向合并</li>
 *   <li>只有一级表头的列纵向合并（跨所有行）</li>
 * </ul>
 */
public final class HeaderMergeHandler {

  private final Map<String, TableColumn> columns;
  private final List<Integer> headerLevel;
  private final int maxLevel;

  public HeaderMergeHandler(Map<String, TableColumn> columns, List<Integer> headerLevel) {
    this.columns = columns;
    this.headerLevel = headerLevel;
    this.maxLevel = headerLevel.stream().mapToInt(Integer::intValue).max().orElse(1);
  }

  /**
   * 应用表头单元格合并
   */
  public void applyMerges(Sheet sheet) {
    if (maxLevel <= 0) {
      return;
    }

    // 收集每个列的一级标题
    List<String> firstLevelHeaders = getFirstLevelHeaders();

    // 第一步：横向合并具有相同一级标题的单元格
    mergeHorizontal(sheet, firstLevelHeaders);

    // 第二步：纵向合并只有一级表头的列（跨所有表头行）
    mergeVertical(sheet, firstLevelHeaders);
  }

  /**
   * 获取每个列的一级标题
   */
  private List<String> getFirstLevelHeaders() {
    List<String> firstLevelHeaders = new ArrayList<>();
    int colIdx = 0;
    for (TableColumn column : columns.values()) {
      List<String> header = column.getHeader();
      if (header != null && !header.isEmpty()) {
        firstLevelHeaders.add(header.get(0));
      } else {
        firstLevelHeaders.add("");
      }
      colIdx++;
    }
    return firstLevelHeaders;
  }

  /**
   * 横向合并：具有相同一级标题的单元格在第一行合并
   */
  private void mergeHorizontal(Sheet sheet, List<String> firstLevelHeaders) {
    int colIdx = 0;
    while (colIdx < firstLevelHeaders.size()) {
      String currentHeader = firstLevelHeaders.get(colIdx);
      int startIdx = colIdx;

      // 找到具有相同一级标题的连续列
      while (colIdx < firstLevelHeaders.size() && Objects.equals(firstLevelHeaders.get(colIdx), currentHeader)) {
        colIdx++;
      }

      // 如果跨越了多列，需要横向合并（只在第一行）
      if (colIdx - startIdx > 1) {
        int rowFrom = 0;
        int rowTo = 0;  // 只合并第一行
        int colFrom = startIdx;
        int colTo = colIdx - 1;
        sheet.addMergedRegion(new CellRangeAddress(rowFrom, rowTo, colFrom, colTo));
      }
    }
  }

  /**
   * 纵向合并：只有一级表头的列，需要纵向合并所有表头行
   */
  private void mergeVertical(Sheet sheet, List<String> firstLevelHeaders) {
    if (maxLevel <= 1) {
      return;  // 没有多级表头，不需要纵向合并
    }

    int rowFrom = 0;
    int rowTo = maxLevel - 1;  // 跨所有表头行

    for (int colIdx = 0; colIdx < firstLevelHeaders.size(); colIdx++) {
      // 如果只有一个一级标题（即没有多级表头），需要纵向合并
      if (headerLevel.get(colIdx) == 1) {
        sheet.addMergedRegion(new CellRangeAddress(rowFrom, rowTo, colIdx, colIdx));
      }
    }
  }
}