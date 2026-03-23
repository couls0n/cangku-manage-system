package com.warehouse.config;

import com.warehouse.security.web.AuthInterceptor;
import com.warehouse.security.web.PermissionInterceptor;
import com.warehouse.security.web.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final PermissionInterceptor permissionInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;
    private final WebCorsProperties webCorsProperties;

    public WebConfig(AuthInterceptor authInterceptor,
                     PermissionInterceptor permissionInterceptor,
                     RateLimitInterceptor rateLimitInterceptor,
                     WebCorsProperties webCorsProperties) {
        this.authInterceptor = authInterceptor;
        this.permissionInterceptor = permissionInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
        this.webCorsProperties = webCorsProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(webCorsProperties.getAllowedOrigins().toArray(String[]::new))
                .allowedMethods(webCorsProperties.getAllowedMethods().toArray(String[]::new))
                .allowedHeaders(webCorsProperties.getAllowedHeaders().toArray(String[]::new))
                .maxAge(webCorsProperties.getMaxAge());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(permissionInterceptor).addPathPatterns("/api/**");
    }
}
