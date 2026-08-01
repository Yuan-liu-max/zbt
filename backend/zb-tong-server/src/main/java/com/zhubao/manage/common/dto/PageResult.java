package com.zhubao.manage.common.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/**
 * 分页返回结果 — 字段名对齐 API 规范 { list, total, page, size }
 *
 * @param <T> 列表元素类型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResult<T> {

    @JsonProperty("list")
    private List<T> records;

    @JsonProperty("total")
    private long total;

    @JsonProperty("page")
    private long page;

    @JsonProperty("size")
    private long size;

    public PageResult() {}

    public PageResult(long page, long size, long total, List<T> records) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.records = records;
    }

    public static <T> PageResult<T> of(IPage<T> mpPage) {
        return new PageResult<>(mpPage.getCurrent(), mpPage.getSize(),
                mpPage.getTotal(), mpPage.getRecords());
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>(1, 20, 0, Collections.emptyList());
    }

    public long getPage() { return page; }
    public void setPage(long page) { this.page = page; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }
}
