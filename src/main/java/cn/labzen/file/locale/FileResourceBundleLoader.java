package cn.labzen.file.locale;

import cn.labzen.file.exception.I18nException;
import cn.labzen.file.locale.internal.ChainableResourceBundle;
import cn.labzen.file.meta.FileConfiguration;
import cn.labzen.meta.Labzens;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class FileResourceBundleLoader {

  public static final Locale DEFAULT_LOCALE;
  private static final List<FileResourceBundleProvider> PROVIDERS;
  private static final Map<Locale, FormattableResourceBundle> RESOURCE_BUNDLE_CACHE = new ConcurrentHashMap<>();

  //  private static final FileResourceBundleProvider PROVIDER;
//
  static {
    FileConfiguration configuration = Labzens.configurationWith(FileConfiguration.class);
    String language = configuration.defaultLocale();
    DEFAULT_LOCALE = forLanguage(language);

    ServiceLoader<FileResourceBundleProvider> serviceLoader = ServiceLoader.load(FileResourceBundleProvider.class);

    List<FileResourceBundleProvider> providers = Lists.newArrayList();
    for (FileResourceBundleProvider provider : serviceLoader) {
      providers.add(provider);
    }
    providers.sort(Comparator.comparingInt(FileResourceBundleProvider::order));

    PROVIDERS = providers;
//
//    FileResourceBundleProvider latestProvider =null;
//    Iterator<FileResourceBundleProvider> iterator = providers.iterator();
//    while (iterator.hasNext()) {
//      FileResourceBundleProvider current = iterator.next();
//      if (latestProvider != null) {
//        current.getResourceBundle()
//      }
//      latestProvider = iterator.next();
//    }
  }

  private FileResourceBundleLoader() {

  }

//  public static void main(String[] args) {
//    System.out.println(Locale.SIMPLIFIED_CHINESE);
//    System.out.println(Locale.forLanguageTag("zh-CN"));
//    System.out.println(Locale.forLanguageTag("en-US"));
//    System.out.println(Locale.forLanguageTag("xx-fS"));
//  }

  public static FormattableResourceBundle load(Locale locale) {
    return RESOURCE_BUNDLE_CACHE.computeIfAbsent(locale, FileResourceBundleLoader::compute);
  }

  private static FormattableResourceBundle compute(Locale locale) {
    List<ResourceBundle> resourceBundles = PROVIDERS.stream().map(provider -> {
      try {
        return provider.getResourceBundle(locale);
      } catch (I18nException e) {
        logger.atWarn().setCause(e).log("无法加载国际化文案资源包 for locale: {}, from: {}", locale, provider.getClass());
        return null;
      }
    }).toList();

    return new ChainableResourceBundle(resourceBundles);
  }

  public static Locale forLanguage(String language) {
    try {
      return Locale.forLanguageTag(language);
    } catch (Exception e) {
      logger.error("不支持的语言：{}", language);
      return Locale.getDefault();
    }
  }
}
