package com.zhubao.manage.module.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("new_product_plan")
public class NewProductPlan {
    @TableId(type = IdType.AUTO) private Long id;
    private String planMonth;
    private Long storeId;
    private Long plannerId;
    private String newProductList;
    private String sellingPoints;
    private String targetCustomerGroup;
    private String displayPlan;
    private String trainingPlan;
    private BigDecimal salesTarget;
    private String promotionScript;
    private String attachmentUrls;
    private String status;
    @TableLogic private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
}
