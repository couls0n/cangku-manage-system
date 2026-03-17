package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {

    private String productCode;

    private String productName;

    private Long categoryId;

    private String unit;

    private BigDecimal price;

    private String specification;

    private String brand;

    private Integer status;

    private String remark;
}
