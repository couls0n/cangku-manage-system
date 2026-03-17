package com.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.entity.InboundOrderItem;
import com.warehouse.mapper.InboundOrderItemMapper;
import com.warehouse.service.InboundOrderItemService;
import org.springframework.stereotype.Service;

@Service
public class InboundOrderItemServiceImpl extends ServiceImpl<InboundOrderItemMapper, InboundOrderItem> implements InboundOrderItemService {
}
