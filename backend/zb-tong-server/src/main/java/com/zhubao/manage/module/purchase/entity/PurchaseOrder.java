package com.zhubao.manage.module.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("purchase_order")
public class PurchaseOrder {
    @TableId(type = IdType.AUTO) private Long id;
    private String orderNo;
    private Long storeId;
    private Long supplierId;
    private BigDecimal totalAmount;
    private String status;
    private Long applicantId;
    private Long approverId;
    private String remark;
    @TableLogic private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
}
