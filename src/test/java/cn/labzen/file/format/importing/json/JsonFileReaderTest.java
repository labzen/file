//package cn.labzen.file.format.importing.json;
//
//import cn.labzen.file.definition.bean.DataDefinition;
//import cn.labzen.file.definition.bean.column.Column;
//import cn.labzen.file.format.json.JsonFileReader;
//import org.junit.jupiter.api.Test;
//
//import java.io.ByteArrayInputStream;
//import java.nio.charset.StandardCharsets;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * JsonFileReader 单元测试
// */
//class JsonFileReaderTest {
//
//  private DataDefinition createSimpleDefinition() {
//    DataDefinition definition = new DataDefinition();
//    definition.setDomainName("TestBean");
//    Map<String, Column> columns = new LinkedHashMap<>();
//    columns.put("name", new Column());
//    columns.put("age", new Column());
//    definition.setColumns(columns);
//    return definition;
//  }
//
//  @Test
//  void readJsonArray_basic() {
//    String json = """
//      [
//        {"name": "张三", "age": "25"},
//        {"name": "李四", "age": "30"}
//      ]
//      """;
//
//    JsonFileReader reader = new JsonFileReader();
//    ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
//
//    List<Map<String, String>> iterator = reader.read(createSimpleDefinition(), is);
//
//    List<Map<String, String>> rows = new ArrayList<>(iterator);
//
//    assertEquals(2, rows.size());
//    assertEquals("张三", rows.get(0).get("name"));
//    assertEquals("25", rows.get(0).get("age"));
//    assertEquals("李四", rows.get(1).get("name"));
//  }
//
//  @Test
//  void readJsonArray_empty() {
//    String json = "[]";
//
//    JsonFileReader reader = new JsonFileReader();
//    ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
//
//    List<Map<String, String>> iterator = reader.read(createSimpleDefinition(), is);
//
//    List<Map<String, String>> rows = new ArrayList<>(iterator);
//    assertTrue(rows.isEmpty());
//  }
//
//  @Test
//  void readJsonArray_nullValues() {
//    String json = """
//      [{"name": "张三", "age": null}]
//      """;
//
//    JsonFileReader reader = new JsonFileReader();
//    ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
//
//    List<Map<String, String>> iterator = reader.read(createSimpleDefinition(), is);
//
//    List<Map<String, String>> rows = new ArrayList<>(iterator);
//
//    assertEquals(1, rows.size());
//    assertEquals("张三", rows.get(0).get("name"));
//    assertNull(rows.get(0).get("age"));
//  }
//}
