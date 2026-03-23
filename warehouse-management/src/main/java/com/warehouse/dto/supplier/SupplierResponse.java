package com.warehouse.dto.supplier;

import com.warehouse.common.MaskingUtils;
import com.warehouse.entity.Supplier;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SupplierResponse {

    private Long id;
    private String supplierCode;
    private String supplierName;
    private String contactPerson;
    private String phone;
    private String address;
    private String email;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static SupplierResponse from(Supplier supplier, boolean revealSensitive) {
        if (supplier == null) {
            return null;
        }
        return SupplierResponse.builder()
                .id(supplier.getId())
                .supplierCode(supplier.getSupplierCode())
                .supplierName(supplier.getSupplierName())
                .contactPerson(supplier.getContactPerson())
                .phone(revealSensitive ? supplier.getPhone() : MaskingUtils.maskPhone(supplier.getPhone()))
                .address(revealSensitive ? supplier.getAddress() : MaskingUtils.maskAddress(supplier.getAddress()))
                .email(revealSensitive ? supplier.getEmail() : MaskingUtils.maskEmail(supplier.getEmail()))
                .status(supplier.getStatus())
                .remark(supplier.getRemark())
                .createTime(supplier.getCreateTime())
                .updateTime(supplier.getUpdateTime())
                .build();
    }
}
