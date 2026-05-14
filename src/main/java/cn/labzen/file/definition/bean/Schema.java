package cn.labzen.file.definition.bean;

import lombok.Data;

import java.util.List;

@Data
@Deprecated
public class Schema {

  private String fileName;

  private List<Column> columns;
}
