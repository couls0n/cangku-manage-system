package com.warehouse.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "warehouse.web.cors")
public class WebCorsProperties {

    private List<String> allowedOrigins = new ArrayList<>();

    private List<String> allowedMethods = new ArrayList<>(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

    private List<String> allowedHeaders = new ArrayList<>(List.of("Authorization", "Content-Type", "X-Request-Id"));

    private long maxAge = 3600L;
}
