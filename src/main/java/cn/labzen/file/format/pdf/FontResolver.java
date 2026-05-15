package cn.labzen.file.format.pdf;

import cn.labzen.file.definition.bean.style.Style;
import cn.labzen.file.meta.FileConfiguration;
import cn.labzen.meta.Labzens;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PDF 字体解析器
 * <p>
 * 负责根据配置解析并缓存 PDF 字体，支持：
 * <ul>
 *   <li>从 Style.font 或全局配置获取字体族名</li>
 *   <li>自动识别当前系统的默认中文字体</li>
 *   <li>指定字体名，验证并使用系统字体</li>
 *   <li>字体缓存，避免重复加载</li>
 * </ul>
 *
 * @author labzen
 */
@Slf4j
public final class FontResolver {

  /**
   * 字体缓存映射：字体族名 -> FontInfo
   */
  private static final Map<String, FontInfo> FONT_CACHE = new ConcurrentHashMap<>();

  /**
   * 字体目录缓存
   */
  private static String cachedFontsDir;

  /**
   * 缓存刷新标识，用于配置变更时清除缓存
   */
  private static String lastConfig;

  private FontResolver() {
    // 私有构造函数，防止实例化
  }

  /**
   * 获取 PDF 常规字体
   *
   * @param style 样式配置（可为 null，使用全局默认字体）
   * @return 常规字体
   */
  public static @NonNull PdfFont getRegularFont(Style style) {
    return getFontInfo(style).regularFont();
  }

  /**
   * 获取 PDF 粗体字体
   *
   * @param style 样式配置（可为 null，使用全局默认字体）
   * @return 粗体字体
   */
  public static @NonNull PdfFont getBoldFont(Style style) {
    return getFontInfo(style).boldFont();
  }

  /**
   * 获取字体信息（包含常规和粗体字体）
   *
   * @param style 样式配置（可为 null，使用全局默认字体）
   * @return FontInfo 对象
   */
  public static @NonNull FontInfo getFontInfo(Style style) {
    // 从 Style.font 或全局配置获取字体族名
    String fontFamily = resolveFontFamily(style);
    // 使用字体族名作为缓存键
    String cacheKey = fontFamily != null ? fontFamily : "auto";

    // 检查是否需要清除缓存（配置已变更）
    checkAndClearCache(cacheKey);

    // 尝试从缓存获取
    return FONT_CACHE.computeIfAbsent(cacheKey, key -> resolveFont(fontFamily, style));
  }

  /**
   * 获取默认字体信息（使用全局配置，不指定样式）
   *
   * @return FontInfo 对象
   */
  public static @NonNull FontInfo getDefaultFontInfo() {
    return getFontInfo(null);
  }

  /**
   * 清除所有字体缓存
   */
  public static void clearCache() {
    FONT_CACHE.clear();
    cachedFontsDir = null;
    lastConfig = null;
    logger.debug("字体缓存已清除");
  }

  /**
   * 检查配置是否变更，如变更则清除缓存
   *
   * @param cacheKey 当前缓存键
   */
  private static void checkAndClearCache(String cacheKey) {
    if (!cacheKey.equals(lastConfig)) {
      clearCache();
      lastConfig = cacheKey;
    }
  }

  /**
   * 从样式配置或全局配置中解析字体族名
   *
   * @param style 样式配置（可为 null）
   * @return 字体族名（null 表示使用 auto 自动选择）
   */
  private static String resolveFontFamily(Style style) {
    // 优先级：Style.font.family > 全局 defaultFontFamily
    if (style != null && style.getFont() != null && style.getFont().getFamily() != null) {
      String family = style.getFont().getFamily();
      logger.debug("使用样式指定的字体: {}", family);
      return family;
    }

    // 使用全局配置
    FileConfiguration config = Labzens.configurationWith(FileConfiguration.class);
    String defaultFamily = config.defaultFontFamily();

    // 如果是 "auto" 或空，返回 null 表示自动选择
    if (defaultFamily == null || defaultFamily.isEmpty() || "auto".equalsIgnoreCase(defaultFamily)) {
      return null;
    }

    logger.debug("使用全局配置的字体: {}", defaultFamily);
    return defaultFamily;
  }

  /**
   * 解析字体配置，获取字体信息
   *
   * @param fontFamily 字体族名（null 表示自动选择）
   * @param style      样式配置（用于获取 bold 属性）
   * @return FontInfo 对象
   */
  private static FontInfo resolveFont(String fontFamily, Style style) {
    logger.debug("解析字体配置: {}", fontFamily != null ? fontFamily : "auto");

    // 尝试加载字体，失败则回退到自动选择
    try {
      return createSystemFont(fontFamily, style);
    } catch (Exception e) {
      logger.debug("字体 [{}] 加载失败，尝试自动选择: {}", fontFamily, e.getMessage());
      return createSystemFont(null, null);
    }
  }

  /**
   * 创建系统字体
   *
   * @param preferredFont 优先使用的字体名（null 表示自动选择）
   * @param style         样式配置（用于获取 bold 属性）
   * @return FontInfo 对象
   */
  private static FontInfo createSystemFont(String preferredFont, Style style) {
    String osName = System.getProperty("os.name", "").toLowerCase();

    // 如果指定了字体名且该字体存在，直接使用
    if (preferredFont != null && isFontAvailable(preferredFont)) {
      logger.debug("使用指定字体: {}", preferredFont);
      return createFontByName(preferredFont, style);
    }

    // 自动选择系统字体
    if (osName.contains("win")) {
      return createWindowsFont(preferredFont, style);
    } else if (osName.contains("linux")) {
      return createLinuxFont(style);
    } else if (osName.contains("mac") || osName.contains("darwin")) {
      return createMacFont(style);
    } else {
      return createGenericFont(style);
    }
  }

  /**
   * 检查字体在当前系统中是否可用
   *
   * @param fontName 字体名
   * @return 是否可用
   */
  private static boolean isFontAvailable(String fontName) {
    java.awt.Font[] fonts = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    for (java.awt.Font font : fonts) {
      if (font.getFontName().equalsIgnoreCase(fontName) || font.getName().equalsIgnoreCase(fontName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 根据字体名创建字体对象
   *
   * @param fontName 字体名
   * @param style    样式配置（用于获取 bold 属性）
   * @return FontInfo 对象
   */
  private static FontInfo createFontByName(String fontName, Style style) {
    String osName = System.getProperty("os.name", "").toLowerCase();
    String fontsDir = getFontsDirectory();
    String baseFontPath;
    String boldFontPath;

    if (osName.contains("win")) {
      // Windows 系统字体路径（优先 .ttc）
      baseFontPath = fontsDir + "\\" + fontName + ".ttc";
      boldFontPath = fontsDir + "\\" + fontName + ",1";
    } else if (osName.contains("linux")) {
      // Linux 系统字体路径
      baseFontPath = "/usr/share/fonts/truetype/" + fontName + ".ttc";
      boldFontPath = "/usr/share/fonts/truetype/" + fontName + ",1";
    } else {
      // Mac 系统
      baseFontPath = "/Library/Fonts/" + fontName + ".ttf";
      boldFontPath = "/Library/Fonts/" + fontName + "bd.ttf";
    }

    return loadFontFromPath(baseFontPath, boldFontPath, fontName, style);
  }

  /**
   * 从指定路径加载字体
   *
   * @param baseFontPath 常规字体路径
   * @param boldFontPath 粗体字体路径（可以是索引如 "font.ttc,1"）
   * @param fontName     字体名（用于日志）
   * @param style        样式配置（用于获取 bold 属性）
   * @return FontInfo 对象
   */
  private static FontInfo loadFontFromPath(String baseFontPath, String boldFontPath, String fontName, Style style) {
    File regularFile = new File(baseFontPath);
    File boldFile = new File(boldFontPath.contains(",") ? extractPath(boldFontPath) : boldFontPath);
    boolean isTtc = baseFontPath.toLowerCase().endsWith(".ttc");

    // 判断是否需要加载粗体字体
    boolean useBold = isBoldRequested(style);

    if (regularFile.exists()) {
      try {
        if (isTtc) {
          // .ttc 文件（TrueType Collection）
          PdfFont regularFont = PdfFontFactory.createFont(regularFile.getAbsolutePath() + ",0", "Identity-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
          PdfFont boldFont = useBold ? loadBoldFont(regularFile.getAbsolutePath(), regularFont) : regularFont;
          logger.debug("使用 TTC 字体: {}", fontName);
          return new FontInfo(regularFont, boldFont);
        } else {
          // .ttf 文件
          PdfFont regularFont = PdfFontFactory.createFont(regularFile.getAbsolutePath(), "Identity-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
          PdfFont boldFont = (useBold && boldFile.exists())
            ? PdfFontFactory.createFont(boldFile.getAbsolutePath(), "Identity-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED)
            : regularFont;
          logger.debug("使用 TTF 字体: {}", fontName);
          return new FontInfo(regularFont, boldFont);
        }
      } catch (Exception e) {
        logger.warn("从路径加载字体失败: {}", e.getMessage());
      }
    }

    // 尝试使用 iText 的字体工厂方法（支持字体名）
    try {
      PdfFont regularFont = PdfFontFactory.createFont(fontName, "Identity-H");
      PdfFont boldFont = useBold ? PdfFontFactory.createFont(fontName + " Bold", "Identity-H") : regularFont;
      return new FontInfo(regularFont, boldFont);
    } catch (Exception e) {
      throw new RuntimeException("无法创建字体: " + fontName, e);
    }
  }

  /**
   * 加载粗体字体
   */
  private static PdfFont loadBoldFont(String fontPath, PdfFont fallback) {
    try {
      return PdfFontFactory.createFont(fontPath + ",1", "Identity-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
    } catch (Exception e) {
      return fallback;
    }
  }

  /**
   * 从完整路径（含索引）中提取纯文件路径
   */
  private static String extractPath(String pathWithIndex) {
    int commaIndex = pathWithIndex.lastIndexOf(',');
    return commaIndex > 0 ? pathWithIndex.substring(0, commaIndex) : pathWithIndex;
  }

  /**
   * 创建 Windows 系统字体
   *
   * @param preferredFont 优先使用的字体名（null 表示自动选择）
   * @param style         样式配置（用于获取 bold 属性）
   * @return FontInfo 对象
   */
  private static FontInfo createWindowsFont(String preferredFont, Style style) {
    // Windows 常用的中文字体
    String[] commonFonts = {
      "MSYHBD",   // 微软雅黑 Bold
      "MSYH",     // 微软雅黑
      "SIMHEI",   // 黑体
      "SIMSUN",   // 宋体
      "STZHONGS", // 华文中宋
      "STKAITI",  // 华文楷体
      "STSONG"    // 华文宋体
    };

    String fontsDir = getFontsDirectory();
    boolean useBold = isBoldRequested(style);

    for (String fontName : commonFonts) {
      // 尝试 .ttc 格式（微软雅黑等）
      File ttcFile = new File(fontsDir + "\\" + fontName + ".ttc");
      File ttfFile = new File(fontsDir + "\\" + fontName + ".ttf");
      File boldFile = new File(fontsDir + "\\" + fontName + "bd.ttf");

      if (ttcFile.exists()) {
        try {
          PdfFont regularFont = PdfFontFactory.createFont(ttcFile.getAbsolutePath() + ",0", "Identity-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
          PdfFont boldFont = useBold ? loadBoldFont(ttcFile.getAbsolutePath(), regularFont) : regularFont;
          logger.debug("Windows 系统使用 TTC 字体: {}", fontName);
          return new FontInfo(regularFont, boldFont);
        } catch (Exception e) {
          logger.debug("TTC 字体 [{}] 加载失败: {}", fontName, e.getMessage());
        }
      } else if (ttfFile.exists()) {
        try {
          PdfFont regularFont = PdfFontFactory.createFont(ttfFile.getAbsolutePath(), "Identity-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
          PdfFont boldFont = (useBold && boldFile.exists())
            ? PdfFontFactory.createFont(boldFile.getAbsolutePath(), "Identity-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED)
            : regularFont;
          logger.debug("Windows 系统使用 TTF 字体: {}", fontName);
          return new FontInfo(regularFont, boldFont);
        } catch (Exception e) {
          logger.debug("TTF 字体 [{}] 加载失败: {}", fontName, e.getMessage());
        }
      }
    }

    return createGenericFont(style);
  }

  /**
   * 创建 Linux 系统字体
   *
   * @param style 样式配置（用于获取 bold 属性）
   * @return FontInfo 对象
   */
  private static FontInfo createLinuxFont(Style style) {
    // Linux 常用的中文字体路径
    String[] fontPaths = {
      "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc",
      "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc",
      "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
      "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
      "/usr/share/fonts/truetype/arphic/uming.ttc"
    };

    boolean useBold = isBoldRequested(style);

    for (String fontPath : fontPaths) {
      File fontFile = new File(fontPath);
      if (fontFile.exists()) {
        try {
          // 尝试获取常规和粗体变体
          PdfFont regularFont = PdfFontFactory.createFont(fontPath + ",1", "Identity-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
          PdfFont boldFont = useBold
            ? PdfFontFactory.createFont(fontPath + ",0", "Identity-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED)
            : regularFont;
          logger.debug("Linux 系统使用字体: {}", fontPath);
          return new FontInfo(regularFont, boldFont);
        } catch (Exception e) {
          try {
            PdfFont regularFont = PdfFontFactory.createFont(fontPath, "Identity-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            PdfFont boldFont = regularFont;
            logger.debug("Linux 系统使用字体: {} (单一字体)", fontPath);
            return new FontInfo(regularFont, boldFont);
          } catch (Exception ex) {
            logger.debug("字体 [{}] 加载失败: {}", fontPath, ex.getMessage());
          }
        }
      }
    }

    return createGenericFont(style);
  }

  /**
   * 创建 Mac 系统字体
   *
   * @param style 样式配置（用于获取 bold 属性）
   * @return FontInfo 对象
   */
  private static FontInfo createMacFont(Style style) {
    // Mac 常用的中文字体
    String[] fontNames = {
      "PingFang HK",
      "PingFang SC",
      "STHeiti Light",
      "STHeiti",
      "Hiragino Sans GB"
    };

    boolean useBold = isBoldRequested(style);

    for (String fontName : fontNames) {
      try {
        PdfFont regularFont = PdfFontFactory.createFont(fontName, "Identity-H");
        PdfFont boldFont = useBold ? PdfFontFactory.createFont(fontName + " Bold", "Identity-H") : regularFont;
        logger.debug("Mac 系统使用字体: {}", fontName);
        return new FontInfo(regularFont, boldFont);
      } catch (Exception e) {
        logger.debug("字体 [{}] 加载失败: {}", fontName, e.getMessage());
      }
    }

    return createGenericFont(style);
  }

  /**
   * 创建通用字体（作为最终回退方案）
   *
   * @param style 样式配置（用于获取 bold 属性）
   * @return FontInfo 对象
   */
  private static FontInfo createGenericFont(Style style) {
    boolean useBold = isBoldRequested(style);

    try {
      PdfFont regularFont = PdfFontFactory.createFont("Helvetica", "Cp1252");
      PdfFont boldFont = useBold ? PdfFontFactory.createFont("Helvetica-Bold", "Cp1252") : regularFont;
      logger.debug("使用 Helvetica 字体（可能不支持中文）");
      return new FontInfo(regularFont, boldFont);
    } catch (Exception e) {
      try {
        PdfFont font = PdfFontFactory.createFont();
        return new FontInfo(font, font);
      } catch (Exception ex) {
        logger.warn("无法创建任何字体，使用空白字体", ex);
        throw new RuntimeException("无法创建 PDF 字体", ex);
      }
    }
  }

  /**
   * 获取系统字体目录
   */
  private static String getFontsDirectory() {
    if (cachedFontsDir != null) {
      return cachedFontsDir;
    }

    String windir = System.getenv("WINDIR");
    cachedFontsDir = (windir != null ? windir : "C:\\Windows") + "\\Fonts";
    return cachedFontsDir;
  }

  /**
   * 判断是否需要加载粗体字体
   *
   * @param style 样式配置
   * @return 是否需要粗体
   */
  private static boolean isBoldRequested(Style style) {
    if (style != null && style.getFont() != null && Boolean.TRUE.equals(style.getFont().getBold())) {
      return true;
    }
    return false;
  }

  /**
   * 字体信息记录
   *
   * @param regularFont 常规字体
   * @param boldFont    粗体字体
   */
  public record FontInfo(PdfFont regularFont, PdfFont boldFont) {
  }
}