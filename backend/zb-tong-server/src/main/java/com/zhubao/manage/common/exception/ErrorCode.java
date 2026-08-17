package com.zhubao.manage.common.exception;

/**
 * 错误码枚举
 */
public enum ErrorCode {

    // ---- 通用错误 ----
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    INTERNAL_ERROR(500, "系统内部异常"),

    // ---- 认证相关 1xxx ----
    LOGIN_FAILED(1001, "用户名或密码错误"),
    TOKEN_EXPIRED(1002, "Token已过期，请重新登录"),
    TOKEN_INVALID(1003, "Token无效"),
    ACCOUNT_DISABLED(1004, "账号已被禁用"),
    ACCOUNT_LOCKED(1005, "账号已被锁定"),
    PASSWORD_EXPIRED(1006, "密码已过期，请修改密码"),
    NO_STORE_SELECTED(1007, "请先选择门店"),

    // ---- 用户相关 2xxx ----
    USER_NOT_FOUND(2001, "用户不存在"),
    USER_ALREADY_EXISTS(2002, "用户名已存在"),
    PHONE_ALREADY_EXISTS(2003, "手机号已被使用"),
    CANNOT_DELETE_SELF(2004, "不能删除自己"),
    INVALID_STORE_BINDING(2005, "门店绑定关系无效"),

    // ---- 组织相关 3xxx ----
    ORG_NOT_FOUND(3001, "组织不存在"),
    ORG_HAS_CHILDREN(3002, "该组织下有子节点，无法删除"),
    ORG_HAS_USERS(3003, "该组织下有用户，无法删除"),
    STORE_NOT_FOUND(3004, "门店不存在"),
    STORE_MANAGER_EXISTS(3005, "该门店已绑定店长"),

    // ---- 角色权限 4xxx ----
    ROLE_NOT_FOUND(4001, "角色不存在"),
    ROLE_CODE_EXISTS(4002, "角色编码已存在"),
    PERMISSION_NOT_FOUND(4003, "权限不存在"),
    ROLE_BUILTIN(4004, "内置角色不可删除"),

    // ---- 任务相关 5xxx ----
    TASK_NOT_FOUND(5001, "任务不存在"),
    TASK_STATUS_INVALID(5002, "当前任务状态不允许此操作"),
    TASK_ALREADY_SUBMITTED(5003, "任务已提交，不可重复提交"),
    TASK_OVERDUE(5004, "任务已超时"),
    TASK_TEMPLATE_NOT_FOUND(5005, "任务模板不存在"),
    ACTION_TEMPLATE_NOT_FOUND(5006, "动作不存在"),

    // ---- 文件相关 6xxx ----
    FILE_UPLOAD_FAILED(6001, "文件上传失败"),
    FILE_TOO_LARGE(6002, "文件大小超出限制"),
    FILE_TYPE_NOT_ALLOWED(6003, "不支持的文件类型"),
    FILE_NOT_FOUND(6004, "文件不存在"),

    // ---- 数据相关 7xxx ----
    DATA_NOT_FOUND(7001, "数据不存在"),
    DATA_DUPLICATE(7002, "数据重复"),
    DATA_LOCKED(7003, "数据已被锁定"),

    // ---- AI相关 8xxx ----
    AI_SERVICE_ERROR(8001, "AI服务异常"),
    AI_TIMEOUT(8002, "AI服务超时"),
    PROMPT_TEMPLATE_NOT_FOUND(8003, "提示词模板不存在");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
