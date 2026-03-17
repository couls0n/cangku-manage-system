package com.warehouse.stock;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "warehouse.stock.lock")
public class StockLockProperties {

    private boolean enabled = true;

    private long waitSeconds = 3L;

    private long leaseSeconds = 10L;
}
