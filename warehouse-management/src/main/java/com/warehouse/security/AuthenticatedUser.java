package com.warehouse.security;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

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

    public Set<String> permissions() {
        return RolePermissionMatrix.permissionsFor(role);
    }
}
