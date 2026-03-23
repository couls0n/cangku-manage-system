package com.warehouse.dto.stock;

import com.warehouse.entity.StockAdjustment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class StockAdjustmentResponse {

    private Long id;
    private String requestId;
    private Long stockId;
    private Long warehouseId;
    private Long productId;
    private Long operatorId;
    private String adjustmentType;
    private BigDecimal quantityChange;
    private BigDecimal beforeQuantity;
    private BigDecimal afterQuantity;
    private String batchNo;
    private String reason;
    private String remark;
    private String status;
    private Long approverId;
    private LocalDateTime approvedTime;
    private String approvalComment;
    private LocalDateTime createTime;

    public static StockAdjustmentResponse from(StockAdjustment adjustment) {
        if (adjustment == null) {
            return null;
        }
        return StockAdjustmentResponse.builder()
                .id(adjustment.getId())
                .requestId(adjustment.getRequestId())
                .stockId(adjustment.getStockId())
                .warehouseId(adjustment.getWarehouseId())
                .productId(adjustment.getProductId())
                .operatorId(adjustment.getOperatorId())
                .adjustmentType(adjustment.getAdjustmentType())
                .quantityChange(adjustment.getQuantityChange())
                .beforeQuantity(adjustment.getBeforeQuantity())
                .afterQuantity(adjustment.getAfterQuantity())
                .batchNo(adjustment.getBatchNo())
                .reason(adjustment.getReason())
                .remark(adjustment.getRemark())
                .status(adjustment.getStatus())
                .approverId(adjustment.getApproverId())
                .approvedTime(adjustment.getApprovedTime())
                .approvalComment(adjustment.getApprovalComment())
                .createTime(adjustment.getCreateTime())
                .build();
    }
}
