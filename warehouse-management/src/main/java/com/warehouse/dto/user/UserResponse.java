package com.warehouse.dto.user;

import com.warehouse.common.MaskingUtils;
import com.warehouse.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private Integer status;
    private Integer role;
    private Long warehouseId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static UserResponse from(User user, boolean revealSensitive) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(revealSensitive ? user.getPhone() : MaskingUtils.maskPhone(user.getPhone()))
                .email(revealSensitive ? user.getEmail() : MaskingUtils.maskEmail(user.getEmail()))
                .status(user.getStatus())
                .role(user.getRole())
                .warehouseId(user.getWarehouseId())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }
}
