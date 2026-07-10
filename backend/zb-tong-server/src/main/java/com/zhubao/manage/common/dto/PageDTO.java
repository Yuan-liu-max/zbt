package com.zhubao.manage.common.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 分页请求参数
 */
public class PageDTO {

    /** 页码 */
    @Min(1)
    private long pageNum = 1;

    /** 每页条数 */
    @Min(1)
    @Max(100)
    private long pageSize = 20;

    public PageDTO() {}

    public PageDTO(long pageNum, long pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public long getPageNum() {
        return pageNum;
    }

    public void setPageNum(long pageNum) {
        this.pageNum = pageNum;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }
}
