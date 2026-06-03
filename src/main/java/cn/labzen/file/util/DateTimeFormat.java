package cn.labzen.file.util;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DateTimeFormat {

  private static final Map<String, DateTimeFormatter> FORMATTERS = new ConcurrentHashMap<>();

  public static DateTimeFormatter get(String pattern) {
    return FORMATTERS.computeIfAbsent(pattern, DateTimeFormatter::ofPattern);
  }

  private DateTimeFormat() {

  }
}
