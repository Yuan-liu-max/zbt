package com.zhubao.manage.module.file.service;

import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.file.entity.FileResource;
import com.zhubao.manage.module.file.mapper.FileResourceMapper;
import com.zhubao.manage.module.file.storage.FileStorageStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 文件服务 —— 含安全校验（魔数/路径穿越/大小/类型白名单）
 */
@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    // ---- 类型白名单（扩展名 → MIME + 魔数） ----
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "pdf"));

    // 魔数（Magic Number）映射
    private static final Map<String, byte[]> MAGIC_NUMBERS = new LinkedHashMap<>();
    static {
        MAGIC_NUMBERS.put("jpg",  new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        MAGIC_NUMBERS.put("jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        MAGIC_NUMBERS.put("png",  new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47});
        MAGIC_NUMBERS.put("gif",  new byte[]{0x47, 0x49, 0x46, 0x38});
        MAGIC_NUMBERS.put("webp", new byte[]{0x52, 0x49, 0x46, 0x46});
        MAGIC_NUMBERS.put("pdf",  new byte[]{0x25, 0x50, 0x44, 0x46});
    }

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB

    private final FileResourceMapper fileResourceMapper;
    private final FileStorageStrategy storageStrategy;
    private final UserContextHolder userContextHolder;

    @Value("${file.storage.pre-url-expiry:600}")
    private int presignedUrlExpiry;

    public FileService(FileResourceMapper fileResourceMapper,
                       FileStorageStrategy storageStrategy,
                       UserContextHolder userContextHolder) {
        this.fileResourceMapper = fileResourceMapper;
        this.storageStrategy = storageStrategy;
        this.userContextHolder = userContextHolder;
    }

    /** 单文件上传 */
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public FileResource upload(MultipartFile file) {
        return doUpload(file);
    }

    /** 批量上传 */
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public List<FileResource> uploadBatch(MultipartFile[] files) {
        List<FileResource> results = new ArrayList<>();
        for (MultipartFile f : files) results.add(doUpload(f));
        return results;
    }

    /** 预签名上传URL —— 防路径穿越 */
    @PreAuthorize("isAuthenticated()")
    public Map<String, String> getPresignedUrl(String originalFileName) {
        // 路径穿越过滤：拒绝含 .. / \ 的文件名
        if (originalFileName == null || originalFileName.contains("..")
                || originalFileName.contains("/") || originalFileName.contains("\\")) {
            throw new BusinessException(400, "文件名包含非法字符");
        }

        String ext = getExtension(originalFileName);
        validateExtension(ext);

        String objectName = buildObjectName(ext);
        String presignedUrl = storageStrategy.getPresignedUploadUrl(objectName, presignedUrlExpiry);

        Map<String, String> result = new LinkedHashMap<>();
        result.put("uploadUrl", presignedUrl);
        result.put("objectName", objectName);
        result.put("expiresIn", String.valueOf(presignedUrlExpiry));
        return result;
    }

    /** 逻辑删除 */
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void delete(Long id) {
        FileResource file = fileResourceMapper.selectById(id);
        if (file == null) throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        fileResourceMapper.deleteById(id);
        log.info("文件已逻辑删除: id={}, fileKey={}", id, file.getFileKey());
    }

    // ========== 内部 ==========

    private FileResource doUpload(MultipartFile file) {
        if (file.isEmpty()) throw new BusinessException(400, "文件不能为空");

        String originalName = file.getOriginalFilename();
        String ext = getExtension(originalName);

        // 1. 扩展名白名单
        validateExtension(ext);

        // 2. 文件大小限制
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE.getCode(), "文件大小不能超过20MB");
        }

        // 3. 魔数（Magic Number）校验
        validateMagicNumber(file, ext);

        // 4. 上传
        String objectName = buildObjectName(ext);
        try {
            String fileUrl = storageStrategy.upload(objectName, file.getInputStream(),
                    file.getSize(), file.getContentType());

            FileResource resource = new FileResource();
            resource.setFileName(originalName);
            resource.setFileKey(objectName);
            resource.setFileUrl(fileUrl);
            resource.setFileType(getFileType(ext));
            resource.setFileSize(file.getSize());
            resource.setMimeType(file.getContentType());
            resource.setStorageType(storageStrategy.getStorageType());
            resource.setUploaderId(userContextHolder.getUserId());
            fileResourceMapper.insert(resource);
            return resource;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传异常", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private void validateExtension(String ext) {
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED.getCode(),
                    "不支持的文件类型: ." + ext + "，仅允许: " + ALLOWED_EXTENSIONS);
        }
    }

    /** 魔数校验 —— 读取文件头字节比对 */
    private void validateMagicNumber(MultipartFile file, String ext) {
        byte[] expected = MAGIC_NUMBERS.get(ext);
        if (expected == null) return; // 无魔数定义的跳过

        try (java.io.InputStream in = file.getInputStream()) {
            byte[] header = new byte[expected.length];
            int read = in.read(header);
            if (read != expected.length) {
                throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED.getCode(),
                        "文件类型不匹配，上传被拒绝");
            }
            for (int i = 0; i < expected.length; i++) {
                if (header[i] != expected[i]) {
                    log.warn("魔数校验失败: 期望={}, 实际={}", bytesToHex(expected), bytesToHex(header));
                    throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED.getCode(),
                            "文件类型不匹配，上传被拒绝");
                }
            }
        } catch (java.io.IOException e) {
            log.error("文件读取失败: 文件名={}, 扩展名={}", file.getOriginalFilename(), ext, e);
            throw new BusinessException(400, "文件读取失败");
        }
    }

    private String buildObjectName(String ext) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String prefix = "gif".equals(ext) || "webp".equals(ext) || isImageExt(ext) ? "images" : "documents";
        return prefix + "/" + datePath + "/" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
    }

    private boolean isImageExt(String ext) { return Arrays.asList("jpg","jpeg","png","gif","webp").contains(ext); }
    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "";
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
    private String getFileType(String ext) { return isImageExt(ext) ? "IMAGE" : "DOCUMENT"; }
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X ", b));
        return sb.toString().trim();
    }
}
