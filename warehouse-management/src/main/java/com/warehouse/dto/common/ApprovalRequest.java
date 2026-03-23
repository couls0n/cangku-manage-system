package com.warehouse.dto.common;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ApprovalRequest {

    @NotNull(message = "Approval decision cannot be null")
    private Boolean approved;

    private String comment;
}
