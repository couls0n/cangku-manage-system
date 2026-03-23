package com.warehouse.controller;

import com.warehouse.audit.AuditOperation;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.warehouse.common.OperationNotAllowedException;
import com.warehouse.common.PageResult;
import com.warehouse.common.ResourceNotFoundException;
import com.warehouse.common.Result;
import com.warehouse.dto.common.ApprovalRequest;
import com.warehouse.dto.stock.StockChangeRequest;
import com.warehouse.dto.stock.StockAdjustmentRequest;
import com.warehouse.dto.stock.StockAdjustmentResponse;
import com.warehouse.dto.stock.StockCheckRecordResponse;
import com.warehouse.dto.stock.StockCheckRequest;
import com.warehouse.entity.Stock;
import com.warehouse.entity.StockAdjustment;
import com.warehouse.entity.StockCheckRecord;
import com.warehouse.security.AccessGuard;
import com.warehouse.security.PermissionConstants;
import com.warehouse.security.RequiresPermission;
import com.warehouse.service.StockAdjustmentService;
import com.warehouse.service.StockCheckRecordService;
import com.warehouse.service.StockService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stock")
@CrossOrigin
public class StockController {

    private final StockService stockService;
    private final StockAdjustmentService stockAdjustmentService;
    private final StockCheckRecordService stockCheckRecordService;
    private final AccessGuard accessGuard;

    public StockController(StockService stockService,
                           StockAdjustmentService stockAdjustmentService,
                           StockCheckRecordService stockCheckRecordService,
                           AccessGuard accessGuard) {
        this.stockService = stockService;
        this.stockAdjustmentService = stockAdjustmentService;
        this.stockCheckRecordService = stockCheckRecordService;
        this.accessGuard = accessGuard;
    }

    @GetMapping("/list")
    @RequiresPermission(PermissionConstants.STOCK_READ)
    public Result<List<Stock>> list() {
        QueryWrapper<Stock> wrapper = new QueryWrapper<>();
        if (!accessGuard.currentUser().isAdmin()) {
            wrapper.eq("warehouse_id", accessGuard.resolveWarehouseScope(null));
        }
        return Result.success(stockService.list(wrapper));
    }

    @GetMapping("/page")
    @RequiresPermission(PermissionConstants.STOCK_READ)
    public Result<PageResult<Stock>> page(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(required = false) Long warehouseId,
                                          @RequestParam(required = false) Long productId) {
        Page<Stock> page = new Page<>(current, size);
        QueryWrapper<Stock> wrapper = new QueryWrapper<>();
        Long scopedWarehouseId = accessGuard.resolveWarehouseScope(warehouseId);
        if (scopedWarehouseId != null) {
            wrapper.eq("warehouse_id", scopedWarehouseId);
        }
        if (productId != null) {
            wrapper.eq("product_id", productId);
        }
        Page<Stock> result = stockService.page(page, wrapper);
        PageResult<Stock> pageResult = new PageResult<>(result.getTotal(), result.getRecords());
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @RequiresPermission(PermissionConstants.STOCK_READ)
    public Result<Stock> getById(@PathVariable Long id) {
        Stock stock = stockService.getById(id);
        if (stock == null) {
            throw new ResourceNotFoundException("Stock not found: " + id);
        }
        accessGuard.checkWarehouseAccess(stock.getWarehouseId());
        return Result.success(stock);
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody Stock stock) {
        throw new OperationNotAllowedException("Direct stock creation is not allowed");
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody Stock stock) {
        throw new OperationNotAllowedException("Direct stock update is not allowed");
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        throw new OperationNotAllowedException("Direct stock deletion is not allowed");
    }

    @PostMapping("/adjustments")
    @RequiresPermission(PermissionConstants.STOCK_ADJUST_WRITE)
    @AuditOperation(action = "stock.adjust", resource = "stock")
    public Result<StockAdjustmentResponse> adjustStock(@Valid @RequestBody StockAdjustmentRequest request) {
        Stock stock = stockService.getById(request.getStockId());
        if (stock == null) {
            throw new ResourceNotFoundException("Stock not found: " + request.getStockId());
        }
        accessGuard.checkWarehouseAccess(stock.getWarehouseId());
        StockAdjustment adjustment = stockAdjustmentService.adjustStock(request, accessGuard.currentUser().getId());
        return Result.success(StockAdjustmentResponse.from(adjustment));
    }

    @PostMapping("/checks")
    @RequiresPermission(PermissionConstants.STOCK_CHECK_WRITE)
    @AuditOperation(action = "stock.check", resource = "stock")
    public Result<StockCheckRecordResponse> checkStock(@Valid @RequestBody StockCheckRequest request) {
        Stock stock = stockService.getById(request.getStockId());
        if (stock == null) {
            throw new ResourceNotFoundException("Stock not found: " + request.getStockId());
        }
        accessGuard.checkWarehouseAccess(stock.getWarehouseId());
        StockCheckRecord record = stockCheckRecordService.checkStock(request, accessGuard.currentUser().getId());
        return Result.success(StockCheckRecordResponse.from(record));
    }

    @PostMapping("/checks/apply")
    @RequiresPermission(PermissionConstants.STOCK_CHECK_WRITE)
    @AuditOperation(action = "stock.check.apply", resource = "stock")
    public Result<StockCheckRecordResponse> applyStockCheck(@Valid @RequestBody StockCheckRequest request) {
        Stock stock = stockService.getById(request.getStockId());
        if (stock == null) {
            throw new ResourceNotFoundException("Stock not found: " + request.getStockId());
        }
        accessGuard.checkWarehouseAccess(stock.getWarehouseId());
        StockCheckRecord record = stockCheckRecordService.submitStockCheck(request, accessGuard.currentUser().getId());
        return Result.success(StockCheckRecordResponse.from(record));
    }

    @PostMapping("/checks/{id}/approve")
    @RequiresPermission(PermissionConstants.STOCK_CHECK_APPROVE)
    @AuditOperation(action = "stock.check.approve", resource = "stock")
    public Result<StockCheckRecordResponse> approveStockCheck(@PathVariable Long id, @Valid @RequestBody ApprovalRequest request) {
        StockCheckRecord record = stockCheckRecordService.approveCheck(id, request, accessGuard.currentUser().getId());
        return Result.success(StockCheckRecordResponse.from(record));
    }

    @PostMapping("/losses")
    @RequiresPermission(PermissionConstants.STOCK_LOSS_WRITE)
    @AuditOperation(action = "stock.loss", resource = "stock")
    public Result<StockAdjustmentResponse> reportLoss(@Valid @RequestBody StockChangeRequest request) {
        Stock stock = stockService.getById(request.getStockId());
        if (stock == null) {
            throw new ResourceNotFoundException("Stock not found: " + request.getStockId());
        }
        accessGuard.checkWarehouseAccess(stock.getWarehouseId());
        StockAdjustment adjustment = stockAdjustmentService.reportLoss(request, accessGuard.currentUser().getId());
        return Result.success(StockAdjustmentResponse.from(adjustment));
    }

    @PostMapping("/losses/apply")
    @RequiresPermission(PermissionConstants.STOCK_LOSS_WRITE)
    @AuditOperation(action = "stock.loss.apply", resource = "stock")
    public Result<StockAdjustmentResponse> applyLoss(@Valid @RequestBody StockChangeRequest request) {
        Stock stock = stockService.getById(request.getStockId());
        if (stock == null) {
            throw new ResourceNotFoundException("Stock not found: " + request.getStockId());
        }
        accessGuard.checkWarehouseAccess(stock.getWarehouseId());
        StockAdjustment adjustment = stockAdjustmentService.submitLoss(request, accessGuard.currentUser().getId());
        return Result.success(StockAdjustmentResponse.from(adjustment));
    }

    @PostMapping("/overflows")
    @RequiresPermission(PermissionConstants.STOCK_OVERFLOW_WRITE)
    @AuditOperation(action = "stock.overflow", resource = "stock")
    public Result<StockAdjustmentResponse> reportOverflow(@Valid @RequestBody StockChangeRequest request) {
        Stock stock = stockService.getById(request.getStockId());
        if (stock == null) {
            throw new ResourceNotFoundException("Stock not found: " + request.getStockId());
        }
        accessGuard.checkWarehouseAccess(stock.getWarehouseId());
        StockAdjustment adjustment = stockAdjustmentService.reportOverflow(request, accessGuard.currentUser().getId());
        return Result.success(StockAdjustmentResponse.from(adjustment));
    }

    @PostMapping("/overflows/apply")
    @RequiresPermission(PermissionConstants.STOCK_OVERFLOW_WRITE)
    @AuditOperation(action = "stock.overflow.apply", resource = "stock")
    public Result<StockAdjustmentResponse> applyOverflow(@Valid @RequestBody StockChangeRequest request) {
        Stock stock = stockService.getById(request.getStockId());
        if (stock == null) {
            throw new ResourceNotFoundException("Stock not found: " + request.getStockId());
        }
        accessGuard.checkWarehouseAccess(stock.getWarehouseId());
        StockAdjustment adjustment = stockAdjustmentService.submitOverflow(request, accessGuard.currentUser().getId());
        return Result.success(StockAdjustmentResponse.from(adjustment));
    }

    @PostMapping("/adjustments/{id}/approve")
    @RequiresPermission(PermissionConstants.STOCK_ADJUST_APPROVE)
    @AuditOperation(action = "stock.adjust.approve", resource = "stock")
    public Result<StockAdjustmentResponse> approveAdjustment(@PathVariable Long id, @Valid @RequestBody ApprovalRequest request) {
        StockAdjustment adjustment = stockAdjustmentService.approveAdjustment(id, request, accessGuard.currentUser().getId());
        return Result.success(StockAdjustmentResponse.from(adjustment));
    }

    @GetMapping("/adjustments/page")
    @RequiresPermission(PermissionConstants.STOCK_ADJUST_READ)
    public Result<PageResult<StockAdjustmentResponse>> pageAdjustments(@RequestParam(defaultValue = "1") Integer current,
                                                                      @RequestParam(defaultValue = "10") Integer size,
                                                                      @RequestParam(required = false) Long warehouseId,
                                                                      @RequestParam(required = false) Long productId,
                                                                      @RequestParam(required = false) Long stockId,
                                                                      @RequestParam(required = false) String status) {
        Long scopedWarehouseId = accessGuard.resolveWarehouseScope(warehouseId);
        Page<StockAdjustment> result = stockAdjustmentService.pageAdjustments(current, size, scopedWarehouseId, productId, stockId, status);
        List<StockAdjustmentResponse> records = result.getRecords().stream()
                .map(StockAdjustmentResponse::from)
                .collect(Collectors.toList());
        return Result.success(new PageResult<>(result.getTotal(), records));
    }

    @GetMapping("/checks/page")
    @RequiresPermission(PermissionConstants.STOCK_CHECK_READ)
    public Result<PageResult<StockCheckRecordResponse>> pageChecks(@RequestParam(defaultValue = "1") Integer current,
                                                                   @RequestParam(defaultValue = "10") Integer size,
                                                                   @RequestParam(required = false) Long warehouseId,
                                                                   @RequestParam(required = false) Long productId,
                                                                   @RequestParam(required = false) Long stockId,
                                                                   @RequestParam(required = false) String status) {
        Long scopedWarehouseId = accessGuard.resolveWarehouseScope(warehouseId);
        Page<StockCheckRecord> result = stockCheckRecordService.pageChecks(current, size, scopedWarehouseId, productId, stockId, status);
        List<StockCheckRecordResponse> records = result.getRecords().stream()
                .map(StockCheckRecordResponse::from)
                .collect(Collectors.toList());
        return Result.success(new PageResult<>(result.getTotal(), records));
    }
}
