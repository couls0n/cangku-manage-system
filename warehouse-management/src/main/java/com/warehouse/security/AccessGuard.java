package com.warehouse.security;

import org.springframework.stereotype.Component;

@Component
public class AccessGuard {

    private final com.warehouse.monitoring.service.SecurityMonitoringService securityMonitoringService;

    public AccessGuard(com.warehouse.monitoring.service.SecurityMonitoringService securityMonitoringService) {
        this.securityMonitoringService = securityMonitoringService;
    }

    public AuthenticatedUser currentUser() {
        AuthenticatedUser user = SecurityContext.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("请先登录");
        }
        return user;
    }

    public void requireAdmin() {
        if (!currentUser().isAdmin()) {
            securityMonitoringService.recordApplicationAlert("ACCESS_DENIED", "HIGH",
                    "检测到垂直越权拦截",
                    "非管理员账号尝试执行管理员操作");
            throw new ForbiddenException("当前账号没有管理员权限");
        }
    }

    public void requireUserAccess(Long userId) {
        AuthenticatedUser currentUser = currentUser();
        if (!currentUser.isAdmin() && !currentUser.getId().equals(userId)) {
            securityMonitoringService.recordApplicationAlert("ACCESS_DENIED", "HIGH",
                    "检测到水平越权拦截",
                    "用户 " + currentUser.getId() + " 尝试访问用户 " + userId + " 的数据");
            throw new ForbiddenException("禁止访问其他用户的数据");
        }
    }

    public Long resolveWarehouseScope(Long warehouseId) {
        AuthenticatedUser currentUser = currentUser();
        if (currentUser.isAdmin()) {
            return warehouseId;
        }
        if (currentUser.getWarehouseId() == null) {
            throw new ForbiddenException("当前用户未绑定仓库，无法访问仓库数据");
        }
        if (warehouseId != null && !currentUser.getWarehouseId().equals(warehouseId)) {
            securityMonitoringService.recordApplicationAlert("ACCESS_DENIED", "HIGH",
                    "检测到仓库水平越权拦截",
                    "用户 " + currentUser.getId() + " 尝试访问仓库 " + warehouseId);
            throw new ForbiddenException("禁止访问其他仓库的数据");
        }
        return currentUser.getWarehouseId();
    }

    public void checkWarehouseAccess(Long warehouseId) {
        resolveWarehouseScope(warehouseId);
    }
}
