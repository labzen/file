package cn.labzen.file.format;

import cn.labzen.file.bean.Property;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MockData {

  /**
   * 创建模拟数据
   */
  public static List<Property> createMockData() {
    Property p1 = new Property();
    p1.setName("系统配置");
    p1.setValue("debug=true");
    p1.setIndexical(1);
    p1.setPhone("13800138000");
    p1.setCreateTime(new Date());
    p1.setSize(1024.5);

    Property p2 = new Property();
    p2.setName("数据库连接");
    p2.setValue("jdbc:mysql://localhost:3306/test");
    p2.setIndexical(2);
    p2.setPhone("13512199325");
    p2.setCreateTime(new Date(System.currentTimeMillis() - 86400000));
    p2.setSize(2048.75);

    Property p3 = new Property();
    p3.setName("日志级别");
    p3.setValue("\"INFO\"");
    p3.setIndexical(3);
    p3.setPhone("17699481022");
    p3.setCreateTime(new Date(System.currentTimeMillis() - 172800000));
    p3.setSize(512.0);

    Property p4 = new Property();
    p4.setName("备案信息");
    p4.setValue(null);
    p4.setIndexical(7);
    p4.setPhone("17699481022");
    p4.setCreateTime(new Date(System.currentTimeMillis() + 202500000));
    p4.setSize(-3.0);

    return Arrays.asList(p1, p2, p3, p4);
  }
}
