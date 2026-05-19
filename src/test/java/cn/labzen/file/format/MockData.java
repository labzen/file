package cn.labzen.file.format;

import cn.labzen.file.bean.Property;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 模拟测试数据工厂
 * <p>
 * 提供丰富的测试数据，覆盖各种数据类型和边界情况：
 * <ul>
 *   <li>null 值 — 测试 when-null 转换器</li>
 *   <li>空字符串 — 测试 when-empty 转换器</li>
 *   <li>不同日期类型 — 测试 date 转换器</li>
 *   <li>不同数值 — 测试 number 转换器（正数、负数、零、小数）</li>
 *   <li>手机号/邮箱 — 测试 desensitize 转换器</li>
 *   <li>长文本 — 测试 truncate 转换器</li>
 *   <li>状态码 — 测试 enum/mapping 转换器</li>
 *   <li>分类编码 — 测试 mapping 转换器</li>
 * </ul>
 *
 * @author labzen
 */
public class MockData {

  /**
   * 创建完整的模拟数据集（10条）
   */
  public static List<Property> createMockData() {
    long now = System.currentTimeMillis();

    Property p1 = new Property();
    p1.setName("系统配置");
    p1.setValue("debug=true");
    p1.setIndexical(1);
    p1.setPhone("13800138000");
    p1.setEmail("admin@example.com");
    p1.setCategory("A");
    p1.setDescription("这是一个系统级别的配置项，用于控制调试模式的开关状态。");
    p1.setCreateTime(new Date(now));
    p1.setEffectiveDate(LocalDate.now());
    p1.setLastModified(LocalDateTime.now());
    p1.setRemindTime(LocalTime.of(9, 30));
    p1.setSize(1024.5);
    p1.setAmount(new BigDecimal("9999.99"));
    p1.setStatus(1);

    Property p2 = new Property();
    p2.setName("数据库连接");
    p2.setValue("jdbc:mysql://localhost:3306/test");
    p2.setIndexical(2);
    p2.setPhone("13512199325");
    p2.setEmail("dbops@company.cn");
    p2.setCategory("B");
    p2.setDescription("生产环境数据库连接字符串，包含主机地址、端口号和数据库名称。");
    p2.setCreateTime(new Date(now - 86400000));
    p2.setEffectiveDate(LocalDate.now().minusDays(1));
    p2.setLastModified(LocalDateTime.now().minusHours(12));
    p2.setRemindTime(LocalTime.of(14, 0));
    p2.setSize(2048.75);
    p2.setAmount(new BigDecimal("-500.00"));
    p2.setStatus(2);

    Property p3 = new Property();
    p3.setName("日志级别");
    p3.setValue("INFO");
    p3.setIndexical(3);
    p3.setPhone("17699481022");
    p3.setEmail("logger@system.org");
    p3.setCategory("A");
    p3.setDescription("定义系统日志的输出级别，可选值为 DEBUG、INFO、WARN、ERROR。");
    p3.setCreateTime(new Date(now - 172800000));
    p3.setEffectiveDate(LocalDate.now().minusWeeks(1));
    p3.setLastModified(LocalDateTime.now().minusDays(2));
    p3.setRemindTime(LocalTime.of(23, 59));
    p3.setSize(512.0);
    p3.setAmount(BigDecimal.ZERO);
    p3.setStatus(3);

    // p4: 测试 null 值和 when-null 转换器
    Property p4 = new Property();
    p4.setName(null);
    p4.setValue(null);
    p4.setIndexical(null);
    p4.setPhone("13912345678");
    p4.setEmail("null-test@test.io");
    p4.setCategory("C");
    p4.setDescription(null);
    p4.setCreateTime(null);
    p4.setEffectiveDate(null);
    p4.setLastModified(null);
    p4.setRemindTime(null);
    p4.setSize(null);
    p4.setAmount(null);
    p4.setStatus(null);

    // p5: 测试空字符串和 when-empty 转换器
    Property p5 = new Property();
    p5.setName("");
    p5.setValue("");
    p5.setIndexical(5);
    p5.setPhone("15098765432");
    p5.setEmail("empty@test.com");
    p5.setCategory("B");
    p5.setDescription("");
    p5.setCreateTime(new Date(now - 259200000L));
    p5.setEffectiveDate(LocalDate.now().minusMonths(1));
    p5.setLastModified(LocalDateTime.now().minusWeeks(1));
    p5.setRemindTime(LocalTime.of(12, 0));
    p5.setSize(0.0);
    p5.setAmount(new BigDecimal("0.01"));
    p5.setStatus(1);

    // p6: 测试极大值和长文本截断
    Property p6 = new Property();
    p6.setName("超长属性名称测试项用于验证表格显示效果");
    p6.setValue("value-with-many-hyphens-and-special-chars_12345");
    p6.setIndexical(999);
    p6.setPhone("18888888888");
    p6.setEmail("very.long.email.address@subdomain.example.company.com");
    p6.setCategory("A");
    p6.setDescription("这是一段非常长的描述文本，用于测试字符串截断转换器的功能。"
      + "当文本长度超过设定的阈值时，应该在末尾自动添加省略号。"
      + "这段文本的长度明显超过了通常的截断阈值，因此可以充分验证截断逻辑是否正确。");
    p6.setCreateTime(new Date(now - 345600000L));
    p6.setEffectiveDate(LocalDate.of(2024, 1, 1));
    p6.setLastModified(LocalDateTime.of(2024, 6, 15, 10, 30, 0));
    p6.setRemindTime(LocalTime.of(0, 0));
    p6.setSize(999999.999);
    p6.setAmount(new BigDecimal("123456789.12"));
    p6.setStatus(2);

    // p7: 测试负数和小数
    Property p7 = new Property();
    p7.setName("温度传感器");
    p7.setValue("-15.5C");
    p7.setIndexical(7);
    p7.setPhone("13111111111");
    p7.setEmail("sensor@device.local");
    p7.setCategory("C");
    p7.setDescription("负数值测试");
    p7.setCreateTime(new Date(now - 432000000L));
    p7.setEffectiveDate(LocalDate.now().minusYears(1));
    p7.setLastModified(LocalDateTime.now().minusMonths(3));
    p7.setRemindTime(LocalTime.of(18, 30));
    p7.setSize(-3.14);
    p7.setAmount(new BigDecimal("-0.99"));
    p7.setStatus(3);

    // p8: 测试边界时间和特殊字符
    Property p8 = new Property();
    p8.setName("边界测试<>&\"'");
    p8.setValue("特殊字符测试：中文、English、123、!@#$%^&*()");
    p8.setIndexical(8);
    p8.setPhone("19900000000");
    p8.setEmail("test+alias@example.org");
    p8.setCategory("B");
    p8.setDescription("包含特殊字符的测试数据，用于验证导出格式对特殊字符的处理。");
    p8.setCreateTime(new Date(0)); // 1970-01-01
    p8.setEffectiveDate(LocalDate.of(1970, 1, 1));
    p8.setLastModified(LocalDateTime.of(2000, 1, 1, 0, 0, 0));
    p8.setRemindTime(LocalTime.of(12, 34, 56));
    p8.setSize(Double.MAX_VALUE);
    p8.setAmount(new BigDecimal("999999999999.99"));
    p8.setStatus(1);

    // p9: 测试最小值和边界数值
    Property p9 = new Property();
    p9.setName("最小值测试");
    p9.setValue("min");
    p9.setIndexical(0);
    p9.setPhone("10000000000");
    p9.setEmail("a@b.c");
    p9.setCategory("A");
    p9.setDescription("短");
    p9.setCreateTime(new Date(Long.MIN_VALUE));
    p9.setEffectiveDate(LocalDate.now().plusYears(1));
    p9.setLastModified(LocalDateTime.now().plusDays(7));
    p9.setRemindTime(LocalTime.of(6, 0));
    p9.setSize(Double.MIN_VALUE);
    p9.setAmount(new BigDecimal("0.001"));
    p9.setStatus(2);

    // p10: 综合测试
    Property p10 = new Property();
    p10.setName("综合测试项");
    p10.setValue("综合");
    p10.setIndexical(10);
    p10.setPhone("16666666666");
    p10.setEmail("comprehensive.test@mail.example");
    p10.setCategory("C");
    p10.setDescription("综合多种场景的最终测试数据。");
    p10.setCreateTime(new Date(now));
    p10.setEffectiveDate(LocalDate.now());
    p10.setLastModified(LocalDateTime.now());
    p10.setRemindTime(LocalTime.now());
    p10.setSize(100.0);
    p10.setAmount(new BigDecimal("100.50"));
    p10.setStatus(1);

    return Arrays.asList(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10);
  }
}
