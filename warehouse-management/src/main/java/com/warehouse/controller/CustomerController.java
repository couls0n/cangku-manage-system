package com.warehouse.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.entity.Customer;
import com.warehouse.security.AccessGuard;
import com.warehouse.service.CustomerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin
public class CustomerController {

    private final CustomerService customerService;
    private final AccessGuard accessGuard;

    public CustomerController(CustomerService customerService, AccessGuard accessGuard) {
        this.customerService = customerService;
        this.accessGuard = accessGuard;
    }

    @GetMapping("/list")
    public Result<List<Customer>> list() {
        return Result.success(customerService.list());
    }

    @GetMapping("/page")
    public Result<PageResult<Customer>> page(@RequestParam(defaultValue = "1") Integer current,
                                              @RequestParam(defaultValue = "10") Integer size,
                                              @RequestParam(required = false) String customerName) {
        Page<Customer> page = new Page<>(current, size);
        QueryWrapper<Customer> wrapper = new QueryWrapper<>();
        if (customerName != null && !customerName.isEmpty()) {
            wrapper.like("customer_name", customerName);
        }
        Page<Customer> result = customerService.page(page, wrapper);
        PageResult<Customer> pageResult = new PageResult<>(result.getTotal(), result.getRecords());
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<Customer> getById(@PathVariable Long id) {
        return Result.success(customerService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody Customer customer) {
        accessGuard.requireAdmin();
        return Result.success(customerService.save(customer));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody Customer customer) {
        accessGuard.requireAdmin();
        return Result.success(customerService.updateById(customer));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        accessGuard.requireAdmin();
        return Result.success(customerService.removeById(id));
    }
}
