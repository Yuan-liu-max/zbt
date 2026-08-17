package com.zhubao.manage.module.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("product_inventory_check")
public class ProductInventoryCheck {
    @TableId(type = IdType.AUTO) private Long id;
    private String checkCode;
    private String checkName;
    private String checkType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private LocalDate checkDate;
    private Long storeId;
    private Long checkedBy;
    private Integer totalCheckedCount;
    private Integer abnormalCount;
    private String abnormalItems;
    private String photos;
    private String warehouse;
    private String remark;
    @TableLogic private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
