package com.zhubao.manage.module.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("order_log")
public class OrderLog {
    @TableId(type = IdType.AUTO) private Long id;
    private Long orderId;
    private String time;
    private String content;
}
