package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("operation_audit_log")
public class OperationAuditLog extends BaseEntity {

    private String requestId;

    private Long operatorId;

    private String username;

    private String action;

    private String resource;

    private String httpMethod;

    private String requestUri;

    private String ipAddress;

    private Integer success;

    private Integer resultCode;

    private String errorMessage;

    private String requestPayload;
}
