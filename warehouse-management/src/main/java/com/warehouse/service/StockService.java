package com.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.entity.Stock;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

public interface StockService extends IService<Stock> {

    <T> T executeWithStockLocks(Long warehouseId, Collection<Long> productIds, Supplier<T> action);

    void deductStocksInLock(Long warehouseId, Map<Long, BigDecimal> productQuantities);
}
