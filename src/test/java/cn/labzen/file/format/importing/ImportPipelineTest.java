package cn.labzen.file.format.importing;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.bean.scoped.TableImporting;
import cn.labzen.file.definition.bean.column.Importing;
import cn.labzen.file.definition.bean.column.Column;
import cn.labzen.file.format.core.reader.ImportFailure;
import cn.labzen.file.format.core.reader.ImportPhase;
import cn.labzen.file.format.core.reader.ImportPipeline;
import cn.labzen.file.format.core.reader.ImportResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ImportPipeline 单元测试
 */
class ImportPipelineTest {

  private DataDefinition definition;

  // 简单的测试Bean
  public static class TestBean {
    private String name;
    private Integer age;
    private Boolean active;

    public TestBean() {}

    public void setName(String name) { this.name = name; }
    public void setAge(Integer age) { this.age = age; }
    public void setActive(Boolean active) { this.active = active; }

    public String getName() { return name; }
    public Integer getAge() { return age; }
    public Boolean getActive() { return active; }
  }

  @BeforeEach
  void setUp() {
    definition = new DataDefinition();
    definition.setDomainName("TestBean");

    Map<String, Column> columns = new LinkedHashMap<>();

    // name 列：必填，最大50字
    Column nameColumn = new Column();
    nameColumn.setHeader("名称");
    Importing nameImporting = new Importing();
    nameImporting.setRequired(true);
    nameImporting.setMaxLength(50);
    nameColumn.setImporting(nameImporting);
    columns.put("name", nameColumn);

    // age 列：范围0~200
    Column ageColumn = new Column();
    ageColumn.setHeader("年龄");
    Importing ageImporting = new Importing();
    ageImporting.setMin("0");
    ageImporting.setMax("200");
    ageColumn.setImporting(ageImporting);
    columns.put("age", ageColumn);

    // active 列
    Column activeColumn = new Column();
    activeColumn.setHeader("是否激活");
    columns.put("active", activeColumn);

    definition.setColumns(columns);

    TableImporting importingDef = new TableImporting();
    importingDef.setCleansing(List.of("trim"));
    definition.setImporting(importingDef);
  }

  @Test
  void pipeline_normalRow_shouldSuccess() {
    ImportPipeline<TestBean> pipeline = new ImportPipeline<>(definition, TestBean.class, null);

    Map<String, String> rowData = new LinkedHashMap<>();
    rowData.put("name", "张三");
    rowData.put("age", "25");
    rowData.put("active", "true");

    pipeline.processRow(1, rowData);
    ImportResult<TestBean> result = pipeline.buildResult();

    assertEquals(1, result.getSuccessCount());
    assertEquals(0, result.getFailureCount());

    TestBean bean = result.getData().get(0);
    assertEquals("张三", bean.getName());
    assertEquals(25, bean.getAge());
    assertTrue(bean.getActive());
  }

  @Test
  void pipeline_requiredFieldMissing_shouldFail() {
    ImportPipeline<TestBean> pipeline = new ImportPipeline<>(definition, TestBean.class, null);

    Map<String, String> rowData = new LinkedHashMap<>();
    rowData.put("name", "");
    rowData.put("age", "25");

    pipeline.processRow(1, rowData);
    ImportResult<TestBean> result = pipeline.buildResult();

    assertEquals(0, result.getSuccessCount());
    assertEquals(1, result.getFailureCount());

    ImportFailure failure = result.getFailures().get(0);
    assertEquals(1, failure.getRowIndex());
    boolean hasRequiredError = failure.getErrors().stream()
      .anyMatch(e -> e.getPhase() == ImportPhase.VALIDATE && e.getFieldName().equals("name"));
    assertTrue(hasRequiredError);
  }

  @Test
  void pipeline_typeConvertFail_shouldFail() {
    ImportPipeline<TestBean> pipeline = new ImportPipeline<>(definition, TestBean.class, null);

    Map<String, String> rowData = new LinkedHashMap<>();
    rowData.put("name", "张三");
    rowData.put("age", "not-a-number");

    pipeline.processRow(1, rowData);
    ImportResult<TestBean> result = pipeline.buildResult();

    assertEquals(0, result.getSuccessCount());
    assertEquals(1, result.getFailureCount());

    ImportFailure failure = result.getFailures().get(0);
    boolean hasConvertError = failure.getErrors().stream()
      .anyMatch(e -> e.getPhase() == ImportPhase.CONVERT && e.getFieldName().equals("age"));
    assertTrue(hasConvertError);
  }

  @Test
  void pipeline_rangeViolation_shouldFail() {
    ImportPipeline<TestBean> pipeline = new ImportPipeline<>(definition, TestBean.class, null);

    Map<String, String> rowData = new LinkedHashMap<>();
    rowData.put("name", "张三");
    rowData.put("age", "300"); // 超过200

    pipeline.processRow(1, rowData);
    ImportResult<TestBean> result = pipeline.buildResult();

    assertEquals(0, result.getSuccessCount());
    assertEquals(1, result.getFailureCount());

    ImportFailure failure = result.getFailures().get(0);
    boolean hasRangeError = failure.getErrors().stream()
      .anyMatch(e -> e.getPhase() == ImportPhase.POST_VALIDATE && e.getFieldName().equals("age"));
    assertTrue(hasRangeError);
  }

  @Test
  void pipeline_multipleRows_mixedResults() {
    ImportPipeline<TestBean> pipeline = new ImportPipeline<>(definition, TestBean.class, null);

    // 行1：正常
    Map<String, String> row1 = new LinkedHashMap<>();
    row1.put("name", "张三");
    row1.put("age", "25");
    pipeline.processRow(1, row1);

    // 行2：name为空
    Map<String, String> row2 = new LinkedHashMap<>();
    row2.put("name", "");
    row2.put("age", "30");
    pipeline.processRow(2, row2);

    // 行3：正常
    Map<String, String> row3 = new LinkedHashMap<>();
    row3.put("name", "李四");
    row3.put("age", "28");
    pipeline.processRow(3, row3);

    ImportResult<TestBean> result = pipeline.buildResult();

    assertEquals(2, result.getSuccessCount());
    assertEquals(1, result.getFailureCount());
    assertEquals(3, result.getTotalRows());
  }

  @Test
  void importResult_immutable() {
    List<Object> data = new ArrayList<>();
    List<ImportFailure> failures = new ArrayList<>();
    ImportResult<Object> result = new ImportResult<>(10, 8, 2, data, failures);

    assertThrows(UnsupportedOperationException.class, () -> result.getData().add(new Object()));
    assertThrows(UnsupportedOperationException.class, () -> result.getFailures().add(null));
  }
}
