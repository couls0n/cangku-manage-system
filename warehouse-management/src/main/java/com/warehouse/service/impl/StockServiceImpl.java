package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.entity.Stock;
import com.warehouse.mapper.StockMapper;
import com.warehouse.service.StockService;
import com.warehouse.stock.InsufficientStockException;
import com.warehouse.stock.StockLockException;
import com.warehouse.stock.StockLockProperties;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class StockServiceImpl extends ServiceImpl<StockMapper, Stock> implements StockService {

    private final RedissonClient redissonClient;
    private final StockLockProperties stockLockProperties;
    private final Map<String, ReentrantLock> localLocks = new ConcurrentHashMap<>();

    public StockServiceImpl(ObjectProvider<RedissonClient> redissonClientProvider, StockLockProperties stockLockProperties) {
        this.redissonClient = redissonClientProvider.getIfAvailable();
        this.stockLockProperties = stockLockProperties;
    }

    @Override
    public <T> T executeWithStockLocks(Long warehouseId, Collection<Long> productIds, Supplier<T> action) {
        List<LockHandle> acquiredLocks = new ArrayList<>();
        List<Long> sortedProductIds = productIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        try {
            for (Long productId : sortedProductIds) {
                acquiredLocks.add(acquireLock(warehouseId, productId));
            }
            return action.get();
        } finally {
            for (int index = acquiredLocks.size() - 1; index >= 0; index--) {
                acquiredLocks.get(index).unlock();
            }
        }
    }

    @Override
    public void deductStocksInLock(Long warehouseId, Map<Long, BigDecimal> productQuantities) {
        for (Map.Entry<Long, BigDecimal> entry : productQuantities.entrySet()) {
            deductSingleProduct(warehouseId, entry.getKey(), entry.getValue());
        }
    }

    private void deductSingleProduct(Long warehouseId, Long productId, BigDecimal requestedQuantity) {
        if (requestedQuantity == null || requestedQuantity.signum() <= 0) {
            throw new IllegalArgumentException("Outbound quantity must be greater than 0");
        }

        QueryWrapper<Stock> wrapper = new QueryWrapper<>();
        wrapper.eq("warehouse_id", warehouseId)
                .eq("product_id", productId)
                .gt("quantity", 0)
                .orderByAsc("id");
        List<Stock> stocks = list(wrapper);
        if (stocks.isEmpty()) {
            throw new InsufficientStockException("Product " + productId + " has no available stock");
        }

        BigDecimal totalAvailable = stocks.stream()
                .map(this::availableQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalAvailable.compareTo(requestedQuantity) < 0) {
            throw new InsufficientStockException("Product " + productId + " has insufficient stock, available=" + totalAvailable);
        }

        BigDecimal remaining = requestedQuantity;
        for (Stock stock : stocks) {
            BigDecimal available = availableQuantity(stock);
            if (available.signum() <= 0) {
                continue;
            }
            BigDecimal deductQuantity = remaining.min(available);
            int updatedRows = baseMapper.deductAvailableStock(stock.getId(), deductQuantity);
            if (updatedRows != 1) {
                throw new StockLockException("Failed to deduct stock, please retry");
            }
            remaining = remaining.subtract(deductQuantity);
            if (remaining.signum() == 0) {
                return;
            }
        }

        throw new InsufficientStockException("Product " + productId + " stock deduction not completed");
    }

    private BigDecimal availableQuantity(Stock stock) {
        BigDecimal quantity = stock.getQuantity() == null ? BigDecimal.ZERO : stock.getQuantity();
        BigDecimal frozenQuantity = stock.getFrozenQuantity() == null ? BigDecimal.ZERO : stock.getFrozenQuantity();
        return quantity.subtract(frozenQuantity).max(BigDecimal.ZERO);
    }

    private LockHandle acquireLock(Long warehouseId, Long productId) {
        String lockKey = "stock:deduct:" + warehouseId + ":" + productId;
        if (!stockLockProperties.isEnabled()) {
            ReentrantLock localLock = localLocks.computeIfAbsent(lockKey, ignored -> new ReentrantLock());
            boolean locked;
            try {
                locked = localLock.tryLock(stockLockProperties.getWaitSeconds(), TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new StockLockException("Interrupted while waiting for local stock lock", ex);
            }
            if (!locked) {
                throw new StockLockException("Stock operation is busy, please retry");
            }
            return localLock::unlock;
        }

        if (redissonClient == null) {
            throw new StockLockException("RedissonClient is not initialized");
        }

        RLock lock = redissonClient.getLock(lockKey);
        boolean locked;
        try {
            locked = lock.tryLock(
                    stockLockProperties.getWaitSeconds(),
                    stockLockProperties.getLeaseSeconds(),
                    TimeUnit.SECONDS
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new StockLockException("Interrupted while waiting for distributed stock lock", ex);
        }
        if (!locked) {
            throw new StockLockException("Stock deduction is busy, please retry");
        }
        return () -> {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        };
    }

    @FunctionalInterface
    private interface LockHandle {
        void unlock();
    }
}
