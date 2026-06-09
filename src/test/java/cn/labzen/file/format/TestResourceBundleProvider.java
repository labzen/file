package cn.labzen.file.format;

import cn.labzen.file.exception.I18nException;
import cn.labzen.file.locale.FileResourceBundleProvider;

import java.util.Locale;
import java.util.ResourceBundle;

public class TestResourceBundleProvider implements FileResourceBundleProvider {

  private static final String BUNDLE_BASE_NAME = "text.test";

  @Override
  public ResourceBundle getResourceBundle(Locale locale) throws I18nException {
    return ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale, TestResourceBundleProvider.class.getClassLoader());
  }

  @Override
  public int order() {
    return 0;
  }
}
