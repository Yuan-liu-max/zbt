package com.zhubao.manage.module.region.controller;

import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.module.region.service.RegionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Api(tags = "行政区域")
@RestController
@RequestMapping("/regions")
public class RegionController {

    private final RegionService regionService;

    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    @ApiOperation("省市区树（三级联动数据源）")
    @GetMapping("/tree")
    public ApiResult<List<Map<String, Object>>> tree() {
        return ApiResult.ok(regionService.tree());
    }

    @ApiOperation("Vant Area 格式 area-list")
    @GetMapping("/area-list")
    public ApiResult<Map<String, Map<String, String>>> areaList() {
        return ApiResult.ok(regionService.areaList());
    }

    @ApiOperation("按区县查询街道/乡镇列表（选中省市区后街道下拉）")
    @GetMapping("/streets")
    public ApiResult<List<Map<String, String>>> streets(@RequestParam String districtCode) {
        return ApiResult.ok(regionService.streets(districtCode));
    }

    @ApiOperation("手动同步行政区字典（启动自动同步失败时使用）")
    @PostMapping("/sync")
    public ApiResult<Map<String, Object>> sync() {
        int count = regionService.syncFromAmap();
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("count", count);
        return ApiResult.ok(data);
    }
}
