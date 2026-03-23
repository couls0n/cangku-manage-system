package com.warehouse.dto.outbound;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class OutboundSubmitItemRequest {

    @NotNull(message = "Product id cannot be null")
    private Long productId;

    @NotNull(message = "Outbound quantity cannot be null")
    @DecimalMin(value = "0.01", message = "Outbound quantity must be greater than 0")
    private BigDecimal quantity;

    @DecimalMin(value = "0.00", message = "Unit price cannot be negative")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.00", message = "Total price cannot be negative")
    private BigDecimal totalPrice;

    private String remark;
}
