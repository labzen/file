package cn.labzen.file.meta;

import cn.labzen.meta.component.DeclaredComponent;

public class FileMeta implements DeclaredComponent {

  @Override
  public String mark() {
    return "Labzen-Web-File";
  }

  @Override
  public String packageBased() {
    return "cn.labzen.file";
  }

  @Override
  public String description() {
    return "提供安全、高可扩展性、灵活的文件上传和下载能力，支持Excel、PDF、HTML等多种格式的导入导出";
  }
}
