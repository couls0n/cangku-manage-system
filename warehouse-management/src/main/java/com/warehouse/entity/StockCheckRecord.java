package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_check_record")
public class StockCheckRecord extends BaseEntity {

    private String requestId;

    private Long stockId;

    private Long warehouseId;

    private Long productId;

    private Long operatorId;

    private BigDecimal systemQuantity;

    private BigDecimal countedQuantity;

    private BigDecimal differenceQuantity;

    private String resultType;

    private String batchNo;

    private String reason;

    private String remark;

    private String status;

    private Long approverId;

    private LocalDateTime approvedTime;

    private String approvalComment;
}
