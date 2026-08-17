package com.zhubao.manage.module.shop.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * C端下单请求
 */
public class CreateOrderRequest {

    private List<Long> cartItemIds;       // 从购物车结算的商品cartId列表
    private Long addressId;               // 收货地址ID
    private String paymentMethod;         // 支付方式: WECHAT/ALIPAY/BALANCE
    private String deliveryMethod;        // 配送方式: EXPRESS/SELF_PICKUP
    private String couponCode;            // 优惠券码(可选)
    private String remark;                // 备注

    // 直接下单（不通过购物车）
    private Long productId;
    private Integer quantity;
    private BigDecimal price;

    public List<Long> getCartItemIds() { return cartItemIds; }
    public void setCartItemIds(List<Long> v) { this.cartItemIds = v; }
    public Long getAddressId() { return addressId; }
    public void setAddressId(Long v) { this.addressId = v; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String v) { this.paymentMethod = v; }
    public String getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(String v) { this.deliveryMethod = v; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String v) { this.couponCode = v; }
    public String getRemark() { return remark; }
    public void setRemark(String v) { this.remark = v; }
    public Long getProductId() { return productId; }
    public void setProductId(Long v) { this.productId = v; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer v) { this.quantity = v; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal v) { this.price = v; }
}
