package com.warehouse.security.web;

import com.warehouse.security.AccessGuard;
import com.warehouse.security.RequiresPermission;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private final AccessGuard accessGuard;

    public PermissionInterceptor(AccessGuard accessGuard) {
        this.accessGuard = accessGuard;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequiresPermission requiresPermission = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), RequiresPermission.class);
        if (requiresPermission == null) {
            requiresPermission = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), RequiresPermission.class);
        }
        if (requiresPermission == null) {
            return true;
        }

        accessGuard.requirePermission(requiresPermission.value());
        return true;
    }
}
