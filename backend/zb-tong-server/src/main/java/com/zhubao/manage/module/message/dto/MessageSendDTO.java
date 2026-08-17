package com.zhubao.manage.module.message.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class MessageSendDTO {

    @NotNull(message = "接收人不能为空")
    private Long receiverId;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String content;

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
