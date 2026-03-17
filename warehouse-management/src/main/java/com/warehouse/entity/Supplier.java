package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supplier")
public class Supplier extends BaseEntity {

    private String supplierCode;

    private String supplierName;

    private String contactPerson;

    private String phone;

    private String address;

    private String email;

    private Integer status;

    private String remark;
}
