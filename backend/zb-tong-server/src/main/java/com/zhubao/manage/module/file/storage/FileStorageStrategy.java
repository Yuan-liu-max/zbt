package com.zhubao.manage.module.file.storage;

import java.io.InputStream;

/**
 * 文件存储策略接口 —— MinIO / OSS / COS
 */
public interface FileStorageStrategy {

    /**
     * 上传文件
     *
     * @param objectName 存储路径（含文件名）
     * @param inputStream 文件流
     * @param fileSize   文件大小
     * @param contentType MIME类型
     * @return 文件访问URL
     */
    String upload(String objectName, InputStream inputStream, long fileSize, String contentType);

    /**
     * 删除文件
     */
    void delete(String objectName);

    /**
     * 生成预签名上传URL（移动端直传用）
     *
     * @param objectName 存储路径
     * @param expirySeconds 过期秒数
     * @return 预签名PUT URL
     */
    String getPresignedUploadUrl(String objectName, int expirySeconds);

    /**
     * 存储类型标识
     */
    String getStorageType();
}
