package com.warehouse.monitoring.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.monitoring.entity.EbpfEvent;
import com.warehouse.monitoring.mapper.EbpfEventMapper;
import com.warehouse.monitoring.service.EbpfEventService;
import org.springframework.stereotype.Service;

@Service
public class EbpfEventServiceImpl extends ServiceImpl<EbpfEventMapper, EbpfEvent> implements EbpfEventService {
}
