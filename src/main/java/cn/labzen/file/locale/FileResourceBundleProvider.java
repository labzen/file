package cn.labzen.file.locale;

import cn.labzen.file.exception.I18nException;

import java.util.Locale;
import java.util.ResourceBundle;

public interface FileResourceBundleProvider {

  ResourceBundle getResourceBundle(Locale locale) throws I18nException;

  int order();
}
