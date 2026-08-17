-- V49: 提示词模板默认数据（文档生成用）
INSERT INTO `prompt_template` (`template_name`, `business_type`, `prompt_content`, `model_name`, `status`, `is_deleted`, `created_at`, `updated_at`) VALUES
('员工分析报告', 'EMPLOYEE', '请生成一份《员工分析报告》，要求包含以下章节：一、员工基本情况；二、优势与亮点；三、待提升领域；四、改进与发展建议。语言专业、条理清晰。', 'deepseek-v4-flash', 'ENABLED', 0, NOW(), NOW()),
('货品运营分析报告', 'PRODUCT', '请生成一份《货品运营分析报告》，要求包含以下章节：一、商品结构分析；二、动销与库存情况；三、问题诊断；四、运营改进建议。', 'deepseek-v4-flash', 'ENABLED', 0, NOW(), NOW()),
('门店场景巡检报告', 'SCENE', '请生成一份《门店场景巡检报告》，要求包含以下章节：一、巡检概况；二、发现问题；三、整改措施；四、后续跟进计划。', 'deepseek-v4-flash', 'ENABLED', 0, NOW(), NOW()),
('任务复盘报告', 'TASK', '请生成一份《任务复盘报告》，要求包含以下章节：一、任务背景与目标；二、执行情况；三、问题与不足；四、改进计划。', 'deepseek-v4-flash', 'ENABLED', 0, NOW(), NOW());
