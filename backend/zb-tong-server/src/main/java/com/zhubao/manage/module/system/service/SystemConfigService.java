package com.zhubao.manage.module.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.module.system.entity.SystemConfig;
import com.zhubao.manage.module.system.mapper.SystemConfigMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SystemConfigService {

    private final SystemConfigMapper mapper;

    public SystemConfigService(SystemConfigMapper mapper) {
        this.mapper = mapper;
    }

    public List<SystemConfig> listByGroup(String configGroup) {
        LambdaQueryWrapper<SystemConfig> w = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(configGroup)) {
            w.eq(SystemConfig::getConfigGroup, configGroup);
        }
        w.orderByAsc(SystemConfig::getSortOrder);
        return mapper.selectList(w);
    }

    @Transactional
    public void saveConfigs(List<SystemConfig> configs) {
        for (SystemConfig config : configs) {
            LambdaQueryWrapper<SystemConfig> w = new LambdaQueryWrapper<>();
            w.eq(SystemConfig::getConfigKey, config.getConfigKey());
            SystemConfig exist = mapper.selectOne(w);
            if (exist != null) {
                config.setId(exist.getId());
                mapper.updateById(config);
            } else {
                mapper.insert(config);
            }
        }
    }
}
