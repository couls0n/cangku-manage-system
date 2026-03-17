package com.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.entity.OutboundOrder;
import com.warehouse.mapper.OutboundOrderMapper;
import com.warehouse.service.OutboundOrderService;
import org.springframework.stereotype.Service;

@Service
public class OutboundOrderServiceImpl extends ServiceImpl<OutboundOrderMapper, OutboundOrder> implements OutboundOrderService {
}
