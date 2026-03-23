package com.warehouse.controller;

import com.warehouse.audit.AuditOperation;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.warehouse.common.PageResult;
import com.warehouse.common.ResourceNotFoundException;
import com.warehouse.common.Result;
import com.warehouse.dto.supplier.SupplierResponse;
import com.warehouse.dto.supplier.SupplierUpsertRequest;
import com.warehouse.entity.Supplier;
import com.warehouse.security.AccessGuard;
import com.warehouse.security.PermissionConstants;
import com.warehouse.security.RequiresPermission;
import com.warehouse.service.SupplierService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/supplier")
@CrossOrigin
@Validated
public class SupplierController {

    private final SupplierService supplierService;
    private final AccessGuard accessGuard;

    public SupplierController(SupplierService supplierService, AccessGuard accessGuard) {
        this.supplierService = supplierService;
        this.accessGuard = accessGuard;
    }

    @GetMapping("/list")
    @RequiresPermission(PermissionConstants.SUPPLIER_READ)
    public Result<List<SupplierResponse>> list() {
        accessGuard.currentUser();
        List<SupplierResponse> suppliers = supplierService.list().stream()
                .map(supplier -> SupplierResponse.from(supplier, false))
                .collect(Collectors.toList());
        return Result.success(suppliers);
    }

    @GetMapping("/page")
    @RequiresPermission(PermissionConstants.SUPPLIER_READ)
    public Result<PageResult<SupplierResponse>> page(@RequestParam(defaultValue = "1") Integer current,
                                                     @RequestParam(defaultValue = "10") Integer size,
                                                     @RequestParam(required = false) String supplierName) {
        accessGuard.currentUser();
        Page<Supplier> page = new Page<>(current, size);
        QueryWrapper<Supplier> wrapper = new QueryWrapper<>();
        if (supplierName != null && !supplierName.isEmpty()) {
            wrapper.like("supplier_name", supplierName);
        }
        Page<Supplier> result = supplierService.page(page, wrapper);
        List<SupplierResponse> records = result.getRecords().stream()
                .map(supplier -> SupplierResponse.from(supplier, false))
                .collect(Collectors.toList());
        PageResult<SupplierResponse> pageResult = new PageResult<>(result.getTotal(), records);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @RequiresPermission(PermissionConstants.SUPPLIER_READ)
    public Result<SupplierResponse> getById(@PathVariable Long id) {
        Supplier supplier = requireSupplier(id);
        boolean revealSensitive = accessGuard.currentUser().isAdmin();
        return Result.success(SupplierResponse.from(supplier, revealSensitive));
    }

    @PostMapping
    @RequiresPermission(PermissionConstants.SUPPLIER_MANAGE)
    @AuditOperation(action = "supplier.create", resource = "supplier")
    public Result<Boolean> save(@Valid @RequestBody SupplierUpsertRequest request) {
        Supplier supplier = toEntity(request);
        return Result.success(supplierService.save(supplier));
    }

    @PutMapping
    @RequiresPermission(PermissionConstants.SUPPLIER_MANAGE)
    @AuditOperation(action = "supplier.update", resource = "supplier")
    public Result<Boolean> update(@Valid @RequestBody SupplierUpsertRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("Supplier id cannot be null");
        }
        requireSupplier(request.getId());
        Supplier supplier = toEntity(request);
        return Result.success(supplierService.updateById(supplier));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission(PermissionConstants.SUPPLIER_MANAGE)
    @AuditOperation(action = "supplier.delete", resource = "supplier")
    public Result<Boolean> delete(@PathVariable Long id) {
        requireSupplier(id);
        return Result.success(supplierService.removeById(id));
    }

    private Supplier requireSupplier(Long id) {
        Supplier supplier = supplierService.getById(id);
        if (supplier == null) {
            throw new ResourceNotFoundException("Supplier not found: " + id);
        }
        return supplier;
    }

    private Supplier toEntity(SupplierUpsertRequest request) {
        Supplier supplier = new Supplier();
        supplier.setId(request.getId());
        supplier.setSupplierCode(request.getSupplierCode());
        supplier.setSupplierName(request.getSupplierName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setEmail(request.getEmail());
        supplier.setStatus(request.getStatus());
        supplier.setRemark(request.getRemark());
        return supplier;
    }
}
