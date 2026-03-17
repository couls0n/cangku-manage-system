package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_category")
public class Category extends BaseEntity {

    private String categoryName;

    private Long parentId;

    private Integer sort;

    private Integer status;

    private String remark;
}
