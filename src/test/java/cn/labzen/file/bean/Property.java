package cn.labzen.file.bean;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * 属性实体类
 * <p>
 * 用于测试数据导出功能的模拟实体，涵盖多种数据类型和转换器场景：
 * <ul>
 *   <li>字符串类型：name, value, phone, email, description, category — 支持 when-null、when-empty、prefix、suffix、mapping、truncate、desensitize</li>
 *   <li>数值类型：indexical(Integer), size(Double), amount(BigDecimal) — 支持 number pattern、prefix、suffix</li>
 *   <li>日期时间：createTime(Date), localDate(LocalDate), localDateTime(LocalDateTime), localTime(LocalTime) — 支持 date pattern</li>
 *   <li>状态枚举：status(Integer) — 支持 enum converter、mapping converter</li>
 * </ul>
 *
 * @author labzen
 */
@Data
public class Property {

  // ==================== 字符串类型 ====================

  /**
   * 属性名称
   * <p>测试场景：when-null、prefix、style.font
   */
  private String name;

  /**
   * 属性值
   * <p>测试场景：when-null、when-empty、mapping
   */
  private String value;

  /**
   * 电话号码
   * <p>测试场景：desensitize（手机号脱敏）
   */
  private String phone;

  /**
   * 电子邮箱
   * <p>测试场景：desensitize（邮箱脱敏）
   */
  private String email;

  /**
   * 分类编码
   * <p>测试场景：mapping（A=系统, B=业务, C=其他）
   */
  private String category;

  /**
   * 属性描述
   * <p>测试场景：truncate（长文本截断）
   */
  private String description;

  // ==================== 数值类型 ====================

  /**
   * 属性索引
   * <p>测试场景：when-null、prefix、style.align
   */
  private Integer indexical;

  /**
   * 属性大小
   * <p>测试场景：number pattern、suffix、when-null
   */
  private Double size;

  /**
   * 金额
   * <p>测试场景：number pattern（#,##0.00）、负数、零值
   */
  private BigDecimal amount;

  // ==================== 日期时间类型 ====================

  /**
   * 创建时间（java.util.Date）
   * <p>测试场景：date pattern
   */
  private Date createTime;

  /**
   * 生效日期（java.time.LocalDate）
   * <p>测试场景：date pattern
   */
  private LocalDate effectiveDate;

  /**
   * 最后修改时间（java.time.LocalDateTime）
   * <p>测试场景：date pattern
   */
  private LocalDateTime lastModified;

  /**
   * 提醒时间（java.time.LocalTime）
   * <p>测试场景：date pattern
   */
  private LocalTime remindTime;

  // ==================== 状态枚举 ====================

  /**
   * 状态码
   * <p>测试场景：enum converter、mapping converter（1=启用, 2=禁用, 3=待审核）
   */
  private Integer status;
}
