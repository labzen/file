package cn.labzen.file.definition.bean.table;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.Column;
import cn.labzen.tool.util.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HeaderBuilder {

  /**
   * 多级表头分隔符
   */
  private static final String HEADER_LEVEL_SEPARATOR = ":-:";
  private static final int SUPPORT_MAX_HEADER_LEVEL = 2;

  public static boolean isValidHeaderLevel(String headerString) {
    return Strings.times(headerString, HEADER_LEVEL_SEPARATOR) <= SUPPORT_MAX_HEADER_LEVEL;
  }

  public static HeaderStructure build(DataDefinition definition) {
    Map<String, Column> columnMap = definition.getColumns();
    List<Column> columns = List.copyOf(columnMap.values());
    int size = columns.size();
    int index = 0;

    // 判断当前是否配置的是单级表头（所有的header集合中都是一个表头）
    boolean isSingleHeader = columns.stream().allMatch(column -> Strings.times(column.getHeader(), HEADER_LEVEL_SEPARATOR) == 0);
    // 默认的合并行数
    int defaultRowSpanWhenSingle = isSingleHeader ? 1 : SUPPORT_MAX_HEADER_LEVEL;

//    return build(definition.getColumns());
//  }
//
//  public static HeaderStructure build(List<Column> columns) {
//    int size = columns.size();
//    int index = 0;
//
//    // 判断当前是否配置的是单级表头（所有的header集合中都是一个表头）
//    boolean isSingleHeader = columns.stream().allMatch(column -> Strings.times(column.getHeader(), HEADER_LEVEL_SEPARATOR) == 0);
//    // 默认的合并行数
//    int defaultRowSpanWhenSingle = isSingleHeader ? 1 : SUPPORT_MAX_HEADER_LEVEL;

    List<HeaderCell> firstRowHeaderCells = Lists.newArrayList();
    List<HeaderCell> secondRowHeaderCells = Lists.newArrayList();

    while (index < size) {
      Column current = columns.get(index);
      String[] headers = current.getHeader() == null ? new String[]{""} : current.getHeader().split(HEADER_LEVEL_SEPARATOR);
      // 如果当前是单级表头，或者header集合中只有一个表头，则直接添加到第一行表头中
      if (isSingleHeader || headers.length == 1) {
        firstRowHeaderCells.add(new HeaderCell(headers[0], index, 1, defaultRowSpanWhenSingle));
        index++;
        continue;
      }

      // 如果当前是多级表头，则需要将多级表头拆分为多个表头
      String firstTopLevelHeader = headers[0];
      int start = index;
      int colSpan = 0;
      while (index < size) {
        Column next = columns.get(index);
        String[] nextHeaders = next.getHeader().split(HEADER_LEVEL_SEPARATOR);
        // 如果当前列的header集合中只有一级表头，则跳出循环
        if (nextHeaders.length == 1) {
          break;
        }
        String nextTopLevelHeader = nextHeaders[0];
        // 如果当前列的header集合的第一级表头和前一列的header集合的第一级表头不一致，则跳出循环
        if (!Objects.equals(firstTopLevelHeader, nextTopLevelHeader)) {
          break;
        }

        colSpan++;
        index++;
      }

      firstRowHeaderCells.add(new HeaderCell(firstTopLevelHeader, start, colSpan, 1));

      // 添加第二行表头
      for (int i = start; i < start + colSpan; i++) {
        Column column = columns.get(i);
        String[] columnHeaders = column.getHeader().split(HEADER_LEVEL_SEPARATOR);
        // 通过前面的逻辑，这里能保证columnHeaders的长度是2
        secondRowHeaderCells.add(new HeaderCell(columnHeaders[1], i, 1, 1));
      }
    }

    return new HeaderStructure(isSingleHeader, ImmutableList.copyOf(firstRowHeaderCells), ImmutableList.copyOf(secondRowHeaderCells));
  }
}
