package cn.labzen.file.locale.internal;

import cn.labzen.file.locale.FormattableResourceBundle;
import jakarta.annotation.Nonnull;

import java.util.*;

public class ChainableResourceBundle extends FormattableResourceBundle {

  private final List<ResourceBundle> bundles;

  public ChainableResourceBundle(List<ResourceBundle> bundles) {
    this.bundles = List.copyOf(bundles);
  }

  @Override
  protected Object handleGetObject(@Nonnull String key) {
    for (ResourceBundle bundle : bundles) {
      if (bundle.containsKey(key)) {
        return bundle.getObject(key);
      }
    }

    return key;
  }

  @Nonnull
  @Override
  public Enumeration<String> getKeys() {
    Set<String> keys = new LinkedHashSet<>();

    for (ResourceBundle bundle : bundles) {
      keys.addAll(bundle.keySet());
    }

    return Collections.enumeration(keys);
  }
}
