package com.zhubao.manage.module.sales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sales_item")
public class SalesItem {
    @TableId(type = IdType.AUTO) private Long id;
    private Long salesRecordId;
    private Long productId;
    private String productName;
    private String category;
    private String style;
    private String material;
    private String weight;
    private String size;
    private String color;
    private String shape;
    private String meaning;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal grossMarginRate;
    private String customerFavoritePoint;
    private String objection;
    private String closingReason;
    private String productPhotoUrls;
    @TableLogic private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
