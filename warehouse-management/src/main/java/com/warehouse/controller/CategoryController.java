package com.warehouse.controller;

import com.warehouse.audit.AuditOperation;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.entity.Category;
import com.warehouse.security.PermissionConstants;
import com.warehouse.security.RequiresPermission;
import com.warehouse.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@CrossOrigin
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/list")
    @RequiresPermission(PermissionConstants.CATEGORY_READ)
    public Result<List<Category>> list() {
        return Result.success(categoryService.list());
    }

    @GetMapping("/page")
    @RequiresPermission(PermissionConstants.CATEGORY_READ)
    public Result<PageResult<Category>> page(@RequestParam(defaultValue = "1") Integer current,
                                              @RequestParam(defaultValue = "10") Integer size,
                                              @RequestParam(required = false) String categoryName) {
        Page<Category> page = new Page<>(current, size);
        QueryWrapper<Category> wrapper = new QueryWrapper<>();
        if (categoryName != null && !categoryName.isEmpty()) {
            wrapper.like("category_name", categoryName);
        }
        Page<Category> result = categoryService.page(page, wrapper);
        PageResult<Category> pageResult = new PageResult<>(result.getTotal(), result.getRecords());
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @RequiresPermission(PermissionConstants.CATEGORY_READ)
    public Result<Category> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    @PostMapping
    @RequiresPermission(PermissionConstants.CATEGORY_MANAGE)
    @AuditOperation(action = "category.create", resource = "category")
    public Result<Boolean> save(@RequestBody Category category) {
        return Result.success(categoryService.save(category));
    }

    @PutMapping
    @RequiresPermission(PermissionConstants.CATEGORY_MANAGE)
    @AuditOperation(action = "category.update", resource = "category")
    public Result<Boolean> update(@RequestBody Category category) {
        return Result.success(categoryService.updateById(category));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission(PermissionConstants.CATEGORY_MANAGE)
    @AuditOperation(action = "category.delete", resource = "category")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(categoryService.removeById(id));
    }
}
