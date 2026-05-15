package cn.labzen.file.bean;

import lombok.Data;

import java.util.Date;

@Data
public class Property {

  /**
   * 属性名称
   */
  private String name;
  /**
   * 属性值
   */
  private String value;
  /**
   * 属性索引
   */
  private Integer indexical;
  /**
   * 电话
   */
  private String phone;
  /**
   * 属性创建时间
   */
  private Date createTime;
  /**
   * 属性大小
   */
  private Double size;
}
