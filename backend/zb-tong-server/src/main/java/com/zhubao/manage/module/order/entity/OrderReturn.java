package com.zhubao.manage.module.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_return")
public class OrderReturn {
    @TableId(type = IdType.AUTO) private Long id;
    private String returnCode;
    private String orderCode;
    private String returnType;
    private String reason;
    private String applyTime;
    private String status;
    private BigDecimal orderAmount;
    private String productName;
    private String productSpec;
    private String imageUrl;
    private Integer quantity;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
