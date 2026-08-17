package com.zhubao.manage.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 分页请求参数 — 同时接受 { page, size } 和 { page, pageSize }
 */
public class PageDTO {

    @JsonProperty("page")
    @Min(1)
    private long page = 1;

    @JsonProperty("pageSize")
    @Min(1)
    @Max(2000)
    private long size = 20;

    public PageDTO() {}

    public PageDTO(long page, long size) {
        this.page = page;
        this.size = size;
    }

    public long getPageNum() { return page; }
    public void setPageNum(long page) { this.page = page; }

    public long getPageSize() { return size; }
    public void setPageSize(long size) { this.size = size; }

    /** API 规范别名 */
    public long getPage() { return page; }
    public void setPage(long page) { this.page = page; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
}
