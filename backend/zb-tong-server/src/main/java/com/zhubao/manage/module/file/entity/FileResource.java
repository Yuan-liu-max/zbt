package com.zhubao.manage.module.file.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_resource")
public class FileResource {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 原始文件名 */
    private String fileName;

    /** 存储Key（MinIO object name） */
    private String fileKey;

    /** 访问URL */
    private String fileUrl;

    /** IMAGE / DOCUMENT / VIDEO / OTHER */
    private String fileType;

    /** 文件大小（字节） */
    private Long fileSize;

    /** MIME类型 */
    private String mimeType;

    /** MINIO / OSS / COS */
    private String storageType;

    /** 上传人ID */
    private Long uploaderId;

    /** 关联业务类型 */
    private String businessType;

    /** 关联业务ID */
    private Long businessId;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
