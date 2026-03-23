package com.warehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.entity.OperationAuditLog;

import java.time.LocalDateTime;
import java.util.List;

public interface OperationAuditLogService extends IService<OperationAuditLog> {

    Page<OperationAuditLog> pageLogs(int current,
                                     int size,
                                     String action,
                                     String resource,
                                     Long operatorId,
                                     Integer success,
                                     String requestId,
                                     String requestUri,
                                     LocalDateTime fromTime,
                                     LocalDateTime toTime);

    List<OperationAuditLog> listLogs(String action,
                                     String resource,
                                     Long operatorId,
                                     Integer success,
                                     String requestId,
                                     String requestUri,
                                     LocalDateTime fromTime,
                                     LocalDateTime toTime);
}
