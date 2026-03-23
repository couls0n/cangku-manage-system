package com.warehouse.stock;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class StockInboundCommand {

    private Long productId;

    private BigDecimal quantity;

    private String batchNo;
}
