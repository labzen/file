package cn.labzen.file.definition.bean.column.constraint;

import cn.labzen.file.definition.bean.column.Importing;
import cn.labzen.file.util.DateTimeFormat;
import cn.labzen.tool.util.Strings;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public record DateRange(String min, String max, String pattern) {

  public static DateRange get(Importing importing, String pattern) {
    if (pattern == null) {
      return null;
    }

    String min = importing.getMin();
    String max = importing.getMax();

    if (Strings.isAllBlank(min, max)) {
      return null;
    }

    boolean minIsDate = min != null && isDate(min, pattern);
    boolean maxIsDate = max != null && isDate(max, pattern);

    // 如果两个都有值，必须同时符合日期格式才封装
    if (minIsDate && maxIsDate) {
      return new DateRange(min, max, pattern);
    }

    // 只有一个有值且另一个为空时，该值需符合日期格式才封装
    if (minIsDate && Strings.isBlank(max)) {
      return new DateRange(min, null, pattern);
    }
    if (maxIsDate && Strings.isBlank(min)) {
      return new DateRange(null, max, pattern);
    }

    return null;
  }

  private static boolean isDate(String value, String pattern) {
    try {
      DateTimeFormatter formatter = DateTimeFormat.get(pattern);
      formatter.parse(value);
      return true;
    } catch (DateTimeParseException | IllegalArgumentException e) {
      return false;
    }
  }
}
