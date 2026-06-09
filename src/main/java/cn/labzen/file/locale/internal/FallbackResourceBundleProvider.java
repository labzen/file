package cn.labzen.file.locale.internal;

import cn.labzen.file.locale.FileResourceBundleProvider;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class FallbackResourceBundleProvider implements FileResourceBundleProvider {

  private static final String BUNDLE_BASE_NAME = "i18n.messages";

  @Override
  public ResourceBundle getResourceBundle(Locale locale) {
    try {
      return ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale, FallbackResourceBundleProvider.class.getClassLoader());
    } catch (MissingResourceException e) {
      return null;
    }
  }

  @Override
  public int order() {
    return Integer.MAX_VALUE;
  }
}
