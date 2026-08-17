package com.zhubao.manage.module.file.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 本地磁盘存储实现 —— 无需外部存储服务，仅用于开发/测试环境
 * 使用方式: file.storage.type=local
 */
@Component
@ConditionalOnProperty(name = "file.storage.type", havingValue = "local")
public class LocalStorageImpl implements FileStorageStrategy {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageImpl.class);

    @Value("${file.storage.local.path:${user.dir}/data/files}")
    private String basePath;

    private Path rootPath;

    @PostConstruct
    public void init() {
        try {
            rootPath = Paths.get(basePath).toAbsolutePath().normalize();
            Files.createDirectories(rootPath);
            log.info("本地存储目录: {}", rootPath);
        } catch (Exception e) {
            log.error("本地存储目录初始化失败: {}", e.getMessage());
        }
    }

    @Override
    public String upload(String objectName, InputStream inputStream, long fileSize, String contentType) {
        try {
            Path target = rootPath.resolve(objectName).normalize();
            if (!target.startsWith(rootPath)) {
                throw new RuntimeException("非法的文件路径: " + objectName);
            }
            Files.createDirectories(target.getParent());
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            String url = "/files/static/" + objectName;
            log.debug("本地文件上传成功: {}", url);
            return url;
        } catch (Exception e) {
            log.error("本地文件上传失败: {}", e.getMessage());
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void delete(String objectName) {
        try {
            Path target = rootPath.resolve(objectName).normalize();
            if (target.startsWith(rootPath)) {
                Files.deleteIfExists(target);
            }
        } catch (Exception e) {
            log.warn("本地文件删除失败: {}", e.getMessage());
        }
    }

    @Override
    public String getPresignedUploadUrl(String objectName, int expirySeconds) {
        throw new UnsupportedOperationException("本地存储不支持预签名上传URL");
    }

    @Override
    public String getStorageType() {
        return "LOCAL";
    }
}
