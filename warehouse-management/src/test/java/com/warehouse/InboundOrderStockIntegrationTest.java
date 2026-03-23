package com.warehouse;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.warehouse.dto.inbound.InboundSubmitItemRequest;
import com.warehouse.dto.inbound.InboundSubmitRequest;
import com.warehouse.entity.InboundOrder;
import com.warehouse.entity.Stock;
import com.warehouse.service.InboundOrderService;
import com.warehouse.service.StockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:warehouse_inbound;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.flyway.locations=classpath:db/migration,classpath:db/dev",
        "warehouse.security.token-secret=test-token-secret",
        "warehouse.security.ebpf-ingest-key=test-ebpf-key",
        "warehouse.stock.lock.enabled=false"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class InboundOrderStockIntegrationTest {

    @Autowired
    private InboundOrderService inboundOrderService;

    @Autowired
    private StockService stockService;

    @Test
    void submitOrderShouldIncreaseStockAndPersistOrder() {
        InboundSubmitRequest request = buildRequest("req-inbound-1", BigDecimal.valueOf(5));

        inboundOrderService.submitOrder(request, 1L);

        Stock stock = currentStock("B20260301");
        assertNotNull(stock);
        assertEquals(0, stock.getQuantity().compareTo(BigDecimal.valueOf(25)));
    }

    @Test
    void submitOrderShouldBeIdempotentByRequestId() {
        InboundSubmitRequest request = buildRequest("req-inbound-idempotent", BigDecimal.valueOf(4));

        InboundOrder first = inboundOrderService.submitOrder(request, 1L);
        InboundOrder second = inboundOrderService.submitOrder(request, 1L);

        Stock stock = currentStock("B20260301");
        assertNotNull(first);
        assertNotNull(second);
        assertEquals(first.getId(), second.getId());
        assertEquals(0, stock.getQuantity().compareTo(BigDecimal.valueOf(24)));
    }

    private InboundSubmitRequest buildRequest(String requestId, BigDecimal quantity) {
        InboundSubmitItemRequest item = new InboundSubmitItemRequest();
        item.setProductId(1L);
        item.setQuantity(quantity);
        item.setUnitPrice(BigDecimal.valueOf(100));
        item.setBatchNo("B20260301");

        InboundSubmitRequest request = new InboundSubmitRequest();
        request.setRequestId(requestId);
        request.setWarehouseId(1L);
        request.setSupplierId(1L);
        request.setItems(List.of(item));
        return request;
    }

    private Stock currentStock(String batchNo) {
        QueryWrapper<Stock> wrapper = new QueryWrapper<>();
        wrapper.eq("warehouse_id", 1L)
                .eq("product_id", 1L)
                .eq("batch_no", batchNo)
                .last("limit 1");
        return stockService.getOne(wrapper);
    }
}
