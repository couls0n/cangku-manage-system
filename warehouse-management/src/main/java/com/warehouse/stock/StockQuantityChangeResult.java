package com.warehouse.stock;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class StockQuantityChangeResult {

    private Long stockId;

    private Long warehouseId;

    private Long productId;

    private String batchNo;

    private BigDecimal beforeQuantity;

    private BigDecimal afterQuantity;
}
