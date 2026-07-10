package com.zhubao.manage.module.task.dto;

import com.zhubao.manage.common.dto.PageDTO;

public class TaskQueryDTO extends PageDTO {

    private String status;
    private Long storeId;
    private Long assigneeId;
    private Long auditorId;
    private String dimension;
    private String category;
    private String sourceType;
    private String priority;
    private String keyword;
    private Integer isOverdue;
    private String startDate;
    private String endDate;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }
    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
    public Long getAuditorId() { return auditorId; }
    public void setAuditorId(Long auditorId) { this.auditorId = auditorId; }
    public String getDimension() { return dimension; }
    public void setDimension(String dimension) { this.dimension = dimension; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Integer getIsOverdue() { return isOverdue; }
    public void setIsOverdue(Integer isOverdue) { this.isOverdue = isOverdue; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
