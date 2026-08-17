package com.zhubao.manage.module.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sales_order")
public class Order {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private String orderCode;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private Long buyerId;
    private Long addressId;
    private String addressSnapshot;
    private BigDecimal totalAmount;
    private BigDecimal freight;
    private BigDecimal couponDiscount;
    private BigDecimal orderAmount;
    private String orderStatus;
    private String paymentStatus;
    private String paymentMethod;
    private LocalDateTime paymentTime;
    private String paymentTradeNo;
    private String deliveryMethod;
    private String deliveryCompany;
    private String deliveryTrackNo;
    private LocalDateTime deliveryTime;
    private LocalDateTime receiveTime;
    private LocalDateTime finishTime;
    private String orderType;
    private String remark;
    @TableLogic private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
    @TableField(exist = false) private java.util.List<java.util.Map<String, Object>> items;
    @TableField(exist = false) private Long creatorId;
}
