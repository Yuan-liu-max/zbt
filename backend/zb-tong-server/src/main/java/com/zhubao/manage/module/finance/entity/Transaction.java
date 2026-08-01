package com.zhubao.manage.module.finance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("transaction")
public class Transaction {
    @TableId(type = IdType.AUTO) private Long id;
    private String code;
    private String type;
    private String account;
    private String relatedObject;
    private BigDecimal amount;
    private String transactionDate;
    private String remark;
    @TableLogic private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
