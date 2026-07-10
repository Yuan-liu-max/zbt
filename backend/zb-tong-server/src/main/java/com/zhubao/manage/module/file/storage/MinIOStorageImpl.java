package com.zhubao.manage.module.file.storage;

import io.minio.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;

/**
 * MinIO 存储实现
 */
@Component
@ConditionalOnProperty(name = "file.storage.type", havingValue = "minio", matchIfMissing = true)
public class MinIOStorageImpl implements FileStorageStrategy {

    private static final Logger log = LoggerFactory.getLogger(MinIOStorageImpl.class);

    @Value("${file.storage.minio.endpoint}")
    private String endpoint;

    @Value("${file.storage.minio.access-key}")
    private String accessKey;

    @Value("${file.storage.minio.secret-key}")
    private String secretKey;

    @Value("${file.storage.minio.bucket}")
    private String bucket;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        ensureBucket();
    }

    private void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("MinIO 桶已创建: {}", bucket);
            }
        } catch (Exception e) {
            log.error("MinIO 桶初始化失败: {}", e.getMessage());
        }
    }

    @Override
    public String upload(String objectName, InputStream inputStream, long fileSize, String contentType) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(inputStream, fileSize, -1)
                    .contentType(contentType)
                    .build());
            String url = endpoint + "/" + bucket + "/" + objectName;
            log.debug("MinIO 上传成功: {}", url);
            return url;
        } catch (Exception e) {
            log.error("MinIO 上传失败: {}", e.getMessage());
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void delete(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.warn("MinIO 删除失败: {}", e.getMessage());
        }
    }

    @Override
    public String getPresignedUploadUrl(String objectName, int expirySeconds) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucket)
                            .object(objectName)
                            .expiry(expirySeconds)
                            .build());
        } catch (Exception e) {
            log.error("生成预签名URL失败: {}", e.getMessage());
            throw new RuntimeException("生成预签名URL失败: " + e.getMessage());
        }
    }

    @Override
    public String getStorageType() {
        return "MINIO";
    }
}
