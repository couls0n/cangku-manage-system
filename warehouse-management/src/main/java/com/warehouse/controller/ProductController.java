package com.warehouse.controller;

import com.warehouse.audit.AuditOperation;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.entity.Product;
import com.warehouse.security.PermissionConstants;
import com.warehouse.security.RequiresPermission;
import com.warehouse.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@CrossOrigin
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/list")
    @RequiresPermission(PermissionConstants.PRODUCT_READ)
    public Result<List<Product>> list() {
        return Result.success(productService.list());
    }

    @GetMapping("/page")
    @RequiresPermission(PermissionConstants.PRODUCT_READ)
    public Result<PageResult<Product>> page(@RequestParam(defaultValue = "1") Integer current,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) String productName,
                                             @RequestParam(required = false) Long categoryId) {
        Page<Product> page = new Page<>(current, size);
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        if (productName != null && !productName.isEmpty()) {
            wrapper.like("product_name", productName);
        }
        if (categoryId != null) {
            wrapper.eq("category_id", categoryId);
        }
        Page<Product> result = productService.page(page, wrapper);
        PageResult<Product> pageResult = new PageResult<>(result.getTotal(), result.getRecords());
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @RequiresPermission(PermissionConstants.PRODUCT_READ)
    public Result<Product> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @PostMapping
    @RequiresPermission(PermissionConstants.PRODUCT_MANAGE)
    @AuditOperation(action = "product.create", resource = "product")
    public Result<Boolean> save(@RequestBody Product product) {
        return Result.success(productService.save(product));
    }

    @PutMapping
    @RequiresPermission(PermissionConstants.PRODUCT_MANAGE)
    @AuditOperation(action = "product.update", resource = "product")
    public Result<Boolean> update(@RequestBody Product product) {
        return Result.success(productService.updateById(product));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission(PermissionConstants.PRODUCT_MANAGE)
    @AuditOperation(action = "product.delete", resource = "product")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(productService.removeById(id));
    }
}
