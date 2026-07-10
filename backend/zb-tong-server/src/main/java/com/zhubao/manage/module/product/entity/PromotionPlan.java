package com.zhubao.manage.module.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("promotion_plan")
public class PromotionPlan {
    @TableId(type = IdType.AUTO) private Long id;
    private String activityMonth;
    private Long storeId;
    private String activityName;
    private String activityTheme;
    private LocalDate activityPeriodStart;
    private LocalDate activityPeriodEnd;
    private String promotionRules;
    private String mainProducts;
    private String materialRequirements;
    private String employeeScript;
    private String preheatPlan;
    private String customerReachPlan;
    private BigDecimal expectedSales;
    private String status;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
}
