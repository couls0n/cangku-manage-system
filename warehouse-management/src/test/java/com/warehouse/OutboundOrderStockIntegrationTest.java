package com.warehouse;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.warehouse.dto.outbound.OutboundSubmitItemRequest;
import com.warehouse.dto.outbound.OutboundSubmitRequest;
import com.warehouse.entity.Stock;
import com.warehouse.service.OutboundOrderService;
import com.warehouse.service.StockService;
import com.warehouse.stock.InsufficientStockException;
import com.warehouse.stock.StockLockException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:warehouse_stock;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.flyway.locations=classpath:db/migration,classpath:db/dev",
        "warehouse.security.token-secret=test-token-secret",
        "warehouse.security.ebpf-ingest-key=test-ebpf-key",
        "warehouse.stock.lock.enabled=false",
        "warehouse.stock.lock.wait-seconds=30"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OutboundOrderStockIntegrationTest {

    @Autowired
    private OutboundOrderService outboundOrderService;

    @Autowired
    private StockService stockService;

    @Test
    void submitOrderShouldDeductStockAndPersistOrder() {
        OutboundSubmitRequest request = buildRequest(BigDecimal.valueOf(3));

        outboundOrderService.submitOrder(request, 1L);

        Stock stock = currentStock();
        assertEquals(0, stock.getQuantity().compareTo(BigDecimal.valueOf(17)));
    }

    @Test
    void submitOrderShouldRejectWhenStockIsInsufficient() {
        OutboundSubmitRequest request = buildRequest(BigDecimal.valueOf(25));

        assertThrows(InsufficientStockException.class, () -> outboundOrderService.submitOrder(request, 1L));
    }

    @Test
    void concurrentSubmitShouldNotOversell() throws Exception {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failedCount = new AtomicInteger();

        List<Future<?>> futures = java.util.stream.IntStream.range(0, threadCount)
                .mapToObj(index -> executorService.submit(() -> {
                    ready.countDown();
                    start.await();
                    try {
                        stockService.executeWithStockLocks(1L, Collections.singletonList(1L), () -> {
                            stockService.deductStocksInLock(1L, Collections.singletonMap(1L, BigDecimal.valueOf(3)));
                            return null;
                        });
                        successCount.incrementAndGet();
                    } catch (InsufficientStockException | StockLockException ex) {
                        failedCount.incrementAndGet();
                    }
                    return null;
                }))
                .collect(Collectors.toList());

        try {
            ready.await();
            start.countDown();
            for (Future<?> future : futures) {
                future.get(30, TimeUnit.SECONDS);
            }
        } finally {
            executorService.shutdownNow();
        }

        Stock stock = currentStock();
        assertEquals(6, successCount.get());
        assertEquals(4, failedCount.get());
        assertEquals(0, stock.getQuantity().compareTo(BigDecimal.valueOf(2)));
    }

    private OutboundSubmitRequest buildRequest(BigDecimal quantity) {
        OutboundSubmitItemRequest item = new OutboundSubmitItemRequest();
        item.setProductId(1L);
        item.setQuantity(quantity);
        item.setUnitPrice(BigDecimal.valueOf(100));

        OutboundSubmitRequest request = new OutboundSubmitRequest();
        request.setRequestId("req-" + quantity.toPlainString().replace('.', '-') + "-" + System.nanoTime());
        request.setWarehouseId(1L);
        request.setCustomerId(1L);
        request.setItems(List.of(item));
        return request;
    }

    private Stock currentStock() {
        QueryWrapper<Stock> wrapper = new QueryWrapper<>();
        wrapper.eq("warehouse_id", 1L)
                .eq("product_id", 1L)
                .last("limit 1");
        return stockService.getOne(wrapper);
    }
}
