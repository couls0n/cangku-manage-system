package com.warehouse.dto.customer;

import com.warehouse.common.MaskingUtils;
import com.warehouse.entity.Customer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CustomerResponse {

    private Long id;
    private String customerCode;
    private String customerName;
    private String contactPerson;
    private String phone;
    private String address;
    private String email;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static CustomerResponse from(Customer customer, boolean revealSensitive) {
        if (customer == null) {
            return null;
        }
        return CustomerResponse.builder()
                .id(customer.getId())
                .customerCode(customer.getCustomerCode())
                .customerName(customer.getCustomerName())
                .contactPerson(customer.getContactPerson())
                .phone(revealSensitive ? customer.getPhone() : MaskingUtils.maskPhone(customer.getPhone()))
                .address(revealSensitive ? customer.getAddress() : MaskingUtils.maskAddress(customer.getAddress()))
                .email(revealSensitive ? customer.getEmail() : MaskingUtils.maskEmail(customer.getEmail()))
                .status(customer.getStatus())
                .remark(customer.getRemark())
                .createTime(customer.getCreateTime())
                .updateTime(customer.getUpdateTime())
                .build();
    }
}
