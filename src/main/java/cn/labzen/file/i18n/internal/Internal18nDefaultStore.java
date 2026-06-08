package cn.labzen.file.i18n.internal;

import cn.labzen.file.i18n.I18nStoreProvider;
import cn.labzen.file.i18n.ManualI18nStoreProvider;
import cn.labzen.tool.util.Strings;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import static cn.labzen.file.i18n.internal.Internal18nKeys.*;

public class Internal18nDefaultStore extends ManualI18nStoreProvider {

  private final I18nStoreProvider coreStore;

  public Internal18nDefaultStore(I18nStoreProvider store) {
//    coreStore = I18nStoreHolder.get();
    coreStore = store;
//    super.setDefaultLocale(I18nStoreHolder.defaultLocale());
    super.setDefaultLocale(store.getDefaultLocale());

    init();
  }

  private void init() {
    Map<String, String> enUS = Maps.newHashMap();
    enUS.put(TEMPLATE_MARKER_CODE_COMMENT, "Don't modify this cell! Otherwise, it will affect the accuracy of the data.");
    enUS.put(TEMPLATE_MARKER_HINT_COMMENT, "Don't modify this cell! This row shows the constraints for each column.");
    enUS.put(TEMPLATE_MARKER_MOCK_COMMENT, "Don't modify this cell! This line is an example of data.");
    enUS.put(TEMPLATE_HINT_REQUIRED_VALUE, "Required value");
    enUS.put(TEMPLATE_HINT_MAX_LENGTH, "Limit the maximum length to {} characters.");
    enUS.put(TEMPLATE_HINT_MIN_LENGTH, "Limit the minimum length to {} characters.");
    enUS.put(TEMPLATE_HINT_MAX_NUMBER, "The maximum number of acceptances is {}.");
    enUS.put(TEMPLATE_HINT_MIN_NUMBER, "The minimum number of acceptances is {}.");
    enUS.put(TEMPLATE_HINT_DEPENDS_ON, "The value is required, when columns [{}] have values.");
    enUS.put(TEMPLATE_HINT_OPTIONS, "Allowed values: ");
    enUS.put(TEMPLATE_CONSTRAINT_BOX_TITLE, "Error");
    enUS.put(TEMPLATE_CONSTRAINT_BOX_MESSAGE, "The content you entered does not comply with the specified restrictions. Please check the annotation in the HINT row (in blue).");

    Map<String, String> zhCN = Maps.newHashMap();
    zhCN.put(TEMPLATE_MARKER_CODE_COMMENT, "不要修改这个单元格！这会影响数据的正常导入。");
    zhCN.put(TEMPLATE_MARKER_HINT_COMMENT, "不要修改这个单元格！这一行显示了每一列的约束条件。");
    zhCN.put(TEMPLATE_MARKER_MOCK_COMMENT, "不要修改这个单元格！这一行展示了示例数据。");
    zhCN.put(TEMPLATE_HINT_REQUIRED_VALUE, "必填");
    zhCN.put(TEMPLATE_HINT_MAX_LENGTH, "限制最大长度为 {}");
    zhCN.put(TEMPLATE_HINT_MIN_LENGTH, "限制最小长度为 {}");
    zhCN.put(TEMPLATE_HINT_MAX_NUMBER, "最大数值为 {}");
    zhCN.put(TEMPLATE_HINT_MIN_NUMBER, "最小数值为 {}");
    zhCN.put(TEMPLATE_HINT_DEPENDS_ON, "一行中，当这些列 [{}] 有值时，本列为必填");
    zhCN.put(TEMPLATE_HINT_OPTIONS, "允许的值: ");
    zhCN.put(TEMPLATE_CONSTRAINT_BOX_TITLE, "错误提示");
    zhCN.put(TEMPLATE_CONSTRAINT_BOX_MESSAGE, "您输入的内容，不符合限制条件。请查看HINT行（蓝色）的批注。");

    super.putAll("en-US", enUS);
    super.putAll("zh-CN", zhCN);
  }

  @Override
  public String getText(String locale, String key) {
    String text = coreStore.getText(locale, key);
    if (Strings.isBlank(text) || text.equals(key)) {
      text = super.getText(locale, key);
    }
    return text;
  }

  @Override
  public String getText(String locale, String key, Object... args) {
    String text = coreStore.getText(locale, key, args);
    if (Strings.isBlank(text) || text.equals(key)) {
      text = super.getText(locale, key, args);
    }
    return text;
  }

  @Override
  public String getText(String locale, String key, List<Object> args) {
    String text = coreStore.getText(locale, key, args);
    if (Strings.isBlank(text) || text.equals(key)) {
      text = super.getText(locale, key, args);
    }
    return text;
  }
}
