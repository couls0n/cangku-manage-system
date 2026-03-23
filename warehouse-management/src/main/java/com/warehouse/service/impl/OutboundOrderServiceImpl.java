package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.dto.outbound.OutboundSubmitItemRequest;
import com.warehouse.dto.outbound.OutboundSubmitRequest;
import com.warehouse.entity.OutboundOrder;
import com.warehouse.entity.OutboundOrderItem;
import com.warehouse.mapper.OutboundOrderMapper;
import com.warehouse.service.OutboundOrderItemService;
import com.warehouse.service.OutboundOrderService;
import com.warehouse.service.StockService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OutboundOrderServiceImpl extends ServiceImpl<OutboundOrderMapper, OutboundOrder> implements OutboundOrderService {

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OutboundOrderItemService outboundOrderItemService;
    private final StockService stockService;
    private final TransactionTemplate transactionTemplate;

    public OutboundOrderServiceImpl(OutboundOrderItemService outboundOrderItemService,
                                    StockService stockService,
                                    TransactionTemplate transactionTemplate) {
        this.outboundOrderItemService = outboundOrderItemService;
        this.stockService = stockService;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public OutboundOrder submitOrder(OutboundSubmitRequest request, Long operatorId) {
        OutboundOrder existingOrder = findByRequestId(request.getRequestId());
        if (existingOrder != null) {
            return existingOrder;
        }
        Map<Long, BigDecimal> aggregatedQuantities = aggregateQuantities(request.getItems());
        try {
            return stockService.executeWithStockLocks(request.getWarehouseId(), aggregatedQuantities.keySet(), () ->
                    transactionTemplate.execute(status -> {
                        stockService.deductStocksInLock(request.getWarehouseId(), aggregatedQuantities);

                        OutboundOrder outboundOrder = buildOrder(request, operatorId);
                        save(outboundOrder);

                        List<OutboundOrderItem> items = buildOrderItems(outboundOrder.getId(), request.getItems());
                        outboundOrderItemService.saveBatch(items);
                        return outboundOrder;
                    })
            );
        } catch (DuplicateKeyException ex) {
            OutboundOrder duplicatedOrder = findByRequestId(request.getRequestId());
            if (duplicatedOrder != null) {
                return duplicatedOrder;
            }
            throw ex;
        }
    }

    private OutboundOrder buildOrder(OutboundSubmitRequest request, Long operatorId) {
        OutboundOrder outboundOrder = new OutboundOrder();
        outboundOrder.setRequestId(request.getRequestId());
        outboundOrder.setOrderNo(request.getOrderNo() == null || request.getOrderNo().isBlank() ? generateOrderNo() : request.getOrderNo());
        outboundOrder.setWarehouseId(request.getWarehouseId());
        outboundOrder.setCustomerId(request.getCustomerId());
        outboundOrder.setOperatorId(operatorId);
        outboundOrder.setOrderTime(request.getOrderTime() == null ? LocalDateTime.now() : request.getOrderTime());
        outboundOrder.setRemark(request.getRemark());
        outboundOrder.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        outboundOrder.setTotalAmount(calculateTotalAmount(request.getItems()));
        return outboundOrder;
    }

    private List<OutboundOrderItem> buildOrderItems(Long orderId, List<OutboundSubmitItemRequest> requestItems) {
        List<OutboundOrderItem> items = new ArrayList<>(requestItems.size());
        for (OutboundSubmitItemRequest requestItem : requestItems) {
            OutboundOrderItem orderItem = new OutboundOrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setProductId(requestItem.getProductId());
            orderItem.setQuantity(requestItem.getQuantity());
            orderItem.setUnitPrice(defaultAmount(requestItem.getUnitPrice()));
            orderItem.setTotalPrice(resolveLineTotal(requestItem));
            orderItem.setRemark(requestItem.getRemark());
            items.add(orderItem);
        }
        return items;
    }

    private Map<Long, BigDecimal> aggregateQuantities(List<OutboundSubmitItemRequest> items) {
        Map<Long, BigDecimal> aggregated = new LinkedHashMap<>();
        for (OutboundSubmitItemRequest item : items) {
            aggregated.merge(item.getProductId(), item.getQuantity(), BigDecimal::add);
        }
        return aggregated;
    }

    private BigDecimal calculateTotalAmount(List<OutboundSubmitItemRequest> items) {
        return items.stream()
                .map(this::resolveLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal resolveLineTotal(OutboundSubmitItemRequest item) {
        if (item.getTotalPrice() != null) {
            return item.getTotalPrice();
        }
        return defaultAmount(item.getUnitPrice()).multiply(item.getQuantity());
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String generateOrderNo() {
        return "OUT" + LocalDateTime.now().format(ORDER_NO_FORMATTER) + ThreadLocalRandom.current().nextInt(100000, 1000000);
    }

    private OutboundOrder findByRequestId(String requestId) {
        QueryWrapper<OutboundOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("request_id", requestId).last("limit 1");
        return getOne(wrapper);
    }
}
