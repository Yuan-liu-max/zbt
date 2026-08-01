package com.zhubao.manage.module.human.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("employee_meeting")
public class Meeting {
    @TableId(type = IdType.AUTO) private Long id;
    private String topic;
    @JsonProperty("meetingType") private String type;
    private String meetingDate;
    private String host;
    private String participants;
    private String status;
    @TableLogic private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
