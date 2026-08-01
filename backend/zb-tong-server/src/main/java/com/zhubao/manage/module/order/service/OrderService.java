package com.zhubao.manage.module.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.module.order.dto.*;
import com.zhubao.manage.module.order.entity.*;
import com.zhubao.manage.module.order.mapper.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper itemMapper;
    private final OrderLogMapper logMapper;
    private final OrderReturnMapper returnMapper;

    public OrderService(OrderMapper om, OrderItemMapper im, OrderLogMapper lm, OrderReturnMapper rm) {
        this.orderMapper = om; this.itemMapper = im; this.logMapper = lm; this.returnMapper = rm;
    }

    // ==================== 订单 ====================

    /** 分页列表 — 组装 items + logs */
    public IPage<OrderVO> pageOrders(OrderQueryDTO q) {
        LambdaQueryWrapper<Order> w = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(q.getKeyword()))
            w.and(x -> x.like(Order::getOrderCode, q.getKeyword()).or().like(Order::getCustomerName, q.getKeyword()));
        if (StringUtils.isNotBlank(q.getStatus())) w.eq(Order::getOrderStatus, q.getStatus());
        if (StringUtils.isNotBlank(q.getStartDate())) w.ge(Order::getCreatedAt, q.getStartDate() + " 00:00:00");
        if (StringUtils.isNotBlank(q.getEndDate())) w.le(Order::getCreatedAt, q.getEndDate() + " 23:59:59");
        w.orderByDesc(Order::getCreatedAt);

        Page<Order> page = new Page<>(q.getPageNum(), q.getPageSize());
        IPage<Order> result = orderMapper.selectPage(page, w);

        List<Long> orderIds = result.getRecords().stream().map(Order::getId).collect(Collectors.toList());
        Map<Long, List<OrderItem>> itemMap = buildItemMap(orderIds);
        Map<Long, List<OrderLog>> logMap = buildLogMap(orderIds);

        IPage<OrderVO> voPage = new Page<>(q.getPageNum(), q.getPageSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(o -> toVO(o, itemMap, logMap)).collect(Collectors.toList()));
        return voPage;
    }

    /** 订单详情 — 含 items + logs */
    public OrderVO detail(Long id) {
        Order o = orderMapper.selectById(id);
        if (o == null) return null;
        List<OrderItem> items = itemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        List<OrderLog> logs = logMapper.selectList(new LambdaQueryWrapper<OrderLog>().eq(OrderLog::getOrderId, id));
        Map<Long, List<OrderItem>> im = Collections.singletonMap(id, items);
        Map<Long, List<OrderLog>> lm = Collections.singletonMap(id, logs);
        return toVO(o, im, lm);
    }

    /** 更新订单 */
    @Transactional
    public void update(Long id, Order order) {
        order.setId(id);
        orderMapper.updateById(order);
    }

    /** 取消订单 */
    @Transactional
    public void cancel(Long id) {
        Order o = orderMapper.selectById(id);
        if (o == null || "cancelled".equals(o.getOrderStatus())) return;
        o.setOrderStatus("cancelled");
        orderMapper.updateById(o);
        OrderLog log = new OrderLog();
        log.setOrderId(id);
        log.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        log.setContent("订单已取消");
        logMapper.insert(log);
    }

    /** 追加操作日志 */
    @Transactional
    public void addLog(Long orderId, String content) {
        OrderLog log = new OrderLog();
        log.setOrderId(orderId);
        log.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        log.setContent(content);
        logMapper.insert(log);
    }

    // ==================== 退换货 ====================

    public IPage<OrderReturn> pageReturns(ReturnQueryDTO q) {
        LambdaQueryWrapper<OrderReturn> w = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(q.getKeyword()))
            w.and(x -> x.like(OrderReturn::getReturnCode, q.getKeyword()).or().like(OrderReturn::getOrderCode, q.getKeyword()));
        if (StringUtils.isNotBlank(q.getReturnType())) w.eq(OrderReturn::getReturnType, q.getReturnType());
        if (StringUtils.isNotBlank(q.getStatus())) w.eq(OrderReturn::getStatus, q.getStatus());
        if (StringUtils.isNotBlank(q.getStartDate())) w.ge(OrderReturn::getCreatedAt, q.getStartDate() + " 00:00:00");
        if (StringUtils.isNotBlank(q.getEndDate())) w.le(OrderReturn::getCreatedAt, q.getEndDate() + " 23:59:59");
        w.orderByDesc(OrderReturn::getCreatedAt);
        return returnMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
    }

    public OrderReturn detailReturn(Long id) {
        return returnMapper.selectById(id);
    }

    @Transactional
    public void cancelReturn(Long id) {
        OrderReturn r = returnMapper.selectById(id);
        if (r != null && "applying".equals(r.getStatus())) {
            r.setStatus("cancelled"); returnMapper.updateById(r);
        }
    }

    // ==================== 内部 ====================

    private Map<Long, List<OrderItem>> buildItemMap(List<Long> orderIds) {
        if (orderIds.isEmpty()) return Collections.emptyMap();
        return itemMapper.selectList(new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds))
                .stream().collect(Collectors.groupingBy(OrderItem::getOrderId));
    }

    private Map<Long, List<OrderLog>> buildLogMap(List<Long> orderIds) {
        if (orderIds.isEmpty()) return Collections.emptyMap();
        return logMapper.selectList(new LambdaQueryWrapper<OrderLog>().in(OrderLog::getOrderId, orderIds))
                .stream().collect(Collectors.groupingBy(OrderLog::getOrderId));
    }

    private OrderVO toVO(Order o, Map<Long, List<OrderItem>> im, Map<Long, List<OrderLog>> lm) {
        OrderVO vo = new OrderVO();
        vo.setId(String.valueOf(o.getId()));
        vo.setOrderCode(o.getOrderCode());
        vo.setCustomerName(o.getCustomerName());
        vo.setCustomerPhone(o.getCustomerPhone());
        vo.setCustomerAddress(o.getCustomerAddress());

        List<OrderItem> items = im.getOrDefault(o.getId(), Collections.emptyList());
        vo.setItems(items.stream().map(i -> {
            OrderItemVO iv = new OrderItemVO();
            iv.setId(String.valueOf(i.getId()));
            iv.setProductCode(i.getProductCode());
            iv.setProductName(i.getProductName());
            iv.setImageUrl(i.getImageUrl());
            iv.setSpec(i.getSpec());
            iv.setQuantity(i.getQuantity() != null ? i.getQuantity() : 1);
            iv.setPrice(i.getPrice());
            return iv;
        }).collect(Collectors.toList()));

        vo.setTotalAmount(o.getTotalAmount());
        vo.setFreight(o.getFreight());
        vo.setCouponDiscount(o.getCouponDiscount());
        vo.setOrderAmount(o.getOrderAmount());
        vo.setOrderStatus(o.getOrderStatus());
        vo.setPaymentStatus(o.getPaymentStatus());
        vo.setPaymentMethod(o.getPaymentMethod());
        vo.setDeliveryMethod(o.getDeliveryMethod());
        vo.setRemark(o.getRemark());
        vo.setCreatedAt(o.getCreatedAt() != null ? o.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);

        List<OrderLog> logs = lm.getOrDefault(o.getId(), Collections.emptyList());
        vo.setLogs(logs.stream().map(l -> {
            OrderLogVO lv = new OrderLogVO(); lv.setTime(l.getTime()); lv.setContent(l.getContent()); return lv;
        }).collect(Collectors.toList()));

        return vo;
    }
}
