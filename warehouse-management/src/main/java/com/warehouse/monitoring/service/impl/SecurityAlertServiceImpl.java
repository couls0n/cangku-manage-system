package com.warehouse.monitoring.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.monitoring.entity.SecurityAlert;
import com.warehouse.monitoring.mapper.SecurityAlertMapper;
import com.warehouse.monitoring.service.SecurityAlertService;
import org.springframework.stereotype.Service;

@Service
public class SecurityAlertServiceImpl extends ServiceImpl<SecurityAlertMapper, SecurityAlert> implements SecurityAlertService {
}
