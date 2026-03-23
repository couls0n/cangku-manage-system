package com.warehouse.dto.stock;

import com.warehouse.entity.StockCheckRecord;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class StockCheckRecordResponse {

    private Long id;
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
    private LocalDateTime createTime;

    public static StockCheckRecordResponse from(StockCheckRecord record) {
        if (record == null) {
            return null;
        }
        return StockCheckRecordResponse.builder()
                .id(record.getId())
                .requestId(record.getRequestId())
                .stockId(record.getStockId())
                .warehouseId(record.getWarehouseId())
                .productId(record.getProductId())
                .operatorId(record.getOperatorId())
                .systemQuantity(record.getSystemQuantity())
                .countedQuantity(record.getCountedQuantity())
                .differenceQuantity(record.getDifferenceQuantity())
                .resultType(record.getResultType())
                .batchNo(record.getBatchNo())
                .reason(record.getReason())
                .remark(record.getRemark())
                .status(record.getStatus())
                .approverId(record.getApproverId())
                .approvedTime(record.getApprovedTime())
                .approvalComment(record.getApprovalComment())
                .createTime(record.getCreateTime())
                .build();
    }
}
