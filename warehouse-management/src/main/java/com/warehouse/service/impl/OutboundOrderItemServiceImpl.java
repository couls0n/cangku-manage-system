package com.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.entity.OutboundOrderItem;
import com.warehouse.mapper.OutboundOrderItemMapper;
import com.warehouse.service.OutboundOrderItemService;
import org.springframework.stereotype.Service;

@Service
public class OutboundOrderItemServiceImpl extends ServiceImpl<OutboundOrderItemMapper, OutboundOrderItem> implements OutboundOrderItemService {
}
