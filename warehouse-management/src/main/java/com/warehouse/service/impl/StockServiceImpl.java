package com.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.entity.Stock;
import com.warehouse.mapper.StockMapper;
import com.warehouse.service.StockService;
import org.springframework.stereotype.Service;

@Service
public class StockServiceImpl extends ServiceImpl<StockMapper, Stock> implements StockService {
}
