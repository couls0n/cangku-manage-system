package com.warehouse.controller;

import com.warehouse.audit.AuditOperation;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.dto.audit.OperationAuditLogResponse;
import com.warehouse.entity.OperationAuditLog;
import com.warehouse.security.PermissionConstants;
import com.warehouse.security.RequiresPermission;
import com.warehouse.service.OperationAuditLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final OperationAuditLogService operationAuditLogService;

    public AuditController(OperationAuditLogService operationAuditLogService) {
        this.operationAuditLogService = operationAuditLogService;
    }

    @GetMapping("/page")
    @RequiresPermission(PermissionConstants.AUDIT_LOG_READ)
    public Result<PageResult<OperationAuditLogResponse>> page(@RequestParam(defaultValue = "1") Integer current,
                                                              @RequestParam(defaultValue = "10") Integer size,
                                                              @RequestParam(required = false) String action,
                                                              @RequestParam(required = false) String resource,
                                                              @RequestParam(required = false) Long operatorId,
                                                              @RequestParam(required = false) Integer success,
                                                              @RequestParam(required = false) String requestId,
                                                              @RequestParam(required = false) String requestUri,
                                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromTime,
                                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toTime) {
        Page<OperationAuditLog> page = operationAuditLogService.pageLogs(
                current, size, action, resource, operatorId, success, requestId, requestUri, fromTime, toTime
        );
        List<OperationAuditLogResponse> records = page.getRecords().stream()
                .map(OperationAuditLogResponse::from)
                .collect(Collectors.toList());
        return Result.success(new PageResult<>(page.getTotal(), records));
    }

    @GetMapping("/export")
    @RequiresPermission(PermissionConstants.AUDIT_LOG_EXPORT)
    @AuditOperation(action = "audit.export", resource = "operation_audit_log")
    public void export(@RequestParam(required = false) String action,
                       @RequestParam(required = false) String resource,
                       @RequestParam(required = false) Long operatorId,
                       @RequestParam(required = false) Integer success,
                       @RequestParam(required = false) String requestId,
                       @RequestParam(required = false) String requestUri,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromTime,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toTime,
                       HttpServletResponse response) throws IOException {
        List<OperationAuditLog> logs = operationAuditLogService.listLogs(
                action, resource, operatorId, success, requestId, requestUri, fromTime, toTime
        );
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=operation-audit-log.csv");
        StringBuilder builder = new StringBuilder();
        builder.append("id,requestId,operatorId,username,action,resource,httpMethod,requestUri,ipAddress,success,resultCode,errorMessage,requestPayload,createTime\n");
        for (OperationAuditLog log : logs) {
            builder.append(csv(log.getId()))
                    .append(',').append(csv(log.getRequestId()))
                    .append(',').append(csv(log.getOperatorId()))
                    .append(',').append(csv(log.getUsername()))
                    .append(',').append(csv(log.getAction()))
                    .append(',').append(csv(log.getResource()))
                    .append(',').append(csv(log.getHttpMethod()))
                    .append(',').append(csv(log.getRequestUri()))
                    .append(',').append(csv(log.getIpAddress()))
                    .append(',').append(csv(log.getSuccess()))
                    .append(',').append(csv(log.getResultCode()))
                    .append(',').append(csv(log.getErrorMessage()))
                    .append(',').append(csv(log.getRequestPayload()))
                    .append(',').append(csv(log.getCreateTime()))
                    .append('\n');
        }
        response.getWriter().write(builder.toString());
    }

    private String csv(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + text + "\"";
    }
}
