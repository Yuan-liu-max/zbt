package com.zhubao.manage.module.order.dto;

import java.math.BigDecimal;
import java.util.List;

/** 订单 VO — JSON 字段名严格对齐前端 OrderList.vue */
public class OrderVO {
    private String id;
    private String orderCode;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private List<OrderItemVO> items;
    private BigDecimal totalAmount;
    private BigDecimal freight;
    private BigDecimal couponDiscount;
    private BigDecimal orderAmount;
    private String orderStatus;
    private String paymentStatus;
    private String paymentMethod;
    private String deliveryMethod;
    private String remark;
    private String createdAt;
    private List<OrderLogVO> logs;

    // getters + setters
    public String getId() { return id; } public void setId(String v) { this.id = v; }
    public String getOrderCode() { return orderCode; } public void setOrderCode(String v) { this.orderCode = v; }
    public String getCustomerName() { return customerName; } public void setCustomerName(String v) { this.customerName = v; }
    public String getCustomerPhone() { return customerPhone; } public void setCustomerPhone(String v) { this.customerPhone = v; }
    public String getCustomerAddress() { return customerAddress; } public void setCustomerAddress(String v) { this.customerAddress = v; }
    public List<OrderItemVO> getItems() { return items; } public void setItems(List<OrderItemVO> v) { this.items = v; }
    public BigDecimal getTotalAmount() { return totalAmount; } public void setTotalAmount(BigDecimal v) { this.totalAmount = v; }
    public BigDecimal getFreight() { return freight; } public void setFreight(BigDecimal v) { this.freight = v; }
    public BigDecimal getCouponDiscount() { return couponDiscount; } public void setCouponDiscount(BigDecimal v) { this.couponDiscount = v; }
    public BigDecimal getOrderAmount() { return orderAmount; } public void setOrderAmount(BigDecimal v) { this.orderAmount = v; }
    public String getOrderStatus() { return orderStatus; } public void setOrderStatus(String v) { this.orderStatus = v; }
    public String getPaymentStatus() { return paymentStatus; } public void setPaymentStatus(String v) { this.paymentStatus = v; }
    public String getPaymentMethod() { return paymentMethod; } public void setPaymentMethod(String v) { this.paymentMethod = v; }
    public String getDeliveryMethod() { return deliveryMethod; } public void setDeliveryMethod(String v) { this.deliveryMethod = v; }
    public String getRemark() { return remark; } public void setRemark(String v) { this.remark = v; }
    public String getCreatedAt() { return createdAt; } public void setCreatedAt(String v) { this.createdAt = v; }
    public List<OrderLogVO> getLogs() { return logs; } public void setLogs(List<OrderLogVO> v) { this.logs = v; }
}
