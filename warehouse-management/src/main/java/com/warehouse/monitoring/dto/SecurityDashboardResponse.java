package com.warehouse.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityDashboardResponse {
    private long totalEvents;
    private long openAlerts;
    private Map<String, Long> severityDistribution;
    private Map<String, Long> eventTypeDistribution;
    private List<?> latestAlerts;
    private List<?> latestEvents;
}
