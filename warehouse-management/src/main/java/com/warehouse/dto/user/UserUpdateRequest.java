package com.warehouse.dto.user;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class UserUpdateRequest {

    @NotNull(message = "User id cannot be null")
    private Long id;

    @Size(min = 6, max = 64, message = "Password length must be between 6 and 64")
    private String password;

    @Size(max = 50, message = "Real name is too long")
    private String realName;

    @Size(max = 20, message = "Phone is too long")
    private String phone;

    @Email(message = "Email format is invalid")
    @Size(max = 100, message = "Email is too long")
    private String email;

    private Integer status;

    private Integer role;

    private Long warehouseId;
}
