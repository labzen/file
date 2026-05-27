package cn.labzen.file.bean;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * 国际化测试用属性实体类（与 Property 结构相同，用于独立的 i18n 测试）
 */
@Data
public class PropertyI18n {

  private String name;
  private String value;
  private String phone;
  private String email;
  private String category;
  private String description;
  private Integer indexical;
  private Double size;
  private BigDecimal amount;
  private Date createTime;
  private LocalDate effectiveDate;
  private LocalDateTime lastModified;
  private LocalTime remindTime;
  private Integer status;
}
