package com.warehouse.security;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.warehouse.entity.User;
import com.warehouse.security.dto.LoginRequest;
import com.warehouse.security.dto.LoginResponse;
import com.warehouse.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public LoginResponse login(LoginRequest request) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", request.getUsername());
        wrapper.last("limit 1");
        User user = userService.getOne(wrapper);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new ForbiddenException("当前账号已被禁用");
        }
        String token = tokenService.generateToken(user);
        return LoginResponse.builder()
                .token(token)
                .user(LoginResponse.UserProfile.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .realName(user.getRealName())
                        .role(user.getRole())
                        .warehouseId(user.getWarehouseId())
                        .build())
                .permissions(new ArrayList<>(RolePermissionMatrix.permissionsFor(user.getRole())))
                .build();
    }
}
