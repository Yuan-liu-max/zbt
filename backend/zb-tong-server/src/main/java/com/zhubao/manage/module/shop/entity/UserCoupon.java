package com.zhubao.manage.module.shop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * C端用户领取的优惠券（营销三端打通）
 */
@Data
@TableName("user_coupon")
public class UserCoupon {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long promotionId;
    private String name;
    private String type;
    private String discountMethod;
    private BigDecimal threshold;
    private BigDecimal discountValue;
    private String status;              // UNUSED|USED|EXPIRED|DISABLED
    private Long usedOrderId;
    private LocalDateTime usedAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime receivedAt;
    private LocalDateTime expireTime;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
