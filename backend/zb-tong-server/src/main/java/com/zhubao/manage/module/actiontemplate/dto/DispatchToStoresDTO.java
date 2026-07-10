package com.zhubao.manage.module.actiontemplate.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public class DispatchToStoresDTO {

    @NotNull(message = "动作ID不能为空")
    private Long actionId;

    @NotEmpty(message = "门店列表不能为空")
    private List<Long> storeIds;

    public Long getActionId() { return actionId; }
    public void setActionId(Long actionId) { this.actionId = actionId; }
    public List<Long> getStoreIds() { return storeIds; }
    public void setStoreIds(List<Long> storeIds) { this.storeIds = storeIds; }
}
