package com.zhubao.manage.module.scene.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.module.scene.entity.*;
import com.zhubao.manage.module.scene.mapper.*;
import com.zhubao.manage.module.scene.service.SceneService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "场景运营")
@RestController
@RequestMapping("/scenes")
public class SceneController {

    private final SceneService svc;
    private final SceneHealthInspectionMapper healthMapper;
    private final SceneDisplayInspectionMapper displayMapper;
    private final SceneMaterialUpdateMapper materialMapper;
    private final SceneEquipmentCheckMapper equipmentMapper;
    private final SceneCustomerExperienceReviewMapper experienceMapper;

    public SceneController(SceneService svc, SceneHealthInspectionMapper hm,
                           SceneDisplayInspectionMapper dm, SceneMaterialUpdateMapper mm,
                           SceneEquipmentCheckMapper em, SceneCustomerExperienceReviewMapper xm) {
        this.svc = svc; this.healthMapper = hm; this.displayMapper = dm;
        this.materialMapper = mm; this.equipmentMapper = em; this.experienceMapper = xm;
    }

    // ===== 卫生巡检 =====
    @ApiOperation("卫生巡检列表") @GetMapping("/health-inspections")
    public ApiResult<List<SceneHealthInspection>> listHealth() { return ApiResult.ok(svc.all(healthMapper, new LambdaQueryWrapper<>())); }
    @ApiOperation("新增卫生巡检") @PostMapping("/health-inspections")
    public ApiResult<Void> createHealth(@RequestBody SceneHealthInspection e) { svc.save(healthMapper, e); return ApiResult.ok(); }
    @ApiOperation("更新卫生巡检") @PutMapping("/health-inspections/{id}")
    public ApiResult<Void> updateHealth(@PathVariable Long id, @RequestBody SceneHealthInspection e) { e.setId(id); svc.update(healthMapper, e); return ApiResult.ok(); }

    // ===== 陈列检查 =====
    @ApiOperation("陈列检查列表") @GetMapping("/display-inspections")
    public ApiResult<List<SceneDisplayInspection>> listDisplay() { return ApiResult.ok(svc.all(displayMapper, new LambdaQueryWrapper<>())); }
    @ApiOperation("新增陈列检查") @PostMapping("/display-inspections")
    public ApiResult<Void> createDisplay(@RequestBody SceneDisplayInspection e) { svc.save(displayMapper, e); return ApiResult.ok(); }
    @ApiOperation("更新陈列检查") @PutMapping("/display-inspections/{id}")
    public ApiResult<Void> updateDisplay(@PathVariable Long id, @RequestBody SceneDisplayInspection e) { e.setId(id); svc.update(displayMapper, e); return ApiResult.ok(); }

    // ===== 物料更新 =====
    @ApiOperation("物料更新列表") @GetMapping("/material-updates")
    public ApiResult<List<SceneMaterialUpdate>> listMaterial() { return ApiResult.ok(svc.all(materialMapper, new LambdaQueryWrapper<>())); }
    @ApiOperation("新增物料更新") @PostMapping("/material-updates")
    public ApiResult<Void> createMaterial(@RequestBody SceneMaterialUpdate e) { svc.save(materialMapper, e); return ApiResult.ok(); }

    // ===== 设备检查 =====
    @ApiOperation("设备检查列表") @GetMapping("/equipment-checks")
    public ApiResult<List<SceneEquipmentCheck>> listEquipment() { return ApiResult.ok(svc.all(equipmentMapper, new LambdaQueryWrapper<>())); }
    @ApiOperation("新增设备检查") @PostMapping("/equipment-checks")
    public ApiResult<Void> createEquipment(@RequestBody SceneEquipmentCheck e) { svc.save(equipmentMapper, e); return ApiResult.ok(); }

    // ===== 客户体验复盘 =====
    @ApiOperation("客户体验复盘列表") @GetMapping("/experience-reviews")
    public ApiResult<List<SceneCustomerExperienceReview>> listExperience() { return ApiResult.ok(svc.all(experienceMapper, new LambdaQueryWrapper<>())); }
    @ApiOperation("新增客户体验复盘") @PostMapping("/experience-reviews")
    public ApiResult<Void> createExperience(@RequestBody SceneCustomerExperienceReview e) { svc.save(experienceMapper, e); return ApiResult.ok(); }
}
