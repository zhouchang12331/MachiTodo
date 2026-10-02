-- ===================================================================
-- MachiTodo 麻薯待办 · 数据库初始化脚本
-- 在 MySQL 客户端（Navicat / 命令行 / IDEA Database）里直接执行本文件即可
-- ===================================================================

-- 1. 建库：字符集用 utf8mb4，才能正常存中文和 emoji
CREATE DATABASE IF NOT EXISTS machi_todo
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE machi_todo;

-- 2. 建表
DROP TABLE IF EXISTS task;
CREATE TABLE task (
    id       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
    content  VARCHAR(255) NOT NULL                COMMENT '任务文本',
    is_done  TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '是否完成：0 未完成，1 已完成',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '待办任务表';

-- 3. 插几条示例数据，方便第一次打开页面就能看到效果（可删）
INSERT INTO task (content, is_done) VALUES
    ('给猫换水', 0),
    ('写今天的复盘', 0),
    ('喝一杯草莓拿铁', 1);
