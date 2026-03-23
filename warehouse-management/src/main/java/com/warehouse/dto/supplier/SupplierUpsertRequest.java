package com.warehouse.dto.supplier;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class SupplierUpsertRequest {

    private Long id;

    @NotBlank(message = "Supplier code cannot be blank")
    @Size(max = 50, message = "Supplier code is too long")
    private String supplierCode;

    @NotBlank(message = "Supplier name cannot be blank")
    @Size(max = 100, message = "Supplier name is too long")
    private String supplierName;

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
