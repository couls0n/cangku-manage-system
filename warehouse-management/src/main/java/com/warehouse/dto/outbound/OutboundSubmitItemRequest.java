package com.warehouse.dto.outbound;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class OutboundSubmitItemRequest {

    @NotNull(message = "商品不能为空")
    private Long productId;

    @NotNull(message = "出库数量不能为空")
    @DecimalMin(value = "0.01", message = "出库数量必须大于 0")
    private BigDecimal quantity;

    @DecimalMin(value = "0.00", message = "单价不能为负数")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.00", message = "行金额不能为负数")
    private BigDecimal totalPrice;

    private String remark;
}
