package com.warehouse.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.entity.Supplier;
import com.warehouse.security.AccessGuard;
import com.warehouse.service.SupplierService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplier")
@CrossOrigin
public class SupplierController {

    private final SupplierService supplierService;
    private final AccessGuard accessGuard;

    public SupplierController(SupplierService supplierService, AccessGuard accessGuard) {
        this.supplierService = supplierService;
        this.accessGuard = accessGuard;
    }

    @GetMapping("/list")
    public Result<List<Supplier>> list() {
        return Result.success(supplierService.list());
    }

    @GetMapping("/page")
    public Result<PageResult<Supplier>> page(@RequestParam(defaultValue = "1") Integer current,
                                              @RequestParam(defaultValue = "10") Integer size,
                                              @RequestParam(required = false) String supplierName) {
        Page<Supplier> page = new Page<>(current, size);
        QueryWrapper<Supplier> wrapper = new QueryWrapper<>();
        if (supplierName != null && !supplierName.isEmpty()) {
            wrapper.like("supplier_name", supplierName);
        }
        Page<Supplier> result = supplierService.page(page, wrapper);
        PageResult<Supplier> pageResult = new PageResult<>(result.getTotal(), result.getRecords());
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<Supplier> getById(@PathVariable Long id) {
        return Result.success(supplierService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody Supplier supplier) {
        accessGuard.requireAdmin();
        return Result.success(supplierService.save(supplier));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody Supplier supplier) {
        accessGuard.requireAdmin();
        return Result.success(supplierService.updateById(supplier));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        accessGuard.requireAdmin();
        return Result.success(supplierService.removeById(id));
    }
}
