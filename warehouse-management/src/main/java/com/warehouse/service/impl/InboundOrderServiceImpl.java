package com.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.entity.InboundOrder;
import com.warehouse.mapper.InboundOrderMapper;
import com.warehouse.service.InboundOrderService;
import org.springframework.stereotype.Service;

@Service
public class InboundOrderServiceImpl extends ServiceImpl<InboundOrderMapper, InboundOrder> implements InboundOrderService {
}
