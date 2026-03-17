package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer")
public class Customer extends BaseEntity {

    private String customerCode;

    private String customerName;

    private String contactPerson;

    private String phone;

    private String address;

    private String email;

    private Integer status;

    private String remark;
}
