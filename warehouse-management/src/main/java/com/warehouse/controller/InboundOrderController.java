package com.warehouse.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.entity.InboundOrder;
import com.warehouse.security.AccessGuard;
import com.warehouse.service.InboundOrderService;
import org.springframework.web.bind.annotation.*;

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
    public Result<List<InboundOrder>> list() {
        QueryWrapper<InboundOrder> wrapper = new QueryWrapper<>();
        if (!accessGuard.currentUser().isAdmin()) {
            wrapper.eq("warehouse_id", accessGuard.resolveWarehouseScope(null));
        }
        return Result.success(inboundOrderService.list(wrapper));
    }

    @GetMapping("/page")
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
    public Result<InboundOrder> getById(@PathVariable Long id) {
        InboundOrder inboundOrder = inboundOrderService.getById(id);
        if (inboundOrder != null) {
            accessGuard.checkWarehouseAccess(inboundOrder.getWarehouseId());
        }
        return Result.success(inboundOrder);
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody InboundOrder inboundOrder) {
        accessGuard.checkWarehouseAccess(inboundOrder.getWarehouseId());
        if (!accessGuard.currentUser().isAdmin()) {
            inboundOrder.setOperatorId(accessGuard.currentUser().getId());
        }
        return Result.success(inboundOrderService.save(inboundOrder));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody InboundOrder inboundOrder) {
        accessGuard.checkWarehouseAccess(inboundOrder.getWarehouseId());
        if (!accessGuard.currentUser().isAdmin()) {
            inboundOrder.setOperatorId(accessGuard.currentUser().getId());
        }
        return Result.success(inboundOrderService.updateById(inboundOrder));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        InboundOrder inboundOrder = inboundOrderService.getById(id);
        if (inboundOrder != null) {
            accessGuard.checkWarehouseAccess(inboundOrder.getWarehouseId());
        }
        return Result.success(inboundOrderService.removeById(id));
    }
}
