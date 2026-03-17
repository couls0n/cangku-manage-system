package com.warehouse.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "warehouse.security")
public class SecurityProperties {
    private String tokenSecret;
    private Integer tokenExpireHours = 12;
    private String ebpfIngestKey;
    private RateLimit rateLimit = new RateLimit();

    @Data
    public static class RateLimit {
        private Integer defaultLimit = 120;
        private Integer defaultWindowSeconds = 60;
        private Integer loginLimit = 10;
        private Integer loginWindowSeconds = 60;
    }
}
