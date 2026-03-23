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
        int limit = loginPath
                ? securityProperties.getRateLimit().getLoginLimit()
                : securityProperties.getRateLimit().getDefaultLimit();
        int window = loginPath
                ? securityProperties.getRateLimit().getLoginWindowSeconds()
                : securityProperties.getRateLimit().getDefaultWindowSeconds();

        String key = buildKey(request, path);
        if (!rateLimiterService.allow(key, limit, window)) {
            securityMonitoringService.recordApplicationAlert(
                    "API_RATE_LIMIT",
                    "MEDIUM",
                    "API rate limit triggered",
                    "Path " + path + " exceeded " + limit + " requests within " + window + " seconds"
            );
            throw new RateLimitException("Too many requests, please try again later");
        }
        return true;
    }

    private String buildKey(HttpServletRequest request, String path) {
        AuthenticatedUser user = SecurityContext.getCurrentUser();
        String principal = user != null ? "USER:" + user.getId() : "IP:" + request.getRemoteAddr();
        return principal + ":" + path;
    }
}
