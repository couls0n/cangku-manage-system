package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inbound_order")
public class InboundOrder extends BaseEntity {

    private String requestId;

    private String orderNo;

    private Long warehouseId;

    private Long supplierId;

    private Long operatorId;

    private LocalDateTime orderTime;

    private BigDecimal totalAmount;

    private Integer status;

    private String remark;
}
