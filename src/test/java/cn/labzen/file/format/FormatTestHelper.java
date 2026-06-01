package cn.labzen.file.format;

import cn.labzen.file.definition.DefinitionLoader;
import cn.labzen.file.definition.DefinitionRegistry;
import cn.labzen.meta.LabzenMetaInitializer;

import java.io.File;

public class FormatTestHelper {

  public static final String OUTPUT_DIR = System.getProperty("user.dir") + "/.testing";

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public static void setup() {
    //noinspection DataFlowIssue
    new LabzenMetaInitializer().initialize(null);
    DefinitionRegistry.clear();

    DefinitionLoader loader = new DefinitionLoader(
      "classpath*:data-export/**/*.yml",
      "classpath*:data-export/__global__.yml"
    );
    loader.load();

    I18nData.prepare();

    File outputDir = new File(OUTPUT_DIR);
    if (!outputDir.exists()) {
      outputDir.mkdirs();
    }
  }

  public static String outputFolder() {
    return OUTPUT_DIR;
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public static File withFile(String filename) {
    File file = new File(OUTPUT_DIR, filename);

    if (file.exists()) {
      file.delete();
    }

    return file;
  }

  public static void tearDown() {
    DefinitionRegistry.clear();
  }

}
