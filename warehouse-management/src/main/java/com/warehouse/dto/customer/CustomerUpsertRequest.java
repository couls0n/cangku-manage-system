package com.warehouse.dto.customer;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class CustomerUpsertRequest {

    private Long id;

    @NotBlank(message = "Customer code cannot be blank")
    @Size(max = 50, message = "Customer code is too long")
    private String customerCode;

    @NotBlank(message = "Customer name cannot be blank")
    @Size(max = 100, message = "Customer name is too long")
    private String customerName;

    @Size(max = 50, message = "Contact person is too long")
    private String contactPerson;

    @Size(max = 20, message = "Phone is too long")
    private String phone;

    @Size(max = 200, message = "Address is too long")
    private String address;

    @Email(message = "Email format is invalid")
    @Size(max = 100, message = "Email is too long")
    private String email;

    private Integer status;

    @Size(max = 500, message = "Remark is too long")
    private String remark;
}
