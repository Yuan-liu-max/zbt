-- V43: 添加 token_version 字段，用于强制下线（token版本号机制）
ALTER TABLE sys_user ADD COLUMN token_version INT NOT NULL DEFAULT 0 COMMENT '令牌版本号，强制下线时+1';
