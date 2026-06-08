package cn.labzen.file.cleanser;

import cn.labzen.file.annotation.DataCleanser;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 清理器链执行器
 * <p>
 * 按 priority 顺序链式执行 Cleanser，前一个输出作为后一个输入。
 *
 * @author labzen
 */
@Slf4j
public class ChainableCleanserExecutor {

  private static final Map<String, CleanserInstance> CLEANSER_INSTANCES = Maps.newHashMap();

  static {
    ServiceLoader.load(Cleanser.class).forEach(cleanser -> {
      Class<?> type = cleanser.getClass();
      if (!type.isAnnotationPresent(DataCleanser.class)) {
        return;
      }
      DataCleanser annotation = type.getAnnotation(DataCleanser.class);
      CLEANSER_INSTANCES.put(annotation.name(),
        new CleanserInstance(annotation.name(), annotation.priority(), cleanser));
    });
  }

  private final List<Cleanser> chain;

  public ChainableCleanserExecutor(@Nonnull List<String> cleanserNames) {
    this.chain = Lists.newArrayList();
    List<CleanserInstance> instances = new ArrayList<>();
    for (String name : cleanserNames) {
      CleanserInstance instance = CLEANSER_INSTANCES.get(name);
      if (instance == null) {
        logger.error("不存在的清理器: {}", name);
        continue;
      }
      instances.add(instance);
    }
    instances.sort(Comparator.comparingInt(CleanserInstance::priority));
    instances.forEach(i -> chain.add(i.cleanser()));
  }

  /**
   * 执行清理链
   *
   * @param input 原始输入
   * @return 清理后的字符串
   */
  public String execute(String input) {
    if (input == null) {
      return null;
    }
    String result = input;
    for (Cleanser cleanser : chain) {
      result = cleanser.cleanse(result);
      if (result == null) {
        break;
      }
    }
    return result;
  }

  /**
   * 获取所有可用的清理器名称
   */
  public static Set<String> availableCleanserNames() {
    return CLEANSER_INSTANCES.keySet();
  }

  record CleanserInstance(String name, int priority, Cleanser cleanser) {
  }
}
