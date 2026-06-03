package cn.labzen.file.definition.bean.column.constraint;

import cn.labzen.file.definition.bean.column.Importing;
import cn.labzen.tool.util.Strings;

public record NumericRange(String min, String max) {

  public static NumericRange get(Importing importing) {
    String min = importing.getMin();
    String max = importing.getMax();

    if (Strings.isAllBlank(min, max)) {
      return null;
    }

    boolean minIsNumber = min != null && isNumeric(min);
    boolean maxIsNumber = max != null && isNumeric(max);

    // 如果两个都有值，必须同时为数字才封装
    if (minIsNumber && maxIsNumber) {
      return new NumericRange(min, max);
    }

    // 只有一个有值且另一个为空时，该值需为数字才封装
    if (minIsNumber && Strings.isBlank(max)) {
      return new NumericRange(min, null);
    }
    if (maxIsNumber && Strings.isBlank(min)) {
      return new NumericRange(null, max);
    }

    return null;
  }

  private static boolean isNumeric(String value) {
    return value.matches("-?\\d+(\\.\\d+)?");
  }
}
