package com.zhubao.manage.module.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.module.order.dto.*;
import com.zhubao.manage.module.order.entity.*;
import com.zhubao.manage.module.order.mapper.*;
import com.zhubao.manage.module.order.service.OrderService;
import com.zhubao.manage.module.product.entity.Product;
import com.zhubao.manage.module.product.mapper.ProductMapper;
import com.zhubao.manage.module.shop.dto.CreateOrderRequest;
import com.zhubao.manage.module.customer.entity.Customer;
import com.zhubao.manage.module.customer.mapper.CustomerMapper;
import com.zhubao.manage.module.shop.entity.ShopCart;
import com.zhubao.manage.module.shop.entity.UserAddress;
import com.zhubao.manage.module.shop.mapper.ShopCartMapper;
import com.zhubao.manage.module.shop.mapper.UserAddressMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShopOrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper itemMapper;
    private final OrderLogMapper logMapper;
    private final OrderReturnMapper returnMapper;
    private final ProductMapper productMapper;
    private final ShopCartMapper cartMapper;
    private final UserAddressMapper addressMapper;
    private final CustomerMapper customerMapper;
    private final OrderService orderService;

    public ShopOrderService(OrderMapper om, OrderItemMapper im, OrderLogMapper lm,
                            OrderReturnMapper rm, ProductMapper pm, ShopCartMapper cm,
                            UserAddressMapper am, CustomerMapper customerMapper,
                            OrderService os) {
        this.orderMapper = om; this.itemMapper = im; this.logMapper = lm;
        this.returnMapper = rm; this.productMapper = pm; this.cartMapper = cm;
        this.addressMapper = am; this.customerMapper = customerMapper;
        this.orderService = os;
    }

    /**
     * 从购物车提交订单
     */
    @Transactional
    public Order createOrder(Long userId, CreateOrderRequest req) {
        List<Map<String, Object>> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (req.getCartItemIds() != null && !req.getCartItemIds().isEmpty()) {
            // 从购物车结算
            List<ShopCart> carts = cartMapper.selectBatchIds(req.getCartItemIds());
            for (ShopCart cart : carts) {
                if (!cart.getUserId().equals(userId)) continue;
                Product p = productMapper.selectById(cart.getProductId());
                if (p == null) continue;
                if (!"ON_SALE".equals(p.getStatus())) throw new BusinessException(ErrorCode.BAD_REQUEST, "商品「" + p.getProductName() + "」已下架");
                int curStock = p.getStock() != null ? p.getStock() : 0;
                if (curStock < cart.getQuantity()) throw new BusinessException(ErrorCode.BAD_REQUEST, "商品「" + p.getProductName() + "」库存不足");

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("productId", p.getId());
                item.put("productCode", p.getProductCode());
                item.put("productName", p.getProductName());
                item.put("imageUrl", p.getImageUrl());
                item.put("spec", p.getSize() != null ? p.getSize() : p.getMaterial());
                item.put("quantity", cart.getQuantity());
                item.put("price", p.getRetailPrice());
                orderItems.add(item);

                totalAmount = totalAmount.add(p.getRetailPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
            }
            // 清空已购购物车项
            cartMapper.deleteBatchIds(req.getCartItemIds());
        } else if (req.getProductId() != null) {
            // 直接购买
            Product p = productMapper.selectById(req.getProductId());
            if (p == null) throw new BusinessException(ErrorCode.BAD_REQUEST, "商品不存在");
            if (!"ON_SALE".equals(p.getStatus())) throw new BusinessException(ErrorCode.BAD_REQUEST, "商品已下架");
            int qty = req.getQuantity() != null ? req.getQuantity() : 1;
            int curStock = p.getStock() != null ? p.getStock() : 0;
            if (curStock < qty) throw new BusinessException(ErrorCode.BAD_REQUEST, "库存不足");

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("productId", p.getId());
            item.put("productCode", p.getProductCode());
            item.put("productName", p.getProductName());
            item.put("imageUrl", p.getImageUrl());
            item.put("spec", p.getSize() != null ? p.getSize() : p.getMaterial());
            item.put("quantity", qty);
            item.put("price", p.getRetailPrice());
            orderItems.add(item);
            totalAmount = p.getRetailPrice().multiply(BigDecimal.valueOf(qty));
        }

        if (orderItems.isEmpty()) throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择商品");

        // 获取地址信息
        UserAddress address = null;
        if (req.getAddressId() != null) {
            address = addressMapper.selectById(req.getAddressId());
            if (address != null && !address.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "地址不属于当前用户");
            }
        }

        // 构建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderCode("OD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                String.format("%03d", (int) (Math.random() * 1000)));
        order.setOrderStatus("PENDING_PAY");
        order.setPaymentStatus("UNPAID");
        order.setOrderType("SHOP");

        if (address != null) {
            order.setCustomerName(address.getReceiverName());
            order.setCustomerPhone(address.getReceiverPhone());
            order.setCustomerAddress(buildAddressStr(address));
            // 地址快照 JSON
            order.setAddressSnapshot(buildAddressSnapshot(address));
            order.setAddressId(address.getId());
        }
        order.setPaymentMethod(req.getPaymentMethod() != null ? req.getPaymentMethod() : "WECHAT");
        order.setDeliveryMethod(req.getDeliveryMethod() != null ? req.getDeliveryMethod() : "EXPRESS");
        order.setRemark(req.getRemark());
        order.setTotalAmount(totalAmount);
        order.setFreight(BigDecimal.ZERO);
        order.setCouponDiscount(BigDecimal.ZERO);
        order.setOrderAmount(totalAmount);
        order.setItems(orderItems);

        // 委托给已有 OrderService 完成插入+扣库存+日志
        return orderService.create(order);
    }

    /** 我的订单分页 */
    public IPage<OrderVO> pageMyOrders(Long userId, PageDTO dto, String status) {
        LambdaQueryWrapper<Order> w = new LambdaQueryWrapper<>();
        w.eq(Order::getUserId, userId);
        if (StringUtils.isNotBlank(status)) {
            if ("PENDING_PAY".equals(status) || "PAID".equals(status) || "SHIPPED".equals(status)
                    || "RECEIVED".equals(status) || "FINISHED".equals(status) || "CANCELLED".equals(status)
                    || "completed".equals(status) || "refund".equals(status)) {
                // "已完成" 标签：匹配所有终态（RECEIVED / completed / refund）
                if ("RECEIVED".equals(status)) {
                    w.and(x -> x.eq(Order::getOrderStatus, "RECEIVED")
                            .or().eq(Order::getOrderStatus, "completed")
                            .or().eq(Order::getOrderStatus, "refund"));
                } else {
                    w.eq(Order::getOrderStatus, status);
                }
            }
        }
        w.orderByDesc(Order::getCreatedAt);
        Page<Order> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        IPage<Order> result = orderMapper.selectPage(page, w);

        List<Long> orderIds = result.getRecords().stream().map(Order::getId).collect(Collectors.toList());
        Map<Long, List<OrderItem>> itemMap = buildItemMap(orderIds);
        Map<Long, List<OrderLog>> logMap = buildLogMap(orderIds);

        IPage<OrderVO> voPage = new Page<>(dto.getPageNum(), dto.getPageSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(o -> toVO(o, itemMap, logMap)).collect(Collectors.toList()));
        return voPage;
    }

    /** C端订单详情 */
    public OrderVO detail(Long userId, Long orderId) {
        Order o = orderMapper.selectById(orderId);
        if (o == null || !o.getUserId().equals(userId)) return null;
        List<OrderItem> items = itemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        List<OrderLog> logs = logMapper.selectList(new LambdaQueryWrapper<OrderLog>().eq(OrderLog::getOrderId, orderId));
        return toVO(o, Collections.singletonMap(orderId, items), Collections.singletonMap(orderId, logs));
    }

    /** 取消订单（仅待付款可取消） */
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order o = orderMapper.selectById(orderId);
        if (o == null || !o.getUserId().equals(userId)) throw new BusinessException(ErrorCode.BAD_REQUEST, "订单不存在");
        if (!"PENDING_PAY".equals(o.getOrderStatus())) throw new BusinessException(ErrorCode.BAD_REQUEST, "仅待付款订单可取消");
        o.setOrderStatus("CANCELLED");
        orderMapper.updateById(o);
        addLog(orderId, "用户取消订单");
    }

    /** 模拟支付（开发阶段） */
    @Transactional
    public Order payOrder(Long userId, Long orderId) {
        Order o = orderMapper.selectById(orderId);
        if (o == null || !o.getUserId().equals(userId)) throw new BusinessException(ErrorCode.BAD_REQUEST, "订单不存在");
        if (!"PENDING_PAY".equals(o.getOrderStatus())) throw new BusinessException(ErrorCode.BAD_REQUEST, "订单状态不正确");
        o.setOrderStatus("PAID");
        o.setPaymentStatus("PAID");
        o.setPaymentTime(LocalDateTime.now());
        o.setPaymentTradeNo("PAY" + System.currentTimeMillis());
        orderMapper.updateById(o);
        addLog(orderId, "支付成功");

        // 同步客户消费数据到 customer 表
        syncCustomerConsumption(userId, o.getOrderAmount());
        return o;
    }

    /** 更新客户累计消费、最近消费时间、积分 */
    private void syncCustomerConsumption(Long userId, BigDecimal orderAmount) {
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) return;
        Customer customer = customerMapper.selectOne(
                new LambdaQueryWrapper<Customer>().eq(Customer::getUserId, userId));
        if (customer == null) return; // 客户记录不存在时跳过（如历史用户未同步到 customer 表）
        BigDecimal currentTotal = customer.getTotalConsumption() != null
                ? customer.getTotalConsumption() : BigDecimal.ZERO;
        customer.setTotalConsumption(currentTotal.add(orderAmount));
        customer.setLastConsumptionAt(LocalDate.now().toString());
        int earnedPoints = orderAmount.divide(BigDecimal.TEN, 0, BigDecimal.ROUND_DOWN).intValue();
        customer.setPoints((customer.getPoints() != null ? customer.getPoints() : 0) + earnedPoints);
        customerMapper.updateById(customer);
    }

    /** 确认收货 */
    @Transactional
    public void confirmReceive(Long userId, Long orderId) {
        Order o = orderMapper.selectById(orderId);
        if (o == null || !o.getUserId().equals(userId)) throw new BusinessException(ErrorCode.BAD_REQUEST, "订单不存在");
        if (!"SHIPPED".equals(o.getOrderStatus())) throw new BusinessException(ErrorCode.BAD_REQUEST, "订单状态不正确");
        o.setOrderStatus("RECEIVED");
        o.setReceiveTime(LocalDateTime.now());
        orderMapper.updateById(o);
        addLog(orderId, "用户确认收货");
    }

    /** 申请退货/退款 */
    @Transactional
    public OrderReturn applyReturn(Long userId, Long orderId, String reason, BigDecimal refundAmount) {
        Order o = orderMapper.selectById(orderId);
        if (o == null || !o.getUserId().equals(userId)) throw new BusinessException(ErrorCode.BAD_REQUEST, "订单不存在");
        if ("PENDING_PAY".equals(o.getOrderStatus())) throw new BusinessException(ErrorCode.BAD_REQUEST, "待付款订单无需退货");

        OrderReturn r = new OrderReturn();
        r.setReturnCode("RT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                String.format("%03d", (int) (Math.random() * 1000)));
        r.setOrderCode(o.getOrderCode());
        r.setOrderId(orderId);
        r.setReason(reason);
        r.setRefundAmount(refundAmount != null ? refundAmount : o.getOrderAmount());
        r.setOrderAmount(o.getOrderAmount());
        r.setStatus("APPLYING");
        r.setReturnType("refund");
        r.setApplyTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 获取商品信息
        List<OrderItem> items = itemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        if (!items.isEmpty()) {
            OrderItem firstItem = items.get(0);
            r.setProductName(firstItem.getProductName());
            r.setProductSpec(firstItem.getSpec());
            r.setImageUrl(firstItem.getImageUrl());
            r.setQuantity(firstItem.getQuantity());
        }
        returnMapper.insert(r);
        addLog(orderId, "用户申请退货/退款: " + (reason != null ? reason : ""));
        return r;
    }

    // ===== 内部 =====

    private String buildAddressStr(UserAddress a) {
        return (a.getProvince() != null ? a.getProvince() : "") +
                (a.getCity() != null ? a.getCity() : "") +
                (a.getDistrict() != null ? a.getDistrict() : "") +
                (a.getDetailAddress() != null ? a.getDetailAddress() : "");
    }

    private String buildAddressSnapshot(UserAddress a) {
        return "{\"receiverName\":\"" + (a.getReceiverName() != null ? a.getReceiverName() : "") +
                "\",\"receiverPhone\":\"" + (a.getReceiverPhone() != null ? a.getReceiverPhone() : "") +
                "\",\"province\":\"" + (a.getProvince() != null ? a.getProvince() : "") +
                "\",\"city\":\"" + (a.getCity() != null ? a.getCity() : "") +
                "\",\"district\":\"" + (a.getDistrict() != null ? a.getDistrict() : "") +
                "\",\"detailAddress\":\"" + (a.getDetailAddress() != null ? a.getDetailAddress() : "") + "\"}";
    }

    private void addLog(Long orderId, String content) {
        OrderLog log = new OrderLog();
        log.setOrderId(orderId);
        log.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        log.setContent(content);
        logMapper.insert(log);
    }

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
            iv.setProductId(i.getProductId());
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
