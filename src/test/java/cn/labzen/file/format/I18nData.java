package cn.labzen.file.format;

import cn.labzen.file.i18n.I18nStoreHolder;
import cn.labzen.file.i18n.I18nStoreProvider;
import cn.labzen.file.i18n.ManualI18nStoreProvider;

public class I18nData {

  public static I18nStoreProvider prepare() {
    // 初始化 i18n 仓库
    I18nStoreProvider store = new ManualI18nStoreProvider();
    store.setDefaultLocale("zh-CN");
    I18nStoreHolder.register(store);

    prepareChineseTexts();
    prepareEnglishTexts();

    return store;
  }

  private static void prepareChineseTexts() {
    ManualI18nStoreProvider store = (ManualI18nStoreProvider) I18nStoreHolder.get();
    store.putAll("zh-CN", java.util.Map.ofEntries(
      java.util.Map.entry("property-title", "系统属性"),
      java.util.Map.entry("basic-info", "基本信息"),
      java.util.Map.entry("prop-name", "属性名称"),
      java.util.Map.entry("index", "索引"),
      java.util.Map.entry("prop-value", "属性值"),
      java.util.Map.entry("status-info", "状态信息"),
      java.util.Map.entry("status", "状态"),
      java.util.Map.entry("category", "分类"),
      java.util.Map.entry("contact-info", "联系方式"),
      java.util.Map.entry("phone-number", "手机号码"),
      java.util.Map.entry("email-addr", "电子邮箱"),
      java.util.Map.entry("desc", "内容描述"),
      // when-null / when-blank
      java.util.Map.entry("unnamed", "未命名"),
      java.util.Map.entry("no-value", "无值"),
      java.util.Map.entry("blank-value", "空值"),
      java.util.Map.entry("unknown", "未知"),
      java.util.Map.entry("uncategorized", "未分类"),
      java.util.Map.entry("no-desc", "暂无描述"),
      // mapping 值
      java.util.Map.entry("debug-on", "调试开启"),
      java.util.Map.entry("debug-off", "调试关闭"),
      java.util.Map.entry("info-level", "信息级别"),
      java.util.Map.entry("enabled", "启用"),
      java.util.Map.entry("disabled", "禁用"),
      java.util.Map.entry("pending", "待审核"),
      java.util.Map.entry("system", "系统"),
      java.util.Map.entry("business", "业务"),
      java.util.Map.entry("other", "其他")
    ));
  }

  private static void prepareEnglishTexts() {
    ManualI18nStoreProvider store = (ManualI18nStoreProvider) I18nStoreHolder.get();
    store.putAll("en-US", java.util.Map.ofEntries(
      java.util.Map.entry("property-title", "System Properties"),
      java.util.Map.entry("basic-info", "Basic Info"),
      java.util.Map.entry("prop-name", "Property Name"),
      java.util.Map.entry("index", "Index"),
      java.util.Map.entry("prop-value", "Property Value"),
      java.util.Map.entry("status-info", "Status Info"),
      java.util.Map.entry("status", "Status"),
      java.util.Map.entry("category", "Category"),
      java.util.Map.entry("contact-info", "Contact"),
      java.util.Map.entry("phone-number", "Phone"),
      java.util.Map.entry("email-addr", "Email"),
      java.util.Map.entry("desc", "Description"),
      // when-null / when-blank
      java.util.Map.entry("unnamed", "Unnamed"),
      java.util.Map.entry("no-value", "No Value"),
      java.util.Map.entry("blank-value", "Blank"),
      java.util.Map.entry("unknown", "Unknown"),
      java.util.Map.entry("uncategorized", "Uncategorized"),
      java.util.Map.entry("no-desc", "No Description"),
      // mapping 值
      java.util.Map.entry("debug-on", "Debug On"),
      java.util.Map.entry("debug-off", "Debug Off"),
      java.util.Map.entry("info-level", "Info Level"),
      java.util.Map.entry("enabled", "Enabled"),
      java.util.Map.entry("disabled", "Disabled"),
      java.util.Map.entry("pending", "Pending"),
      java.util.Map.entry("system", "System"),
      java.util.Map.entry("business", "Business"),
      java.util.Map.entry("other", "Other")
    ));
  }
}
