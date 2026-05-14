package cn.labzen.file.format.excel;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.util.Map;

/**
 * 颜色转换工具类
 */
public final class ColorConvert {

  private final Map<String, XSSFColor> colorCache = new java.util.concurrent.ConcurrentHashMap<>();

  public XSSFColor getXssfColor(String hexColor) {
    if (hexColor == null || hexColor.isEmpty()) {
      return null;
    }
    return colorCache.computeIfAbsent(hexColor, this::createXssfColor);
  }

  private XSSFColor createXssfColor(String hexColor) {
    String hex = normalizeHex(hexColor);
    int r = Integer.parseInt(hex.substring(0, 2), 16);
    int g = Integer.parseInt(hex.substring(2, 4), 16);
    int b = Integer.parseInt(hex.substring(4, 6), 16);

    byte[] rgb = new byte[]{(byte) r, (byte) g, (byte) b};
    return new XSSFColor(rgb, null);
  }

  private String normalizeHex(String hexColor) {
    return hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;
  }
}