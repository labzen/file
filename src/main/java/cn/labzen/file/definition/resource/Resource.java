package cn.labzen.file.definition.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * 资源抽象接口，替代 Spring 的 Resource
 */
public interface Resource {

  /**
   * 获取资源文件名
   */
  String getFilename();

  /**
   * 获取资源输入流
   */
  InputStream getInputStream() throws IOException;

  /**
   * 获取资源 URL
   */
  URL getURL() throws IOException;

  /**
   * 获取资源 URI
   */
  URI getURI() throws IOException;

  /**
   * 资源是否存在
   */
  boolean exists();
}
