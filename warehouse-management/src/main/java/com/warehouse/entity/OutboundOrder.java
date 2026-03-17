package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("outbound_order")
public class OutboundOrder extends BaseEntity {

    private String orderNo;

    private Long warehouseId;

    private Long customerId;

    private Long operatorId;

    private LocalDateTime orderTime;

    private BigDecimal totalAmount;

    private Integer status;

    private String remark;
}
