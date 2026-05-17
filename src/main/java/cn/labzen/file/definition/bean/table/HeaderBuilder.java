package cn.labzen.file.definition.bean.table;

import cn.labzen.file.definition.bean.column.TableColumn;
import cn.labzen.tool.util.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;

import static cn.labzen.file.definition.bean.column.TableColumn.HEADER_LEVEL_SEPARATOR;

public class HeaderBuilder {

  private static final int SUPPORT_MAX_HEADER_LEVEL = 2;

  public static HeaderStructure build(List<TableColumn> columns) {
    int size = columns.size();
    int index = 0;

    // 判断当前是否配置的是单级表头（所有的header集合中都是一个表头）
    boolean isSingleHeader = columns.stream().allMatch(column -> Strings.times(column.getHeader(), HEADER_LEVEL_SEPARATOR) == 0);
    // 默认的合并行数
    int defaultRowSpanWhenSingle = isSingleHeader ? 1 : SUPPORT_MAX_HEADER_LEVEL;

    List<HeaderCell> firstRowHeaderCells = Lists.newArrayList();
    List<HeaderCell> secondRowHeaderCells = Lists.newArrayList();

    while (index < size) {
      TableColumn current = columns.get(index);
      String[] headers = current.getHeader().split(HEADER_LEVEL_SEPARATOR);

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
        TableColumn next = columns.get(index);
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
        TableColumn column = columns.get(i);
        String[] columnHeaders = column.getHeader().split(HEADER_LEVEL_SEPARATOR);
        // 通过前面的逻辑，这里能保证columnHeaders的长度是2
        secondRowHeaderCells.add(new HeaderCell(columnHeaders[1], i, 1, 1));
      }
    }

    return new HeaderStructure(isSingleHeader, ImmutableList.copyOf(firstRowHeaderCells), ImmutableList.copyOf(secondRowHeaderCells));
  }

  public static void main(String[] args) {
    List<TableColumn> columns = List.of(

      new TableColumn("姓名", 1, null, null),
      new TableColumn("性别", 1, null, null),
      new TableColumn("成绩:-:数学", 2, null, null),
      new TableColumn("成绩:-:英语", 3, null, null),
      new TableColumn("成绩:-:物理", 4, null, null),
      new TableColumn("年龄", 5, null, null),
      new TableColumn("Other:-:班级", 6, null, null),
      new TableColumn("Other:-:地址", 7, null, null)
    );

    HeaderStructure build = build(columns);
    System.out.println(build);

    build.getLeafLevelHeaders().forEach(System.out::println);
  }
}
