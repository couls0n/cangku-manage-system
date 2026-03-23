package com.warehouse.security;

import java.util.Set;

public final class PermissionConstants {

    public static final String CATEGORY_READ = "category:read";
    public static final String CATEGORY_MANAGE = "category:manage";
    public static final String PRODUCT_READ = "product:read";
    public static final String PRODUCT_MANAGE = "product:manage";
    public static final String CUSTOMER_READ = "customer:read";
    public static final String CUSTOMER_MANAGE = "customer:manage";
    public static final String SUPPLIER_READ = "supplier:read";
    public static final String SUPPLIER_MANAGE = "supplier:manage";
    public static final String WAREHOUSE_READ = "warehouse:read";
    public static final String WAREHOUSE_MANAGE = "warehouse:manage";
    public static final String STOCK_READ = "stock:read";
    public static final String STOCK_ADJUST_READ = "stock:adjust:read";
    public static final String STOCK_ADJUST_WRITE = "stock:adjust:write";
    public static final String STOCK_ADJUST_APPROVE = "stock:adjust:approve";
    public static final String STOCK_CHECK_READ = "stock:check:read";
    public static final String STOCK_CHECK_WRITE = "stock:check:write";
    public static final String STOCK_CHECK_APPROVE = "stock:check:approve";
    public static final String STOCK_LOSS_WRITE = "stock:loss:write";
    public static final String STOCK_OVERFLOW_WRITE = "stock:overflow:write";
    public static final String INBOUND_READ = "inbound:read";
    public static final String INBOUND_SUBMIT = "inbound:submit";
    public static final String OUTBOUND_READ = "outbound:read";
    public static final String OUTBOUND_SUBMIT = "outbound:submit";
    public static final String USER_READ_SELF = "user:self:read";
    public static final String USER_UPDATE_SELF = "user:self:update";
    public static final String USER_READ_ALL = "user:read";
    public static final String USER_MANAGE = "user:manage";
    public static final String SECURITY_MONITOR_READ = "security:monitor:read";
    public static final String AUDIT_LOG_READ = "audit:read";
    public static final String AUDIT_LOG_EXPORT = "audit:export";

    private PermissionConstants() {
    }

    public static Set<String> all() {
        return Set.of(
                CATEGORY_READ,
                CATEGORY_MANAGE,
                PRODUCT_READ,
                PRODUCT_MANAGE,
                CUSTOMER_READ,
                CUSTOMER_MANAGE,
                SUPPLIER_READ,
                SUPPLIER_MANAGE,
                WAREHOUSE_READ,
                WAREHOUSE_MANAGE,
                STOCK_READ,
                STOCK_ADJUST_READ,
                STOCK_ADJUST_WRITE,
                STOCK_ADJUST_APPROVE,
                STOCK_CHECK_READ,
                STOCK_CHECK_WRITE,
                STOCK_CHECK_APPROVE,
                STOCK_LOSS_WRITE,
                STOCK_OVERFLOW_WRITE,
                INBOUND_READ,
                INBOUND_SUBMIT,
                OUTBOUND_READ,
                OUTBOUND_SUBMIT,
                USER_READ_SELF,
                USER_UPDATE_SELF,
                USER_READ_ALL,
                USER_MANAGE,
                SECURITY_MONITOR_READ,
                AUDIT_LOG_READ,
                AUDIT_LOG_EXPORT
        );
    }
}
