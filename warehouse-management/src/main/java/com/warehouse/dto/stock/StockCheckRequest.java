package com.warehouse.dto.stock;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class StockCheckRequest {

    @NotBlank(message = "Request id cannot be blank")
    private String requestId;

    @NotNull(message = "Stock id cannot be null")
    private Long stockId;

    @NotNull(message = "Counted quantity cannot be null")
    @DecimalMin(value = "0.00", message = "Counted quantity cannot be negative")
    private BigDecimal countedQuantity;

    @NotBlank(message = "Check reason cannot be blank")
    private String reason;

    private String remark;
}
