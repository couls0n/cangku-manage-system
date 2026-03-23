package com.warehouse.dto.inbound;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InboundSubmitRequest {

    @NotBlank(message = "Request id cannot be blank")
    private String requestId;

    @NotNull(message = "Warehouse id cannot be null")
    private Long warehouseId;

    private String orderNo;

    private Long supplierId;

    private LocalDateTime orderTime;

    private Integer status;

    private String remark;

    @Valid
    @NotEmpty(message = "Inbound items cannot be empty")
    private List<InboundSubmitItemRequest> items;
}
