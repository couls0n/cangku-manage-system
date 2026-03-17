package com.warehouse.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.warehouse.common.Result;
import com.warehouse.monitoring.dto.EbpfIngestRequest;
import com.warehouse.monitoring.dto.SecurityDashboardResponse;
import com.warehouse.monitoring.entity.EbpfEvent;
import com.warehouse.monitoring.entity.SecurityAlert;
import com.warehouse.monitoring.service.EbpfEventService;
import com.warehouse.monitoring.service.SecurityAlertService;
import com.warehouse.monitoring.service.SecurityMonitoringService;
import com.warehouse.security.AccessGuard;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/security")
public class SecurityMonitoringController {

    private final SecurityMonitoringService securityMonitoringService;
    private final EbpfEventService ebpfEventService;
    private final SecurityAlertService securityAlertService;
    private final AccessGuard accessGuard;

    public SecurityMonitoringController(SecurityMonitoringService securityMonitoringService,
                                        EbpfEventService ebpfEventService,
                                        SecurityAlertService securityAlertService,
                                        AccessGuard accessGuard) {
        this.securityMonitoringService = securityMonitoringService;
        this.ebpfEventService = ebpfEventService;
        this.securityAlertService = securityAlertService;
        this.accessGuard = accessGuard;
    }

    @PostMapping("/ebpf/ingest")
    public Result<EbpfEvent> ingest(@RequestBody EbpfIngestRequest request) {
        return Result.success(securityMonitoringService.ingestEbpfEvent(request));
    }

    @GetMapping("/dashboard")
    public Result<SecurityDashboardResponse> dashboard() {
        accessGuard.requireAdmin();
        return Result.success(securityMonitoringService.dashboard());
    }

    @GetMapping("/events")
    public Result<List<EbpfEvent>> events(@RequestParam(defaultValue = "20") Integer size) {
        accessGuard.requireAdmin();
        QueryWrapper<EbpfEvent> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("created_at");
        queryWrapper.last("limit " + Math.min(size, 100));
        List<EbpfEvent> events = ebpfEventService.list(queryWrapper);
        return Result.success(events);
    }

    @GetMapping("/alerts")
    public Result<List<SecurityAlert>> alerts(@RequestParam(defaultValue = "20") Integer size) {
        accessGuard.requireAdmin();
        QueryWrapper<SecurityAlert> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("last_seen");
        queryWrapper.last("limit " + Math.min(size, 100));
        List<SecurityAlert> alerts = securityAlertService.list(queryWrapper);
        return Result.success(alerts);
    }
}
