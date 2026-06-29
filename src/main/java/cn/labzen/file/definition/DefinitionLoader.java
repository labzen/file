package cn.labzen.file.definition;

import cn.labzen.file.converter.executor.ConverterInstanceSupplier;
import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.GlobalDefinition;
import cn.labzen.file.definition.bean.column.*;
import cn.labzen.file.definition.bean.head.HeaderBuilder;
import cn.labzen.file.definition.bean.style.Font;
import cn.labzen.file.definition.bean.style.Style;
import cn.labzen.file.definition.resource.Resource;
import cn.labzen.file.definition.resource.ResourcePatternResolver;
import cn.labzen.file.exception.DefinitionLoaderException;
import cn.labzen.tool.util.Strings;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 数据导入/出定义配置加载器
 * <p>
 * 加载并注册所有数据导入/出配置文件：
 * <ol>
 *   <li>先加载全局配置（通过构造函数传入的全局配置文件路径），作为样式默认值</li>
 *   <li>加载所有其他的表数据导入/出定义yml文件（通过构造函数传入的配置目录）</li>
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

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final String dataLocationPattern;
  private final String globalLocation;

  private final Yaml globalYaml;
  private final Yaml dataYaml;
  private final ResourcePatternResolver resourcePatternResolver = new ResourcePatternResolver();

  private String globalDefinitionUrl;

  /**
   * 创建配置加载器
   *
   * @param dataLocationPattern 数据导入/出配置文件存放目录路径Pattern
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
    logger.info("开始加载[数据导入/出schema定义文件]...");

    ConverterInstanceSupplier.init();

    // 1. 加载全局配置
    GlobalDefinition globalDefinition = loadGlobalDefinition();

    // 2. 加载所有表数据导入/出配置
    Map<String, DataDefinition> dataDefinitionMap = loadDataDefinitions();
    if (dataDefinitionMap == null) {
      return;
    }

    // 3. 合并、校验、排序并注册
    dataDefinitionMap.forEach((key, value) -> {
      // 合并全局配置
      DataDefinition definition = mergeDefinition(globalDefinition, value);

      // 注册配置
      DefinitionRegistry.register(key, definition);
    });

    logger.info("[数据导入/出schema定义文件]加载完成，共加载{}个配置文件", DefinitionRegistry.size());
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
        logger.warn("存在多个全局数据导入/出配置，默认将使用：{}", globalResource.getURI());
      } else if (resources.length == 1) {
        globalResource = resources[0];
        logger.debug("加载全局[数据导入/出schema定义文件]:{}", globalResource.getURI());
      } else {
        throw new DefinitionLoaderException("找不到全局数据导入/出配置文件，请检查路径 labzen.yml 中的 global-definition-name 配置");
      }

      InputStream inputStream = globalResource.getInputStream();
      globalDefinitionUrl = globalResource.getURL().toExternalForm();

      GlobalDefinition definition = globalYaml.load(inputStream);
      validateGlobalDefinition(definition);

      return definition;
    } catch (Exception e) {
      logger.atWarn().setCause(e).log("数据导入/出的全局配置加载失败，将使用默认配置，from: {}", globalLocation);
      return new GlobalDefinition();
    }
  }

  private DataDefinition loadDataDefinition(@Nonnull Resource resource) {
    String filename = resource.getFilename();
    try {
      logger.debug("加载[数据导入/出schema定义文件]:{}", resource.getURI());
      InputStream inputStream = resource.getInputStream();

      DataDefinition definition = dataYaml.load(inputStream);
      validateDataDefinition(definition);

      assert filename != null;
      int extensionIndex = filename.lastIndexOf(".");
      String domainName = filename.substring(0, extensionIndex);
      definition.setName(domainName);

      // 加载文件对应的domain类信息
      loadDomainClass(definition);

      // 加载同名 .mock.json 文件的 mock 数据
      List<Map<String, String>> mockData = loadMockData(resource, definition);
      definition.setMockData(mockData);

      return definition;
    } catch (Exception e) {
      logger.atWarn().setCause(e).log("数据导入/出配置加载失败，from: {}", filename);
      return null;
    }
  }

  private void loadDomainClass(DataDefinition definition) {
    String domain = definition.getDomain();
    Class<?> domainClass;
    try {
      domainClass = Class.forName(domain);
      definition.setDomainClass(domainClass);
    } catch (ClassNotFoundException e) {
      throw new DefinitionLoaderException("配置文件对应的domain类不存在，from: " + definition.getDomain());
    }

    definition.getColumns().forEach((fieldName, column) -> {
      try {
        Field declaredField = domainClass.getDeclaredField(fieldName);
        Class<?> type = declaredField.getType();

        column.setFieldName(fieldName);
        column.setFieldType(type);
        column.setValidated(true);
      } catch (Exception e) {
        throw new DefinitionLoaderException("配置文件中定义的字段 [{}] 在 [] 中不存在，该字段将被忽略，无法在导入/导入/出时使用", fieldName, domainClass);
      }
    });
  }

  /**
   * 加载与 YAML 定义文件同名的 .mock.json 文件内容，作为 mock 数据
   */
  private List<Map<String, String>> loadMockData(Resource yamlResource, DataDefinition definition) {
    try {
      String url = yamlResource.getURL().toExternalForm();
      String mockUrl;
      if (url.endsWith(".yml")) {
        mockUrl = url.substring(0, url.length() - 4) + ".mock.json";
      } else if (url.endsWith(".yaml")) {
        mockUrl = url.substring(0, url.length() - 5) + ".mock.json";
      } else {
        return null;
      }

      Resource mockResource = resourcePatternResolver.getResource(mockUrl);
      if (mockResource.exists()) {
        try (InputStream mockInputStream = mockResource.getInputStream()) {
          return OBJECT_MAPPER.readValue(mockInputStream, new TypeReference<>() {
          });
        }
      }
    } catch (Exception e) {
      logger.warn("Mock数据文件加载失败或不存在，domain: {}", definition.getName());
    }
    return null;
  }

  /**
   * 加载所有表数据导入/出配置
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
        .collect(Collectors.toMap(DataDefinition::getName, definition -> definition));
    } catch (IOException e) {
      logger.atWarn().setCause(e).log("数据导入/出文件配置的目录扫描加载失败，from: {}", dataLocationPattern);
      return null;
    }
  }

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

    // 合并全局表头样式 -> 文件作用域表头样式
    if (globalDefinition.getExportingHeaderStyle() != null) {
      if (dataDefinition.getExportingHeaderStyle() == null) {
        dataDefinition.setExportingHeaderStyle(cloneStyle(globalDefinition.getExportingHeaderStyle()));
      } else {
        mergeStyle(globalDefinition.getExportingHeaderStyle(), dataDefinition.getExportingHeaderStyle());
      }
    }
    // 合并全局单元格样式 -> 文件作用域单元格样式
    if (globalDefinition.getExportingColumnStyle() != null) {
      if (dataDefinition.getExportingColumnStyle() == null) {
        dataDefinition.setExportingColumnStyle(cloneStyle(globalDefinition.getExportingColumnStyle()));
      } else {
        mergeStyle(globalDefinition.getExportingColumnStyle(), dataDefinition.getExportingColumnStyle());
      }
    }

    // 合并全局导入/出配置 -> 文件作用域导入/出配置
    if (globalDefinition.getExporting() != null) {
      if (dataDefinition.getExporting() == null) {
        dataDefinition.setExporting(cloneGlobalExporting(globalDefinition.getExporting()));
      } else {
        mergeGlobalExporting(globalDefinition.getExporting(), dataDefinition.getExporting());
      }
    }
    // 合并全局导入配置 -> 文件作用域导入配置
    if (globalDefinition.getImporting() != null) {
      if (dataDefinition.getImporting() == null) {
        dataDefinition.setImporting(cloneGlobalImporting(globalDefinition.getImporting()));
      } else {
        mergeGlobalImporting(globalDefinition.getImporting(), dataDefinition.getImporting());
      }
    }

    // 合并全局默认列宽 -> 文件作用域列宽
    if (globalDefinition.getWidth() != null && dataDefinition.getWidth() == null) {
      dataDefinition.setWidth(globalDefinition.getWidth());
    }

    // 合并文件作用域表头样式 -> 列作用域样式
    Map<String, Column> columns = dataDefinition.getColumns();
    if (columns != null) {
      for (Column column : columns.values()) {
        mergeColumnDefinition(dataDefinition, column);
      }
    }

    return dataDefinition;
  }

  /**
   * 合并列定义
   * <p>
   * 合并优先级（从低到高）：全局列默认 → 文件作用域导入/出/导入默认 → 列定义配置
   */
  private void mergeColumnDefinition(DataDefinition dataDefinition, Column column) {
    // 列宽：文件作用域默认列宽 → Column.width（列级别显式配置覆盖文件级别默认）
    if (column.getWidth() == null && dataDefinition.getWidth() != null) {
      column.setWidth(dataDefinition.getWidth());
    }

    // 导入/出配置覆盖
    if (dataDefinition.getExporting() != null) {
      if (column.getExporting() == null) {
        column.setExporting(new Exporting());
      }
      mergeGlobalExporting(dataDefinition.getExporting(), column.getExporting());
    }
    if (column.getExporting() == null) {
      column.setExporting(new Exporting());
    }

    // 导入配置覆盖
    if (dataDefinition.getImporting() != null) {
      if (column.getImporting() == null) {
        column.setImporting(new Importing());
      }
      mergeGlobalImporting(dataDefinition.getImporting(), column.getImporting());
    }
    if (column.getImporting() == null) {
      column.setImporting(new Importing());
    }

    // 列样式覆盖
    if (dataDefinition.getExportingColumnStyle() != null) {
      if (column.getExporting().getStyle() == null) {
        column.getExporting().setStyle(cloneStyle(dataDefinition.getExportingColumnStyle()));
      } else {
        mergeStyle(dataDefinition.getExportingColumnStyle(), column.getExporting().getStyle());
      }
    }
    if (column.getExporting().getStyle() == null) {
      column.getExporting().setStyle(new Style());
    }

    // mapping映射覆盖
    if (column.getExporting().getMapping() == null) {
      column.getExporting().setMapping(column.getMapping());
    }
    if (column.getImporting().getMapping() == null) {
      column.getImporting().setMapping(column.getMapping());
    }

    // enumerable枚举覆盖
    if (column.getExporting().getEnumerable() == null) {
      column.getExporting().setEnumerable(column.getEnumerable());
    }
    if (column.getImporting().getEnumerable() == null) {
      column.getImporting().setEnumerable(column.getEnumerable());
    }
  }

  /**
   * 深度拷贝全局导入/出
   */
  private GlobalExporting cloneGlobalExporting(GlobalExporting source) {
    if (source == null) {
      return new GlobalExporting();
    }

    GlobalExporting clone = new GlobalExporting();
    clone.setWhenNull(source.getWhenNull());
    clone.setWhenBlank(source.getWhenBlank());
    return clone;
  }

  /**
   * 合并全局导入/出
   */
  private void mergeGlobalExporting(GlobalExporting source, GlobalExporting target) {
    if (source == null || target == null) {
      return;
    }

    if (target.getWhenNull() == null) {
      target.setWhenNull(source.getWhenNull());
    }
    if (target.getWhenBlank() == null) {
      target.setWhenBlank(source.getWhenBlank());
    }
  }

  /**
   * 深度拷贝全局导入
   */
  private GlobalImporting cloneGlobalImporting(GlobalImporting source) {
    if (source == null) {
      return new GlobalImporting();
    }

    GlobalImporting clone = new GlobalImporting();
    clone.setRequire(source.getRequire());
    clone.setCleansing(source.getCleansing());
    return clone;
  }

  /**
   * 合并全局导入
   */
  private void mergeGlobalImporting(GlobalImporting source, GlobalImporting target) {
    if (source == null || target == null) {
      return;
    }

    if (target.getRequire() == null) {
      target.setRequire(source.getRequire());
    }
    if (target.getCleansing() == null) {
      target.setCleansing(source.getCleansing());
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
   * 校验配置
   */
  private void validateDataDefinition(DataDefinition definition) {
    if (Strings.isBlank(definition.getExportFilename())) {
      throw new DefinitionLoaderException("{} 定义文件中的 filename 不能为空", definition.getName());
    }

    if (Strings.isBlank(definition.getExportTitle())) {
      throw new DefinitionLoaderException("{} 定义文件中的 title 不能为空", definition.getName());
    }

    if (definition.getColumns() == null || definition.getColumns().isEmpty()) {
      throw new DefinitionLoaderException("{} 定义文件中的 columns 不能为空", definition.getName());
    }

    // 校验列定义
    for (Map.Entry<String, Column> entry : definition.getColumns().entrySet()) {
      String fieldName = entry.getKey();
      Column column = entry.getValue();

      if (Strings.isBlank(column.getHeader())) {
        throw new DefinitionLoaderException("{} 定义文件中的列 [{}] 的 header 不能为空", definition.getName(), fieldName);
      }
      if (!HeaderBuilder.isValidHeaderLevel(column.getHeader())) {
        throw new DefinitionLoaderException("{} 定义文件中的列 [{}] 的 header 暂时无法支持定义的多级表头", definition.getName(), fieldName);
      }
    }
  }

  /**
   * 校验全局配置
   */
  private void validateGlobalDefinition(GlobalDefinition config) {
    // 全局配置校验相对宽松，仅校验基本格式
  }
}
