package cn.labzen.file.definition;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.GlobalDefinition;
import cn.labzen.file.definition.bean.column.GlobalColumn;
import cn.labzen.file.definition.bean.column.TableColumn;
import cn.labzen.file.definition.bean.style.Border;
import cn.labzen.file.definition.bean.style.Font;
import cn.labzen.file.definition.bean.style.Style;
import cn.labzen.file.definition.bean.table.HeaderBuilder;
import cn.labzen.file.definition.bean.table.HeaderStructure;
import cn.labzen.file.exception.DefinitionLoaderException;
import cn.labzen.tool.util.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.labzen.file.definition.bean.column.TableColumn.HEADER_LEVEL_SEPARATOR;

/**
 * 数据导出定义配置加载器
 * <p>
 * 加载并注册所有数据导出配置文件：
 * <ol>
 *   <li>先加载全局配置（通过构造函数传入的全局配置文件路径），作为样式默认值</li>
 *   <li>加载所有其他的表数据导出定义yml文件（通过构造函数传入的配置目录）</li>
 *   <li>将全局配置与单独配置合并（单独配置中的值会覆盖全局配置）</li>
 *   <li>校验配置有效性</li>
 *   <li>注册到 DefinitionRegistry 中</li>
 * </ol>
 *
 * @author labzen
 * @see DefinitionRegistry
 * @see GlobalDefinition
 * @see DataDefinition
 */
@Slf4j
public class DefinitionLoader {

  private final String dataLocationPattern;
  private final String globalLocation;

  private final Yaml globalYaml;
  private final Yaml dataYaml;
  private final PathMatchingResourcePatternResolver resourcePatternResolver =
    new PathMatchingResourcePatternResolver(ApplicationContext.class.getClassLoader());

  private String globalDefinitionUrl;

  /**
   * 创建配置加载器
   *
   * @param dataLocationPattern 数据导出配置文件存放目录路径Pattern
   * @param globalLocation      全局配置文件位置
   */
  public DefinitionLoader(String dataLocationPattern, String globalLocation) {
    this.dataLocationPattern = dataLocationPattern;
    this.globalLocation = globalLocation;

    this.globalYaml = createYaml(GlobalDefinition.class);
    this.dataYaml = createYaml(DataDefinition.class);
  }

  /**
   * 创建 YAML 解析器
   */
  private Yaml createYaml(Class<?> clazz) {
    LoaderOptions loaderOptions = new LoaderOptions();
    loaderOptions.setAllowDuplicateKeys(false);
    loaderOptions.setEnumCaseSensitive(false);

    Constructor constructor = new Constructor(clazz, loaderOptions);
    PropertyUtils propertyUtils = new PropertyUtils() {
      @Override
      public Property getProperty(Class<?> type, String name) {
        String convertedName = Strings.camelCase(name);
        return super.getProperty(type, convertedName);
      }
    };
    propertyUtils.setSkipMissingProperties(true);
    constructor.setPropertyUtils(propertyUtils);

    return new Yaml(constructor);
  }

  /**
   * 加载所有配置并注册到 Registry
   */
  public void load() {
    // 1. 加载全局配置
    GlobalDefinition globalDefinition = loadGlobalDefinition();

    // 2. 加载所有表数据导出配置
    Map<String, DataDefinition> dataDefinitionMap = loadDataDefinitions();
    if (dataDefinitionMap == null) {
      return;
    }

    // 3. 合并、校验、排序并注册
    dataDefinitionMap.forEach((key, value) -> {
      // 设置默认 index（按 yml 中出现的顺序）
      assignDefaultIndices(value);
      // 合并全局配置
      DataDefinition mergedDefinition = mergeDefinition(globalDefinition, value);

      List<TableColumn> columns = mergedDefinition.getColumns().values().stream().toList();
      HeaderStructure headerStructure = HeaderBuilder.build(columns);
      mergedDefinition.setHeaders(headerStructure);

      // 注册配置
      DefinitionRegistry.register(key, mergedDefinition);
    });
  }

  /**
   * 加载全局配置
   */
  private GlobalDefinition loadGlobalDefinition() {
    try {
      Resource globalResource;
      Resource[] resources = resourcePatternResolver.getResources(globalLocation);
      if (resources.length > 1) {
        globalResource = resources[0];
        logger.warn("存在多个全局数据导出配置，默认将使用：{}", globalResource.getURI().toString());
      } else if (resources.length == 1) {
        globalResource = resources[0];
      } else {
        throw new DefinitionLoaderException("找不到全局数据导出配置文件，请检查路径 labzen.yml 中的 global-definition-name 配置");
      }

      InputStream inputStream = globalResource.getInputStream();
      globalDefinitionUrl = globalResource.getURL().toExternalForm();

      GlobalDefinition definition = globalYaml.load(inputStream);
      validateGlobalDefinition(definition);

      return definition;
    } catch (Exception e) {
      logger.atWarn().setCause(e).log("数据导出的全局配置加载失败，将使用默认配置，from: {}", globalLocation);
      return new GlobalDefinition();
    }
  }

  private DataDefinition loadDataDefinition(@Nonnull Resource resource) {
    String filename = resource.getFilename();
    try {
      InputStream inputStream = resource.getInputStream();

      DataDefinition definition = dataYaml.load(inputStream);
      validateDataDefinition(definition);

      assert filename != null;
      int extensionIndex = filename.lastIndexOf(".");
      String domainName = filename.substring(0, extensionIndex);
      definition.setDomainName(domainName);

      return definition;
    } catch (Exception e) {
      logger.warn("数据导出配置加载失败，from: {}", filename);
      return null;
    }
  }

  /**
   * 加载所有表数据导出配置
   */
  private Map<String, DataDefinition> loadDataDefinitions() {
    try {
      Resource[] resources = resourcePatternResolver.getResources(dataLocationPattern);
      return Arrays.stream(resources)
        .filter(resource -> {
          // 判断资源对象是已经读取过的全局定义文件，过滤掉
          try {
            String url = resource.getURL().toExternalForm();
            return !globalDefinitionUrl.contains(url);
          } catch (IOException e) {
            return true;
          }
        })
        .map(this::loadDataDefinition)
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(DataDefinition::getDomainName, definition -> definition));
    } catch (IOException e) {
      logger.atWarn().setCause(e).log("数据导出文件配置的目录扫描加载失败，from: {}", dataLocationPattern);
      return null;
    }
  }

  /**
   * 为没有配置 index 的列设置默认 index
   * <p>
   * 按 yml 文件中出现的顺序分配 index（从0开始）
   */
  private void assignDefaultIndices(DataDefinition definition) {
    Map<String, TableColumn> columns = definition.getColumns();
    if (columns == null || columns.isEmpty()) {
      return;
    }

    int index = 0;
    for (TableColumn column : columns.values()) {
      if (column.getIndex() == null) {
        column.setIndex(index);
      }
      index++;
    }
  }

//  /**
//   * 按 index 对列进行排序
//   * <p>
//   * 使用 LinkedHashMap 保持排序后的顺序
//   */
//  private void sortColumnsByIndex(DataDefinition config) {
//    Map<String, TableColumn> columns = config.getColumns();
//    if (columns == null || columns.isEmpty()) {
//      return;
//    }
//
//    // 按 index 值排序并重新放入 LinkedHashMap
//    LinkedHashMap<String, TableColumn> sortedColumns = columns.entrySet().stream()
//      .sorted(Comparator.comparingInt(e -> e.getValue().getIndex() != null ? e.getValue().getIndex() : Integer.MAX_VALUE))
//      .collect(Collectors.toMap(
//        Map.Entry::getKey,
//        Map.Entry::getValue,
//        (e1, e2) -> e1,
//        LinkedHashMap::new
//      ));
//
//    config.setColumns(sortedColumns);
//  }

  /**
   * 合并全局配置与单独配置
   * <p>
   * 覆盖优先级（从低到高）：
   * 1. 全局配置（__global__.yml）
   * 2. 文件作用域配置（header-style, column-style）
   * 3. 列定义配置（columns.*）
   */
  private DataDefinition mergeDefinition(GlobalDefinition globalDefinition, DataDefinition dataDefinition) {
    if (globalDefinition == null || dataDefinition == null) {
      return dataDefinition;
    }

    // 合并全局表头样式
    if (dataDefinition.getHeaderStyle() == null && globalDefinition.getHeader() != null) {
      dataDefinition.setHeaderStyle(cloneStyle(globalDefinition.getHeader()));
    } else if (globalDefinition.getHeader() != null) {
      mergeStyle(globalDefinition.getHeader(), dataDefinition.getHeaderStyle());
    }

    // 合并全局列样式（body -> columnStyle）
    Style fileScopedColumnStyle = dataDefinition.getColumnStyle();
    GlobalColumn globalColumn = globalDefinition.getColumn();
    if (globalColumn != null && globalColumn.getStyle() != null) {
      if (fileScopedColumnStyle == null) {
        dataDefinition.setColumnStyle(cloneStyle(globalColumn.getStyle()));
        fileScopedColumnStyle = dataDefinition.getColumnStyle();
      } else {
        mergeStyle(globalColumn.getStyle(), fileScopedColumnStyle);
      }
    }

    // 合并列定义
    Map<String, TableColumn> columns = dataDefinition.getColumns();
    if (columns != null) {
      for (TableColumn column : columns.values()) {
        mergeColumnDefinition(globalColumn, fileScopedColumnStyle, column);
      }
    }

    return dataDefinition;
  }

  /**
   * 合并列定义
   */
  private void mergeColumnDefinition(GlobalColumn globalColumn, Style fileScopedColumnStyle, TableColumn column) {
    // 列宽
    if (column.getWidth() == null) {
      column.setWidth(globalColumn.getWidth());
    }

    // whenNull
    if (column.getWhenNull() == null) {
      column.setWhenNull(globalColumn.getWhenNull());
    }

    // whenBlank
    if (column.getWhenBlank() == null) {
      column.setWhenBlank(globalColumn.getWhenBlank());
    }

    // prefix
    if (column.getPrefix() == null) {
      column.setPrefix(globalColumn.getPrefix());
    }

    // suffix
    if (column.getSuffix() == null) {
      column.setSuffix(globalColumn.getSuffix());
    }

    // 列样式覆盖
    if (column.getStyle() == null) {
      column.setStyle(cloneStyle(fileScopedColumnStyle));
    } else {
      mergeStyle(fileScopedColumnStyle, column.getStyle());
    }
  }

  /**
   * 合并样式
   */
  private void mergeStyle(Style source, Style target) {
    if (source == null || target == null) {
      return;
    }

    if (target.getAlign() == null) {
      target.setAlign(source.getAlign());
    }
    if (target.getBackground() == null) {
      target.setBackground(source.getBackground());
    }
    if (target.getFont() == null) {
      target.setFont(cloneFont(source.getFont()));
    } else if (source.getFont() != null) {
      mergeFont(source.getFont(), target.getFont());
    }
    if (target.getBorder() == null) {
      target.setBorder(cloneBorder(source.getBorder()));
    } else if (source.getBorder() != null) {
      mergeBorder(source.getBorder(), target.getBorder());
    }
    if (target.getWrapped() == null) {
      target.setWrapped(source.getWrapped());
    }
  }

  /**
   * 合并字体样式
   */
  private void mergeFont(Font source, Font target) {
    if (source == null || target == null) {
      return;
    }

    if (target.getFamily() == null) {
      target.setFamily(source.getFamily());
    }
    if (target.getSize() == null) {
      target.setSize(source.getSize());
    }
    if (target.getColor() == null) {
      target.setColor(source.getColor());
    }
    if (target.getBold() == null) {
      target.setBold(source.getBold());
    }
    if (target.getItalic() == null) {
      target.setItalic(source.getItalic());
    }
  }

  /**
   * 合并边框样式
   */
  private void mergeBorder(Border source, Border target) {
    if (source == null || target == null) {
      return;
    }

    if (target.getColor() == null) {
      target.setColor(source.getColor());
    }
    if (target.getWidth() == null) {
      target.setWidth(source.getWidth());
    }
  }

  /**
   * 深度拷贝样式
   */
  private Style cloneStyle(Style source) {
    if (source == null) {
      return new Style();
    }

    Style clone = new Style();
    clone.setAlign(source.getAlign());
    clone.setBackground(source.getBackground());
    clone.setWrapped(source.getWrapped());
    clone.setFont(cloneFont(source.getFont()));
    clone.setBorder(cloneBorder(source.getBorder()));
    return clone;
  }

  /**
   * 深度拷贝字体
   */
  private Font cloneFont(Font source) {
    if (source == null) {
      return new Font();
    }

    Font clone = new Font();
    clone.setFamily(source.getFamily());
    clone.setSize(source.getSize());
    clone.setColor(source.getColor());
    clone.setBold(source.getBold());
    clone.setItalic(source.getItalic());
    return clone;
  }

  /**
   * 深度拷贝边框
   */
  private Border cloneBorder(Border source) {
    if (source == null) {
      return new Border();
    }

    Border clone = new Border();
    clone.setColor(source.getColor());
    clone.setWidth(source.getWidth());
    return clone;
  }

  /**
   * 校验配置
   */
  private void validateDataDefinition(DataDefinition definition) {
    if (Strings.isBlank(definition.getFilename())) {
      throw new DefinitionLoaderException("{} 定义文件中的 filename 不能为空", definition.getDomainName());
    }

    if (Strings.isBlank(definition.getTitle())) {
      throw new DefinitionLoaderException("{} 定义文件中的 title 不能为空", definition.getDomainName());
    }

    if (definition.getColumns() == null || definition.getColumns().isEmpty()) {
      throw new DefinitionLoaderException("{} 定义文件中的 columns 不能为空", definition.getDomainName());
    }

    // 校验列定义
    for (Map.Entry<String, TableColumn> entry : definition.getColumns().entrySet()) {
      String fieldName = entry.getKey();
      TableColumn column = entry.getValue();

      if (Strings.isBlank(column.getHeader())) {
        throw new DefinitionLoaderException("{} 定义文件中的列 [{}] 的 header 不能为空", definition.getDomainName(), fieldName);
      }
      int headerSeparatorTimes = Strings.times(column.getHeader(), HEADER_LEVEL_SEPARATOR);
      if (headerSeparatorTimes > 1) {
        throw new DefinitionLoaderException("{} 定义文件中的列 [{}] 的 header 暂时只能支持最多2级表头", definition.getDomainName(), fieldName);
      }
    }
  }

  /**
   * 校验全局配置
   */
  private void validateGlobalDefinition(GlobalDefinition config) {
    // 全局配置校验相对宽松，仅校验基本格式
  }

  /**
   * 将 kebab-case 转换为 camelCase
   */
  private String convertKebabToCamel(String kebab) {
    if (kebab == null || !kebab.contains("-")) {
      return kebab;
    }

    StringBuilder result = new StringBuilder();
    boolean capitalizeNext = false;

    for (char c : kebab.toCharArray()) {
      if (c == '-') {
        capitalizeNext = true;
      } else if (capitalizeNext) {
        result.append(Character.toUpperCase(c));
        capitalizeNext = false;
      } else {
        result.append(c);
      }
    }

    return result.toString();
  }
}
