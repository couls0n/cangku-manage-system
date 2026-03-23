package com.warehouse.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.common.RequestTraceFilter;
import com.warehouse.common.Result;
import com.warehouse.entity.OperationAuditLog;
import com.warehouse.security.AuthenticatedUser;
import com.warehouse.security.SecurityContext;
import com.warehouse.service.OperationAuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
public class OperationAuditAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationAuditAspect.class);
    private static final int MAX_PAYLOAD_LENGTH = 2000;

    private final OperationAuditLogService operationAuditLogService;
    private final ObjectMapper objectMapper;

    public OperationAuditAspect(OperationAuditLogService operationAuditLogService, ObjectMapper objectMapper) {
        this.operationAuditLogService = operationAuditLogService;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(auditOperation)")
    public Object audit(ProceedingJoinPoint joinPoint, AuditOperation auditOperation) throws Throwable {
        HttpServletRequest request = currentRequest();
        OperationAuditLog auditLog = buildAuditLog(joinPoint, auditOperation, request);
        try {
            Object result = joinPoint.proceed();
            fillSuccessResult(auditLog, result);
            saveQuietly(auditLog);
            return result;
        } catch (Throwable ex) {
            auditLog.setSuccess(0);
            auditLog.setResultCode(resolveErrorCode(ex));
            auditLog.setErrorMessage(truncate(ex.getMessage()));
            saveQuietly(auditLog);
            throw ex;
        }
    }

    private OperationAuditLog buildAuditLog(ProceedingJoinPoint joinPoint,
                                            AuditOperation auditOperation,
                                            HttpServletRequest request) {
        AuthenticatedUser currentUser = SecurityContext.getCurrentUser();
        OperationAuditLog auditLog = new OperationAuditLog();
        auditLog.setAction(auditOperation.action());
        auditLog.setResource(auditOperation.resource());
        auditLog.setRequestId(resolveRequestId(request));
        auditLog.setOperatorId(currentUser == null ? null : currentUser.getId());
        auditLog.setUsername(currentUser == null ? "anonymous" : currentUser.getUsername());
        auditLog.setHttpMethod(request == null ? null : request.getMethod());
        auditLog.setRequestUri(request == null ? null : request.getRequestURI());
        auditLog.setIpAddress(request == null ? null : request.getRemoteAddr());
        auditLog.setSuccess(1);
        auditLog.setRequestPayload(extractPayload(joinPoint));
        return auditLog;
    }

    private void fillSuccessResult(OperationAuditLog auditLog, Object result) {
        auditLog.setSuccess(1);
        if (result instanceof Result) {
            auditLog.setResultCode(((Result<?>) result).getCode());
        } else {
            auditLog.setResultCode(200);
        }
    }

    private String extractPayload(ProceedingJoinPoint joinPoint) {
        List<Object> payloadArgs = new ArrayList<>();
        for (Object arg : joinPoint.getArgs()) {
            if (arg == null || arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) {
                continue;
            }
            payloadArgs.add(arg);
        }
        if (payloadArgs.isEmpty()) {
            return null;
        }
        try {
            String payload = objectMapper.writeValueAsString(payloadArgs.size() == 1 ? payloadArgs.get(0) : payloadArgs);
            return truncate(payload);
        } catch (Exception ex) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            return truncate("serialization-failed:" + signature.getDeclaringTypeName() + "#" + signature.getMethod().getName());
        }
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        return null;
    }

    private String resolveRequestId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object attribute = request.getAttribute(RequestTraceFilter.REQUEST_ID_ATTRIBUTE);
        if (attribute != null) {
            return String.valueOf(attribute);
        }
        return request.getHeader(RequestTraceFilter.REQUEST_ID_HEADER);
    }

    private Integer resolveErrorCode(Throwable ex) {
        if (ex == null) {
            return 500;
        }
        return 500;
    }

    private void saveQuietly(OperationAuditLog auditLog) {
        try {
            operationAuditLogService.save(auditLog);
        } catch (Exception ex) {
            log.warn("Failed to persist operation audit log for action {}", auditLog.getAction(), ex);
        }
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= MAX_PAYLOAD_LENGTH ? value : value.substring(0, MAX_PAYLOAD_LENGTH);
    }
}
