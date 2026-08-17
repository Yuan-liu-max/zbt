package com.zhubao.manage.module.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("operate_log")
public class OperateLog {
    @TableId(type = IdType.AUTO) private Long id;
    private Long operatorId;
    private String module;
    private String action;
    private String targetType;
    private Long targetId;
    private String requestIp;
    private String requestParams;
    private String oldData;
    private String newData;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(exist = false) private String operatorName;
}
