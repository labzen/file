package cn.labzen.file.format.core.reader.process;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.i18n.I18nStoreHolder;
import cn.labzen.file.i18n.I18nStoreProvider;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import static cn.labzen.file.i18n.internal.Internal18nKeys.IMPORT_CONSTRUCT_ERROR_MESSAGE;

@Getter
public class ProceedRow<T> {

  private final String sequence;
  private final boolean success;
  private final Map<String, Object> data;
  private final List<FieldError> errors;
  private final T instance;

  private static final Map<Class<?>, Constructor<?>> CONSTRUCTOR_CACHE = Maps.newConcurrentMap();
  private static final Map<String, Field> FIELD_CACHE = Maps.newConcurrentMap();

  public ProceedRow(String sequence, boolean success, Map<String, Object> data, List<FieldError> errors, DataDefinition definition) {
    this.sequence = sequence;
    this.success = success;
    this.data = data;
    this.errors = errors;

    T instance = null;
    if (success) {
      try {
        //noinspection unchecked
        instance = structure((Class<T>) definition.getDomainClass());
      } catch (Exception e) {
        if (errors == null) {
          errors = Lists.newArrayList();
        }
        I18nStoreProvider i18nStore = I18nStoreHolder.get();
        String message = i18nStore.getText("", IMPORT_CONSTRUCT_ERROR_MESSAGE);
        errors.add(new FieldError("", "", null, ImportPhase.CONSTRUCT, message));
      }
    }
    this.instance = instance;
  }

  private T structure(Class<T> type) {
    Constructor<?> constructor = CONSTRUCTOR_CACHE.computeIfAbsent(type, t -> {
      try {
        Constructor<?> declaredConstructor = t.getDeclaredConstructor();
        declaredConstructor.setAccessible(true);
        return declaredConstructor;
      } catch (NoSuchMethodException e) {
        throw new IllegalStateException("Bean[" + type.getSimpleName() + "]缺少无参构造函数", e);
      }
    });

    T instance;
    try {
      //noinspection unchecked
      instance = (T) constructor.newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }

    for (String key : data.keySet()) {
      Field field = FIELD_CACHE.computeIfAbsent(key, k -> {
        try {
          Field declaredField = type.getDeclaredField(k);
          declaredField.setAccessible(true);
          return declaredField;
        } catch (NoSuchFieldException e) {
          throw new IllegalStateException("字段[" + k + "]在Bean[" + type.getSimpleName() + "]中不存在", e);
        }
      });

      try {
        Object value = data.get(key);
        field.set(instance, value);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }

    return instance;
  }

  public ImportFailure toFailure() {
    if (success) {
      return null;
    }

    return new ImportFailure(sequence, data, errors);
  }
}
