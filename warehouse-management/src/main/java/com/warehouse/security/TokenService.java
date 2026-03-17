package com.warehouse.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.entity.User;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TokenService {

    private final SecurityProperties securityProperties;
    private final ObjectMapper objectMapper;

    public TokenService(SecurityProperties securityProperties, ObjectMapper objectMapper) {
        this.securityProperties = securityProperties;
        this.objectMapper = objectMapper;
    }

    public String generateToken(User user) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("uid", user.getId());
            payload.put("username", user.getUsername());
            payload.put("role", user.getRole());
            payload.put("warehouseId", user.getWarehouseId());
            payload.put("exp", Instant.now().plusSeconds(securityProperties.getTokenExpireHours() * 3600L).getEpochSecond());
            String payloadJson = objectMapper.writeValueAsString(payload);
            String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
            String signature = sign(encodedPayload);
            return encodedPayload + "." + signature;
        } catch (Exception ex) {
            throw new IllegalStateException("无法生成访问令牌", ex);
        }
    }

    public AuthenticatedUser parseToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 2) {
                throw new UnauthorizedException("访问令牌格式错误");
            }
            String payload = parts[0];
            String signature = parts[1];
            if (!sign(payload).equals(signature)) {
                throw new UnauthorizedException("访问令牌签名无效");
            }
            String payloadJson = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
            Map<String, Object> claims = objectMapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {
            });
            long expiresAt = ((Number) claims.get("exp")).longValue();
            if (Instant.now().getEpochSecond() > expiresAt) {
                throw new UnauthorizedException("访问令牌已过期");
            }
            return AuthenticatedUser.builder()
                    .id(((Number) claims.get("uid")).longValue())
                    .username((String) claims.get("username"))
                    .role(((Number) claims.get("role")).intValue())
                    .warehouseId(claims.get("warehouseId") == null ? null : ((Number) claims.get("warehouseId")).longValue())
                    .build();
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnauthorizedException("访问令牌解析失败");
        }
    }

    private String sign(String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(securityProperties.getTokenSecret().getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] signature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
    }
}
