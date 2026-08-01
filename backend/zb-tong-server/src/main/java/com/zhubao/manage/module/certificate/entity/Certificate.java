package com.zhubao.manage.module.certificate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("certificate")
public class Certificate {
    @TableId(type = IdType.AUTO) private Long id;
    private String code;
    private String type;
    private String productName;
    private String issuer;
    private String issueDate;
    private String expiryDate;
    private String status;
    private String fileUrl;
    private String remark;
    @TableLogic private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
