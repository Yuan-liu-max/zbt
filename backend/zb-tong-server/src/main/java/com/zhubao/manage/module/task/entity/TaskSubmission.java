package com.zhubao.manage.module.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_submission")
public class TaskSubmission {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;
    private Long submitterId;
    private String textContent;
    private String formData;
    private String photoUrls;
    private String attachmentUrls;
    private String location;
    private LocalDateTime submittedAt;

    @TableField(fill = FieldFill.INSERT)
    @TableLogic private Integer isDeleted;

    private LocalDateTime createdAt;
}
