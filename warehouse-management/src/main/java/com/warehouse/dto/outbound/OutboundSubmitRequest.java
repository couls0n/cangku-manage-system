package com.warehouse.dto.outbound;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OutboundSubmitRequest {

    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    private String orderNo;

    private Long customerId;

    private LocalDateTime orderTime;

    private Integer status;

    private String remark;

    @Valid
    @NotEmpty(message = "出库明细不能为空")
    private List<OutboundSubmitItemRequest> items;
}
