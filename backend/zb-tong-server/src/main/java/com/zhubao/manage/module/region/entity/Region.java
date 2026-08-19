package com.zhubao.manage.module.region.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 行政区域字典（省市区三级）
 */
@Data
@TableName("region_dict")
public class Region {
    @TableId(type = IdType.INPUT)
    private String code;          // 行政编码 adcode
    private String name;          // 区域名称
    private String level;         // province|city|district
    private String parentCode;    // 上级编码
    private String pinyin;        // 拼音（预留）
    private Integer sort;
    private LocalDateTime createdAt;
}
