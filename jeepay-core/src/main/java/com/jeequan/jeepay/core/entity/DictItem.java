package com.jeequan.jeepay.core.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jeequan.jeepay.core.model.BaseModel;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_dict_item")
public class DictItem extends BaseModel implements Serializable {

  private static final long serialVersionUID = 1L;

  @TableId(type = IdType.ASSIGN_ID)
  private String id;

  private String dictId;

  private String itemText;

  private String itemValue;

  private Integer sortOrder;

  private Integer status;

  /**
   * 创建者用户ID
   */
  private Long createdUid;

  /**
   * 创建者姓名
   */
  private String createdBy;

  /**
   * 创建时间
   */
  private Date createdAt;

  /**
   * 更新时间
   */
  private Date updatedAt;


}
