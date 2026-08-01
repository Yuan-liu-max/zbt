package com.zhubao.manage.module.customer.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("member_level")
public class MemberLevel {
    @TableId(type = IdType.AUTO) private Long id;
    private String name;
    @JsonProperty("标识") private String identifier;
    private Integer memberCount;
    private BigDecimal totalConsumption;
    private Integer pointsMultiplier;
    private BigDecimal discount;
    private String benefits;
    private String status;
}
