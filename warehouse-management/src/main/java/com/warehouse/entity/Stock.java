package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock")
public class Stock extends BaseEntity {

    private Long warehouseId;

    private Long productId;

    private BigDecimal quantity;

    private BigDecimal frozenQuantity;

    private String batchNo;

    private String location;
}
