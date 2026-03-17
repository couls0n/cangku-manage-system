package com.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.warehouse.entity.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface StockMapper extends BaseMapper<Stock> {

    @Update("UPDATE stock " +
            "SET quantity = quantity - #{deductQuantity} " +
            "WHERE id = #{stockId} " +
            "AND deleted = 0 " +
            "AND (quantity - frozen_quantity) >= #{deductQuantity}")
    int deductAvailableStock(@Param("stockId") Long stockId, @Param("deductQuantity") BigDecimal deductQuantity);
}
