package com.zhubao.manage.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 统一响应体 {@code {code, msg, data}}
 *
 * @param <T> 数据类型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> {

    /** 状态码 */
    private int code;

    /** 消息 */
    private String msg;

    /** 数据 */
    private T data;

    private ApiResult() {}

    private ApiResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ---- 成功 ----

    public static <T> ApiResult<T> ok() {
        return new ApiResult<>(200, "success", null);
    }

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(200, "success", data);
    }

    public static <T> ApiResult<T> ok(String msg, T data) {
        return new ApiResult<>(200, msg, data);
    }

    // ---- 失败 ----

    public static <T> ApiResult<T> fail() {
        return new ApiResult<>(500, "系统异常", null);
    }

    public static <T> ApiResult<T> fail(String msg) {
        return new ApiResult<>(500, msg, null);
    }

    public static <T> ApiResult<T> fail(int code, String msg) {
        return new ApiResult<>(code, msg, null);
    }

    // ---- 参数校验失败 ----

    public static <T> ApiResult<T> invalidParam(String msg) {
        return new ApiResult<>(400, msg, null);
    }

    // ---- 未授权 ----

    public static <T> ApiResult<T> unauthorized(String msg) {
        return new ApiResult<>(401, msg, null);
    }

    // ---- 无权限 ----

    public static <T> ApiResult<T> forbidden(String msg) {
        return new ApiResult<>(403, msg, null);
    }

    // ---- getters/setters ----

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
