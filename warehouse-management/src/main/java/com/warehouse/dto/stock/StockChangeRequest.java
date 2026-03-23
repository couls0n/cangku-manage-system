package com.warehouse.dto.stock;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class StockChangeRequest {

    @NotBlank(message = "Request id cannot be blank")
    private String requestId;

    @NotNull(message = "Stock id cannot be null")
    private Long stockId;

    @NotNull(message = "Quantity cannot be null")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @NotBlank(message = "Reason cannot be blank")
    private String reason;

    private String remark;
}
