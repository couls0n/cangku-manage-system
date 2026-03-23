package com.warehouse.security.web;

import com.warehouse.security.AuthenticatedUser;
import com.warehouse.security.SecurityContext;
import com.warehouse.security.SecurityProperties;
import com.warehouse.security.TokenService;
import com.warehouse.security.UnauthorizedException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;
    private final SecurityProperties securityProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> publicPaths = Arrays.asList("/api/auth/login", "/api/user/login", "/error", "/h2-console/**", "/actuator/health");

    public AuthInterceptor(TokenService tokenService, SecurityProperties securityProperties) {
        this.tokenService = tokenService;
        this.securityProperties = securityProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isPublic(path)) {
            return true;
        }
        if ("/api/security/ebpf/ingest".equals(path)) {
            String ingestKey = request.getHeader("X-EBPF-KEY");
            if (!constantTimeEquals(securityProperties.getEbpfIngestKey(), ingestKey)) {
                throw new UnauthorizedException("Invalid eBPF ingest key");
            }
            return true;
        }

        String token = resolveToken(request);
        AuthenticatedUser authenticatedUser = tokenService.parseToken(token);
        SecurityContext.setCurrentUser(authenticatedUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        SecurityContext.clear();
    }

    private boolean isPublic(String path) {
        return publicPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        String headerToken = request.getHeader("X-Auth-Token");
        if (headerToken != null && !headerToken.isEmpty()) {
            return headerToken;
        }
        throw new UnauthorizedException("Missing access token");
    }

    private boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }
}
