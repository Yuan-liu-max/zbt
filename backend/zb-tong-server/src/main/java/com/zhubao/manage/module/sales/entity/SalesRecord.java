package com.zhubao.manage.module.sales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sales_record")
public class SalesRecord {
    @TableId(type = IdType.AUTO) private Long id;
    private String salesNo;
    private Long storeId;
    private Long employeeId;
    private LocalDate salesDate;
    private String orderNo;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private Integer productCount;
    private String customerType;
    private String customerGender;
    private String customerAgeRange;
    private String purchaseScene;
    private String customerConcern;
    private String salesPhotoUrls;
    private String remark;
    private String auditStatus;
    private Long auditorId;
    private String auditComment;
    private LocalDateTime auditedAt;
    private String externalSource;
    private String externalOrderId;
    private String syncStatus;
    @TableLogic private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
}
