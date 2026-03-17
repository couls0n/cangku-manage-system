package com.warehouse.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.entity.Warehouse;
import com.warehouse.security.AccessGuard;
import com.warehouse.service.WarehouseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouse")
@CrossOrigin
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final AccessGuard accessGuard;

    public WarehouseController(WarehouseService warehouseService, AccessGuard accessGuard) {
        this.warehouseService = warehouseService;
        this.accessGuard = accessGuard;
    }

    @GetMapping("/list")
    public Result<List<Warehouse>> list() {
        QueryWrapper<Warehouse> wrapper = new QueryWrapper<>();
        Long warehouseId = accessGuard.resolveWarehouseScope(null);
        if (!accessGuard.currentUser().isAdmin()) {
            wrapper.eq("id", warehouseId);
        }
        return Result.success(warehouseService.list(wrapper));
    }

    @GetMapping("/page")
    public Result<PageResult<Warehouse>> page(@RequestParam(defaultValue = "1") Integer current,
                                              @RequestParam(defaultValue = "10") Integer size,
                                              @RequestParam(required = false) String warehouseName) {
        Page<Warehouse> page = new Page<>(current, size);
        QueryWrapper<Warehouse> wrapper = new QueryWrapper<>();
        if (warehouseName != null && !warehouseName.isEmpty()) {
            wrapper.like("warehouse_name", warehouseName);
        }
        if (!accessGuard.currentUser().isAdmin()) {
            wrapper.eq("id", accessGuard.resolveWarehouseScope(null));
        }
        Page<Warehouse> result = warehouseService.page(page, wrapper);
        PageResult<Warehouse> pageResult = new PageResult<>(result.getTotal(), result.getRecords());
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<Warehouse> getById(@PathVariable Long id) {
        accessGuard.checkWarehouseAccess(id);
        return Result.success(warehouseService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody Warehouse warehouse) {
        accessGuard.requireAdmin();
        return Result.success(warehouseService.save(warehouse));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody Warehouse warehouse) {
        accessGuard.requireAdmin();
        return Result.success(warehouseService.updateById(warehouse));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        accessGuard.requireAdmin();
        return Result.success(warehouseService.removeById(id));
    }
}
