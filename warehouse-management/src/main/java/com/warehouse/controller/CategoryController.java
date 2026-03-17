package com.warehouse.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.entity.Category;
import com.warehouse.security.AccessGuard;
import com.warehouse.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@CrossOrigin
public class CategoryController {

    private final CategoryService categoryService;
    private final AccessGuard accessGuard;

    public CategoryController(CategoryService categoryService, AccessGuard accessGuard) {
        this.categoryService = categoryService;
        this.accessGuard = accessGuard;
    }

    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.success(categoryService.list());
    }

    @GetMapping("/page")
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
    public Result<Category> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody Category category) {
        accessGuard.requireAdmin();
        return Result.success(categoryService.save(category));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody Category category) {
        accessGuard.requireAdmin();
        return Result.success(categoryService.updateById(category));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        accessGuard.requireAdmin();
        return Result.success(categoryService.removeById(id));
    }
}
