package com.warehouse.monitoring.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.warehouse.monitoring.dto.EbpfIngestRequest;
import com.warehouse.monitoring.dto.SecurityDashboardResponse;
import com.warehouse.monitoring.entity.EbpfEvent;
import com.warehouse.monitoring.entity.SecurityAlert;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SecurityMonitoringService {

    private final EbpfEventService ebpfEventService;
    private final SecurityAlertService securityAlertService;

    public SecurityMonitoringService(EbpfEventService ebpfEventService, SecurityAlertService securityAlertService) {
        this.ebpfEventService = ebpfEventService;
        this.securityAlertService = securityAlertService;
    }

    public EbpfEvent ingestEbpfEvent(EbpfIngestRequest request) {
        EbpfEvent event = new EbpfEvent();
        event.setEventType(defaultValue(request.getEventType(), "UNKNOWN"));
        event.setSeverity(defaultValue(request.getSeverity(), "LOW"));
        event.setProcessId(request.getProcessId());
        event.setProcessName(request.getProcessName());
        event.setSyscallName(request.getSyscallName());
        event.setTargetPath(request.getTargetPath());
        event.setRemoteAddress(request.getRemoteAddress());
        event.setRemotePort(request.getRemotePort());
        event.setProtocol(request.getProtocol());
        event.setSummary(defaultValue(request.getSummary(), "eBPF event"));
        event.setDetail(request.getDetail());
        event.setWarehouseId(request.getWarehouseId());
        event.setCreatedAt(LocalDateTime.now());
        ebpfEventService.save(event);
        analyzeEvent(event);
        return event;
    }

    public void recordApplicationAlert(String alertType, String severity, String title, String content) {
        QueryWrapper<SecurityAlert> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("alert_type", alertType);
        queryWrapper.eq("title", title);
        queryWrapper.eq("status", "OPEN");
        queryWrapper.last("limit 1");
        SecurityAlert alert = securityAlertService.getOne(queryWrapper);
        if (alert == null) {
            alert = new SecurityAlert();
            alert.setAlertType(alertType);
            alert.setSeverity(severity);
            alert.setTitle(title);
            alert.setContent(content);
            alert.setFirstSeen(LocalDateTime.now());
            alert.setLastSeen(LocalDateTime.now());
            alert.setHitCount(1);
            alert.setStatus("OPEN");
            securityAlertService.save(alert);
            return;
        }
        alert.setSeverity(severity);
        alert.setContent(content);
        alert.setLastSeen(LocalDateTime.now());
        alert.setHitCount(alert.getHitCount() == null ? 1 : alert.getHitCount() + 1);
        securityAlertService.updateById(alert);
    }

    public SecurityDashboardResponse dashboard() {
        QueryWrapper<EbpfEvent> latestEventsWrapper = new QueryWrapper<>();
        latestEventsWrapper.orderByDesc("created_at");
        latestEventsWrapper.last("limit 10");
        List<EbpfEvent> latestEvents = ebpfEventService.list(latestEventsWrapper);

        QueryWrapper<SecurityAlert> latestAlertsWrapper = new QueryWrapper<>();
        latestAlertsWrapper.orderByDesc("last_seen");
        latestAlertsWrapper.last("limit 10");
        List<SecurityAlert> latestAlerts = securityAlertService.list(latestAlertsWrapper);

        Map<String, Long> severityDistribution = new LinkedHashMap<>();
        severityDistribution.put("LOW", countEventsBy("severity", "LOW"));
        severityDistribution.put("MEDIUM", countEventsBy("severity", "MEDIUM"));
        severityDistribution.put("HIGH", countEventsBy("severity", "HIGH"));

        Map<String, Long> eventTypeDistribution = new LinkedHashMap<>();
        eventTypeDistribution.put("EXEC", countEventsBy("event_type", "EXEC"));
        eventTypeDistribution.put("FILE", countEventsBy("event_type", "FILE"));
        eventTypeDistribution.put("NETWORK", countEventsBy("event_type", "NETWORK"));
        eventTypeDistribution.put("APP", countEventsBy("event_type", "APP"));

        QueryWrapper<SecurityAlert> openAlertsWrapper = new QueryWrapper<>();
        openAlertsWrapper.eq("status", "OPEN");

        return SecurityDashboardResponse.builder()
                .totalEvents(ebpfEventService.count())
                .openAlerts(securityAlertService.count(openAlertsWrapper))
                .severityDistribution(severityDistribution)
                .eventTypeDistribution(eventTypeDistribution)
                .latestAlerts(latestAlerts)
                .latestEvents(latestEvents)
                .build();
    }

    private void analyzeEvent(EbpfEvent event) {
        boolean suspiciousExec = "EXEC".equalsIgnoreCase(event.getEventType())
                && containsAny(event.getTargetPath(), "/bin/sh", "cmd.exe", "powershell", "bash");
        boolean suspiciousFile = "FILE".equalsIgnoreCase(event.getEventType())
                && containsAny(event.getTargetPath(), "/etc/passwd", "C:\\Windows\\System32\\drivers\\etc\\hosts");
        boolean suspiciousNetwork = "NETWORK".equalsIgnoreCase(event.getEventType())
                && event.getRemotePort() != null
                && (event.getRemotePort() == 22 || event.getRemotePort() == 3389 || event.getRemotePort() > 49151);
        if (suspiciousExec || suspiciousFile || suspiciousNetwork) {
            recordApplicationAlert("EBPF_ANOMALY", "HIGH",
                    "检测到疑似异常系统行为",
                    event.getSummary() + " | " + defaultValue(event.getDetail(), "请检查业务操作链路与主机进程"));
        }
    }

    private long countEventsBy(String column, String value) {
        QueryWrapper<EbpfEvent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(column, value);
        return ebpfEventService.count(queryWrapper);
    }

    private boolean containsAny(String value, String... candidates) {
        if (value == null) {
            return false;
        }
        String lowerCase = value.toLowerCase();
        for (String candidate : candidates) {
            if (lowerCase.contains(candidate.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String defaultValue(String value, String defaultValue) {
        return value == null || value.isEmpty() ? defaultValue : value;
    }
}
