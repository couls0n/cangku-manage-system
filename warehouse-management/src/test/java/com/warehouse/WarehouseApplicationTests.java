package com.warehouse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:warehouse_context;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.flyway.locations=classpath:db/migration,classpath:db/dev",
        "warehouse.security.token-secret=test-token-secret",
        "warehouse.security.ebpf-ingest-key=test-ebpf-key",
        "warehouse.stock.lock.enabled=false"
})
class WarehouseApplicationTests {

    @Test
    void contextLoads() {
    }
}
