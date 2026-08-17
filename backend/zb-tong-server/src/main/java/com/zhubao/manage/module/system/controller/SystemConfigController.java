package com.zhubao.manage.module.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhubao.manage.common.annotation.OperateLog;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.module.system.entity.SystemConfig;
import com.zhubao.manage.module.system.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api(tags = "系统配置管理")
@RestController
@RequestMapping("/system/configs")
@PreAuthorize("hasAnyRole('ADMIN','HQ')")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    public SystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @ApiOperation("获取系统配置列表")
    @GetMapping
    public ApiResult<List<SystemConfig>> list(@RequestParam(required = false) String configGroup) {
        List<SystemConfig> list = systemConfigService.listByGroup(configGroup);
        return ApiResult.ok(list);
    }

    @OperateLog(module = "系统配置", action = "SAVE", targetType = "CONFIG")
    @ApiOperation("批量保存系统配置")
    @PutMapping
    public ApiResult<Void> batchSave(@Valid @RequestBody Map<String, Object> body) {
        Object raw = body.get("configs");
        if (raw instanceof List) {
            ObjectMapper mapper = new ObjectMapper();
            List<SystemConfig> configs = ((List<?>) raw).stream()
                    .map(item -> mapper.convertValue(item, SystemConfig.class))
                    .collect(Collectors.toList());
            systemConfigService.saveConfigs(configs);
        }
        return ApiResult.ok();
    }
}
