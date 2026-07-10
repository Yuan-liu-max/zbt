package com.zhubao.manage.module.file.controller;

import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.module.file.entity.FileResource;
import com.zhubao.manage.module.file.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Api(tags = "文件管理")
@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @ApiOperation("单文件上传")
    @PostMapping("/upload")
    public ApiResult<FileResource> upload(@ApiParam("文件") @RequestParam("file") MultipartFile file) {
        return ApiResult.ok(fileService.upload(file));
    }

    @ApiOperation("批量上传")
    @PostMapping("/upload-batch")
    public ApiResult<List<FileResource>> uploadBatch(@ApiParam("文件列表") @RequestParam("files") MultipartFile[] files) {
        return ApiResult.ok(fileService.uploadBatch(files));
    }

    @ApiOperation("生成预签名上传URL（移动端直传）")
    @GetMapping("/presigned-url")
    public ApiResult<Map<String, String>> presignedUrl(@ApiParam("原始文件名") @RequestParam("fileName") String fileName) {
        return ApiResult.ok(fileService.getPresignedUrl(fileName));
    }

    @ApiOperation("删除文件（逻辑删除）")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        fileService.delete(id);
        return ApiResult.ok();
    }
}
