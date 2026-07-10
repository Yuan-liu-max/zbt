package com.zhubao.manage.module.scene.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.module.scene.entity.*;
import com.zhubao.manage.module.scene.mapper.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SceneService {

    private final SceneHealthInspectionMapper healthMapper;
    private final SceneDisplayInspectionMapper displayMapper;
    private final SceneMaterialUpdateMapper materialMapper;
    private final SceneEquipmentCheckMapper equipmentMapper;
    private final SceneCustomerExperienceReviewMapper experienceMapper;

    public SceneService(SceneHealthInspectionMapper hm, SceneDisplayInspectionMapper dm,
                        SceneMaterialUpdateMapper mm, SceneEquipmentCheckMapper em,
                        SceneCustomerExperienceReviewMapper xm) {
        this.healthMapper = hm; this.displayMapper = dm; this.materialMapper = mm;
        this.equipmentMapper = em; this.experienceMapper = xm;
    }

    public <T> List<T> all(BaseMapper<T> m, LambdaQueryWrapper<T> w) { return m.selectList(w); }
    public <T> IPage<T> page(BaseMapper<T> m, PageDTO page, LambdaQueryWrapper<T> w) {
        return m.selectPage(new Page<>(page.getPageNum(), page.getPageSize()), w);
    }
    public <T> T get(BaseMapper<T> m, Long id, String name) {
        T t = m.selectById(id); if (t == null) throw new BusinessException(ErrorCode.DATA_NOT_FOUND.getCode(), name + "不存在"); return t;
    }
    public <T> void save(BaseMapper<T> m, T t) { m.insert(t); }
    public <T> void update(BaseMapper<T> m, T t) { m.updateById(t); }
    public <T> void del(BaseMapper<T> m, Long id) { m.deleteById(id); }
}
