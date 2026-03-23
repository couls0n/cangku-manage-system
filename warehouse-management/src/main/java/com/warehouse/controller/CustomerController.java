package com.warehouse.controller;

import com.warehouse.audit.AuditOperation;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.warehouse.common.PageResult;
import com.warehouse.common.ResourceNotFoundException;
import com.warehouse.common.Result;
import com.warehouse.dto.customer.CustomerResponse;
import com.warehouse.dto.customer.CustomerUpsertRequest;
import com.warehouse.entity.Customer;
import com.warehouse.security.AccessGuard;
import com.warehouse.security.PermissionConstants;
import com.warehouse.security.RequiresPermission;
import com.warehouse.service.CustomerService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin
@Validated
public class CustomerController {

    private final CustomerService customerService;
    private final AccessGuard accessGuard;

    public CustomerController(CustomerService customerService, AccessGuard accessGuard) {
        this.customerService = customerService;
        this.accessGuard = accessGuard;
    }

    @GetMapping("/list")
    @RequiresPermission(PermissionConstants.CUSTOMER_READ)
    public Result<List<CustomerResponse>> list() {
        accessGuard.currentUser();
        List<CustomerResponse> customers = customerService.list().stream()
                .map(customer -> CustomerResponse.from(customer, false))
                .collect(Collectors.toList());
        return Result.success(customers);
    }

    @GetMapping("/page")
    @RequiresPermission(PermissionConstants.CUSTOMER_READ)
    public Result<PageResult<CustomerResponse>> page(@RequestParam(defaultValue = "1") Integer current,
                                                     @RequestParam(defaultValue = "10") Integer size,
                                                     @RequestParam(required = false) String customerName) {
        accessGuard.currentUser();
        Page<Customer> page = new Page<>(current, size);
        QueryWrapper<Customer> wrapper = new QueryWrapper<>();
        if (customerName != null && !customerName.isEmpty()) {
            wrapper.like("customer_name", customerName);
        }
        Page<Customer> result = customerService.page(page, wrapper);
        List<CustomerResponse> records = result.getRecords().stream()
                .map(customer -> CustomerResponse.from(customer, false))
                .collect(Collectors.toList());
        PageResult<CustomerResponse> pageResult = new PageResult<>(result.getTotal(), records);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @RequiresPermission(PermissionConstants.CUSTOMER_READ)
    public Result<CustomerResponse> getById(@PathVariable Long id) {
        Customer customer = requireCustomer(id);
        boolean revealSensitive = accessGuard.currentUser().isAdmin();
        return Result.success(CustomerResponse.from(customer, revealSensitive));
    }

    @PostMapping
    @RequiresPermission(PermissionConstants.CUSTOMER_MANAGE)
    @AuditOperation(action = "customer.create", resource = "customer")
    public Result<Boolean> save(@Valid @RequestBody CustomerUpsertRequest request) {
        Customer customer = toEntity(request);
        return Result.success(customerService.save(customer));
    }

    @PutMapping
    @RequiresPermission(PermissionConstants.CUSTOMER_MANAGE)
    @AuditOperation(action = "customer.update", resource = "customer")
    public Result<Boolean> update(@Valid @RequestBody CustomerUpsertRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("Customer id cannot be null");
        }
        requireCustomer(request.getId());
        Customer customer = toEntity(request);
        return Result.success(customerService.updateById(customer));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission(PermissionConstants.CUSTOMER_MANAGE)
    @AuditOperation(action = "customer.delete", resource = "customer")
    public Result<Boolean> delete(@PathVariable Long id) {
        requireCustomer(id);
        return Result.success(customerService.removeById(id));
    }

    private Customer requireCustomer(Long id) {
        Customer customer = customerService.getById(id);
        if (customer == null) {
            throw new ResourceNotFoundException("Customer not found: " + id);
        }
        return customer;
    }

    private Customer toEntity(CustomerUpsertRequest request) {
        Customer customer = new Customer();
        customer.setId(request.getId());
        customer.setCustomerCode(request.getCustomerCode());
        customer.setCustomerName(request.getCustomerName());
        customer.setContactPerson(request.getContactPerson());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setEmail(request.getEmail());
        customer.setStatus(request.getStatus());
        customer.setRemark(request.getRemark());
        return customer;
    }
}
