# 珠宝通后端 SpringBoot 项目创建文档

> 目标：从零创建可运行的 SpringBoot 项目，按模块顺序逐步搭建
> 技术栈：SpringBoot 2.7.x + MyBatis-Plus + MySQL + Redis + MinIO + XXL-JOB + JWT

---

## 第一步：创建项目骨架

### 1.1 使用 Spring Initializr 创建

访问 https://start.spring.io/ 或使用 IDEA 新建 SpringBoot 项目：

```
Project: Maven
Language: Java (JDK 8 或 11)
Spring Boot: 2.7.18
Group: com.zhubao.manage
Artifact: zb-tong-server
Name: zb-tong-server
Packaging: Jar
```

**依赖选择（勾选以下）:**
- Spring Web (spring-boot-starter-web)
- Spring Security (spring-boot-starter-security)
- MySQL Driver (mysql-connector-j)
- MyBatis-Plus (手动添加，Initializr 默认不带)
- Spring Data Redis (spring-boot-starter-data-redis)
- Validation (spring-boot-starter-validation)
- Lombok
- Spring Boot Actuator

### 1.2 完整 pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
        <relativePath/>
    </parent>

    <groupId>com.zhubao.manage</groupId>
    <artifactId>zb-tong-server</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>zb-tong-server</name>
    <description>珠宝通零售连锁门店主动式管理系统</description>

    <properties>
        <java.version>11</java.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        <jjwt.version>0.11.5</jjwt.version>
        <knife4j.version>4.3.0</knife4j.version>
        <minio.version>8.5.7</minio.version>
        <easyexcel.version>3.3.3</easyexcel.version>
        <xxl-job.version>2.4.0</xxl-job.version>
        <hutool.version>5.8.25</hutool.version>
        <flyway.version>9.22.3</flyway.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>

        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- MySQL Driver -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- MinIO -->
        <dependency>
            <groupId>io.minio</groupId>
            <artifactId>minio</artifactId>
            <version>${minio.version}</version>
        </dependency>

        <!-- XXL-JOB -->
        <dependency>
            <groupId>com.xuxueli</groupId>
            <artifactId>xxl-job-core</artifactId>
            <version>${xxl-job.version}</version>
        </dependency>

        <!-- Knife4j (Swagger 文档) -->
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-openapi3-spring-boot-starter</artifactId>
            <version>${knife4j.version}</version>
        </dependency>

        <!-- EasyExcel -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>easyexcel</artifactId>
            <version>${easyexcel.version}</version>
        </dependency>

        <!-- Hutool 工具库 -->
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>${hutool.version}</version>
        </dependency>

        <!-- Flyway 数据库迁移 -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
            <version>${flyway.version}</version>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-mysql</artifactId>
            <version>${flyway.version}</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 1.3 创建 application.yml

**文件**: `src/main/resources/application.yml`

```yaml
server:
  port: 8080

spring:
  application:
    name: zb-tong-server
  profiles:
    active: dev
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8
    serialization:
      write-dates-as-timestamps: false
  servlet:
    multipart:
      max-file-size: 20MB
      max-request-size: 100MB
```

**文件**: `src/main/resources/application-dev.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/zb_tong?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 300000
      max-lifetime: 1200000

  redis:
    host: localhost
    port: 6379
    password:
    database: 0
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.zhubao.manage.module
  global-config:
    db-config:
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

# JWT 配置
jwt:
  secret: your-256-bit-secret-key-change-in-production-xxxxxxxxxxxxxx
  expiration: 86400000  # 24小时，单位ms

# MinIO 配置
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket: zhubao

# XXL-JOB 配置
xxl:
  job:
    admin:
      addresses: http://localhost:9080/xxl-job-admin
    executor:
      appname: zb-tong-server
      port: 9999
      logpath: ./logs/xxl-job

# AI 配置
ai:
  provider: openai  # openai / wenxin / tongyi
  openai:
    api-key: your-openai-api-key
    model: gpt-4
    base-url: https://api.openai.com

# Knife4j
springdoc:
  swagger-ui:
    path: /doc.html
  api-docs:
    path: /v3/api-docs
```

---

## 第二步：建立目录结构和公共代码

### 2.1 创建包结构（按顺序执行）

```bash
# 在 src/main/java/com/zhubao/manage/ 下创建以下目录:

# 启动类
mkdir -p src/main/java/com/zhubao/manage

# 公共模块
mkdir -p src/main/java/com/zhubao/manage/common/config
mkdir -p src/main/java/com/zhubao/manage/common/interceptor
mkdir -p src/main/java/com/zhubao/manage/common/annotation
mkdir -p src/main/java/com/zhubao/manage/common/enums
mkdir -p src/main/java/com/zhubao/manage/common/exception
mkdir -p src/main/java/com/zhubao/manage/common/dto
mkdir -p src/main/java/com/zhubao/manage/common/utils

# 业务模块（11个模块）
mkdir -p src/main/java/com/zhubao/manage/module/auth/controller
mkdir -p src/main/java/com/zhubao/manage/module/auth/service/impl
mkdir -p src/main/java/com/zhubao/manage/module/auth/dto

mkdir -p src/main/java/com/zhubao/manage/module/organization/controller
mkdir -p src/main/java/com/zhubao/manage/module/organization/service/impl
mkdir -p src/main/java/com/zhubao/manage/module/organization/mapper
mkdir -p src/main/java/com/zhubao/manage/module/organization/entity

mkdir -p src/main/java/com/zhubao/manage/module/user/controller
mkdir -p src/main/java/com/zhubao/manage/module/user/service/impl
mkdir -p src/main/java/com/zhubao/manage/module/user/mapper
mkdir -p src/main/java/com/zhubao/manage/module/user/entity

mkdir -p src/main/java/com/zhubao/manage/module/role/controller
mkdir -p src/main/java/com/zhubao/manage/module/role/service/impl
mkdir -p src/main/java/com/zhubao/manage/module/role/mapper
mkdir -p src/main/java/com/zhubao/manage/module/role/entity

mkdir -p src/main/java/com/zhubao/manage/module/task/controller
mkdir -p src/main/java/com/zhubao/manage/module/task/service/impl
mkdir -p src/main/java/com/zhubao/manage/module/task/mapper
mkdir -p src/main/java/com/zhubao/manage/module/task/entity

mkdir -p src/main/java/com/zhubao/manage/module/actiontemplate/controller
mkdir -p src/main/java/com/zhubao/manage/module/actiontemplate/service/impl
mkdir -p src/main/java/com/zhubao/manage/module/actiontemplate/mapper
mkdir -p src/main/java/com/zhubao/manage/module/actiontemplate/entity

mkdir -p src/main/java/com/zhubao/manage/module/human/controller
mkdir -p src/main/java/com/zhubao/manage/module/human/service/impl
mkdir -p src/main/java/com/zhubao/manage/module/human/mapper
mkdir -p src/main/java/com/zhubao/manage/module/human/entity

mkdir -p src/main/java/com/zhubao/manage/module/product/controller
mkdir -p src/main/java/com/zhubao/manage/module/product/service/impl
mkdir -p src/main/java/com/zhubao/manage/module/product/mapper
mkdir -p src/main/java/com/zhubao/manage/module/product/entity

mkdir -p src/main/java/com/zhubao/manage/module/scene/controller
mkdir -p src/main/java/com/zhubao/manage/module/scene/service/impl
mkdir -p src/main/java/com/zhubao/manage/module/scene/mapper
mkdir -p src/main/java/com/zhubao/manage/module/scene/entity

mkdir -p src/main/java/com/zhubao/manage/module/sales/controller
mkdir -p src/main/java/com/zhubao/manage/module/sales/service/impl
mkdir -p src/main/java/com/zhubao/manage/module/sales/mapper
mkdir -p src/main/java/com/zhubao/manage/module/sales/entity

mkdir -p src/main/java/com/zhubao/manage/module/ai/controller
mkdir -p src/main/java/com/zhubao/manage/module/ai/service/impl
mkdir -p src/main/java/com/zhubao/manage/module/ai/gateway/impl
mkdir -p src/main/java/com/zhubao/manage/module/ai/assembler
mkdir -p src/main/java/com/zhubao/manage/module/ai/parser
mkdir -p src/main/java/com/zhubao/manage/module/ai/mapper
mkdir -p src/main/java/com/zhubao/manage/module/ai/entity

mkdir -p src/main/java/com/zhubao/manage/module/report/controller
mkdir -p src/main/java/com/zhubao/manage/module/report/service/impl
mkdir -p src/main/java/com/zhubao/manage/module/report/mapper
mkdir -p src/main/java/com/zhubao/manage/module/report/entity

mkdir -p src/main/java/com/zhubao/manage/module/notification/controller
mkdir -p src/main/java/com/zhubao/manage/module/notification/service/impl
mkdir -p src/main/java/com/zhubao/manage/module/notification/channel
mkdir -p src/main/java/com/zhubao/manage/module/notification/mapper
mkdir -p src/main/java/com/zhubao/manage/module/notification/entity

mkdir -p src/main/java/com/zhubao/manage/module/file/controller
mkdir -p src/main/java/com/zhubao/manage/module/file/service/impl
mkdir -p src/main/java/com/zhubao/manage/module/file/storage
mkdir -p src/main/java/com/zhubao/manage/module/file/mapper
mkdir -p src/main/java/com/zhubao/manage/module/file/entity

# 定时任务 + 基础设施
mkdir -p src/main/java/com/zhubao/manage/scheduler
mkdir -p src/main/java/com/zhubao/manage/infrastructure/mybatis
mkdir -p src/main/java/com/zhubao/manage/infrastructure/aop

# 资源目录
mkdir -p src/main/resources/db/migration
```

### 2.2 按优先级顺序编写公共代码

**优先级A（必须先写，其他所有代码依赖）:**

```
1. ZbtApplication.java            — 启动类
2. common/dto/ApiResult.java      — 统一响应体 {code, msg, data}
3. common/dto/PageDTO.java        — 分页请求 {page, size, orderBy, orderDir}
4. common/dto/PageResult.java     — 分页响应 {list, total, page, size}
5. common/enums/* (全部枚举类)     — 所有 Enum 定义
6. common/exception/ErrorCode.java     — 错误码枚举
7. common/exception/BusinessException.java — 业务异常
8. common/exception/GlobalExceptionHandler.java — 全局异常处理
9. common/utils/JwtUtil.java      — JWT token 生成/解析/验证
10. common/config/SecurityConfig.java  — Spring Security + JWT 配置
11. common/interceptor/AuthInterceptor.java — JWT 认证拦截器
```

**优先级B（业务开发前写）:**

```
12. common/config/WebMvcConfig.java       — 注册拦截器
13. common/config/MyBatisPlusConfig.java  — 分页插件/乐观锁/逻辑删除
14. common/config/RedisConfig.java        — Redis 序列化配置
15. common/config/CorsConfig.java         — 跨域
16. common/config/SwaggerConfig.java      — Knife4j API 文档
17. common/config/FileUploadConfig.java   — 文件上传大小限制
18. common/utils/CodeGenerator.java       — 编码生成器
19. common/utils/DateUtil.java            — 日期工具
20. common/utils/FileUtil.java            — 文件校验
```

**优先级C（权限相关）:**

```
21. common/annotation/DataScope.java           — 数据权限注解
22. common/annotation/OperateLog.java          — 操作日志注解
23. infrastructure/mybatis/DataScopePlugin.java — MyBatis 数据权限拦截器
24. infrastructure/aop/OperateLogAspect.java    — 操作日志 AOP
25. common/interceptor/DataScopeInterceptor.java — 数据权限拦截器
26. common/interceptor/LogInterceptor.java      — 日志拦截器
```

### 2.3 核心公共类代码模板

#### ApiResult.java

```java
package com.zhubao.manage.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(200, "success", data);
    }

    public static <T> ApiResult<T> success() {
        return new ApiResult<>(200, "success", null);
    }

    public static <T> ApiResult<T> error(int code, String msg) {
        return new ApiResult<>(code, msg, null);
    }

    public static <T> ApiResult<T> error(String msg) {
        return new ApiResult<>(500, msg, null);
    }
}
```

#### PageDTO.java

```java
package com.zhubao.manage.common.dto;

import lombok.Data;

@Data
public class PageDTO {
    private int page = 1;
    private int size = 20;
    private String orderBy;
    private String orderDir = "DESC";
}
```

#### PageResult.java

```java
package com.zhubao.manage.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private List<T> list;
    private long total;
    private int page;
    private int size;
}
```

---

## 第三步：Flyway 数据库迁移脚本（按依赖顺序创建）

### V1__init_org_user.sql

创建所有权限相关表：

```
sys_organization, sys_store, sys_user, sys_role,
sys_permission, sys_user_role, sys_role_permission, sys_role_data_scope
```

### V2__init_task.sql

创建任务相关表：

```
action_template, task_template, task_instance,
task_submission, task_audit, task_reminder_log
```

### V3__init_human.sql

创建人效相关表：

```
employee_profile, employee_interview, employee_assessment,
employee_training, employee_training_record,
employee_monthly_review, employee_level_record
```

### V4__init_product.sql

创建货品相关表：

```
product, product_inventory_check, product_maintenance_check,
product_sales_analysis, new_product_plan, promotion_plan
```

### V5__init_scene.sql

创建场景相关表：

```
scene_health_inspection, scene_display_inspection,
scene_material_update, scene_equipment_check,
scene_customer_experience_review
```

### V6__init_sales.sql

创建销售相关表：

```
sales_record, sales_item
```

### V7__init_ai_report.sql

创建 AI + 报表 + 通知 + 文件 + 日志表：

```
prompt_template, ai_result,
store_monthly_score, report_snapshot,
notification, file_resource, form_schema, operate_log
```

### V8__init_seed_data.sql

种子数据：

```sql
-- 默认角色
INSERT INTO sys_role (role_code, role_name, data_scope) VALUES
('ROLE_ADMIN', '系统管理员', 'ALL'),
('ROLE_HQ', '总部运营', 'ALL'),
('ROLE_REGIONAL', '区域经理', 'REGION'),
('ROLE_MANAGER', '店长', 'STORE'),
('ROLE_ASSOCIATE', '导购', 'SELF');

-- 默认动作库（人效8个 + 货品9个 + 场景8个）
-- 见 PRD 8.3 节，此处省略具体 SQL，开发时逐条 INSERT
-- 示例:
INSERT INTO action_template (action_name, dimension, category, frequency_type, required_photos, required_text, require_audit, is_default, is_force)
VALUES ('晨会记录', 'HUMAN', '每日管理', 'DAILY', 1, 1, 1, 1, 1);

-- ... 其余25个默认动作同理
```

> 完整建表 SQL 参见 [ARCHITECTURE.md](./ARCHITECTURE.md) 第4节的表结构定义，逐表编写 DDL。

---

## 第四步：按模块顺序开发

### 开发顺序规划

```
M1 认证+组织权限 (第1-2周)
  ├── Module: auth        → 登录/Token 刷新/密码修改
  ├── Module: organization → 组织树/门店 CRUD
  ├── Module: user        → 用户 CRUD
  └── Module: role        → 角色 CRUD + 权限分配

M7 文件管理 (第2周)
  └── Module: file        → 上传/下载/预签名URL

M2 任务中心 + 动作库 (第3-4周)
  ├── Module: actiontemplate → 动作库 CRUD + 启停 + 下发
  └── Module: task      → 模板 CRUD + 任务生成 + 执行 + 审核 + 提醒
                          + 定时任务: TaskGenerateJob / TaskOverdueJob

M3+M4+M5 人货场 (第5-7周，可并行)
  ├── Module: human     → 晨夕会/面谈/考核/培训/复盘/分层
  ├── Module: product   → 商品/盘点/养护/动销/新品/促销
  └── Module: scene     → 卫生/陈列/物料/设备/体验复盘

M6 业绩 (第7-8周)
  └── Module: sales     → 销售录入/审核/统计

M9 通知 (第8周)
  └── Module: notification → 站内信/多级提醒

M8 报表 (第9周)
  └── Module: report    → 评分计算/排名/报表/导出 + 定时任务: StoreScoreCalcJob

M7 AI (第10周)
  └── Module: ai        → 提示词管理 + AI 网关 + 评分 + 建议 + 定时任务: AIAnalysisJob
```

### 每个模块的开发步骤模板

以 **M1 auth** 为例，每个模块内部按以下顺序：

```
1. entity/      → 对应数据库表的实体类（@TableName, @TableId, @TableField）
2. mapper/      → MyBatis-Plus BaseMapper 接口（继承 BaseMapper<Entity>）
3. dto/         → 请求/响应 DTO 类
4. service/     → 接口定义
5. service/impl/→ 实现类
6. controller/  → RESTful Controller
7. 单元测试      → 验证 API 能通
```

---

## 第五步：XXL-JOB 调度中心部署

### 5.1 下载并启动 XXL-JOB

```bash
# 1. 下载 xxl-job
wget https://github.com/xuxueli/xxl-job/archive/refs/tags/2.4.0.zip
unzip 2.4.0.zip

# 2. 导入 xxl-job-admin 的 SQL
mysql -u root -p < xxl-job-2.4.0/doc/db/tables_xxl_job.sql

# 3. 修改 xxl-job-admin 的 application.properties 中的数据库连接

# 4. 启动 xxl-job-admin (默认端口 9080)
cd xxl-job-2.4.0/xxl-job-admin
mvn spring-boot:run
```

### 5.2 在项目中配置 XXL-JOB 执行器

```java
// src/main/java/com/zhubao/manage/common/config/XxlJobConfig.java
package com.zhubao.manage.common.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XxlJobConfig {

    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;

    @Value("${xxl.job.executor.appname}")
    private String appname;

    @Value("${xxl.job.executor.port}")
    private int port;

    @Value("${xxl.job.executor.logpath}")
    private String logPath;

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(adminAddresses);
        executor.setAppname(appname);
        executor.setPort(port);
        executor.setLogPath(logPath);
        return executor;
    }
}
```

### 5.3 第一个定时任务示例

```java
// src/main/java/com/zhubao/manage/scheduler/TaskGenerateJob.java
package com.zhubao.manage.scheduler;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskGenerateJob {

    // TODO: 注入 TaskGenerateService
    // private final TaskGenerateService taskGenerateService;

    @XxlJob("dailyTaskGenerateHandler")
    public void generateDailyTasks() {
        log.info("=== 每日任务生成开始 ===");
        // TODO: taskGenerateService.generateDailyTasks();
        log.info("=== 每日任务生成完成 ===");
    }

    @XxlJob("weeklyTaskGenerateHandler")
    public void generateWeeklyTasks() {
        log.info("=== 每周任务生成开始 ===");
        // TODO: taskGenerateService.generateWeeklyTasks();
        log.info("=== 每周任务生成完成 ===");
    }

    @XxlJob("monthlyTaskGenerateHandler")
    public void generateMonthlyTasks() {
        log.info("=== 每月任务生成开始 ===");
        // TODO: taskGenerateService.generateMonthlyTasks();
        log.info("=== 每月任务生成完成 ===");
    }
}
```

---

## 第六步：开发环境中间件对照表

| 中间件 | 版本 | 端口 | 用途 | 安装命令（Windows/Mac/Linux） |
|--------|------|------|------|------|
| MySQL | 8.0+ | 3306 | 主数据库 | 已有安装(`D:\mysql\install`) |
| Redis | 7.0+ | 6379 | 缓存/Token | `docker run -d -p 6379:6379 redis:7` 或 Windows 安装包 |
| MinIO | latest | 9000(API) / 9001(Console) | 文件存储 | `docker run -d -p 9000:9000 -p 9001:9001 minio/minio server /data --console-address ":9001"` |
| XXL-JOB | 2.4.0 | 9080 | 定时任务 | 见第五步 |
| JDK | 11/17 | - | Java 运行时 | 已有安装(`D:\java\jdk1.8.0_341`, 建议升级到 JDK 11+) |
| Maven | 3.6+ | - | 构建工具 | 已有安装(`D:\apache-maven-3.6.3`) |

---

## 第七步：启动验证清单

项目搭建完成后，按以下顺序验证：

```
[ ] MySQL 连接正常 → application-dev.yml 中数据库 zb_tong 已创建
[ ] Flyway 自动建表 → 启动项目后，8个迁移脚本依次执行，50+张表创建成功
[ ] 种子数据 → V8 脚本中的角色、默认动作库数据已写入
[ ] Redis 连接正常 → 启动无报错
[ ] MinIO 连接正常 → Bucket 'zhubao' 已创建
[ ] XXL-JOB 调度中心 → http://localhost:9080/xxl-job-admin 可访问
[ ] Swagger 文档 → http://localhost:8080/doc.html 可访问
[ ] 登录 API → POST /api/v1/auth/login 返回 200 + token
[ ] JWT 认证 → 带 token 请求其他 API 不报 401
[ ] 分页查询 → GET /api/v1/users?page=1&size=10 返回正确分页格式
```
