package com.zhubao.manage.module.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO) private Long id;
    @NotBlank(message = "商品编码不能为空") private String productCode;
    @NotBlank(message = "商品名称不能为空") private String productName;
    @NotBlank(message = "品类不能为空") private String category;
    @DecimalMin(value = "0.01", message = "零售价必须大于0.01") private BigDecimal retailPrice;
    private String style;
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
    @TableLogic private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
}
