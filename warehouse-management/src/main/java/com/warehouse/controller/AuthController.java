package com.warehouse.controller;

import com.warehouse.common.Result;
import com.warehouse.security.AuthService;
import com.warehouse.security.SecurityContext;
import com.warehouse.security.dto.LoginRequest;
import com.warehouse.security.dto.LoginResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @GetMapping("/me")
    public Result<?> me() {
        return Result.success(SecurityContext.getCurrentUser());
    }
}
