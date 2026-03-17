package com.warehouse.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.entity.Product;
import com.warehouse.security.AccessGuard;
import com.warehouse.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@CrossOrigin
public class ProductController {

    private final ProductService productService;
    private final AccessGuard accessGuard;

    public ProductController(ProductService productService, AccessGuard accessGuard) {
        this.productService = productService;
        this.accessGuard = accessGuard;
    }

    @GetMapping("/list")
    public Result<List<Product>> list() {
        return Result.success(productService.list());
    }

    @GetMapping("/page")
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
    public Result<Product> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody Product product) {
        accessGuard.requireAdmin();
        return Result.success(productService.save(product));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody Product product) {
        accessGuard.requireAdmin();
        return Result.success(productService.updateById(product));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        accessGuard.requireAdmin();
        return Result.success(productService.removeById(id));
    }
}
