package com.zhubao.manage.module.file.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * 阿里云 OSS 存储实现（预留）
 *
 * 后续接入步骤：
 * 1. 添加依赖 com.aliyun.oss:aliyun-sdk-oss
 * 2. 修改 application.yml 中 file.storage.type=oss
 * 3. 取消 @Component 注释并实现接口
 */
//@Component
//@ConditionalOnProperty(name = "file.storage.type", havingValue = "oss")
public class OSSStorageImpl implements FileStorageStrategy {

    private static final Logger log = LoggerFactory.getLogger(OSSStorageImpl.class);

    @Override
    public String upload(String objectName, InputStream inputStream, long fileSize, String contentType) {
        throw new UnsupportedOperationException("OSS 存储尚未接入");
    }

    @Override
    public void delete(String objectName) {
        throw new UnsupportedOperationException("OSS 存储尚未接入");
    }

    @Override
    public String getPresignedUploadUrl(String objectName, int expirySeconds) {
        throw new UnsupportedOperationException("OSS 存储尚未接入");
    }

    @Override
    public String getStorageType() {
        return "OSS";
    }
}
