package com.zhubao.manage.module.sales.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class SalesCreateDTO {
    @NotNull private Long storeId;
    @NotNull private Long employeeId;
    @NotNull private String salesDate;
    private String orderNo;
    @NotNull private BigDecimal totalAmount;
    @NotNull private BigDecimal paidAmount;
    @NotNull private Integer productCount;
    @NotNull private String customerType;
    private String customerGender;
    private String customerAgeRange;
    @NotNull private String purchaseScene;
    private String customerConcern;
    private String salesPhotoUrls;
    private String remark;
    @NotEmpty private List<SalesItemDTO> items;

    // getters/setters
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long v) { this.storeId = v; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long v) { this.employeeId = v; }
    public String getSalesDate() { return salesDate; }
    public void setSalesDate(String v) { this.salesDate = v; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String v) { this.orderNo = v; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal v) { this.totalAmount = v; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal v) { this.paidAmount = v; }
    public Integer getProductCount() { return productCount; }
    public void setProductCount(Integer v) { this.productCount = v; }
    public String getCustomerType() { return customerType; }
    public void setCustomerType(String v) { this.customerType = v; }
    public String getCustomerGender() { return customerGender; }
    public void setCustomerGender(String v) { this.customerGender = v; }
    public String getCustomerAgeRange() { return customerAgeRange; }
    public void setCustomerAgeRange(String v) { this.customerAgeRange = v; }
    public String getPurchaseScene() { return purchaseScene; }
    public void setPurchaseScene(String v) { this.purchaseScene = v; }
    public String getCustomerConcern() { return customerConcern; }
    public void setCustomerConcern(String v) { this.customerConcern = v; }
    public String getSalesPhotoUrls() { return salesPhotoUrls; }
    public void setSalesPhotoUrls(String v) { this.salesPhotoUrls = v; }
    public String getRemark() { return remark; }
    public void setRemark(String v) { this.remark = v; }
    public List<SalesItemDTO> getItems() { return items; }
    public void setItems(List<SalesItemDTO> v) { this.items = v; }

    public static class SalesItemDTO {
        private Long productId;
        private String productName;
        private String category;
        private String style;
        private String material;
        private String weight;
        private String size;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal grossMarginRate;
        private String customerFavoritePoint;
        private String objection;
        private String closingReason;
        public Long getProductId() { return productId; }
        public void setProductId(Long v) { this.productId = v; }
        public String getProductName() { return productName; }
        public void setProductName(String v) { this.productName = v; }
        public String getCategory() { return category; }
        public void setCategory(String v) { this.category = v; }
        public String getStyle() { return style; }
        public void setStyle(String v) { this.style = v; }
        public String getMaterial() { return material; }
        public void setMaterial(String v) { this.material = v; }
        public String getWeight() { return weight; }
        public void setWeight(String v) { this.weight = v; }
        public String getSize() { return size; }
        public void setSize(String v) { this.size = v; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal v) { this.price = v; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer v) { this.quantity = v; }
        public BigDecimal getGrossMarginRate() { return grossMarginRate; }
        public void setGrossMarginRate(BigDecimal v) { this.grossMarginRate = v; }
        public String getCustomerFavoritePoint() { return customerFavoritePoint; }
        public void setCustomerFavoritePoint(String v) { this.customerFavoritePoint = v; }
        public String getObjection() { return objection; }
        public void setObjection(String v) { this.objection = v; }
        public String getClosingReason() { return closingReason; }
        public void setClosingReason(String v) { this.closingReason = v; }
    }
}
