package cn.labzen.file.converter.util;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public final class NamedConverterParser {

  public static List<MethodInvokeInfo> parseMethod(String text) {
    List<MethodInvokeInfo> result = new ArrayList<>();

    int i = 0;
    int len = text.length();

    while (i < len) {
      // 跳过空白
      while (i < len && Character.isWhitespace(text.charAt(i))) {
        i++;
      }
      if (i >= len) {
        break;
      }

      // 解析方法名
      int start = i;
      while (i < len) {
        char c = text.charAt(i);
        if (Character.isLetterOrDigit(c) || c == '_' || c == '$') {
          i++;
        } else {
          break;
        }
      }

      String methodName = text.substring(start, i);
      // 跳过空白
      while (i < len && Character.isWhitespace(text.charAt(i))) {
        i++;
      }

      // 必须是 (
      if (i >= len || text.charAt(i) != '(') {
        logger.error("解析转换器方法格式错误，数据转换功能无法正常使用: {}", text);
        return result;
      }

      i++; // skip (
      // 提取参数区域
      StringBuilder argsBuilder = new StringBuilder();
      boolean inString = false;
      boolean escape = false;
      int depth = 1;

      while (i < len && depth > 0) {
        char c = text.charAt(i);
        if (escape) {
          argsBuilder.append(c);
          escape = false;
        } else if (c == '\\') {
          argsBuilder.append(c);
          escape = true;
        } else if (c == '"') {
          argsBuilder.append(c);
          inString = !inString;
        } else if (!inString) {
          if (c == '(') {
            depth++;
            argsBuilder.append(c);
          } else if (c == ')') {
            depth--;
            if (depth > 0) {
              argsBuilder.append(c);
            }
          } else {
            argsBuilder.append(c);
          }
        } else {
          argsBuilder.append(c);
        }
        i++;
      }

      String argsText = argsBuilder.toString();
      result.add(new MethodInvokeInfo(methodName, parseArgs(argsText)));

      // 跳过 ; 和空白
      while (i < len) {
        char c = text.charAt(i);

        if (c == ';' || Character.isWhitespace(c)) {
          i++;
        } else {
          break;
        }
      }
    }

    return result;
  }

  private static List<String> parseArgs(String text) {
    if (text == null || text.isBlank()) {
      return Collections.emptyList();
    }

    List<String> result = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean inString = false;
    boolean escape = false;

    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);

      if (escape) {
        current.append(c);
        escape = false;
        continue;
      }

      if (c == '\\') {
        current.append(c);
        escape = true;
        continue;
      }

      if (c == '"') {
        current.append(c);
        inString = !inString;
        continue;
      }

      // 非字符串中的 ,
      if (c == ',' && !inString) {
        result.add(cleanArg(current.toString()));
        current.setLength(0);
        continue;
      }
      current.append(c);
    }

    if (!current.isEmpty()) {
      result.add(cleanArg(current.toString()));
    }

    return result;
  }

  private static String cleanArg(String str) {
    str = str.trim();

    // 去除双引号
    if (str.length() >= 2 && str.startsWith("\"") && str.endsWith("\"")) {
      str = str.substring(1, str.length() - 1);
    }

    return str;
  }

  public record MethodInvokeInfo(String methodName, List<String> args) {
  }
}
