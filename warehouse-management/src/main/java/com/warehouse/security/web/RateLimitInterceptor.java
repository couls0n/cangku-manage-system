package com.warehouse.security.web;

import com.warehouse.monitoring.service.SecurityMonitoringService;
import com.warehouse.security.AuthenticatedUser;
import com.warehouse.security.RateLimitException;
import com.warehouse.security.RateLimiterService;
import com.warehouse.security.SecurityContext;
import com.warehouse.security.SecurityProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;
    private final SecurityProperties securityProperties;
    private final SecurityMonitoringService securityMonitoringService;

    public RateLimitInterceptor(RateLimiterService rateLimiterService,
                                SecurityProperties securityProperties,
                                SecurityMonitoringService securityMonitoringService) {
        this.rateLimiterService = rateLimiterService;
        this.securityProperties = securityProperties;
        this.securityMonitoringService = securityMonitoringService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        boolean loginPath = "/api/auth/login".equals(path) || "/api/user/login".equals(path);
        int limit = loginPath ? securityProperties.getRateLimit().getLoginLimit() : securityProperties.getRateLimit().getDefaultLimit();
        int window = loginPath ? securityProperties.getRateLimit().getLoginWindowSeconds() : securityProperties.getRateLimit().getDefaultWindowSeconds();
        String key = buildKey(request, path);
        if (!rateLimiterService.allow(key, limit, window)) {
            securityMonitoringService.recordApplicationAlert("API_RATE_LIMIT", "MEDIUM",
                    "触发接口频控",
                    "路径 " + path + " 在 " + window + " 秒内超过 " + limit + " 次访问");
            throw new RateLimitException("访问过于频繁，请稍后再试");
        }
        return true;
    }

    private String buildKey(HttpServletRequest request, String path) {
        AuthenticatedUser user = SecurityContext.getCurrentUser();
        String principal = user != null ? "USER:" + user.getId() : "IP:" + request.getRemoteAddr();
        return principal + ":" + path;
    }
}
