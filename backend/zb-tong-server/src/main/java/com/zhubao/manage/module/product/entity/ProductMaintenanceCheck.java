package com.zhubao.manage.module.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("product_maintenance_check")
public class ProductMaintenanceCheck {
    @TableId(type = IdType.AUTO) private Long id;
    private Long productId;
    private String category;
    private LocalDate checkDate;
    private Long checkerId;
    private String maintenanceResult;
    private String issueDescription;
    private String photoUrls;
    private String handledResult;
    @TableLogic private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
