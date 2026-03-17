package com.warehouse.security;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticatedUser {
    private Long id;
    private String username;
    private Integer role;
    private Long warehouseId;

    public boolean isAdmin() {
        return role != null && role == RoleConstants.ADMIN;
    }
}
