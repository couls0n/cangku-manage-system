package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.dto.inbound.InboundSubmitItemRequest;
import com.warehouse.dto.inbound.InboundSubmitRequest;
import com.warehouse.entity.InboundOrder;
import com.warehouse.entity.InboundOrderItem;
import com.warehouse.mapper.InboundOrderMapper;
import com.warehouse.service.InboundOrderItemService;
import com.warehouse.service.InboundOrderService;
import com.warehouse.service.StockService;
import com.warehouse.stock.StockInboundCommand;
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
import java.util.stream.Collectors;

@Service
public class InboundOrderServiceImpl extends ServiceImpl<InboundOrderMapper, InboundOrder> implements InboundOrderService {

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final InboundOrderItemService inboundOrderItemService;
    private final StockService stockService;
    private final TransactionTemplate transactionTemplate;

    public InboundOrderServiceImpl(InboundOrderItemService inboundOrderItemService,
                                   StockService stockService,
                                   TransactionTemplate transactionTemplate) {
        this.inboundOrderItemService = inboundOrderItemService;
        this.stockService = stockService;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public InboundOrder submitOrder(InboundSubmitRequest request, Long operatorId) {
        InboundOrder existingOrder = findByRequestId(request.getRequestId());
        if (existingOrder != null) {
            return existingOrder;
        }

        List<StockInboundCommand> stockCommands = buildStockCommands(request.getItems());
        List<Long> productIds = stockCommands.stream()
                .map(StockInboundCommand::getProductId)
                .distinct()
                .collect(Collectors.toList());

        try {
            return stockService.executeWithStockLocks(request.getWarehouseId(), productIds, () ->
                    transactionTemplate.execute(status -> {
                        stockService.addStocksInLock(request.getWarehouseId(), stockCommands);

                        InboundOrder inboundOrder = buildOrder(request, operatorId);
                        save(inboundOrder);

                        List<InboundOrderItem> items = buildOrderItems(inboundOrder.getId(), request.getItems());
                        inboundOrderItemService.saveBatch(items);
                        return inboundOrder;
                    })
            );
        } catch (DuplicateKeyException ex) {
            InboundOrder duplicatedOrder = findByRequestId(request.getRequestId());
            if (duplicatedOrder != null) {
                return duplicatedOrder;
            }
            throw ex;
        }
    }

    private InboundOrder buildOrder(InboundSubmitRequest request, Long operatorId) {
        InboundOrder inboundOrder = new InboundOrder();
        inboundOrder.setRequestId(request.getRequestId());
        inboundOrder.setOrderNo(request.getOrderNo() == null || request.getOrderNo().isBlank() ? generateOrderNo() : request.getOrderNo());
        inboundOrder.setWarehouseId(request.getWarehouseId());
        inboundOrder.setSupplierId(request.getSupplierId());
        inboundOrder.setOperatorId(operatorId);
        inboundOrder.setOrderTime(request.getOrderTime() == null ? LocalDateTime.now() : request.getOrderTime());
        inboundOrder.setRemark(request.getRemark());
        inboundOrder.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        inboundOrder.setTotalAmount(calculateTotalAmount(request.getItems()));
        return inboundOrder;
    }

    private List<InboundOrderItem> buildOrderItems(Long orderId, List<InboundSubmitItemRequest> requestItems) {
        List<InboundOrderItem> items = new ArrayList<>(requestItems.size());
        for (InboundSubmitItemRequest requestItem : requestItems) {
            InboundOrderItem orderItem = new InboundOrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setProductId(requestItem.getProductId());
            orderItem.setQuantity(requestItem.getQuantity());
            orderItem.setUnitPrice(defaultAmount(requestItem.getUnitPrice()));
            orderItem.setTotalPrice(resolveLineTotal(requestItem));
            orderItem.setBatchNo(requestItem.getBatchNo());
            orderItem.setRemark(requestItem.getRemark());
            items.add(orderItem);
        }
        return items;
    }

    private List<StockInboundCommand> buildStockCommands(List<InboundSubmitItemRequest> items) {
        Map<String, StockInboundCommand> aggregated = new LinkedHashMap<>();
        for (InboundSubmitItemRequest item : items) {
            String batchNo = item.getBatchNo() == null ? "" : item.getBatchNo();
            String key = item.getProductId() + "|" + batchNo;
            aggregated.compute(key, (ignored, existing) -> {
                if (existing == null) {
                    return StockInboundCommand.builder()
                            .productId(item.getProductId())
                            .batchNo(item.getBatchNo())
                            .quantity(item.getQuantity())
                            .build();
                }
                existing.setQuantity(existing.getQuantity().add(item.getQuantity()));
                return existing;
            });
        }
        return new ArrayList<>(aggregated.values());
    }

    private BigDecimal calculateTotalAmount(List<InboundSubmitItemRequest> items) {
        return items.stream()
                .map(this::resolveLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal resolveLineTotal(InboundSubmitItemRequest item) {
        if (item.getTotalPrice() != null) {
            return item.getTotalPrice();
        }
        return defaultAmount(item.getUnitPrice()).multiply(item.getQuantity());
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String generateOrderNo() {
        return "IN" + LocalDateTime.now().format(ORDER_NO_FORMATTER) + ThreadLocalRandom.current().nextInt(100000, 1000000);
    }

    private InboundOrder findByRequestId(String requestId) {
        QueryWrapper<InboundOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("request_id", requestId).last("limit 1");
        return getOne(wrapper);
    }
}
