package com.warehouse.security;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public final class RolePermissionMatrix {

    private static final Map<Integer, Set<String>> PERMISSIONS_BY_ROLE = Map.of(
            RoleConstants.ADMIN, PermissionConstants.all(),
            RoleConstants.AUDITOR, Set.of(
                    PermissionConstants.CATEGORY_READ,
                    PermissionConstants.PRODUCT_READ,
                    PermissionConstants.CUSTOMER_READ,
                    PermissionConstants.SUPPLIER_READ,
                    PermissionConstants.WAREHOUSE_READ,
                    PermissionConstants.STOCK_READ,
                    PermissionConstants.STOCK_ADJUST_READ,
                    PermissionConstants.STOCK_CHECK_READ,
                    PermissionConstants.INBOUND_READ,
                    PermissionConstants.OUTBOUND_READ,
                    PermissionConstants.USER_READ_ALL,
                    PermissionConstants.SECURITY_MONITOR_READ,
                    PermissionConstants.AUDIT_LOG_READ,
                    PermissionConstants.AUDIT_LOG_EXPORT
            ),
            RoleConstants.OPERATOR, Set.of(
                    PermissionConstants.CATEGORY_READ,
                    PermissionConstants.PRODUCT_READ,
                    PermissionConstants.CUSTOMER_READ,
                    PermissionConstants.SUPPLIER_READ,
                    PermissionConstants.WAREHOUSE_READ,
                    PermissionConstants.STOCK_READ,
                    PermissionConstants.STOCK_ADJUST_READ,
                    PermissionConstants.STOCK_CHECK_READ,
                    PermissionConstants.STOCK_LOSS_WRITE,
                    PermissionConstants.STOCK_OVERFLOW_WRITE,
                    PermissionConstants.STOCK_CHECK_WRITE,
                    PermissionConstants.INBOUND_READ,
                    PermissionConstants.INBOUND_SUBMIT,
                    PermissionConstants.OUTBOUND_READ,
                    PermissionConstants.OUTBOUND_SUBMIT,
                    PermissionConstants.USER_READ_SELF,
                    PermissionConstants.USER_UPDATE_SELF
            )
    );

    private RolePermissionMatrix() {
    }

    public static Set<String> permissionsFor(Integer role) {
        if (role == null) {
            return Collections.emptySet();
        }
        return PERMISSIONS_BY_ROLE.getOrDefault(role, Collections.emptySet());
    }
}
