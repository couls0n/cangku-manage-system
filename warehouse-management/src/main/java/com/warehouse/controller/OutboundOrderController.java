package com.warehouse.controller;

import com.warehouse.audit.AuditOperation;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.warehouse.common.OperationNotAllowedException;
import com.warehouse.common.PageResult;
import com.warehouse.common.ResourceNotFoundException;
import com.warehouse.common.Result;
import com.warehouse.dto.outbound.OutboundSubmitRequest;
import com.warehouse.entity.OutboundOrder;
import com.warehouse.security.AccessGuard;
import com.warehouse.security.PermissionConstants;
import com.warehouse.security.RequiresPermission;
import com.warehouse.service.OutboundOrderService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/outbound")
@CrossOrigin
public class OutboundOrderController {

    private final OutboundOrderService outboundOrderService;
    private final AccessGuard accessGuard;

    public OutboundOrderController(OutboundOrderService outboundOrderService, AccessGuard accessGuard) {
        this.outboundOrderService = outboundOrderService;
        this.accessGuard = accessGuard;
    }

    @GetMapping("/list")
    @RequiresPermission(PermissionConstants.OUTBOUND_READ)
    public Result<List<OutboundOrder>> list() {
        QueryWrapper<OutboundOrder> wrapper = new QueryWrapper<>();
        if (!accessGuard.currentUser().isAdmin()) {
            wrapper.eq("warehouse_id", accessGuard.resolveWarehouseScope(null));
        }
        return Result.success(outboundOrderService.list(wrapper));
    }

    @GetMapping("/page")
    @RequiresPermission(PermissionConstants.OUTBOUND_READ)
    public Result<PageResult<OutboundOrder>> page(@RequestParam(defaultValue = "1") Integer current,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  @RequestParam(required = false) String orderNo,
                                                  @RequestParam(required = false) Long warehouseId) {
        Page<OutboundOrder> page = new Page<>(current, size);
        QueryWrapper<OutboundOrder> wrapper = new QueryWrapper<>();
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like("order_no", orderNo);
        }
        Long scopedWarehouseId = accessGuard.resolveWarehouseScope(warehouseId);
        if (scopedWarehouseId != null) {
            wrapper.eq("warehouse_id", scopedWarehouseId);
        }
        wrapper.orderByDesc("create_time");
        Page<OutboundOrder> result = outboundOrderService.page(page, wrapper);
        PageResult<OutboundOrder> pageResult = new PageResult<>(result.getTotal(), result.getRecords());
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @RequiresPermission(PermissionConstants.OUTBOUND_READ)
    public Result<OutboundOrder> getById(@PathVariable Long id) {
        OutboundOrder outboundOrder = outboundOrderService.getById(id);
        if (outboundOrder == null) {
            throw new ResourceNotFoundException("Outbound order not found: " + id);
        }
        accessGuard.checkWarehouseAccess(outboundOrder.getWarehouseId());
        return Result.success(outboundOrder);
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody OutboundOrder outboundOrder) {
        throw new OperationNotAllowedException("Use /api/outbound/submit to create outbound orders");
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody OutboundOrder outboundOrder) {
        throw new OperationNotAllowedException("Outbound orders cannot be updated directly");
    }

    @PostMapping("/submit")
    @RequiresPermission(PermissionConstants.OUTBOUND_SUBMIT)
    @AuditOperation(action = "outbound.submit", resource = "outbound_order")
    public Result<OutboundOrder> submit(@Valid @RequestBody OutboundSubmitRequest request) {
        Long scopedWarehouseId = accessGuard.resolveWarehouseScope(request.getWarehouseId());
        request.setWarehouseId(scopedWarehouseId);
        return Result.success(outboundOrderService.submitOrder(request, accessGuard.currentUser().getId()));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        throw new OperationNotAllowedException("Outbound orders cannot be deleted directly");
    }
}
