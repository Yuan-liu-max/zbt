package com.zhubao.manage.module.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("purchase_item")
public class PurchaseItem {
    @TableId(type = IdType.AUTO) private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}
