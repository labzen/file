package cn.labzen.file.format.xml;

import cn.labzen.file.definition.bean.DataDefinition;
import cn.labzen.file.definition.enums.FileFormat;
import cn.labzen.file.format.core.reader.AbstractDataFileReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.*;

/**
 * XML 文件读取器
 * <p>
 * 使用 StAX 流式解析XML，每个 &lt;record&gt; 标签为一行数据，
 * 子标签名为字段名，文本内容为值。
 *
 * @author labzen
 */
public class XmlFileReader extends AbstractDataFileReader {

  @Override
  public FileFormat format() {
    return FileFormat.XML;
  }

  @Override
  protected Iterator<Map<String, String>> doRead(InputStream inputStream, DataDefinition definition) {
    try {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      XMLStreamReader reader = factory.createXMLStreamReader(inputStream);

      List<Map<String, String>> dataRows = new ArrayList<>();
      String currentFieldName = null;
      Map<String, String> currentRow = null;

      while (reader.hasNext()) {
        int event = reader.next();

        switch (event) {
          case XMLStreamReader.START_ELEMENT:
            String localName = reader.getLocalName();
            if ("record".equals(localName)) {
              currentRow = new LinkedHashMap<>();
            } else if (currentRow != null) {
              currentFieldName = localName;
            }
            break;

          case XMLStreamReader.CHARACTERS:
            if (currentFieldName != null && currentRow != null) {
              String text = reader.getText().trim();
              if (!text.isEmpty()) {
                currentRow.put(currentFieldName, text);
              }
            }
            break;

          case XMLStreamReader.END_ELEMENT:
            if ("record".equals(reader.getLocalName()) && currentRow != null) {
              dataRows.add(currentRow);
              currentRow = null;
            }
            currentFieldName = null;
            break;
        }
      }

      reader.close();
      return dataRows.iterator();
    } catch (Exception e) {
      throw new RuntimeException("XML文件读取失败", e);
    }
  }
}
