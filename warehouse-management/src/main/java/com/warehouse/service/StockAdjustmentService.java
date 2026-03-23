package com.warehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.dto.common.ApprovalRequest;
import com.warehouse.dto.stock.StockAdjustmentRequest;
import com.warehouse.dto.stock.StockChangeRequest;
import com.warehouse.entity.StockAdjustment;

public interface StockAdjustmentService extends IService<StockAdjustment> {

    StockAdjustment adjustStock(StockAdjustmentRequest request, Long operatorId);

    StockAdjustment submitLoss(StockChangeRequest request, Long operatorId);

    StockAdjustment submitOverflow(StockChangeRequest request, Long operatorId);

    StockAdjustment reportLoss(StockChangeRequest request, Long operatorId);

    StockAdjustment reportOverflow(StockChangeRequest request, Long operatorId);

    StockAdjustment approveAdjustment(Long adjustmentId, ApprovalRequest request, Long approverId);

    Page<StockAdjustment> pageAdjustments(int current, int size, Long warehouseId, Long productId, Long stockId, String status);
}
