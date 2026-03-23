package com.warehouse.controller;

import com.warehouse.audit.AuditOperation;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.warehouse.common.OperationNotAllowedException;
import com.warehouse.common.PageResult;
import com.warehouse.common.ResourceNotFoundException;
import com.warehouse.common.Result;
import com.warehouse.dto.inbound.InboundSubmitRequest;
import com.warehouse.entity.InboundOrder;
import com.warehouse.security.AccessGuard;
import com.warehouse.security.PermissionConstants;
import com.warehouse.security.RequiresPermission;
import com.warehouse.service.InboundOrderService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/inbound")
@CrossOrigin
public class InboundOrderController {

    private final InboundOrderService inboundOrderService;
    private final AccessGuard accessGuard;

    public InboundOrderController(InboundOrderService inboundOrderService, AccessGuard accessGuard) {
        this.inboundOrderService = inboundOrderService;
        this.accessGuard = accessGuard;
    }

    @GetMapping("/list")
    @RequiresPermission(PermissionConstants.INBOUND_READ)
    public Result<List<InboundOrder>> list() {
        QueryWrapper<InboundOrder> wrapper = new QueryWrapper<>();
        if (!accessGuard.currentUser().isAdmin()) {
            wrapper.eq("warehouse_id", accessGuard.resolveWarehouseScope(null));
        }
        return Result.success(inboundOrderService.list(wrapper));
    }

    @GetMapping("/page")
    @RequiresPermission(PermissionConstants.INBOUND_READ)
    public Result<PageResult<InboundOrder>> page(@RequestParam(defaultValue = "1") Integer current,
                                                 @RequestParam(defaultValue = "10") Integer size,
                                                 @RequestParam(required = false) String orderNo,
                                                 @RequestParam(required = false) Long warehouseId) {
        Page<InboundOrder> page = new Page<>(current, size);
        QueryWrapper<InboundOrder> wrapper = new QueryWrapper<>();
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like("order_no", orderNo);
        }
        Long scopedWarehouseId = accessGuard.resolveWarehouseScope(warehouseId);
        if (scopedWarehouseId != null) {
            wrapper.eq("warehouse_id", scopedWarehouseId);
        }
        wrapper.orderByDesc("create_time");
        Page<InboundOrder> result = inboundOrderService.page(page, wrapper);
        PageResult<InboundOrder> pageResult = new PageResult<>(result.getTotal(), result.getRecords());
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @RequiresPermission(PermissionConstants.INBOUND_READ)
    public Result<InboundOrder> getById(@PathVariable Long id) {
        InboundOrder inboundOrder = inboundOrderService.getById(id);
        if (inboundOrder == null) {
            throw new ResourceNotFoundException("Inbound order not found: " + id);
        }
        accessGuard.checkWarehouseAccess(inboundOrder.getWarehouseId());
        return Result.success(inboundOrder);
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody InboundOrder inboundOrder) {
        throw new OperationNotAllowedException("Use /api/inbound/submit to create inbound orders");
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody InboundOrder inboundOrder) {
        throw new OperationNotAllowedException("Inbound orders cannot be updated directly");
    }

    @PostMapping("/submit")
    @RequiresPermission(PermissionConstants.INBOUND_SUBMIT)
    @AuditOperation(action = "inbound.submit", resource = "inbound_order")
    public Result<InboundOrder> submit(@Valid @RequestBody InboundSubmitRequest request) {
        Long scopedWarehouseId = accessGuard.resolveWarehouseScope(request.getWarehouseId());
        request.setWarehouseId(scopedWarehouseId);
        return Result.success(inboundOrderService.submitOrder(request, accessGuard.currentUser().getId()));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        throw new OperationNotAllowedException("Inbound orders cannot be deleted directly");
    }
}
