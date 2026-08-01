package com.zhubao.manage.module.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sales_order")
public class Order {
    @TableId(type = IdType.AUTO) private Long id;
    private String orderCode;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private BigDecimal totalAmount;
    private BigDecimal freight;
    private BigDecimal couponDiscount;
    private BigDecimal orderAmount;
    private String orderStatus;
    private String paymentStatus;
    private String paymentMethod;
    private String deliveryMethod;
    private String remark;
    @TableLogic private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
}
