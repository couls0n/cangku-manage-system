package com.warehouse.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.entity.User;
import com.warehouse.security.AccessGuard;
import com.warehouse.security.AuthService;
import com.warehouse.security.dto.LoginRequest;
import com.warehouse.security.dto.LoginResponse;
import com.warehouse.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    private final UserService userService;
    private final AccessGuard accessGuard;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public UserController(UserService userService,
                          AccessGuard accessGuard,
                          PasswordEncoder passwordEncoder,
                          AuthService authService) {
        this.userService = userService;
        this.accessGuard = accessGuard;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    @GetMapping("/list")
    public Result<List<User>> list() {
        accessGuard.requireAdmin();
        return Result.success(userService.list());
    }

    @GetMapping("/page")
    public Result<PageResult<User>> page(@RequestParam(defaultValue = "1") Integer current,
                                         @RequestParam(defaultValue = "10") Integer size,
                                         @RequestParam(required = false) String username) {
        accessGuard.requireAdmin();
        Page<User> page = new Page<>(current, size);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like("username", username);
        }
        Page<User> result = userService.page(page, wrapper);
        PageResult<User> pageResult = new PageResult<>(result.getTotal(), result.getRecords());
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        accessGuard.requireUserAccess(id);
        return Result.success(userService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody User user) {
        accessGuard.requireAdmin();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return Result.success(userService.save(user));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody User user) {
        accessGuard.requireUserAccess(user.getId());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            User existingUser = userService.getById(user.getId());
            if (existingUser != null) {
                user.setPassword(existingUser.getPassword());
            }
        }
        if (!accessGuard.currentUser().isAdmin()) {
            user.setRole(null);
            user.setWarehouseId(accessGuard.currentUser().getWarehouseId());
            user.setStatus(null);
        }
        return Result.success(userService.updateById(user));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        accessGuard.requireAdmin();
        return Result.success(userService.removeById(id));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }
}
