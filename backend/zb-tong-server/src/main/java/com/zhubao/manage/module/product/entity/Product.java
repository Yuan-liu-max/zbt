package com.zhubao.manage.module.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO) private Long id;
    @JsonProperty("code") private String productCode;
    @NotBlank @JsonProperty("name") private String productName;
    @NotBlank @JsonProperty("categoryName") private String category;
    @DecimalMin("0") @JsonProperty("price") @JsonAlias("retailPrice") private BigDecimal retailPrice;
    @JsonProperty("brandName") private String style;
    private String material;
    private String weight;
    private String size;
    private String color;
    private String shape;
    private String meaning;
    private BigDecimal costPrice;
    private BigDecimal grossMarginRate;
    private String status;
    private Long storeId;
    @JsonProperty("stock") private Integer stock;
    @TableLogic private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
    private LocalDateTime warningHandledAt;
    private String imageUrl;
    @TableField(exist = false) @JsonProperty("storeName") private String storeName;
}
