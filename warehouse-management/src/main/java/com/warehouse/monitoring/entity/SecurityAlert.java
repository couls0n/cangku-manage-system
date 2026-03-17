package com.warehouse.monitoring.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("security_alert")
public class SecurityAlert {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String alertType;
    private String severity;
    private String title;
    private String content;
    private LocalDateTime firstSeen;
    private LocalDateTime lastSeen;
    private Integer hitCount;
    private String status;
}
