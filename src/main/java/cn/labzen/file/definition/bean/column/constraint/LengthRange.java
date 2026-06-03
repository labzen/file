package cn.labzen.file.definition.bean.column.constraint;

import cn.labzen.file.definition.bean.column.Importing;
import cn.labzen.tool.util.Objects;

public record LengthRange(Integer min, Integer max) {

  public static LengthRange get(Importing importing) {
    if (Objects.isAllNull(importing.getMinLength(), importing.getMaxLength())) {
      return null;
    }

    return new LengthRange(importing.getMinLength(), importing.getMaxLength());
  }
}
