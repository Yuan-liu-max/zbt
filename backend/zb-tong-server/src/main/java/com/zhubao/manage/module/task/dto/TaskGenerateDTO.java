package com.zhubao.manage.module.task.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public class TaskGenerateDTO {

    @NotNull(message = "模板ID不能为空")
    private Long templateId;

    @NotEmpty(message = "门店列表不能为空")
    private List<Long> storeIds;

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public List<Long> getStoreIds() { return storeIds; }
    public void setStoreIds(List<Long> storeIds) { this.storeIds = storeIds; }
}
