package com.warehouse.security;

import com.warehouse.monitoring.service.SecurityMonitoringService;
import org.springframework.stereotype.Component;

@Component
public class AccessGuard {

    private final SecurityMonitoringService securityMonitoringService;

    public AccessGuard(SecurityMonitoringService securityMonitoringService) {
        this.securityMonitoringService = securityMonitoringService;
    }

    public AuthenticatedUser currentUser() {
        AuthenticatedUser user = SecurityContext.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("Please login first");
        }
        return user;
    }

    public void requireAdmin() {
        if (!currentUser().isAdmin()) {
            securityMonitoringService.recordApplicationAlert(
                    "ACCESS_DENIED",
                    "HIGH",
                    "Blocked vertical privilege escalation attempt",
                    "A non-admin account attempted to perform an admin-only operation"
            );
            throw new ForbiddenException("Current account does not have admin permission");
        }
    }

    public boolean hasPermission(String permission) {
        return currentUser().permissions().contains(permission);
    }

    public void requirePermission(String permission) {
        if (hasPermission(permission)) {
            return;
        }
        securityMonitoringService.recordApplicationAlert(
                "ACCESS_DENIED",
                "HIGH",
                "Blocked permission violation",
                "User " + currentUser().getId() + " lacks permission " + permission
        );
        throw new ForbiddenException("Current account does not have permission: " + permission);
    }

    public void requireUserAccess(Long userId) {
        AuthenticatedUser currentUser = currentUser();
        if (!currentUser.isAdmin() && !currentUser.getId().equals(userId)) {
            securityMonitoringService.recordApplicationAlert(
                    "ACCESS_DENIED",
                    "HIGH",
                    "Blocked horizontal privilege escalation attempt",
                    "User " + currentUser.getId() + " attempted to access user " + userId
            );
            throw new ForbiddenException("Access to other users' data is forbidden");
        }
    }

    public Long resolveWarehouseScope(Long warehouseId) {
        AuthenticatedUser currentUser = currentUser();
        if (currentUser.isAdmin()) {
            return warehouseId;
        }
        if (currentUser.getWarehouseId() == null) {
            throw new ForbiddenException("Current user is not bound to a warehouse");
        }
        if (warehouseId != null && !currentUser.getWarehouseId().equals(warehouseId)) {
            securityMonitoringService.recordApplicationAlert(
                    "ACCESS_DENIED",
                    "HIGH",
                    "Blocked cross-warehouse access attempt",
                    "User " + currentUser.getId() + " attempted to access warehouse " + warehouseId
            );
            throw new ForbiddenException("Access to other warehouses' data is forbidden");
        }
        return currentUser.getWarehouseId();
    }

    public void checkWarehouseAccess(Long warehouseId) {
        resolveWarehouseScope(warehouseId);
    }
}
