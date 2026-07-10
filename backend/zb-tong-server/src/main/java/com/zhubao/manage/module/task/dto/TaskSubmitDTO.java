package com.zhubao.manage.module.task.dto;

import javax.validation.constraints.NotNull;

public class TaskSubmitDTO {

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    private String textContent;
    private String formData;
    private String photoUrls;
    private String attachmentUrls;
    private String location;

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }
    public String getFormData() { return formData; }
    public void setFormData(String formData) { this.formData = formData; }
    public String getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(String photoUrls) { this.photoUrls = photoUrls; }
    public String getAttachmentUrls() { return attachmentUrls; }
    public void setAttachmentUrls(String attachmentUrls) { this.attachmentUrls = attachmentUrls; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
