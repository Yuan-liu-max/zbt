package com.zhubao.manage.module.order.dto;

import java.math.BigDecimal;

public class OrderItemVO {
    private String id;
    private Long productId;
    private String productCode;
    private String productName;
    private String imageUrl;
    private String spec;
    private int quantity;
    private BigDecimal price;

    public String getId() { return id; } public void setId(String v) { this.id = v; }
    public Long getProductId() { return productId; } public void setProductId(Long v) { this.productId = v; }
    public String getProductCode() { return productCode; } public void setProductCode(String v) { this.productCode = v; }
    public String getProductName() { return productName; } public void setProductName(String v) { this.productName = v; }
    public String getImageUrl() { return imageUrl; } public void setImageUrl(String v) { this.imageUrl = v; }
    public String getSpec() { return spec; } public void setSpec(String v) { this.spec = v; }
    public int getQuantity() { return quantity; } public void setQuantity(int v) { this.quantity = v; }
    public BigDecimal getPrice() { return price; } public void setPrice(BigDecimal v) { this.price = v; }
}
