package com.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.dto.inbound.InboundSubmitRequest;
import com.warehouse.entity.InboundOrder;

public interface InboundOrderService extends IService<InboundOrder> {

    InboundOrder submitOrder(InboundSubmitRequest request, Long operatorId);
}
