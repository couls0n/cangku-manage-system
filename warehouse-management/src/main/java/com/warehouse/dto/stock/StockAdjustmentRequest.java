package com.warehouse.dto.stock;

import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class StockAdjustmentRequest {

    @NotBlank(message = "Request id cannot be blank")
    private String requestId;

    @NotNull(message = "Stock id cannot be null")
    private Long stockId;

    @NotNull(message = "Adjustment quantity cannot be null")
    private BigDecimal quantityChange;

    @NotBlank(message = "Adjustment reason cannot be blank")
    private String reason;

    private String remark;

    @AssertTrue(message = "Adjustment quantity cannot be zero")
    public boolean isQuantityChangeValid() {
        return quantityChange != null && quantityChange.signum() != 0;
    }
}
