package com.warehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.dto.common.ApprovalRequest;
import com.warehouse.dto.stock.StockCheckRequest;
import com.warehouse.entity.StockCheckRecord;

public interface StockCheckRecordService extends IService<StockCheckRecord> {

    StockCheckRecord checkStock(StockCheckRequest request, Long operatorId);

    StockCheckRecord submitStockCheck(StockCheckRequest request, Long operatorId);

    StockCheckRecord approveCheck(Long checkId, ApprovalRequest request, Long approverId);

    Page<StockCheckRecord> pageChecks(int current, int size, Long warehouseId, Long productId, Long stockId, String status);
}
