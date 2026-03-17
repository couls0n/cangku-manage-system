package com.warehouse.monitoring.dto;

import lombok.Data;

@Data
public class EbpfIngestRequest {
    private String eventType;
    private String severity;
    private Long processId;
    private String processName;
    private String syscallName;
    private String targetPath;
    private String remoteAddress;
    private Integer remotePort;
    private String protocol;
    private String summary;
    private String detail;
    private Long warehouseId;
}
