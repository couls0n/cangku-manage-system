package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.common.OperationNotAllowedException;
import com.warehouse.common.ResourceNotFoundException;
import com.warehouse.dto.common.ApprovalRequest;
import com.warehouse.dto.stock.StockAdjustmentRequest;
import com.warehouse.dto.stock.StockChangeRequest;
import com.warehouse.entity.Stock;
import com.warehouse.entity.StockAdjustment;
import com.warehouse.mapper.StockAdjustmentMapper;
import com.warehouse.service.StockAdjustmentService;
import com.warehouse.service.StockService;
import com.warehouse.stock.StockQuantityChangeResult;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class StockAdjustmentServiceImpl extends ServiceImpl<StockAdjustmentMapper, StockAdjustment> implements StockAdjustmentService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    private final StockService stockService;
    private final TransactionTemplate transactionTemplate;

    public StockAdjustmentServiceImpl(StockService stockService, TransactionTemplate transactionTemplate) {
        this.stockService = stockService;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public StockAdjustment adjustStock(StockAdjustmentRequest request, Long operatorId) {
        String adjustmentType = request.getQuantityChange().signum() > 0 ? "MANUAL_INCREASE" : "MANUAL_DECREASE";
        return applyImmediateAdjustment(
                request.getRequestId(),
                request.getStockId(),
                request.getQuantityChange(),
                request.getReason(),
                request.getRemark(),
                operatorId,
                adjustmentType
        );
    }

    @Override
    public StockAdjustment submitLoss(StockChangeRequest request, Long operatorId) {
        return createPendingAdjustment(
                request.getRequestId(),
                request.getStockId(),
                request.getQuantity().negate(),
                request.getReason(),
                request.getRemark(),
                operatorId,
                "LOSS"
        );
    }

    @Override
    public StockAdjustment submitOverflow(StockChangeRequest request, Long operatorId) {
        return createPendingAdjustment(
                request.getRequestId(),
                request.getStockId(),
                request.getQuantity(),
                request.getReason(),
                request.getRemark(),
                operatorId,
                "OVERFLOW"
        );
    }

    @Override
    public StockAdjustment reportLoss(StockChangeRequest request, Long operatorId) {
        return applyImmediateAdjustment(
                request.getRequestId(),
                request.getStockId(),
                request.getQuantity().negate(),
                request.getReason(),
                request.getRemark(),
                operatorId,
                "LOSS"
        );
    }

    @Override
    public StockAdjustment reportOverflow(StockChangeRequest request, Long operatorId) {
        return applyImmediateAdjustment(
                request.getRequestId(),
                request.getStockId(),
                request.getQuantity(),
                request.getReason(),
                request.getRemark(),
                operatorId,
                "OVERFLOW"
        );
    }

    @Override
    public StockAdjustment approveAdjustment(Long adjustmentId, ApprovalRequest request, Long approverId) {
        StockAdjustment adjustment = requireAdjustment(adjustmentId);
        if (!STATUS_PENDING.equals(adjustment.getStatus())) {
            return adjustment;
        }

        if (Boolean.FALSE.equals(request.getApproved())) {
            adjustment.setStatus(STATUS_REJECTED);
            adjustment.setApproverId(approverId);
            adjustment.setApprovedTime(LocalDateTime.now());
            adjustment.setApprovalComment(request.getComment());
            updateById(adjustment);
            return adjustment;
        }

        return stockService.executeWithStockLocks(adjustment.getWarehouseId(), Collections.singletonList(adjustment.getProductId()), () ->
                transactionTemplate.execute(status -> {
                    Stock stock = requireStock(adjustment.getStockId());
                    BigDecimal currentQuantity = stock.getQuantity() == null ? BigDecimal.ZERO : stock.getQuantity();
                    if (currentQuantity.compareTo(adjustment.getBeforeQuantity()) != 0) {
                        throw new OperationNotAllowedException("Stock quantity changed after adjustment submission; please resubmit");
                    }

                    StockQuantityChangeResult changeResult = stockService.changeStockQuantityInLock(adjustment.getStockId(), adjustment.getQuantityChange());
                    adjustment.setBeforeQuantity(changeResult.getBeforeQuantity());
                    adjustment.setAfterQuantity(changeResult.getAfterQuantity());
                    adjustment.setStatus(STATUS_APPROVED);
                    adjustment.setApproverId(approverId);
                    adjustment.setApprovedTime(LocalDateTime.now());
                    adjustment.setApprovalComment(request.getComment());
                    updateById(adjustment);
                    return adjustment;
                })
        );
    }

    @Override
    public Page<StockAdjustment> pageAdjustments(int current, int size, Long warehouseId, Long productId, Long stockId, String status) {
        Page<StockAdjustment> page = new Page<>(current, size);
        QueryWrapper<StockAdjustment> wrapper = new QueryWrapper<>();
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

    private StockAdjustment applyImmediateAdjustment(String requestId,
                                                    Long stockId,
                                                    BigDecimal quantityChange,
                                                    String reason,
                                                    String remark,
                                                    Long operatorId,
                                                    String adjustmentType) {
        StockAdjustment existing = findByRequestId(requestId);
        if (existing != null) {
            return existing;
        }

        Stock stock = requireStock(stockId);
        try {
            return stockService.executeWithStockLocks(stock.getWarehouseId(), Collections.singletonList(stock.getProductId()), () ->
                    transactionTemplate.execute(status -> {
                        StockQuantityChangeResult changeResult = stockService.changeStockQuantityInLock(stockId, quantityChange);

                        StockAdjustment adjustment = buildAdjustment(requestId, operatorId, adjustmentType, quantityChange, reason, remark, changeResult);
                        adjustment.setStatus(STATUS_APPROVED);
                        adjustment.setApproverId(operatorId);
                        adjustment.setApprovedTime(LocalDateTime.now());
                        save(adjustment);
                        return adjustment;
                    })
            );
        } catch (DuplicateKeyException ex) {
            StockAdjustment duplicated = findByRequestId(requestId);
            if (duplicated != null) {
                return duplicated;
            }
            throw ex;
        }
    }

    private StockAdjustment createPendingAdjustment(String requestId,
                                                    Long stockId,
                                                    BigDecimal quantityChange,
                                                    String reason,
                                                    String remark,
                                                    Long operatorId,
                                                    String adjustmentType) {
        StockAdjustment existing = findByRequestId(requestId);
        if (existing != null) {
            return existing;
        }

        Stock stock = requireStock(stockId);
        BigDecimal beforeQuantity = stock.getQuantity() == null ? BigDecimal.ZERO : stock.getQuantity();
        StockAdjustment adjustment = new StockAdjustment();
        adjustment.setRequestId(requestId);
        adjustment.setStockId(stock.getId());
        adjustment.setWarehouseId(stock.getWarehouseId());
        adjustment.setProductId(stock.getProductId());
        adjustment.setOperatorId(operatorId);
        adjustment.setAdjustmentType(adjustmentType);
        adjustment.setQuantityChange(quantityChange);
        adjustment.setBeforeQuantity(beforeQuantity);
        adjustment.setAfterQuantity(beforeQuantity.add(quantityChange));
        adjustment.setBatchNo(stock.getBatchNo());
        adjustment.setReason(reason);
        adjustment.setRemark(remark);
        adjustment.setStatus(STATUS_PENDING);
        save(adjustment);
        return adjustment;
    }

    private StockAdjustment buildAdjustment(String requestId,
                                            Long operatorId,
                                            String adjustmentType,
                                            BigDecimal quantityChange,
                                            String reason,
                                            String remark,
                                            StockQuantityChangeResult changeResult) {
        StockAdjustment adjustment = new StockAdjustment();
        adjustment.setRequestId(requestId);
        adjustment.setStockId(changeResult.getStockId());
        adjustment.setWarehouseId(changeResult.getWarehouseId());
        adjustment.setProductId(changeResult.getProductId());
        adjustment.setOperatorId(operatorId);
        adjustment.setAdjustmentType(adjustmentType);
        adjustment.setQuantityChange(quantityChange);
        adjustment.setBeforeQuantity(changeResult.getBeforeQuantity());
        adjustment.setAfterQuantity(changeResult.getAfterQuantity());
        adjustment.setBatchNo(changeResult.getBatchNo());
        adjustment.setReason(reason);
        adjustment.setRemark(remark);
        return adjustment;
    }

    private Stock requireStock(Long stockId) {
        Stock stock = stockService.getById(stockId);
        if (stock == null) {
            throw new ResourceNotFoundException("Stock not found: " + stockId);
        }
        return stock;
    }

    private StockAdjustment requireAdjustment(Long adjustmentId) {
        StockAdjustment adjustment = getById(adjustmentId);
        if (adjustment == null) {
            throw new ResourceNotFoundException("Stock adjustment not found: " + adjustmentId);
        }
        return adjustment;
    }

    private StockAdjustment findByRequestId(String requestId) {
        QueryWrapper<StockAdjustment> wrapper = new QueryWrapper<>();
        wrapper.eq("request_id", requestId).last("limit 1");
        return getOne(wrapper);
    }
}
