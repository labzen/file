package cn.labzen.file.definition.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * 资源模式解析器，替代 Spring 的 PathMatchingResourcePatternResolver
 * <p>
 * 支持的前缀：
 * <ul>
 *   <li>classpath:  — 从单个 classpath 位置加载资源</li>
 *   <li>classpath*: — 从所有 classpath 位置加载资源（支持 Ant 风格模式匹配）</li>
 *   <li>file:       — 从文件系统加载资源（支持 Ant 风格模式匹配）</li>
 * </ul>
 * <p>
 * 支持的 Ant 风格通配符：
 * <ul>
 *   <li>** — 匹配任意层级目录</li>
 *   <li>*  — 匹配任意字符（不含路径分隔符）</li>
 *   <li>?  — 匹配单个字符（不含路径分隔符）</li>
 * </ul>
 */
public class ResourcePatternResolver {

  private final ClassLoader classLoader;

  public ResourcePatternResolver() {
    this.classLoader = Thread.currentThread().getContextClassLoader();
  }

  public ResourcePatternResolver(ClassLoader classLoader) {
    this.classLoader = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
  }

  /**
   * 根据位置模式解析匹配的资源
   */
  public Resource[] getResources(String pattern) throws IOException {
    if (pattern.startsWith("classpath*:")) {
      return resolveClasspathAll(pattern.substring("classpath*:".length()));
    } else if (pattern.startsWith("classpath:")) {
      return resolveClasspath(pattern.substring("classpath:".length()));
    } else if (pattern.startsWith("file:")) {
      return resolveFile(pattern.substring("file:".length()));
    } else {
      return resolveClasspathAll(pattern);
    }
  }

  /**
   * 根据位置获取单个资源
   */
  public Resource getResource(String location) {
    if (location.startsWith("classpath:")) {
      String path = location.substring("classpath:".length());
      URL url = classLoader.getResource(path);
      if (url != null) {
        return new UrlResource(url);
      }
      return EmptyResource.INSTANCE;
    }

    // 尝试作为 URL 解析（支持 file: 和 jar:file: 等）
    try {
      URL url = new URL(location);
      return new UrlResource(url);
    } catch (MalformedURLException e) {
      // 非 URL 格式，尝试 classpath
    }

    URL url = classLoader.getResource(location);
    if (url != null) {
      return new UrlResource(url);
    }
    return EmptyResource.INSTANCE;
  }

  // ==================== 私有方法 ====================

  private Resource[] resolveClasspath(String path) throws IOException {
    if (!hasWildcard(path)) {
      URL url = classLoader.getResource(path);
      if (url != null) {
        return new Resource[]{new UrlResource(url)};
      }
      return new Resource[0];
    }
    return resolveClasspathAll(path);
  }

  private Resource[] resolveClasspathAll(String path) throws IOException {
    if (!hasWildcard(path)) {
      Enumeration<URL> urls = classLoader.getResources(path);
      List<Resource> result = new ArrayList<>();
      while (urls.hasMoreElements()) {
        result.add(new UrlResource(urls.nextElement()));
      }
      return result.toArray(new Resource[0]);
    }

    String rootDir = determineRootDir(path);
    String subPattern = path.substring(rootDir.length());
    Pattern regexPattern = antToRegex(subPattern);

    Enumeration<URL> rootUrls = classLoader.getResources(rootDir);
    List<Resource> result = new ArrayList<>();

    while (rootUrls.hasMoreElements()) {
      URL rootUrl = rootUrls.nextElement();
      String protocol = rootUrl.getProtocol();

      if ("file".equals(protocol)) {
        result.addAll(scanFilesystem(rootUrl, regexPattern));
      } else if ("jar".equals(protocol)) {
        result.addAll(scanJar(rootUrl, rootDir, regexPattern));
      }
    }

    return result.toArray(new Resource[0]);
  }

  private Resource[] resolveFile(String path) throws IOException {
    if (!hasWildcard(path)) {
      Path filePath = Paths.get(path);
      if (Files.exists(filePath)) {
        return new Resource[]{new FileSystemResource(filePath)};
      }
      return new Resource[0];
    }

    String rootDir = determineRootDir(path);
    String subPattern = path.substring(rootDir.length());
    Pattern regexPattern = antToRegex(subPattern);
    Path rootPath = Paths.get(rootDir);

    List<Resource> result = new ArrayList<>();
    if (Files.isDirectory(rootPath)) {
      Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
          String relativePath = rootPath.relativize(file).toString().replace('\\', '/');
          if (regexPattern.matcher(relativePath).matches()) {
            result.add(new FileSystemResource(file));
          }
          return FileVisitResult.CONTINUE;
        }
      });
    }
    return result.toArray(new Resource[0]);
  }

  /**
   * 扫描文件系统目录，匹配 Ant 模式
   */
  private List<Resource> scanFilesystem(URL rootUrl, Pattern pattern) {
    List<Resource> result = new ArrayList<>();
    try {
      Path rootPath = Paths.get(rootUrl.toURI());
      if (Files.isDirectory(rootPath)) {
        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            String relativePath = rootPath.relativize(file).toString().replace('\\', '/');
            if (pattern.matcher(relativePath).matches()) {
              result.add(new FileSystemResource(file));
            }
            return FileVisitResult.CONTINUE;
          }
        });
      }
    } catch (URISyntaxException | IOException e) {
      // 跳过此根路径
    }
    return result;
  }

  /**
   * 扫描 JAR 文件中的条目，匹配 Ant 模式
   */
  private List<Resource> scanJar(URL rootUrl, String rootDir, Pattern pattern) {
    List<Resource> result = new ArrayList<>();
    try {
      JarURLConnection jarConn = (JarURLConnection) rootUrl.openConnection();
      jarConn.setUseCaches(false);
      JarFile jarFile = jarConn.getJarFile();
      String entryPrefix = jarConn.getEntryName();
      if (entryPrefix != null && !entryPrefix.endsWith("/")) {
        entryPrefix += "/";
      }
      final String basePath = entryPrefix != null ? entryPrefix : rootDir;

      Enumeration<JarEntry> entries = jarFile.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        if (entry.isDirectory()) {
          continue;
        }

        String name = entry.getName();
        if (name.startsWith(basePath)) {
          String relativePath = name.substring(basePath.length());
          if (pattern.matcher(relativePath).matches()) {
            URI jarUri = new File(jarFile.getName()).toURI();
            URL entryUrl = new URL("jar:" + jarUri + "!/" + name);
            result.add(new UrlResource(entryUrl));
          }
        }
      }
    } catch (IOException e) {
      // 跳过此 JAR
    }
    return result;
  }

  /**
   * 确定路径中的根目录（通配符之前的部分）
   */
  private String determineRootDir(String path) {
    int wildcardPos = -1;
    for (int i = 0; i < path.length(); i++) {
      char c = path.charAt(i);
      if (c == '*' || c == '?') {
        wildcardPos = i;
        break;
      }
    }
    if (wildcardPos == -1) {
      return path;
    }
    int lastSlash = path.lastIndexOf('/', wildcardPos - 1);
    return lastSlash >= 0 ? path.substring(0, lastSlash + 1) : "";
  }

  private boolean hasWildcard(String path) {
    return path.contains("*") || path.contains("?");
  }

  /**
   * 将 Ant 风格模式转换为正则表达式
   * <ul>
   *   <li>**  → 匹配零个或多个目录层级</li>
   *   <li>**   → 匹配任意字符</li>
   *   <li>*    → 匹配不含路径分隔符的任意字符</li>
   *   <li>?    → 匹配不含路径分隔符的单个字符</li>
   * </ul>
   */
  private Pattern antToRegex(String pattern) {
    StringBuilder regex = new StringBuilder("^");
    int i = 0;
    while (i < pattern.length()) {
      char c = pattern.charAt(i);
      if (c == '*') {
        if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '*') {
          if (i + 2 < pattern.length() && pattern.charAt(i + 2) == '/') {
            // **/ 匹配零个或多个目录
            regex.append("(?:.+/)?");
            i += 3;
          } else {
            // ** 匹配任意字符
            regex.append(".*");
            i += 2;
          }
        } else {
          // * 匹配不含 / 的任意字符
          regex.append("[^/]*");
          i++;
        }
      } else if (c == '?') {
        regex.append("[^/]");
        i++;
      } else if (".+^${}()|[]\\".indexOf(c) >= 0) {
        regex.append('\\').append(c);
        i++;
      } else {
        regex.append(c);
        i++;
      }
    }
    regex.append("$");
    return Pattern.compile(regex.toString());
  }

  // ==================== 内部 Resource 实现 ====================

  /**
   * 基于 URL 的资源实现（classpath 资源、JAR 条目等）
   */
  private static class UrlResource implements Resource {

    private final URL url;

    UrlResource(URL url) {
      this.url = url;
    }

    @Override
    public String getFilename() {
      String urlPath = url.getPath();
      // 处理 JAR URL：提取条目路径
      int bangIndex = urlPath.indexOf("!/");
      if (bangIndex >= 0) {
        urlPath = urlPath.substring(bangIndex + 2);
      }
      int lastSlash = urlPath.lastIndexOf('/');
      return lastSlash >= 0 ? urlPath.substring(lastSlash + 1) : urlPath;
    }

    @Override
    public InputStream getInputStream() throws IOException {
      URLConnection conn = url.openConnection();
      conn.setUseCaches(false);
      return conn.getInputStream();
    }

    @Override
    public URL getURL() {
      return url;
    }

    @Override
    public URI getURI() throws IOException {
      try {
        return url.toURI();
      } catch (URISyntaxException e) {
        throw new IOException(e);
      }
    }

    @Override
    public boolean exists() {
      try {
        URLConnection conn = url.openConnection();
        conn.setUseCaches(false);
        if (conn instanceof HttpURLConnection) {
          HttpURLConnection httpConn = (HttpURLConnection) conn;
          httpConn.setRequestMethod("HEAD");
          return httpConn.getResponseCode() == HttpURLConnection.HTTP_OK;
        }
        try (InputStream is = conn.getInputStream()) {
          return true;
        }
      } catch (IOException e) {
        return false;
      }
    }
  }

  /**
   * 文件系统资源实现
   */
  private static class FileSystemResource implements Resource {

    private final Path path;

    FileSystemResource(Path path) {
      this.path = path;
    }

    @Override
    public String getFilename() {
      Path fileName = path.getFileName();
      return fileName != null ? fileName.toString() : "";
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return Files.newInputStream(path);
    }

    @Override
    public URL getURL() throws IOException {
      return path.toUri().toURL();
    }

    @Override
    public URI getURI() {
      return path.toUri();
    }

    @Override
    public boolean exists() {
      return Files.exists(path);
    }
  }

  /**
   * 空资源（不存在的资源）
   */
  private static class EmptyResource implements Resource {

    static final EmptyResource INSTANCE = new EmptyResource();

    @Override
    public String getFilename() {
      return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
      throw new IOException("Resource not found");
    }

    @Override
    public URL getURL() throws IOException {
      throw new IOException("Resource not found");
    }

    @Override
    public URI getURI() throws IOException {
      throw new IOException("Resource not found");
    }

    @Override
    public boolean exists() {
      return false;
    }
  }
}
