package com.warehouse.security;

public final class SecurityContext {
    private static final ThreadLocal<AuthenticatedUser> HOLDER = new ThreadLocal<>();

    private SecurityContext() {
    }

    public static void setCurrentUser(AuthenticatedUser user) {
        HOLDER.set(user);
    }

    public static AuthenticatedUser getCurrentUser() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
