-- ============================================================================
-- 上海救助基地潜水救生员训练系统 — 一期数据库建表脚本（永久定型）
-- 数据库：rescue_training
-- 引擎：InnoDB   字符集：utf8mb4   排序规则：utf8mb4_general_ci
-- 版本：V1.0   日期：2026-08-28
-- 说明：一期表结构一次定型，后期仅填充业务数据，不修改表结构。
--       所有业务表统一保留 ext_json 扩展字段与审计字段，用于轻量迭代。
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `rescue_training`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE `rescue_training`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- 模块一：组织架构与权限体系
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 1. 部门/班组表（组织架构，树形结构）
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id`   BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级ID，0为顶级',
  `dept_name`   VARCHAR(64)     NOT NULL COMMENT '部门/班组名称',
  `dept_type`   TINYINT         NOT NULL DEFAULT 1 COMMENT '类型：1=部门 2=班组/分队',
  `sort`        INT             NOT NULL DEFAULT 0 COMMENT '排序号，越小越靠前',
  `status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1=启用 0=停用',
  `remark`      VARCHAR(255)             DEFAULT NULL COMMENT '备注',
  `ext_json`    JSON                     DEFAULT NULL COMMENT '扩展字段（JSON）',
  `create_by`   BIGINT UNSIGNED          DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`   BIGINT UNSIGNED          DEFAULT NULL COMMENT '更新人ID',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='组织架构-部门/班组表';

-- ---------------------------------------------------------------------------
-- 2. 角色表（四级权限体系）
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_code`   VARCHAR(32)     NOT NULL COMMENT '角色编码：SUPER_ADMIN/DEPT_LEADER/RESCUER',
  `role_name`   VARCHAR(64)     NOT NULL COMMENT '角色名称',
  `data_scope`  TINYINT         NOT NULL DEFAULT 1 COMMENT '数据范围：1=全部 2=本部门 3=本人',
  `remark`      VARCHAR(255)             DEFAULT NULL COMMENT '备注',
  `create_by`   BIGINT UNSIGNED          DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`   BIGINT UNSIGNED          DEFAULT NULL COMMENT '更新人ID',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='权限-角色表';

-- ---------------------------------------------------------------------------
-- 3. 用户表（含救生员等级、在岗状态）
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dept_id`          BIGINT UNSIGNED          DEFAULT NULL COMMENT '所属部门/班组ID',
  `username`         VARCHAR(64)     NOT NULL COMMENT '登录账号',
  `password`         VARCHAR(128)    NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name`        VARCHAR(64)     NOT NULL COMMENT '真实姓名',
  `phone`            VARCHAR(20)              DEFAULT NULL COMMENT '手机号',
  `rescue_level`     TINYINT         NOT NULL DEFAULT 1 COMMENT '救生员等级：1-5级',
  `job_status`       TINYINT         NOT NULL DEFAULT 1 COMMENT '在岗状态：1=在岗 2=离岗',
  `enabled`          TINYINT         NOT NULL DEFAULT 1 COMMENT '账号状态：1=启用 0=禁用',
  `last_login_time`  DATETIME                 DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip`    VARCHAR(64)              DEFAULT NULL COMMENT '最后登录IP',
  `remark`           VARCHAR(255)             DEFAULT NULL COMMENT '备注',
  `ext_json`         JSON                     DEFAULT NULL COMMENT '扩展字段（JSON）',
  `create_by`        BIGINT UNSIGNED          DEFAULT NULL COMMENT '创建人ID',
  `create_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`        BIGINT UNSIGNED          DEFAULT NULL COMMENT '更新人ID',
  `update_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`          TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_rescue_level` (`rescue_level`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='权限-用户表';

-- ---------------------------------------------------------------------------
-- 4. 用户角色关联表（多对多）
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `role_id`     BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='权限-用户角色关联表';

-- ---------------------------------------------------------------------------
-- 5. 登录日志表（双端登录同步、异地登录提醒依据）
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT UNSIGNED          DEFAULT NULL COMMENT '用户ID',
  `username`    VARCHAR(64)     NOT NULL COMMENT '登录账号',
  `login_ip`    VARCHAR(64)              DEFAULT NULL COMMENT '登录IP',
  `device_type` TINYINT         NOT NULL DEFAULT 1 COMMENT '终端：1=安卓APP 2=PC后台',
  `login_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '结果：1=成功 0=失败',
  `msg`         VARCHAR(255)             DEFAULT NULL COMMENT '失败原因/提示',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审计-登录日志表';

-- ---------------------------------------------------------------------------
-- 6. 操作日志表（全操作留痕可追溯）
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_op_log`;
CREATE TABLE `sys_op_log` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`    BIGINT UNSIGNED          DEFAULT NULL COMMENT '操作人ID',
  `username`   VARCHAR(64)              DEFAULT NULL COMMENT '操作人账号',
  `module`     VARCHAR(64)              DEFAULT NULL COMMENT '操作模块',
  `action`     VARCHAR(128)             DEFAULT NULL COMMENT '操作内容',
  `method`     VARCHAR(255)             DEFAULT NULL COMMENT '请求方法',
  `request_ip` VARCHAR(64)              DEFAULT NULL COMMENT '请求IP',
  `op_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `status`     TINYINT         NOT NULL DEFAULT 1 COMMENT '结果：1=成功 0=失败',
  `detail`     TEXT                     DEFAULT NULL COMMENT '操作详情/请求参数摘要',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_op_time` (`op_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审计-操作日志表';

-- ============================================================================
-- 模块二：1-5级分级训练（实训骨架）
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 7. 训练项目表（1-5级，业务内容由后台批量导入）
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `train_project`;
CREATE TABLE `train_project` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `level`       TINYINT         NOT NULL COMMENT '等级：1-5级',
  `category`    VARCHAR(64)              DEFAULT NULL COMMENT '训练分类/科目',
  `name`        VARCHAR(128)    NOT NULL COMMENT '训练项目名称',
  `outline`     TEXT                     DEFAULT NULL COMMENT '训练大纲',
  `steps`       TEXT                     DEFAULT NULL COMMENT '实操步骤',
  `standard`    TEXT                     DEFAULT NULL COMMENT '标准要求',
  `tips`        TEXT                     DEFAULT NULL COMMENT '易错提示',
  `sort`        INT             NOT NULL DEFAULT 0 COMMENT '排序号',
  `status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1=启用 0=停用',
  `ext_json`    JSON                     DEFAULT NULL COMMENT '扩展：动作标准细则等',
  `create_by`   BIGINT UNSIGNED          DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`   BIGINT UNSIGNED          DEFAULT NULL COMMENT '更新人ID',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_level` (`level`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='训练-训练项目表';

-- ---------------------------------------------------------------------------
-- 8. 训练素材表（图文/视频素材位置占位）
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `train_material`;
CREATE TABLE `train_material` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `project_id`    BIGINT UNSIGNED NOT NULL COMMENT '关联训练项目ID',
  `material_type` TINYINT         NOT NULL DEFAULT 1 COMMENT '素材类型：1=图片 2=视频 3=文档',
  `title`         VARCHAR(128)             DEFAULT NULL COMMENT '素材标题',
  `url`           VARCHAR(512)             DEFAULT NULL COMMENT '素材地址',
  `sort`          INT             NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_by`     BIGINT UNSIGNED          DEFAULT NULL COMMENT '创建人ID',
  `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`     BIGINT UNSIGNED          DEFAULT NULL COMMENT '更新人ID',
  `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='训练-训练素材表';

-- ---------------------------------------------------------------------------
-- 9. 训练打卡表（实训打卡、自主自评、进度记录）
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `train_checkin`;
CREATE TABLE `train_checkin` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`      BIGINT UNSIGNED NOT NULL COMMENT '队员ID',
  `project_id`   BIGINT UNSIGNED NOT NULL COMMENT '训练项目ID',
  `checkin_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '打卡时间',
  `self_eval`    TINYINT                  DEFAULT NULL COMMENT '自主自评：1=合格 2=基本合格 3=不合格',
  `self_comment` VARCHAR(255)             DEFAULT NULL COMMENT '自评说明',
  `progress`     TINYINT         NOT NULL DEFAULT 100 COMMENT '进度百分比（0-100）',
  `status`       TINYINT         NOT NULL DEFAULT 0 COMMENT '审核状态：0=待审核 1=通过 2=驳回',
  `offline_flag` TINYINT         NOT NULL DEFAULT 0 COMMENT '离线标记：0=在线提交 1=离线补传',
  `ext_json`     JSON                     DEFAULT NULL COMMENT '扩展字段（JSON）',
  `create_by`    BIGINT UNSIGNED          DEFAULT NULL COMMENT '创建人ID',
  `create_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`    BIGINT UNSIGNED          DEFAULT NULL COMMENT '更新人ID',
  `update_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='训练-训练打卡记录表';

-- ---------------------------------------------------------------------------
-- 10. 打卡审核记录表（多级审核：初审/终审）
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `train_checkin_review`;
CREATE TABLE `train_checkin_review` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `checkin_id`   BIGINT UNSIGNED NOT NULL COMMENT '打卡记录ID',
  `reviewer_id`  BIGINT UNSIGNED NOT NULL COMMENT '审核人ID',
  `review_level` TINYINT         NOT NULL DEFAULT 1 COMMENT '审核层级：1=初审 2=终审',
  `result`       TINYINT         NOT NULL COMMENT '审核结果：1=通过 2=驳回',
  `comment`      VARCHAR(255)             DEFAULT NULL COMMENT '审核意见',
  `review_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  PRIMARY KEY (`id`),
  KEY `idx_checkin_id` (`checkin_id`),
  KEY `idx_reviewer_id` (`reviewer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='训练-打卡审核记录表';

-- ============================================================================
-- 模块三：理论学习与等级考核
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 11. 理论题库表（绑定等级，支持单选/多选/判断/简答）
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `question_bank`;
CREATE TABLE `question_bank` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `level`         TINYINT         NOT NULL COMMENT '等级：1-5级',
  `chapter`       VARCHAR(64)              DEFAULT NULL COMMENT '章节',
  `question_type` TINYINT         NOT NULL COMMENT '题型：1=单选 2=多选 3=判断 4=简答',
  `content`       TEXT            NOT NULL COMMENT '题干',
  `options`       JSON                     DEFAULT NULL COMMENT '选项（单选/多选，JSON数组）',
  `answer`        TEXT                     DEFAULT NULL COMMENT '正确答案',
  `analysis`      TEXT                     DEFAULT NULL COMMENT '答案解析',
  `score`         DECIMAL(5,2)    NOT NULL DEFAULT 1.00 COMMENT '默认分值',
  `status`        TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1=启用 0=停用',
  `ext_json`      JSON                     DEFAULT NULL COMMENT '扩展字段（JSON）',
  `create_by`     BIGINT UNSIGNED          DEFAULT NULL COMMENT '创建人ID',
  `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`     BIGINT UNSIGNED          DEFAULT NULL COMMENT '更新人ID',
  `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_level` (`level`),
  KEY `idx_question_type` (`question_type`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试-理论题库表';

-- ---------------------------------------------------------------------------
-- 12. 考试场次表（模拟自测/正式晋升考试）
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `exam_session`;
CREATE TABLE `exam_session` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(128)    NOT NULL COMMENT '考试名称',
  `level`       TINYINT         NOT NULL COMMENT '等级：1-5级',
  `exam_type`   TINYINT         NOT NULL DEFAULT 1 COMMENT '类型：1=模拟自测 2=正式晋升考试',
  `start_time`  DATETIME                 DEFAULT NULL COMMENT '开考时间',
  `end_time`    DATETIME                 DEFAULT NULL COMMENT '结束时间',
  `duration`    INT                      DEFAULT NULL COMMENT '考试时长（分钟）',
  `total_score` DECIMAL(5,2)    NOT NULL DEFAULT 100.00 COMMENT '卷面总分',
  `pass_score`  DECIMAL(5,2)    NOT NULL DEFAULT 60.00 COMMENT '及格线',
  `status`      TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：0=未开始 1=进行中 2=已结束',
  `create_by`   BIGINT UNSIGNED          DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`   BIGINT UNSIGNED          DEFAULT NULL COMMENT '更新人ID',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_level` (`level`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试-考试场次表';

-- ---------------------------------------------------------------------------
-- 13. 考试题目关联表（随机组卷结果）
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `exam_question`;
CREATE TABLE `exam_question` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_id`  BIGINT UNSIGNED NOT NULL COMMENT '考试场次ID',
  `question_id` BIGINT UNSIGNED NOT NULL COMMENT '题目ID',
  `sort`        INT             NOT NULL DEFAULT 0 COMMENT '题目顺序号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_question` (`session_id`, `question_id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试-考试题目关联表';

-- ---------------------------------------------------------------------------
-- 14. 考试记录表（一人一场次一条）
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `exam_record`;
CREATE TABLE `exam_record` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_id`  BIGINT UNSIGNED NOT NULL COMMENT '考试场次ID',
  `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '考生ID',
  `score`       DECIMAL(5,2)             DEFAULT NULL COMMENT '得分',
  `is_pass`     TINYINT                  DEFAULT NULL COMMENT '是否合格：1=合格 0=不合格',
  `status`      TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：0=进行中 1=已交卷 2=已阅卷',
  `submit_time` DATETIME                 DEFAULT NULL COMMENT '交卷时间',
  `review_by`   BIGINT UNSIGNED          DEFAULT NULL COMMENT '人工阅卷人ID（简答题）',
  `review_time` DATETIME                 DEFAULT NULL COMMENT '阅卷时间',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试-考试记录表';

-- ---------------------------------------------------------------------------
-- 15. 作答明细表
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `exam_answer`;
CREATE TABLE `exam_answer` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `record_id`   BIGINT UNSIGNED NOT NULL COMMENT '考试记录ID',
  `question_id` BIGINT UNSIGNED NOT NULL COMMENT '题目ID',
  `user_answer` TEXT                     DEFAULT NULL COMMENT '考生答案',
  `is_correct`  TINYINT                  DEFAULT NULL COMMENT '是否答对：1=对 0=错',
  `score`       DECIMAL(5,2)             DEFAULT NULL COMMENT '本题得分',
  PRIMARY KEY (`id`),
  KEY `idx_record_id` (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试-作答明细表';

-- ---------------------------------------------------------------------------
-- 16. 成绩归档表（历史成绩永久留存快照）
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `exam_score`;
CREATE TABLE `exam_score` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `record_id`    BIGINT UNSIGNED NOT NULL COMMENT '考试记录ID',
  `user_id`      BIGINT UNSIGNED NOT NULL COMMENT '考生ID',
  `session_id`   BIGINT UNSIGNED NOT NULL COMMENT '考试场次ID',
  `level`        TINYINT         NOT NULL COMMENT '等级：1-5级',
  `score`        DECIMAL(5,2)    NOT NULL COMMENT '最终得分',
  `is_pass`      TINYINT         NOT NULL COMMENT '是否合格：1=合格 0=不合格',
  `archive_json` JSON                     DEFAULT NULL COMMENT '答卷快照（JSON）',
  `create_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试-成绩归档表';

-- ============================================================================
-- 模块四：基地团队协同救援训练
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 17. 团队/班组表
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `team_group`;
CREATE TABLE `team_group` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(128)    NOT NULL COMMENT '团队/班组名称',
  `dept_id`     BIGINT UNSIGNED          DEFAULT NULL COMMENT '所属部门ID',
  `leader_id`   BIGINT UNSIGNED          DEFAULT NULL COMMENT '负责人/队长ID',
  `status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1=启用 0=停用',
  `remark`      VARCHAR(255)             DEFAULT NULL COMMENT '备注',
  `create_by`   BIGINT UNSIGNED          DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`   BIGINT UNSIGNED          DEFAULT NULL COMMENT '更新人ID',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='团队-团队/班组表';

-- ---------------------------------------------------------------------------
-- 18. 团队成员表
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `team_group_member`;
CREATE TABLE `team_group_member` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `group_id`    BIGINT UNSIGNED NOT NULL COMMENT '团队ID',
  `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '队员ID',
  `role_name`   VARCHAR(32)              DEFAULT NULL COMMENT '团队内角色/岗位',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`group_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='团队-团队成员表';

-- ---------------------------------------------------------------------------
-- 19. 团队任务/演练计划表
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `team_task`;
CREATE TABLE `team_task` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `group_id`    BIGINT UNSIGNED NOT NULL COMMENT '团队ID',
  `name`        VARCHAR(128)    NOT NULL COMMENT '任务/演练名称',
  `task_type`   TINYINT         NOT NULL DEFAULT 1 COMMENT '类型：1=日常实训 2=团队救援演练',
  `content`     TEXT                     DEFAULT NULL COMMENT '演练方案/任务说明',
  `start_time`  DATETIME                 DEFAULT NULL COMMENT '开始时间',
  `end_time`    DATETIME                 DEFAULT NULL COMMENT '结束时间',
  `issuer_id`   BIGINT UNSIGNED NOT NULL COMMENT '下发人ID',
  `status`      TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：0=未开始 1=进行中 2=已结束',
  `ext_json`    JSON                     DEFAULT NULL COMMENT '扩展：评分细则等',
  `create_by`   BIGINT UNSIGNED          DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`   BIGINT UNSIGNED          DEFAULT NULL COMMENT '更新人ID',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='团队-团队任务/演练计划表';

-- ---------------------------------------------------------------------------
-- 20. 团队参与记录表（打卡、参与、个人贡献评分）
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `team_record`;
CREATE TABLE `team_record` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id`      BIGINT UNSIGNED NOT NULL COMMENT '任务ID',
  `user_id`      BIGINT UNSIGNED NOT NULL COMMENT '队员ID',
  `checkin_time` DATETIME                 DEFAULT NULL COMMENT '打卡时间',
  `contribution` DECIMAL(5,2)             DEFAULT NULL COMMENT '个人贡献评分',
  `score_detail` JSON                     DEFAULT NULL COMMENT '多维度评分明细（JSON）',
  `comment`      VARCHAR(255)             DEFAULT NULL COMMENT '评语',
  `status`       TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：0=未参与 1=已打卡 2=已评分',
  `offline_flag` TINYINT         NOT NULL DEFAULT 0 COMMENT '离线标记：0=在线 1=离线补传',
  `create_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='团队-团队参与记录表';

-- ============================================================================
-- 模块五：个人档案与数据沉淀
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 21. 等级晋升台账表
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `promotion_record`;
CREATE TABLE `promotion_record` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`      BIGINT UNSIGNED NOT NULL COMMENT '队员ID',
  `from_level`   TINYINT         NOT NULL COMMENT '原等级',
  `to_level`     TINYINT         NOT NULL COMMENT '目标等级',
  `reason`       VARCHAR(255)             DEFAULT NULL COMMENT '晋升依据/达标说明',
  `status`       TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：0=待终审 1=已通过 2=已驳回',
  `apply_by`     BIGINT UNSIGNED          DEFAULT NULL COMMENT '申请人ID',
  `apply_time`   DATETIME                 DEFAULT NULL COMMENT '申请时间',
  `approve_by`   BIGINT UNSIGNED          DEFAULT NULL COMMENT '终审人ID',
  `approve_time` DATETIME                 DEFAULT NULL COMMENT '终审时间',
  `create_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='档案-等级晋升台账表';

-- ---------------------------------------------------------------------------
-- 22. 个人在岗能力档案表（全周期数据沉淀快照）
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `user_archive`;
CREATE TABLE `user_archive` (
  `id`                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`           BIGINT UNSIGNED NOT NULL COMMENT '队员ID',
  `current_level`     TINYINT         NOT NULL COMMENT '当前等级：1-5级',
  `total_train_hours` DECIMAL(10,2)   NOT NULL DEFAULT 0.00 COMMENT '累计训练时长（小时）',
  `total_checkin`     INT             NOT NULL DEFAULT 0 COMMENT '累计打卡次数',
  `total_exam`        INT             NOT NULL DEFAULT 0 COMMENT '累计考试次数',
  `weak_points`       JSON                     DEFAULT NULL COMMENT '能力短板台账（JSON）',
  `archive_json`      JSON                     DEFAULT NULL COMMENT '完整档案快照（JSON）',
  `update_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='档案-个人在岗能力档案表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 建表完成。共 22 张表，覆盖方案 7.4 的全部业务数据表。
-- ============================================================================
