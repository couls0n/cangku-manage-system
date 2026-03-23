package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.common.OperationNotAllowedException;
import com.warehouse.common.ResourceNotFoundException;
import com.warehouse.dto.common.ApprovalRequest;
import com.warehouse.dto.stock.StockCheckRequest;
import com.warehouse.entity.Stock;
import com.warehouse.entity.StockCheckRecord;
import com.warehouse.mapper.StockCheckRecordMapper;
import com.warehouse.service.StockCheckRecordService;
import com.warehouse.service.StockService;
import com.warehouse.stock.StockQuantityChangeResult;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class StockCheckRecordServiceImpl extends ServiceImpl<StockCheckRecordMapper, StockCheckRecord> implements StockCheckRecordService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    private final StockService stockService;
    private final TransactionTemplate transactionTemplate;

    public StockCheckRecordServiceImpl(StockService stockService, TransactionTemplate transactionTemplate) {
        this.stockService = stockService;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public StockCheckRecord checkStock(StockCheckRequest request, Long operatorId) {
        return applyImmediateCheck(request, operatorId);
    }

    @Override
    public StockCheckRecord submitStockCheck(StockCheckRequest request, Long operatorId) {
        StockCheckRecord existing = findByRequestId(request.getRequestId());
        if (existing != null) {
            return existing;
        }

        Stock stock = requireStock(request.getStockId());
        BigDecimal systemQuantity = stock.getQuantity() == null ? BigDecimal.ZERO : stock.getQuantity();
        BigDecimal difference = request.getCountedQuantity().subtract(systemQuantity);

        StockCheckRecord record = new StockCheckRecord();
        record.setRequestId(request.getRequestId());
        record.setStockId(stock.getId());
        record.setWarehouseId(stock.getWarehouseId());
        record.setProductId(stock.getProductId());
        record.setOperatorId(operatorId);
        record.setSystemQuantity(systemQuantity);
        record.setCountedQuantity(request.getCountedQuantity());
        record.setDifferenceQuantity(difference);
        record.setResultType(resolveResultType(difference));
        record.setBatchNo(stock.getBatchNo());
        record.setReason(request.getReason());
        record.setRemark(request.getRemark());
        record.setStatus(STATUS_PENDING);
        save(record);
        return record;
    }

    @Override
    public StockCheckRecord approveCheck(Long checkId, ApprovalRequest request, Long approverId) {
        StockCheckRecord record = requireCheckRecord(checkId);
        if (!STATUS_PENDING.equals(record.getStatus())) {
            return record;
        }

        if (Boolean.FALSE.equals(request.getApproved())) {
            record.setStatus(STATUS_REJECTED);
            record.setApproverId(approverId);
            record.setApprovedTime(LocalDateTime.now());
            record.setApprovalComment(request.getComment());
            updateById(record);
            return record;
        }

        return stockService.executeWithStockLocks(record.getWarehouseId(), Collections.singletonList(record.getProductId()), () ->
                transactionTemplate.execute(status -> {
                    Stock stock = requireStock(record.getStockId());
                    BigDecimal currentQuantity = stock.getQuantity() == null ? BigDecimal.ZERO : stock.getQuantity();
                    if (currentQuantity.compareTo(record.getSystemQuantity()) != 0) {
                        throw new OperationNotAllowedException("Stock quantity changed after stock check submission; please resubmit");
                    }

                    if (record.getDifferenceQuantity().signum() != 0) {
                        StockQuantityChangeResult changeResult = stockService.changeStockQuantityInLock(record.getStockId(), record.getDifferenceQuantity());
                        record.setSystemQuantity(changeResult.getBeforeQuantity());
                    }
                    record.setStatus(STATUS_APPROVED);
                    record.setApproverId(approverId);
                    record.setApprovedTime(LocalDateTime.now());
                    record.setApprovalComment(request.getComment());
                    updateById(record);
                    return record;
                })
        );
    }

    @Override
    public Page<StockCheckRecord> pageChecks(int current, int size, Long warehouseId, Long productId, Long stockId, String status) {
        Page<StockCheckRecord> page = new Page<>(current, size);
        QueryWrapper<StockCheckRecord> wrapper = new QueryWrapper<>();
        if (warehouseId != null) {
            wrapper.eq("warehouse_id", warehouseId);
        }
        if (productId != null) {
            wrapper.eq("product_id", productId);
        }
        if (stockId != null) {
            wrapper.eq("stock_id", stockId);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        return page(page, wrapper);
    }

    private StockCheckRecord applyImmediateCheck(StockCheckRequest request, Long operatorId) {
        StockCheckRecord existing = findByRequestId(request.getRequestId());
        if (existing != null) {
            return existing;
        }

        Stock stock = requireStock(request.getStockId());
        BigDecimal systemQuantity = stock.getQuantity() == null ? BigDecimal.ZERO : stock.getQuantity();
        BigDecimal difference = request.getCountedQuantity().subtract(systemQuantity);

        try {
            return stockService.executeWithStockLocks(stock.getWarehouseId(), Collections.singletonList(stock.getProductId()), () ->
                    transactionTemplate.execute(status -> {
                        BigDecimal recordedSystemQuantity = systemQuantity;
                        if (difference.signum() != 0) {
                            stockService.changeStockQuantityInLock(stock.getId(), difference);
                        }

                        StockCheckRecord record = new StockCheckRecord();
                        record.setRequestId(request.getRequestId());
                        record.setStockId(stock.getId());
                        record.setWarehouseId(stock.getWarehouseId());
                        record.setProductId(stock.getProductId());
                        record.setOperatorId(operatorId);
                        record.setSystemQuantity(recordedSystemQuantity);
                        record.setCountedQuantity(request.getCountedQuantity());
                        record.setDifferenceQuantity(difference);
                        record.setResultType(resolveResultType(difference));
                        record.setBatchNo(stock.getBatchNo());
                        record.setReason(request.getReason());
                        record.setRemark(request.getRemark());
                        record.setStatus(STATUS_APPROVED);
                        record.setApproverId(operatorId);
                        record.setApprovedTime(LocalDateTime.now());
                        save(record);
                        return record;
                    })
            );
        } catch (DuplicateKeyException ex) {
            StockCheckRecord duplicated = findByRequestId(request.getRequestId());
            if (duplicated != null) {
                return duplicated;
            }
            throw ex;
        }
    }

    private Stock requireStock(Long stockId) {
        Stock stock = stockService.getById(stockId);
        if (stock == null) {
            throw new ResourceNotFoundException("Stock not found: " + stockId);
        }
        return stock;
    }

    private StockCheckRecord requireCheckRecord(Long checkId) {
        StockCheckRecord record = getById(checkId);
        if (record == null) {
            throw new ResourceNotFoundException("Stock check record not found: " + checkId);
        }
        return record;
    }

    private StockCheckRecord findByRequestId(String requestId) {
        QueryWrapper<StockCheckRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("request_id", requestId).last("limit 1");
        return getOne(wrapper);
    }

    private String resolveResultType(BigDecimal difference) {
        if (difference.signum() > 0) {
            return "CHECK_GAIN";
        }
        if (difference.signum() < 0) {
            return "CHECK_LOSS";
        }
        return "CHECK_MATCH";
    }
}
