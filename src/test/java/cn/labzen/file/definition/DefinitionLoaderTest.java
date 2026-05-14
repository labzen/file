package cn.labzen.file.definition;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.column.TableColumn;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConfigurationLoader 测试类
 * <p>
 * 测试配置加载、合并和注册功能
 *
 * @author labzen
 */
@DisplayName("配置加载器测试")
class DefinitionLoaderTest {

  private DefinitionLoader loader;

  @BeforeEach
  void setUp() {
    // 清理之前的注册数据
    DefinitionRegistry.clear();

    // 创建配置加载器
    // dataLocationPattern: 匹配 export-schema 目录下所有 yml 文件（排除全局配置）
    // globalLocation: 全局配置文件路径
    loader = new DefinitionLoader(
      "classpath*:data-export/**/*.yml",
      "classpath*:global/__global__.yml"
    );
  }

  @AfterEach
  void tearDown() {
    // 测试后清理
    DefinitionRegistry.clear();
  }

  @Test
  @DisplayName("测试配置加载和注册")
  void testLoadAndRegister() {
    // 执行加载
    loader.load();

    // 验证配置已注册
    assertFalse(DefinitionRegistry.isEmpty());
    assertTrue(DefinitionRegistry.contains("Property"));
  }

  @Test
  @DisplayName("测试从 Registry 获取配置")
  void testGetConfiguration() {
    // 执行加载
    loader.load();

    // 从 Registry 获取配置
    Optional<DataDefinition> configOpt = DefinitionRegistry.get("Property");
    assertTrue(configOpt.isPresent());

    DataDefinition config = configOpt.get();
    assertEquals("property", config.getFilename());
    assertEquals("系统属性", config.getTitle());
  }

  @Test
  @DisplayName("测试全局配置合并 - 表头样式")
  void testGlobalHeaderStyleMerge() {
    // 执行加载
    loader.load();

    // 验证全局表头样式已合并
    Optional<DataDefinition> configOpt = DefinitionRegistry.get("Property");
    assertTrue(configOpt.isPresent());

    DataDefinition config = configOpt.get();
    assertNotNull(config.getHeaderStyle());

    // Property.yml 中定义了 header-style，应该覆盖全局配置的 header
    // 验证合并后的样式
    assertNotNull(config.getHeaderStyle().getAlign());
    assertNotNull(config.getHeaderStyle().getBackground());
  }

  @Test
  @DisplayName("测试全局配置合并 - 列默认值")
  void testGlobalColumnDefaultsMerge() {
    // 执行加载
    loader.load();

    // 验证列默认值已从全局配置合并
    Optional<DataDefinition> configOpt = DefinitionRegistry.get("Property");
    assertTrue(configOpt.isPresent());

    DataDefinition config = configOpt.get();
    TableColumn nameColumn = config.getColumns().get("name");

    assertNotNull(nameColumn);

    // Property.yml 中 name 列定义了 when-null="无名氏"
    // 如果配置中没有定义，会使用全局配置的 "-"
    // 这里应该使用 Property.yml 中的值
    assertEquals("无名氏", nameColumn.getWhenNull());
  }

  @Test
  @DisplayName("测试列样式解析 - 仅指定 align")
  void testColumnStyleAlignOnly() {
    // 执行加载
    loader.load();

    Optional<DataDefinition> configOpt = DefinitionRegistry.get("Property");
    assertTrue(configOpt.isPresent());

    DataDefinition config = configOpt.get();
    TableColumn nameColumn = config.getColumns().get("name");

    assertNotNull(nameColumn);
    // Property.yml 中 name 列只有 style.align = CENTER
    // 此时应该能正确解析 style 对象（包含默认值）
    assertNotNull(nameColumn.getStyle(), "列样式不应为 null");
    assertEquals("CENTER", nameColumn.getStyle().getAlign().name(), "align 应该为 CENTER");
  }
}
