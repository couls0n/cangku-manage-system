package com.warehouse.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@Validated
@ConfigurationProperties(prefix = "warehouse.security")
public class SecurityProperties {
    @NotBlank
    private String tokenSecret;

    @Min(1)
    private Integer tokenExpireHours = 12;

    @NotBlank
    private String ebpfIngestKey;

    @Valid
    private RateLimit rateLimit = new RateLimit();

    @Data
    public static class RateLimit {
        @Min(1)
        private Integer defaultLimit = 120;

        @Min(1)
        private Integer defaultWindowSeconds = 60;

        @Min(1)
        private Integer loginLimit = 10;

        @Min(1)
        private Integer loginWindowSeconds = 60;
    }
}
