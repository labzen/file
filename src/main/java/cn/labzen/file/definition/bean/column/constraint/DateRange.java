package cn.labzen.file.definition.bean.column.constraint;

import cn.labzen.file.definition.bean.column.Importing;
import cn.labzen.file.util.DateTimeFormat;
import cn.labzen.tool.util.Strings;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public record DateRange(LocalDateTime min, LocalDateTime max, String pattern) {

  public static DateRange get(Importing importing, String pattern) {
    if (pattern == null) {
      return null;
    }

    String min = importing.getMin();
    String max = importing.getMax();

    if (Strings.isAllBlank(min, max)) {
      return null;
    }

    LocalDateTime minDateTime = parse(min, pattern);
    LocalDateTime maxDateTime = parse(max, pattern);

    // 如果两个都有值，必须同时符合日期格式才封装
    if (minDateTime != null && maxDateTime != null) {
      return new DateRange(minDateTime, maxDateTime, pattern);
    }

    // 只有一个有值且另一个为空时，该值需符合日期格式才封装
    if (minDateTime != null) {
      return new DateRange(minDateTime, null, pattern);
    }
    if (maxDateTime != null) {
      return new DateRange(null, maxDateTime, pattern);
    }

    return null;
  }

  /**
   * 根据 pattern 特征智能解析日期/时间/日期时间字符串为 LocalDateTime
   * <ul>
   *   <li>纯日期 pattern（如 yyyy-MM-dd）→ 解析为 LocalDate，再转为当天 00:00 的 LocalDateTime</li>
   *   <li>纯时间 pattern（如 HH:mm）→ 解析为 LocalTime，再组合 1899-12-30 为 LocalDateTime（Excel 时间基准）</li>
   *   <li>日期时间 pattern → 直接解析为 LocalDateTime</li>
   * </ul>
   */
  private static LocalDateTime parse(String text, String pattern) {
    if (Strings.isBlank(text)) {
      return null;
    }

    try {
      DateTimeFormatter formatter = DateTimeFormat.get(pattern);
      boolean hasDate = pattern.contains("y") || pattern.contains("d") && !pattern.contains("H");
      boolean hasTime = pattern.contains("H") || pattern.contains("h") || pattern.contains("K") || pattern.contains("k")
        || pattern.contains("m") && pattern.contains("H") || pattern.contains("s");

      if (hasDate && !hasTime) {
        // 纯日期：解析为 LocalDate → LocalDateTime(00:00)
        LocalDate date = LocalDate.parse(text, formatter);
        return date.atStartOfDay();
      } else if (hasTime && !hasDate) {
        // 纯时间：解析为 LocalTime → 组合基准日期
        LocalTime time = LocalTime.parse(text, formatter);
        return LocalDateTime.of(LocalDate.of(1899, 12, 30), time);
      } else {
        // 日期时间
        return LocalDateTime.parse(text, formatter);
      }
    } catch (DateTimeParseException | IllegalArgumentException e) {
      return null;
    }
  }
}
