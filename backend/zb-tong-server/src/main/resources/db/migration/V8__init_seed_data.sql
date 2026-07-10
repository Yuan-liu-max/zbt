-- ============================================================
-- V8__init_seed_data.sql
-- 默认角色 + 默认权限树 + 默认动作库
-- ============================================================

-- ==================== 1. 默认角色 ====================

INSERT IGNORE INTO `sys_role` (`id`, `role_code`, `role_name`, `data_scope`, `status`, `remark`) VALUES
(1, 'ROLE_ADMIN',    '系统管理员', 'ALL',    'ENABLED', '管理系统所有功能，拥有全部权限'),
(2, 'ROLE_HQ',       '总部运营',   'ALL',    'ENABLED', '查看全国数据，配置任务模板与动作库'),
(3, 'ROLE_REGIONAL', '区域经理',   'REGION', 'ENABLED', '管理所辖区域门店，查看排名与抽查任务'),
(4, 'ROLE_MANAGER',  '店长',       'STORE',  'ENABLED', '管理门店员工、审核任务、查看门店报表'),
(5, 'ROLE_ASSOCIATE','导购',       'SELF',   'ENABLED', '录入销售、执行个人任务、查看个人业绩');

-- ==================== 2. 默认权限树 ====================

-- 一级菜单
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `perm_name`, `perm_type`, `perm_code`, `path`, `component`, `icon`, `sort_order`, `status`) VALUES
-- 仪表盘
(1,  0,  '仪表盘',     'MENU', 'dashboard',         '/dashboard',     'dashboard/DashboardView',      'DashboardOutlined',   1, 'ENABLED'),
-- 系统管理
(10, 0,  '系统管理',   'MENU', 'system',             '/system',        NULL,                           'SettingOutlined',    10, 'ENABLED'),
(11, 10, '组织架构',   'MENU', 'system:organization', '/system/organization', 'system/SystemOrganization', 'ApartmentOutlined',  11, 'ENABLED'),
(12, 10, '门店管理',   'MENU', 'system:store',       '/system/store',   'system/SystemStore',           'ShopOutlined',       12, 'ENABLED'),
(13, 10, '用户管理',   'MENU', 'system:user',        '/system/user',    'system/SystemUser',            'UserOutlined',       13, 'ENABLED'),
(14, 10, '角色权限',   'MENU', 'system:role',        '/system/role',    'system/SystemRole',            'SafetyOutlined',     14, 'ENABLED'),
-- 任务中心
(20, 0,  '任务中心',   'MENU', 'task',               '/task',           NULL,                           'ScheduleOutlined',   20, 'ENABLED'),
(21, 20, '任务列表',   'MENU', 'task:list',          '/task/list',      'task/TaskList',                'UnorderedListOutlined', 21, 'ENABLED'),
(22, 20, '任务创建',   'MENU', 'task:create',        '/task/create',    'task/TaskCreate',              'PlusCircleOutlined', 22, 'ENABLED'),
(23, 20, '任务模板',   'MENU', 'task:template',      '/task/template',  'task/TaskTemplate',            'FileTextOutlined',   23, 'ENABLED'),
(24, 20, '任务审核',   'MENU', 'task:review',        '/task/review',    'task/TaskReview',              'AuditOutlined',      24, 'ENABLED'),
-- 人效管理
(30, 0,  '人效管理',   'MENU', 'human',              '/human',          NULL,                           'TeamOutlined',       30, 'ENABLED'),
(31, 30, '晨夕会',     'MENU', 'human:meeting',      '/human/meeting',  'human/HumanMeeting',           'CalendarOutlined',   31, 'ENABLED'),
(32, 30, '员工面谈',   'MENU', 'human:interview',    '/human/interview', 'human/HumanInterview',        'MessageOutlined',    32, 'ENABLED'),
(33, 30, '能力考核',   'MENU', 'human:assess',       '/human/assess',   'human/HumanAssess',            'CheckCircleOutlined',33, 'ENABLED'),
(34, 30, '绩效复盘',   'MENU', 'human:performance',  '/human/performance','human/HumanPerformance',       'BarChartOutlined',   34, 'ENABLED'),
-- 货品管理
(40, 0,  '货品管理',   'MENU', 'goods',              '/goods',          NULL,                           'GoldOutlined',       40, 'ENABLED'),
(41, 40, '商品列表',   'MENU', 'goods:list',         '/goods/list',     'goods/GoodsList',              'DatabaseOutlined',   41, 'ENABLED'),
(42, 40, '商品分类',   'MENU', 'goods:category',     '/goods/category', 'goods/GoodsCategory',          'TagsOutlined',       42, 'ENABLED'),
(43, 40, '库存盘点',   'MENU', 'goods:inventory',    '/goods/inventory','inventory/InventoryList',      'InboxOutlined',      43, 'ENABLED'),
(44, 40, '动销分析',   'MENU', 'goods:salesanalysis','/goods/sales',    'goods/GoodsBrand',             'LineChartOutlined',  44, 'ENABLED'),
-- 场景运营
(50, 0,  '场景运营',   'MENU', 'scenario',           '/scenario',       NULL,                           'EnvironmentOutlined',50, 'ENABLED'),
(51, 50, '卫生巡检',   'MENU', 'scenario:health',    '/scenario/health','scenario/ScenarioHealth',      'EyeOutlined',        51, 'ENABLED'),
(52, 50, '陈列检查',   'MENU', 'scenario:display',   '/scenario/display','scenario/ScenarioDisplay',    'PictureOutlined',    52, 'ENABLED'),
(53, 50, '物料更新',   'MENU', 'scenario:material',  '/scenario/material','scenario/ScenarioMaterial',  'ToolOutlined',       53, 'ENABLED'),
(54, 50, '设备检查',   'MENU', 'scenario:device',    '/scenario/device', 'scenario/ScenarioDevice',     'ThunderboltOutlined',54, 'ENABLED'),
-- 业绩数据
(60, 0,  '业绩数据',   'MENU', 'sales',              '/sales',          NULL,                           'DollarOutlined',     60, 'ENABLED'),
(61, 60, '销售录入',   'MENU', 'sales:entry',        '/sales/entry',    'sales/SalesEntry',             'EditOutlined',       61, 'ENABLED'),
(62, 60, '销售报表',   'MENU', 'sales:report',       '/sales/report',   'sales/SalesReport',            'FundOutlined',       62, 'ENABLED'),
-- 报表中心
(70, 0,  '报表中心',   'MENU', 'report',             '/report',         'report/ReportCenter',          'PieChartOutlined',   70, 'ENABLED'),
-- AI 中心
(80, 0,  'AI 中心',    'MENU', 'ai',                 '/ai',             'ai/AICenter',                  'RobotOutlined',      80, 'ENABLED'),
-- 消息通知
(90, 0,  '消息通知',   'MENU', 'notification',       '/notification',   'notification/NotificationList','BellOutlined',       90, 'ENABLED'),
-- 操作日志
(100,0,  '操作日志',   'MENU', 'log',                '/log',            'log/LogManage',                'FileSearchOutlined',100, 'ENABLED');

-- 按钮权限
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `perm_name`, `perm_type`, `perm_code`, `sort_order`, `status`) VALUES
(101, 11, '新增组织',    'BUTTON', 'system:organization:add',    1, 'ENABLED'),
(102, 11, '编辑组织',    'BUTTON', 'system:organization:edit',   2, 'ENABLED'),
(103, 11, '删除组织',    'BUTTON', 'system:organization:delete', 3, 'ENABLED'),
(104, 13, '新增用户',    'BUTTON', 'system:user:add',            1, 'ENABLED'),
(105, 13, '编辑用户',    'BUTTON', 'system:user:edit',           2, 'ENABLED'),
(106, 13, '禁用用户',    'BUTTON', 'system:user:disable',        3, 'ENABLED'),
(107, 13, '重置密码',    'BUTTON', 'system:user:resetpwd',       4, 'ENABLED'),
(108, 14, '分配权限',    'BUTTON', 'system:role:assign',         1, 'ENABLED'),
(109, 21, '批量下发',    'BUTTON', 'task:batch',                 1, 'ENABLED'),
(110, 21, '强制任务',    'BUTTON', 'task:force',                 2, 'ENABLED'),
(111, 24, '审核通过',    'BUTTON', 'task:approve',               1, 'ENABLED'),
(112, 24, '审核驳回',    'BUTTON', 'task:reject',                2, 'ENABLED'),
(113, 24, '审核整改',    'BUTTON', 'task:rectify',               3, 'ENABLED'),
(114, 61, '审核销售',    'BUTTON', 'sales:audit',                1, 'ENABLED'),
(115, 80, '配置提示词',  'BUTTON', 'ai:prompt',                  1, 'ENABLED');

-- API 权限 (接口级别)
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `perm_name`, `perm_type`, `perm_code`, `sort_order`, `status`) VALUES
(200, 11, '组织CRUD API',  'API', 'api:organization',    1, 'ENABLED'),
(201, 12, '门店CRUD API',  'API', 'api:store',            2, 'ENABLED'),
(202, 13, '用户CRUD API',  'API', 'api:user',             3, 'ENABLED'),
(203, 14, '角色CRUD API',  'API', 'api:role',             4, 'ENABLED'),
(204, 20, '任务API',       'API', 'api:task',             5, 'ENABLED'),
(205, 40, '商品API',       'API', 'api:product',          6, 'ENABLED'),
(206, 60, '销售API',       'API', 'api:sales',            7, 'ENABLED'),
(207, 80, 'AI API',        'API', 'api:ai',               8, 'ENABLED'),
(208, 70, '报表API',       'API', 'api:report',           9, 'ENABLED'),
(209, 90, '通知API',       'API', 'api:notification',    10, 'ENABLED'),
(210, 30, '人效API',       'API', 'api:human',           11, 'ENABLED'),
(211, 50, '场景API',       'API', 'api:scene',           12, 'ENABLED');

-- ==================== 3. 默认动作库 (27条，覆盖4个维度×多种频率) ====================

-- 人效维度 (HUMAN) - 7条
INSERT IGNORE INTO `action_template` (`id`, `action_name`, `dimension`, `category`, `description`, `execution_standard`, `frequency_type`, `cron_expression`, `due_time_rule`, `required_photos`, `required_text`, `required_form`, `require_audit`, `default_auditor_role`, `score_weight`, `is_default`, `is_force`, `status`, `created_by`) VALUES
(1,  '每日晨会',          'HUMAN', '晨会',   '店长主持召开晨会，上传会议照片和纪要',     '拍1张会议全景照片+填写晨会纪要',                'DAILY',      NULL, '当日 10:00',  1, 1, 0, 0, NULL,              1.00, 1, 0, 'ENABLED', NULL),
(2,  '每日夕会',          'HUMAN', '夕会',   '店长主持当日业绩复盘夕会',                 '上传夕会总结+当日业绩截图',                     'DAILY',      NULL, '当日 20:00',  1, 1, 0, 0, NULL,              1.00, 1, 0, 'ENABLED', NULL),
(3,  '周度员工面谈',      'HUMAN', '面谈',   '店长与每位员工进行每周1v1面谈',           '填写面谈记录表+拍照确认',                       'WEEKLY',     NULL, '周五 18:00',  1, 1, 0, 1, 'ROLE_REGIONAL',    1.00, 1, 0, 'ENABLED', NULL),
(4,  '周度能力考核',      'HUMAN', '考核',   '店长对员工进行5项能力考核评分',            '按考核项逐项评分(满分100)',                     'WEEKLY',     NULL, '周五 18:00',  0, 1, 1, 1, 'ROLE_REGIONAL',    1.50, 1, 0, 'ENABLED', NULL),
(5,  '月度绩效复盘',      'HUMAN', '复盘',   '店长与员工进行月度绩效复盘并记录',         '填写月度复盘表(销售额/客单/品类/奖惩)',         'MONTHLY',    NULL, '次月5日 18:00', 0, 1, 1, 1, 'ROLE_REGIONAL',    2.00, 1, 0, 'ENABLED', NULL),
(6,  '员工分层定级',      'HUMAN', '分层',   '根据业绩/服务/执行三维度定级员工',          '按评分规则输出 BENCHMARK/STANDARD/IMPROVING',   'MONTHLY',    NULL, '次月5日 18:00', 0, 1, 1, 1, 'ROLE_REGIONAL',    2.00, 1, 0, 'ENABLED', NULL),
(7,  '季度培训计划',      'HUMAN', '培训',   '组织门店员工进行季度产品知识/销售技巧培训', '上传培训签到表+现场照片+考核成绩',              'QUARTERLY',  NULL, '季末 18:00',  2, 1, 0, 1, 'ROLE_REGIONAL',    2.00, 1, 0, 'ENABLED', NULL),

-- 货品维度 (PRODUCT) - 7条
(8,  '每日库存盘点',      'PRODUCT', '盘点',  '每日营业前/后盘点全部在售商品数量',        '扫描/核对全部商品并记录异常',                   'DAILY',      NULL, '当日 10:00 / 当日 21:00', 1, 1, 0, 1, 'ROLE_MANAGER',    1.00, 1, 1, 'ENABLED', NULL),
(9,  '每周货品养护检查',  'PRODUCT', '养护',  '每周检查贵金属/镶嵌类商品成色与完整性',      '逐件检查+拍照记录问题',                          'WEEKLY',     NULL, '周五 18:00',  2, 1, 0, 1, 'ROLE_MANAGER',    1.00, 1, 0, 'ENABLED', NULL),
(10, '周度动销分析',      'PRODUCT', '分析',  '分析本周爆款/平销/滞销商品并制定行动方案',    '输出动销四象限+行动计划',                       'WEEKLY',     NULL, '周一 12:00',  0, 1, 1, 1, 'ROLE_MANAGER',    1.50, 1, 0, 'ENABLED', NULL),
(11, '缺货风险预警检查',  'PRODUCT', '预警',  '检查库存低于安全库存的商品并预警',           '列出缺货风险清单+补货建议',                     'WEEKLY',     NULL, '周三 12:00',  0, 1, 0, 1, 'ROLE_MANAGER',    1.00, 1, 0, 'ENABLED', NULL),
(12, '新品推介方案',      'PRODUCT', '新品',  '月度新品到店后制定推介与陈列计划',           '输出陈列方案+培训脚本+目标销量',                 'MONTHLY',    NULL, '次月3日 12:00', 2, 1, 1, 1, 'ROLE_REGIONAL',    1.50, 1, 0, 'ENABLED', NULL),
(13, '促销活动筹备',      'PRODUCT', '促销',  '大型节假日/周年庆等促销活动策划',           '输出活动方案(主题/商品/优惠/预热/触达)',        'MANUAL',     NULL, '活动前7天',    0, 1, 1, 1, 'ROLE_REGIONAL',    2.00, 1, 0, 'ENABLED', NULL),
(14, '高毛利主推检查',    'PRODUCT', '主推',  '每日检查高毛利推荐商品陈列/话术执行情况',    '确认主推品陈列位+员工话术抽查',                 'DAILY',      NULL, '当日 10:00',  1, 1, 0, 0, NULL,              0.50, 1, 0, 'ENABLED', NULL),

-- 场景维度 (SCENE) - 8条
(15, '晨间卫生巡检',      'SCENE', '卫生',  '开店前检查店内卫生状况',                     '逐区域检查并拍照(黄金/钻石/K金/银饰/公共区)',   'DAILY',      NULL, '当日 09:00',  3, 1, 0, 1, 'ROLE_MANAGER',    1.00, 1, 1, 'ENABLED', NULL),
(16, '午间卫生巡检',      'SCENE', '卫生',  '午间检查店内卫生状况',                       '重点检查试戴区与洗手间',                         'DAILY',      NULL, '当日 14:00',  2, 1, 0, 1, 'ROLE_MANAGER',    0.50, 1, 0, 'ENABLED', NULL),
(17, '晚间卫生巡检',      'SCENE', '卫生',  '打烊前卫生巡检与安全检查',                   '检查电源/水源/消防/柜锁',                        'DAILY',      NULL, '当日 21:00',  3, 1, 0, 1, 'ROLE_MANAGER',    1.00, 1, 1, 'ENABLED', NULL),
(18, '陈列标准检查',      'SCENE', '陈列',  '检查各区域陈列是否符合公司标准',              '按区域评分(满分100)+整改前后对比照',             'WEEKLY',     NULL, '周三 12:00',  4, 1, 0, 1, 'ROLE_REGIONAL',    1.50, 1, 0, 'ENABLED', NULL),
(19, 'C位陈列检查',       'SCENE', '陈列',  '重点检查C位(橱窗/入口)陈列吸引力',            'C位陈列标准对照+拍照',                           'WEEKLY',     NULL, '周三 12:00',  2, 1, 0, 1, 'ROLE_MANAGER',    1.00, 1, 0, 'ENABLED', NULL),
(20, '物料更新检查',      'SCENE', '物料',  '检查海报/吊旗/展架/价签等物料时效与完整',     '逐项检查过期/破损/缺失情况',                     'WEEKLY',     NULL, '周四 12:00',  2, 1, 0, 1, 'ROLE_MANAGER',    1.00, 1, 0, 'ENABLED', NULL),
(21, '设备运行检查',      'SCENE', '设备',  '检查灯光/空调/监控/音响/POS/保险柜等设备',    '逐设备检查运行状态+拍照',                        'WEEKLY',     NULL, '周四 12:00',  2, 1, 0, 1, 'ROLE_MANAGER',    1.00, 1, 0, 'ENABLED', NULL),
(22, '客户体验周复盘',    'SCENE', '体验',  '汇总本周客户反馈/投诉并提出改进方案',         '统计反馈数/投诉数+输出改进计划',                 'WEEKLY',     NULL, '周五 18:00',  0, 1, 1, 1, 'ROLE_MANAGER',    1.50, 1, 0, 'ENABLED', NULL),

-- 综合维度 (COMPREHENSIVE) - 5条
(23, '门店月度综合评分',  'COMPREHENSIVE', '评分', '计算门店人效/货品/场景/纪律四维综合得分',    '按权重计算总分(35%+30%+25%+10%)',              'MONTHLY',    NULL, '次月5日 12:00', 0, 0, 1, 0, NULL,              3.00, 1, 1, 'ENABLED', NULL),
(24, '季度门店规划',      'COMPREHENSIVE', '规划', '制定下季度门店运营计划与目标',               '输出季度目标+行动计划+资源需求',                'QUARTERLY',  NULL, '季末前7天 18:00', 0, 1, 1, 1, 'ROLE_REGIONAL',     3.00, 1, 0, 'ENABLED', NULL),
(25, '节假日特别巡检',    'COMPREHENSIVE', '节日', '国庆/春节/情人节等节日特殊检查',              '卫生+陈列+物料+安全全面检查',                   'HOLIDAY',    NULL, '节前1天 18:00',  4, 1, 0, 1, 'ROLE_REGIONAL',     2.00, 1, 0, 'ENABLED', NULL),
(26, '异常事件上报',      'COMPREHENSIVE', '异常', '门店异常事件(客户投诉/商品遗失/安全事故等)',   '描述事件+拍照证据+处理措施',                    'ABNORMAL',   NULL, '即时',         3, 1, 0, 1, 'ROLE_REGIONAL',     1.00, 1, 0, 'ENABLED', NULL),
(27, '店长交接班检查',    'COMPREHENSIVE', '交班', '店长换班时进行全面交接检查',                  '卫生/商品/现金/设备/异常事项逐项确认',          'MANUAL',     NULL, '交班当日',      1, 1, 0, 1, 'ROLE_MANAGER',     1.00, 1, 0, 'ENABLED', NULL);

-- ==================== 4. 角色-权限默认分配 ====================

-- 系统管理员：拥有全部权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `sys_permission` WHERE status = 'ENABLED';

-- 总部运营：拥有除系统配置外的所有视图和操作权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 2, id FROM `sys_permission`
WHERE status = 'ENABLED' AND perm_code NOT IN ('system:role');

-- 区域经理：任务审核、数据查看、报表查看
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 3, id FROM `sys_permission`
WHERE status = 'ENABLED'
  AND (perm_code LIKE 'dashboard%'
    OR perm_code LIKE 'task:review%' OR perm_code LIKE 'task:approve%' OR perm_code LIKE 'task:reject%'
    OR perm_code LIKE 'human%'
    OR perm_code LIKE 'goods%'
    OR perm_code LIKE 'scenario%'
    OR perm_code LIKE 'sales:report%'
    OR perm_code LIKE 'report%'
    OR perm_code LIKE 'notification%'
    OR perm_code = 'task:batch'
  );

-- 店长：门店管理、任务执行/审核、员工管理、销售查看
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 4, id FROM `sys_permission`
WHERE status = 'ENABLED'
  AND (perm_code LIKE 'dashboard%'
    OR perm_code LIKE 'task%'
    OR perm_code LIKE 'human:meeting%' OR perm_code LIKE 'human:interview%' OR perm_code LIKE 'human:assess%'
    OR perm_code LIKE 'goods:list%' OR perm_code LIKE 'goods:category%' OR perm_code LIKE 'goods:inventory%' OR perm_code LIKE 'goods:salesanalysis%'
    OR perm_code LIKE 'scenario%'
    OR perm_code LIKE 'sales:entry%' OR perm_code LIKE 'sales:report%'
    OR perm_code LIKE 'report%'
    OR perm_code LIKE 'notification%'
  );

-- 导购：个人任务、销售录入、个人业绩查看
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 5, id FROM `sys_permission`
WHERE status = 'ENABLED'
  AND (perm_code LIKE 'dashboard%'
    OR perm_code LIKE 'task:list%'
    OR perm_code LIKE 'sales:entry%'
    OR perm_code LIKE 'ai%'
    OR perm_code LIKE 'notification%'
  );

-- ==================== 5. 默认管理员账号 ====================
-- 用户名: admin  密码: admin123  (BCrypt加密)
INSERT IGNORE INTO `sys_user` (`id`, `username`, `password_hash`, `real_name`, `phone`, `status`, `is_deleted`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '系统管理员', '13800000000', 'ACTIVE', 0);

-- 管理员绑定系统管理员角色
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);
