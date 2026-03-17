package com.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.dto.outbound.OutboundSubmitRequest;
import com.warehouse.entity.OutboundOrder;

public interface OutboundOrderService extends IService<OutboundOrder> {

    OutboundOrder submitOrder(OutboundSubmitRequest request, Long operatorId);
}
