package com.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.entity.Stock;
import com.warehouse.stock.StockInboundCommand;
import com.warehouse.stock.StockQuantityChangeResult;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface StockService extends IService<Stock> {

    <T> T executeWithStockLocks(Long warehouseId, Collection<Long> productIds, Supplier<T> action);

    void deductStocksInLock(Long warehouseId, Map<Long, BigDecimal> productQuantities);

    void addStocksInLock(Long warehouseId, List<StockInboundCommand> stockCommands);

    StockQuantityChangeResult changeStockQuantityInLock(Long stockId, BigDecimal quantityChange);
}
