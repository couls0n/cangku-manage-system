package com.warehouse.monitoring.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ebpf_event")
public class EbpfEvent {
    @TableId(type = IdType.AUTO)
    private Long id;
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
    private LocalDateTime createdAt;
}
