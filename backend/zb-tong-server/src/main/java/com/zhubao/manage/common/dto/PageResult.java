package com.zhubao.manage.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.List;

/**
 * 分页返回结果
 *
 * @param <T> 列表元素类型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResult<T> {

    /** 当前页码 */
    private long pageNum;

    /** 每页条数 */
    private long pageSize;

    /** 总条数 */
    private long total;

    /** 总页数 */
    private long pages;

    /** 数据列表 */
    private List<T> records;

    public PageResult() {}

    public PageResult(long pageNum, long pageSize, long total, List<T> records) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.pages = (total + pageSize - 1) / pageSize;
        this.records = records;
    }

    /**
     * 从 MyBatis-Plus IPage 构建
     */
    public static <T> PageResult<T> of(com.baomidou.mybatisplus.core.metadata.IPage<T> page) {
        return new PageResult<>(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getRecords()
        );
    }

    /**
     * 空页
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>(1, 20, 0, Collections.emptyList());
    }

    // ---- getters/setters ----

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

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPages() {
        return pages;
    }

    public void setPages(long pages) {
        this.pages = pages;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}
