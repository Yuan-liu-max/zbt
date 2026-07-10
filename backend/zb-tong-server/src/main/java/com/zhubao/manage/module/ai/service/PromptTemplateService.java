package com.zhubao.manage.module.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.ai.entity.PromptTemplate;
import com.zhubao.manage.module.ai.mapper.PromptTemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PromptTemplateService {

    private final PromptTemplateMapper mapper;
    private final UserContextHolder userContextHolder;

    public PromptTemplateService(PromptTemplateMapper mapper, UserContextHolder uc) {
        this.mapper = mapper; this.userContextHolder = uc;
    }

    public List<PromptTemplate> listAll() { return mapper.selectList(new LambdaQueryWrapper<>()); }

    public PromptTemplate detail(Long id) { return mapper.selectById(id); }

    @Transactional
    public PromptTemplate create(PromptTemplate pt) {
        pt.setCreatedBy(userContextHolder.getUserId());
        mapper.insert(pt); return pt;
    }

    @Transactional
    public PromptTemplate update(Long id, PromptTemplate pt) { pt.setId(id); mapper.updateById(pt); return detail(id); }

    @Transactional
    public void delete(Long id) { mapper.deleteById(id); }

    /** 按业务类型查询启用模板 */
    public List<PromptTemplate> listByBusinessType(String businessType) {
        return mapper.selectList(new LambdaQueryWrapper<PromptTemplate>()
                .eq(PromptTemplate::getBusinessType, businessType)
                .eq(PromptTemplate::getStatus, "ENABLED"));
    }
}
