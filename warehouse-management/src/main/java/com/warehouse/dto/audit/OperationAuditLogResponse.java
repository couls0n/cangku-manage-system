package com.warehouse.dto.audit;

import com.warehouse.entity.OperationAuditLog;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OperationAuditLogResponse {

    private Long id;
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
    private LocalDateTime createTime;

    public static OperationAuditLogResponse from(OperationAuditLog log) {
        if (log == null) {
            return null;
        }
        return OperationAuditLogResponse.builder()
                .id(log.getId())
                .requestId(log.getRequestId())
                .operatorId(log.getOperatorId())
                .username(log.getUsername())
                .action(log.getAction())
                .resource(log.getResource())
                .httpMethod(log.getHttpMethod())
                .requestUri(log.getRequestUri())
                .ipAddress(log.getIpAddress())
                .success(log.getSuccess())
                .resultCode(log.getResultCode())
                .errorMessage(log.getErrorMessage())
                .requestPayload(log.getRequestPayload())
                .createTime(log.getCreateTime())
                .build();
    }
}
