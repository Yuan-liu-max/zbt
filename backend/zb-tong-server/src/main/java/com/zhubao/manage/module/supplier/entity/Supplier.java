package com.zhubao.manage.module.supplier.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("supplier")
public class Supplier {
    @TableId(type = IdType.AUTO) private Long id;
    private String name;
    private String logo;
    private String type;
    private String contactPerson;
    private String contactPhone;
    private String email;
    private String status;
    private String address;
    private String remark;
    @TableLogic private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
