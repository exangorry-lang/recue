-- ============================================================================
-- 上海救助基地潜水救生员训练系统 — 一期补充表（新增功能叠加，不改动已有表结构）
-- 数据库：rescue_training
-- 说明：以下为「通知公告」「团队演练复盘/问题台账」新增表，叠加开发不影响历史数据。
-- ============================================================================

USE `rescue_training`;

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------------
-- 23. 通知公告表
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title`        VARCHAR(128)    NOT NULL COMMENT '标题',
  `content`      TEXT                     DEFAULT NULL COMMENT '内容',
  `notice_type`  TINYINT         NOT NULL DEFAULT 1 COMMENT '类型：1=通知 2=公告',
  `status`       TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1=已发布 0=草稿',
  `publish_time` DATETIME                 DEFAULT NULL COMMENT '发布时间',
  `create_by`    BIGINT UNSIGNED          DEFAULT NULL COMMENT '创建人ID',
  `create_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`    BIGINT UNSIGNED          DEFAULT NULL COMMENT '更新人ID',
  `update_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='通知公告表';

-- ---------------------------------------------------------------------------
-- 24. 团队演练复盘/问题台账表
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `team_review`;
CREATE TABLE `team_review` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id`      BIGINT UNSIGNED NOT NULL COMMENT '团队任务ID',
  `reviewer_id`  BIGINT UNSIGNED          DEFAULT NULL COMMENT '复盘人ID',
  `summary`      TEXT                     DEFAULT NULL COMMENT '复盘总结',
  `problems`     TEXT                     DEFAULT NULL COMMENT '问题台账',
  `improvements` TEXT                     DEFAULT NULL COMMENT '改进措施',
  `review_time`  DATETIME                 DEFAULT NULL COMMENT '复盘时间',
  `create_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='团队演练复盘/问题台账表';
