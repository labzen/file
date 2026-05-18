package cn.labzen.file.format.pdf;

import cn.labzen.file.definition.bean.style.Style;
import cn.labzen.file.meta.FileConfiguration;
import cn.labzen.meta.Labzens;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.*;
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
 * <p>
 * 缓存策略：以字体族名为 key 缓存 {@link FontInfo}，每个 FontInfo 始终包含常规和粗体两种变体。
 * 不同的字体族名独立缓存，互不影响。
 *
 * @author labzen
 */
@Slf4j
public final class FontResolver {

  /**
   * 自动选择字体的缓存 key
   */
  private static final String AUTO_CACHE_KEY = "__auto__";

  /**
   * 字体缓存映射：字体族名（或 auto 标识）-> FontInfo
   */
  private static final Map<String, FontInfo> FONT_CACHE = new ConcurrentHashMap<>();

  /**
   * Windows 系统已知的中文候选项描述符
   * <p>
   * 每个描述符包含：字体族名、常规字体文件名、粗体字体文件名（可为 null）
   */
  private static final List<FontDescriptor> WINDOWS_FONT_CANDIDATES = List.of(
    FontDescriptor.of("Microsoft YaHei", "msyh.ttc", "msyhbd.ttc"),
    FontDescriptor.of("SimHei", "simhei.ttf", null),
    FontDescriptor.of("SimSun", "simsun.ttc", null),
    FontDescriptor.of("STZhongsong", "STZHONGS.TTF", null),
    FontDescriptor.of("STKaiti", "stkaiti.ttf", null),
    FontDescriptor.of("STSong", "STSONG.TTF", null)
  );

  /**
   * Linux 系统已知的中文候选项字体路径
   */
  private static final List<String> LINUX_FONT_PATHS = List.of(
    "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc",
    "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc",
    "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
    "/usr/share/fonts/truetype/arphic/uming.ttc",
    "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
  );

  /**
   * Mac 系统已知的中文候选项字体族名
   */
  private static final List<String> MAC_FONT_NAMES = List.of(
    "PingFang SC",
    "PingFang HK",
    "STHeiti",
    "Hiragino Sans GB"
  );

  private FontResolver() {
  }

  // ==================== 公共 API ====================

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
   * <p>
   * FontInfo 始终包含常规和粗体两种变体（粗体不可用时回退为常规字体），
   * 不受传入 Style 的 bold 属性影响。
   *
   * @param style 样式配置（可为 null，使用全局默认字体）
   * @return FontInfo 对象
   */
  public static @NonNull FontInfo getFontInfo(Style style) {
    String fontFamily = resolveFontFamily(style);
    String cacheKey = fontFamily != null ? fontFamily : AUTO_CACHE_KEY;
    return FONT_CACHE.computeIfAbsent(cacheKey, key -> resolveFont(fontFamily));
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
    logger.debug("字体缓存已清除");
  }

  // ==================== 字体族名解析 ====================

  /**
   * 从样式配置或全局配置中解析字体族名
   * <p>
   * 优先级：Style.font.family > 全局 defaultFontFamily
   *
   * @param style 样式配置（可为 null）
   * @return 字体族名（null 表示使用 auto 自动选择）
   */
  private static String resolveFontFamily(Style style) {
    if (style != null && style.getFont() != null && style.getFont().getFamily() != null) {
      String family = style.getFont().getFamily();
      logger.debug("使用样式指定的字体: {}", family);
      return family;
    }

    FileConfiguration config = Labzens.configurationWith(FileConfiguration.class);
    String defaultFamily = config.defaultFontFamily();

    if (defaultFamily == null || defaultFamily.isEmpty() || "auto".equalsIgnoreCase(defaultFamily)) {
      return null;
    }

    logger.debug("使用全局配置的字体: {}", defaultFamily);
    return defaultFamily;
  }

  // ==================== 字体解析主流程 ====================

  /**
   * 解析字体：尝试加载指定字体，失败则回退到系统自动选择
   *
   * @param fontFamily 字体族名（null 表示自动选择）
   * @return FontInfo 对象
   */
  private static FontInfo resolveFont(String fontFamily) {
    logger.debug("解析字体: {}", fontFamily != null ? fontFamily : "auto");

    // 尝试加载指定字体
    if (fontFamily != null) {
      FontInfo result = tryLoadSpecifiedFont(fontFamily);
      if (result != null) {
        return result;
      }
      logger.warn("指定字体 [{}] 加载失败，回退到系统自动选择", fontFamily);
    }

    // 自动选择系统字体
    return autoSelectSystemFont();
  }

  // ==================== 指定字体加载 ====================

  /**
   * 尝试加载指定字体的字体族
   *
   * @param fontFamily 字体族名
   * @return FontInfo，加载失败返回 null
   */
  private static FontInfo tryLoadSpecifiedFont(String fontFamily) {
    String os = detectOs();

    return switch (os) {
      case "windows" -> tryLoadSpecifiedWindowsFont(fontFamily);
      case "linux" -> tryLoadSpecifiedLinuxFont(fontFamily);
      case "mac" -> tryLoadSpecifiedMacFont(fontFamily);
      default -> null;
    };
  }

  /**
   * Windows 平台：尝试加载指定字体
   * <p>
   * 先在已知字体描述符中匹配，再尝试用字体族名直接查找字体文件
   */
  private static FontInfo tryLoadSpecifiedWindowsFont(String fontFamily) {
    String fontsDir = getWindowsFontsDir();
    String lowerFamily = fontFamily.toLowerCase();

    // 1. 在已知字体描述符中匹配
    for (FontDescriptor desc : WINDOWS_FONT_CANDIDATES) {
      if (desc.familyName().toLowerCase().contains(lowerFamily) ||
          lowerFamily.contains(desc.familyName().toLowerCase())) {
        FontInfo result = loadFromFontDescriptor(fontsDir, desc);
        if (result != null) {
          return result;
        }
      }
    }

    // 2. 尝试直接用字体族名查找字体文件
    return tryDirectFileLookup(fontsDir, fontFamily);
  }

  /**
   * Linux 平台：尝试加载指定字体
   * <p>
   * 在常用字体目录中递归搜索包含字体族名的字体文件
   */
  private static FontInfo tryLoadSpecifiedLinuxFont(String fontFamily) {
    String[] searchDirs = {
      "/usr/share/fonts/truetype",
      "/usr/share/fonts/opentype",
      "/usr/local/share/fonts",
      System.getProperty("user.home") + "/.fonts",
      System.getProperty("user.home") + "/.local/share/fonts"
    };

    String lowerFamily = fontFamily.toLowerCase().replace(" ", "");

    for (String dir : searchDirs) {
      File found = searchFontFile(new File(dir), lowerFamily);
      if (found != null) {
        FontInfo result = loadFromFontFile(found);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  /**
   * Mac 平台：尝试加载指定字体
   * <p>
   * Mac 支持 iText 的字体名直接加载
   */
  private static FontInfo tryLoadSpecifiedMacFont(String fontFamily) {
    try {
      PdfFont regularFont = PdfFontFactory.createFont(fontFamily, "Identity-H");
      PdfFont boldFont = loadMacBoldFont(fontFamily, regularFont);
      logger.debug("Mac 使用指定字体: {}", fontFamily);
      return new FontInfo(regularFont, boldFont);
    } catch (Exception e) {
      logger.debug("Mac 字体 [{}] 加载失败: {}", fontFamily, e.getMessage());
      return null;
    }
  }

  // ==================== 自动选择 ====================

  /**
   * 自动选择系统默认中文字体
   */
  private static FontInfo autoSelectSystemFont() {
    String os = detectOs();

    return switch (os) {
      case "windows" -> autoSelectWindowsFont();
      case "linux" -> autoSelectLinuxFont();
      case "mac" -> autoSelectMacFont();
      default -> createFallbackFont();
    };
  }

  /**
   * Windows 自动选择：按优先级遍历已知中文字体描述符
   */
  private static FontInfo autoSelectWindowsFont() {
    String fontsDir = getWindowsFontsDir();

    for (FontDescriptor desc : WINDOWS_FONT_CANDIDATES) {
      FontInfo result = loadFromFontDescriptor(fontsDir, desc);
      if (result != null) {
        logger.debug("Windows 自动选择字体: {}", desc.familyName());
        return result;
      }
    }

    return createFallbackFont();
  }

  /**
   * Linux 自动选择：按优先级遍历已知字体路径
   */
  private static FontInfo autoSelectLinuxFont() {
    for (String path : LINUX_FONT_PATHS) {
      File file = new File(path);
      if (file.exists()) {
        FontInfo result = loadFromFontFile(file);
        if (result != null) {
          logger.debug("Linux 自动选择字体: {}", file.getName());
          return result;
        }
      }
    }

    return createFallbackFont();
  }

  /**
   * Mac 自动选择：按优先级遍历已知中文字体族名
   */
  private static FontInfo autoSelectMacFont() {
    for (String fontName : MAC_FONT_NAMES) {
      try {
        PdfFont regularFont = PdfFontFactory.createFont(fontName, "Identity-H");
        PdfFont boldFont = loadMacBoldFont(fontName, regularFont);
        logger.debug("Mac 自动选择字体: {}", fontName);
        return new FontInfo(regularFont, boldFont);
      } catch (Exception e) {
        logger.debug("Mac 字体 [{}] 不可用", fontName);
      }
    }

    return createFallbackFont();
  }

  // ==================== 字体文件加载 ====================

  /**
   * 从字体描述符加载字体
   * <p>
   * 根据描述符中定义的常规/粗体文件名，在指定目录中查找并加载。
   * 始终加载常规和粗体两种变体（粗体不可用时回退为常规字体）。
   *
   * @param dir        字体文件所在目录
   * @param descriptor 字体描述符
   * @return FontInfo，加载失败返回 null
   */
  private static FontInfo loadFromFontDescriptor(String dir, FontDescriptor descriptor) {
    File regularFile = new File(dir, descriptor.regularFile());
    if (!regularFile.exists()) {
      return null;
    }

    try {
      PdfFont regularFont = loadPdfFont(regularFile, 0);
      PdfFont boldFont;

      if (descriptor.boldFile() != null) {
        // 粗体有独立的字体文件
        File boldFile = new File(dir, descriptor.boldFile());
        if (boldFile.exists()) {
          boldFont = loadPdfFont(boldFile, 0);
        } else {
          boldFont = regularFont;
        }
      } else if (isTtcFile(regularFile)) {
        // TTC 文件尝试索引 1 作为粗体变体
        boldFont = loadPdfFont(regularFile, 1, regularFont);
      } else {
        boldFont = regularFont;
      }

      logger.debug("加载字体: {} -> 常规: {}, 粗体: {}",
        descriptor.familyName(), regularFile.getName(),
        boldFont == regularFont ? "(同常规)" : descriptor.boldFile());
      return new FontInfo(regularFont, boldFont);
    } catch (Exception e) {
      logger.debug("字体描述符加载失败 [{}]: {}", descriptor.familyName(), e.getMessage());
      return null;
    }
  }

  /**
   * 从单个字体文件加载字体
   * <p>
   * TTC 文件同时尝试加载索引 0（常规）和索引 1（粗体），
   * TTF 文件常规和粗体使用同一字体。
   *
   * @param file 字体文件
   * @return FontInfo，加载失败返回 null
   */
  private static FontInfo loadFromFontFile(File file) {
    try {
      PdfFont regularFont = loadPdfFont(file, 0);
      PdfFont boldFont;

      if (isTtcFile(file)) {
        boldFont = loadPdfFont(file, 1, regularFont);
      } else {
        boldFont = regularFont;
      }

      logger.debug("加载字体文件: {}", file.getAbsolutePath());
      return new FontInfo(regularFont, boldFont);
    } catch (Exception e) {
      logger.debug("字体文件加载失败 [{}]: {}", file.getName(), e.getMessage());
      return null;
    }
  }

  /**
   * 在目录中递归搜索包含指定名称的字体文件
   *
   * @param dir         搜索目录
   * @param lowerFamily 字体族名（小写、去空格）
   * @return 找到的字体文件，未找到返回 null
   */
  private static File searchFontFile(File dir, String lowerFamily) {
    if (!dir.exists() || !dir.isDirectory()) {
      return null;
    }

    File[] children = dir.listFiles();
    if (children == null) {
      return null;
    }

    for (File child : children) {
      if (child.isDirectory()) {
        File found = searchFontFile(child, lowerFamily);
        if (found != null) {
          return found;
        }
      } else {
        String lowerName = child.getName().toLowerCase().replace(" ", "");
        if ((lowerName.endsWith(".ttf") || lowerName.endsWith(".ttc"))
            && lowerName.contains(lowerFamily)) {
          return child;
        }
      }
    }
    return null;
  }

  /**
   * 尝试直接用字体族名查找字体文件（遍历常见扩展名）
   */
  private static FontInfo tryDirectFileLookup(String dir, String fontFamily) {
    String[] extensions = {".ttc", ".ttf"};

    for (String ext : extensions) {
      // 尝试原始名称
      File file = new File(dir, fontFamily + ext);
      if (file.exists()) {
        FontInfo result = loadFromFontFile(file);
        if (result != null) {
          return result;
        }
      }

      // 尝试去空格后的名称
      File compactFile = new File(dir, fontFamily.replace(" ", "") + ext);
      if (compactFile.exists() && !compactFile.equals(file)) {
        FontInfo result = loadFromFontFile(compactFile);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  // ==================== 底层字体创建 ====================

  /**
   * 从文件加载 PDF 字体（TTC 文件使用指定索引）
   *
   * @param file  字体文件
   * @param index TTC 文件中的字体索引
   * @return PdfFont
   * @throws Exception 加载失败时抛出
   */
  private static PdfFont loadPdfFont(File file, int index) throws Exception {
    String path = isTtcFile(file)
      ? file.getAbsolutePath() + "," + index
      : file.getAbsolutePath();
    return PdfFontFactory.createFont(path, "Identity-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
  }

  /**
   * 从文件加载 PDF 字体（TTC 文件使用指定索引），失败时返回回退字体
   *
   * @param file     字体文件
   * @param index    TTC 文件中的字体索引
   * @param fallback 加载失败时的回退字体
   * @return PdfFont
   */
  private static PdfFont loadPdfFont(File file, int index, PdfFont fallback) {
    try {
      return loadPdfFont(file, index);
    } catch (Exception e) {
      logger.debug("TTC 索引 {} 加载失败，使用回退字体: {}", index, e.getMessage());
      return fallback;
    }
  }

  /**
   * 加载 Mac 粗体字体，失败时返回回退字体
   */
  private static PdfFont loadMacBoldFont(String fontFamily, PdfFont fallback) {
    try {
      return PdfFontFactory.createFont(fontFamily + " Bold", "Identity-H");
    } catch (Exception e) {
      logger.debug("Mac 粗体字体 [{}] 不可用，使用常规字体", fontFamily);
      return fallback;
    }
  }

  /**
   * 创建回退字体（Helvetica），不支持中文
   */
  private static FontInfo createFallbackFont() {
    try {
      PdfFont regularFont = PdfFontFactory.createFont("Helvetica", "Cp1252");
      PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold", "Cp1252");
      logger.warn("使用 Helvetica 回退字体（不支持中文）");
      return new FontInfo(regularFont, boldFont);
    } catch (Exception e) {
      try {
        PdfFont font = PdfFontFactory.createFont();
        logger.warn("使用默认回退字体");
        return new FontInfo(font, font);
      } catch (Exception ex) {
        throw new RuntimeException("无法创建 PDF 字体", ex);
      }
    }
  }

  // ==================== 工具方法 ====================

  /**
   * 检测当前操作系统
   *
   * @return "windows" | "linux" | "mac" | "unknown"
   */
  private static String detectOs() {
    String osName = System.getProperty("os.name", "").toLowerCase();
    if (osName.contains("win")) {
      return "windows";
    } else if (osName.contains("linux")) {
      return "linux";
    } else if (osName.contains("mac") || osName.contains("darwin")) {
      return "mac";
    }
    return "unknown";
  }

  /**
   * 获取 Windows 系统字体目录
   */
  private static String getWindowsFontsDir() {
    String windir = System.getenv("WINDIR");
    return (windir != null ? windir : "C:\\Windows") + "\\Fonts";
  }

  /**
   * 判断是否为 TTC 字体文件
   */
  private static boolean isTtcFile(File file) {
    return file.getName().toLowerCase().endsWith(".ttc");
  }

  // ==================== 内部数据结构 ====================

  /**
   * 字体描述符，描述一个字体族的文件名映射关系
   *
   * @param familyName   字体族名（如 "Microsoft YaHei"）
   * @param regularFile  常规字体文件名（如 "msyh.ttc"）
   * @param boldFile     粗体字体文件名（如 "msyhbd.ttc"），null 表示无独立粗体文件
   */
  private record FontDescriptor(String familyName, String regularFile, String boldFile) {
    static FontDescriptor of(String familyName, String regularFile, String boldFile) {
      return new FontDescriptor(familyName, regularFile, boldFile);
    }
  }

  /**
   * 字体信息记录
   *
   * @param regularFont 常规字体
   * @param boldFont    粗体字体（粗体不可用时与常规字体相同）
   */
  public record FontInfo(PdfFont regularFont, PdfFont boldFont) {
  }
}
