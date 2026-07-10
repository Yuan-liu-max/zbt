package com.zhubao.manage.common.interceptor;

/**
 * 用户上下文 —— 每个请求的线程级用户信息
 */
public class UserContext {

    private Long userId;
    private String username;
    private Long storeId;
    private Long regionId;

    public UserContext() {}

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    @Override
    public String toString() {
        return "UserContext{userId=" + userId + ", username='" + username + "'}";
    }
}
