package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.entity.OperationAuditLog;
import com.warehouse.mapper.OperationAuditLogMapper;
import com.warehouse.service.OperationAuditLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OperationAuditLogServiceImpl extends ServiceImpl<OperationAuditLogMapper, OperationAuditLog> implements OperationAuditLogService {

    @Override
    public Page<OperationAuditLog> pageLogs(int current,
                                            int size,
                                            String action,
                                            String resource,
                                            Long operatorId,
                                            Integer success,
                                            String requestId,
                                            String requestUri,
                                            LocalDateTime fromTime,
                                            LocalDateTime toTime) {
        Page<OperationAuditLog> page = new Page<>(current, size);
        QueryWrapper<OperationAuditLog> wrapper = buildWrapper(action, resource, operatorId, success, requestId, requestUri, fromTime, toTime);
        return page(page, wrapper);
    }

    @Override
    public List<OperationAuditLog> listLogs(String action,
                                            String resource,
                                            Long operatorId,
                                            Integer success,
                                            String requestId,
                                            String requestUri,
                                            LocalDateTime fromTime,
                                            LocalDateTime toTime) {
        return list(buildWrapper(action, resource, operatorId, success, requestId, requestUri, fromTime, toTime));
    }

    private QueryWrapper<OperationAuditLog> buildWrapper(String action,
                                                         String resource,
                                                         Long operatorId,
                                                         Integer success,
                                                         String requestId,
                                                         String requestUri,
                                                         LocalDateTime fromTime,
                                                         LocalDateTime toTime) {
        QueryWrapper<OperationAuditLog> wrapper = new QueryWrapper<>();
        if (action != null && !action.isBlank()) {
            wrapper.eq("action", action);
        }
        if (resource != null && !resource.isBlank()) {
            wrapper.eq("resource", resource);
        }
        if (operatorId != null) {
            wrapper.eq("operator_id", operatorId);
        }
        if (success != null) {
            wrapper.eq("success", success);
        }
        if (requestId != null && !requestId.isBlank()) {
            wrapper.eq("request_id", requestId);
        }
        if (requestUri != null && !requestUri.isBlank()) {
            wrapper.like("request_uri", requestUri);
        }
        if (fromTime != null) {
            wrapper.ge("create_time", fromTime);
        }
        if (toTime != null) {
            wrapper.le("create_time", toTime);
        }
        wrapper.orderByDesc("create_time");
        return wrapper;
    }
}
