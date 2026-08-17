package com.zhubao.manage.module.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zhubao.manage.module.product.entity.Product;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Product VO — 对齐 frontend GoodsItem 字段名：code/name/categoryName/brandName
 */
public class ProductVO {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Long id;
    private String code;
    private String name;
    private String categoryId;
    private String categoryName;
    private String brandId;
    private String brandName;
    private BigDecimal price;
    private BigDecimal costPrice;
    private BigDecimal grossMarginRate;
    private Integer stock;
    private Long storeId;
    private String storeName;
    private String status;
    private String imageUrl;
    private String description;
    private String createdAt;
    private String updatedAt;
    private String material;
    private String weight;
    private String size;
    private String color;
    private String shape;
    private String meaning;

    // shop 前端兼容别名
    @JsonProperty("productCode") private String getProductCodeAlias() { return code; }
    @JsonProperty("productName") private String getProductNameAlias() { return name; }
    @JsonProperty("category") private String getCategoryAlias() { return categoryName; }
    @JsonProperty("style") private String getStyleAlias() { return brandName; }

    public static ProductVO from(Product p) {
        ProductVO vo = new ProductVO();
        vo.id = p.getId();
        vo.code = p.getProductCode();
        vo.name = p.getProductName();
        vo.categoryId = p.getCategory();
        vo.categoryName = p.getCategory();
        vo.brandId = p.getStyle();
        vo.brandName = p.getStyle();
        vo.price = p.getRetailPrice();
        vo.costPrice = p.getCostPrice();
        vo.grossMarginRate = p.getGrossMarginRate();
        vo.stock = p.getStock();
        vo.storeId = p.getStoreId();
        vo.storeName = p.getStoreName();
        vo.status = p.getStatus();
        vo.imageUrl = p.getImageUrl();
        vo.description = p.getMeaning() != null ? p.getMeaning() : "";
        vo.createdAt = p.getCreatedAt() != null ? FORMATTER.format(p.getCreatedAt()) : null;
        vo.updatedAt = p.getUpdatedAt() != null ? FORMATTER.format(p.getUpdatedAt()) : null;
        vo.material = p.getMaterial();
        vo.weight = p.getWeight();
        vo.size = p.getSize();
        vo.color = p.getColor();
        vo.shape = p.getShape();
        vo.meaning = p.getMeaning();
        return vo;
    }

    // getters
    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getBrandId() { return brandId; }
    public String getBrandName() { return brandName; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getCostPrice() { return costPrice; }
    public BigDecimal getGrossMarginRate() { return grossMarginRate; }
    public Integer getStock() { return stock; }
    public Long getStoreId() { return storeId; }
    public String getStoreName() { return storeName; }
    public String getStatus() { return status; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public String getMaterial() { return material; }
    public String getWeight() { return weight; }
    public String getSize() { return size; }
    public String getColor() { return color; }
    public String getShape() { return shape; }
    public String getMeaning() { return meaning; }

    // setters
    public void setId(Long v) { this.id = v; }
    public void setCode(String v) { this.code = v; }
    public void setName(String v) { this.name = v; }
    public void setCategoryId(String v) { this.categoryId = v; }
    public void setCategoryName(String v) { this.categoryName = v; }
    public void setBrandId(String v) { this.brandId = v; }
    public void setBrandName(String v) { this.brandName = v; }
    public void setPrice(BigDecimal v) { this.price = v; }
    public void setCostPrice(BigDecimal v) { this.costPrice = v; }
    public void setGrossMarginRate(BigDecimal v) { this.grossMarginRate = v; }
    public void setStock(Integer v) { this.stock = v; }
    public void setStoreId(Long v) { this.storeId = v; }
    public void setStoreName(String v) { this.storeName = v; }
    public void setStatus(String v) { this.status = v; }
    public void setImageUrl(String v) { this.imageUrl = v; }
    public void setDescription(String v) { this.description = v; }
    public void setCreatedAt(String v) { this.createdAt = v; }
    public void setUpdatedAt(String v) { this.updatedAt = v; }
    public void setMaterial(String v) { this.material = v; }
    public void setWeight(String v) { this.weight = v; }
    public void setSize(String v) { this.size = v; }
    public void setColor(String v) { this.color = v; }
    public void setShape(String v) { this.shape = v; }
    public void setMeaning(String v) { this.meaning = v; }
}
