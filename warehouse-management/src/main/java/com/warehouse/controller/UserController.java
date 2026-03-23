package com.warehouse.controller;

import com.warehouse.audit.AuditOperation;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.warehouse.common.ResourceNotFoundException;
import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.dto.user.UserCreateRequest;
import com.warehouse.dto.user.UserResponse;
import com.warehouse.dto.user.UserUpdateRequest;
import com.warehouse.entity.User;
import com.warehouse.security.AccessGuard;
import com.warehouse.security.AuthService;
import com.warehouse.security.PermissionConstants;
import com.warehouse.security.RequiresPermission;
import com.warehouse.security.dto.LoginRequest;
import com.warehouse.security.dto.LoginResponse;
import com.warehouse.service.UserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
@Validated
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
    @RequiresPermission(PermissionConstants.USER_READ_ALL)
    public Result<List<UserResponse>> list() {
        List<UserResponse> users = userService.list().stream()
                .map(user -> UserResponse.from(user, false))
                .collect(Collectors.toList());
        return Result.success(users);
    }

    @GetMapping("/page")
    @RequiresPermission(PermissionConstants.USER_READ_ALL)
    public Result<PageResult<UserResponse>> page(@RequestParam(defaultValue = "1") Integer current,
                                                 @RequestParam(defaultValue = "10") Integer size,
                                                 @RequestParam(required = false) String username) {
        Page<User> page = new Page<>(current, size);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like("username", username);
        }
        Page<User> result = userService.page(page, wrapper);
        List<UserResponse> records = result.getRecords().stream()
                .map(user -> UserResponse.from(user, false))
                .collect(Collectors.toList());
        PageResult<UserResponse> pageResult = new PageResult<>(result.getTotal(), records);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @RequiresPermission(PermissionConstants.USER_READ_SELF)
    public Result<UserResponse> getById(@PathVariable Long id) {
        accessGuard.requireUserAccess(id);
        User user = requireUser(id);
        boolean revealSensitive = accessGuard.currentUser().isAdmin() || accessGuard.currentUser().getId().equals(id);
        return Result.success(UserResponse.from(user, revealSensitive));
    }

    @PostMapping
    @RequiresPermission(PermissionConstants.USER_MANAGE)
    @AuditOperation(action = "user.create", resource = "user")
    public Result<Boolean> save(@Valid @RequestBody UserCreateRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(request.getStatus());
        user.setRole(request.getRole());
        user.setWarehouseId(request.getWarehouseId());
        return Result.success(userService.save(user));
    }

    @PutMapping
    @RequiresPermission(PermissionConstants.USER_UPDATE_SELF)
    @AuditOperation(action = "user.update", resource = "user")
    public Result<Boolean> update(@Valid @RequestBody UserUpdateRequest request) {
        accessGuard.requireUserAccess(request.getId());
        User user = new User();
        user.setId(request.getId());
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        } else {
            User existingUser = requireUser(request.getId());
            if (existingUser != null) {
                user.setPassword(existingUser.getPassword());
            }
        }
        if (!accessGuard.currentUser().isAdmin()) {
            user.setRole(null);
            user.setWarehouseId(accessGuard.currentUser().getWarehouseId());
            user.setStatus(null);
        } else {
            user.setRole(request.getRole());
            user.setWarehouseId(request.getWarehouseId());
            user.setStatus(request.getStatus());
        }
        return Result.success(userService.updateById(user));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission(PermissionConstants.USER_MANAGE)
    @AuditOperation(action = "user.delete", resource = "user")
    public Result<Boolean> delete(@PathVariable Long id) {
        requireUser(id);
        return Result.success(userService.removeById(id));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    private User requireUser(Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        return user;
    }
}
