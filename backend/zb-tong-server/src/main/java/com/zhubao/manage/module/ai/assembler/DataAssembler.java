package com.zhubao.manage.module.ai.assembler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.module.human.entity.*;
import com.zhubao.manage.module.human.mapper.*;
import com.zhubao.manage.module.product.entity.Product;
import com.zhubao.manage.module.product.mapper.ProductMapper;
import com.zhubao.manage.module.scene.entity.*;
import com.zhubao.manage.module.scene.mapper.*;
import com.zhubao.manage.module.task.entity.TaskInstance;
import com.zhubao.manage.module.task.mapper.TaskInstanceMapper;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 数据组装器 —— 根据 businessType 查询关联数据拼成 prompt 上下文
 */
@Component
public class DataAssembler {

    private final EmployeeProfileMapper profileMapper;
    private final EmployeeInterviewMapper interviewMapper;
    private final EmployeeAssessmentMapper assessmentMapper;
    private final EmployeeMonthlyReviewMapper monthlyReviewMapper;
    private final ProductMapper productMapper;
    private final SceneHealthInspectionMapper healthMapper;
    private final SceneDisplayInspectionMapper displayMapper;
    private final TaskInstanceMapper taskInstanceMapper;

    public DataAssembler(EmployeeProfileMapper pm, EmployeeInterviewMapper im,
                         EmployeeAssessmentMapper am, EmployeeMonthlyReviewMapper mr,
                         ProductMapper prd, SceneHealthInspectionMapper hm,
                         SceneDisplayInspectionMapper dm, TaskInstanceMapper tm) {
        this.profileMapper = pm; this.interviewMapper = im; this.assessmentMapper = am;
        this.monthlyReviewMapper = mr; this.productMapper = prd; this.healthMapper = hm;
        this.displayMapper = dm; this.taskInstanceMapper = tm;
    }

    /**
     * 组装数据上下文
     * @param businessType EMPLOYEE / PRODUCT / SCENE / TASK
     * @param relatedId    关联ID
     * @return JSON 格式的数据快照
     */
    public String assemble(String businessType, Long relatedId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("businessType", businessType);
        data.put("relatedId", relatedId);

        switch (businessType) {
            case "EMPLOYEE":
                EmployeeProfile profile = profileMapper.selectOne(
                        new LambdaQueryWrapper<EmployeeProfile>().eq(EmployeeProfile::getUserId, relatedId));
                if (profile != null) data.put("profile", profile);
                data.put("recentInterviews", interviewMapper.selectList(
                        new LambdaQueryWrapper<EmployeeInterview>().eq(EmployeeInterview::getEmployeeId, relatedId)
                                .orderByDesc(EmployeeInterview::getInterviewDate).last("LIMIT 5")));
                data.put("recentAssessments", assessmentMapper.selectList(
                        new LambdaQueryWrapper<EmployeeAssessment>().eq(EmployeeAssessment::getEmployeeId, relatedId)
                                .orderByDesc(EmployeeAssessment::getAssessmentWeek).last("LIMIT 5")));
                data.put("monthlyReviews", monthlyReviewMapper.selectList(
                        new LambdaQueryWrapper<EmployeeMonthlyReview>().eq(EmployeeMonthlyReview::getEmployeeId, relatedId)
                                .orderByDesc(EmployeeMonthlyReview::getReviewMonth).last("LIMIT 3")));
                break;
            case "PRODUCT":
                List<Product> products = productMapper.selectList(
                        new LambdaQueryWrapper<Product>().eq(Product::getStoreId, relatedId).last("LIMIT 20"));
                data.put("products", products);
                break;
            case "SCENE":
                data.put("healthInspections", healthMapper.selectList(
                        new LambdaQueryWrapper<SceneHealthInspection>().eq(SceneHealthInspection::getStoreId, relatedId)
                                .orderByDesc(SceneHealthInspection::getInspectionDate).last("LIMIT 5")));
                data.put("displayInspections", displayMapper.selectList(
                        new LambdaQueryWrapper<SceneDisplayInspection>().eq(SceneDisplayInspection::getStoreId, relatedId)
                                .orderByDesc(SceneDisplayInspection::getInspectionDate).last("LIMIT 5")));
                break;
            case "STORE":
                data.put("products", productMapper.selectList(
                        new LambdaQueryWrapper<Product>().eq(Product::getStoreId, relatedId).last("LIMIT 20")));
                data.put("healthInspections", healthMapper.selectList(
                        new LambdaQueryWrapper<SceneHealthInspection>().eq(SceneHealthInspection::getStoreId, relatedId)
                                .orderByDesc(SceneHealthInspection::getInspectionDate).last("LIMIT 5")));
                data.put("displayInspections", displayMapper.selectList(
                        new LambdaQueryWrapper<SceneDisplayInspection>().eq(SceneDisplayInspection::getStoreId, relatedId)
                                .orderByDesc(SceneDisplayInspection::getInspectionDate).last("LIMIT 5")));
                break;
            case "TASK":
                data.put("recentTasks", taskInstanceMapper.selectList(
                        new LambdaQueryWrapper<TaskInstance>().eq(TaskInstance::getAssigneeId, relatedId)
                                .orderByDesc(TaskInstance::getCreatedAt).last("LIMIT 10")));
                break;
        }
        return toJson(data);
    }

    private String toJson(Map<String, Object> data) {
        // simplified JSON serialization
        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, Object> e : data.entrySet()) {
            if (i++ > 0) sb.append(",");
            sb.append("\"").append(e.getKey()).append("\":");
            Object v = e.getValue();
            if (v instanceof String) sb.append("\"").append(v).append("\"");
            else if (v instanceof Number) sb.append(v);
            else sb.append("\"").append(String.valueOf(v)).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }
}
