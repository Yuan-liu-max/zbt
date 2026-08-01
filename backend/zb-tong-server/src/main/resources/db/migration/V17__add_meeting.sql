-- V17__add_meeting.sql — 晨夕会表
DROP TABLE IF EXISTS `employee_meeting`;

CREATE TABLE `employee_meeting` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `topic`        VARCHAR(200) NULL,
    `type`         VARCHAR(20)  NULL COMMENT 'regular|temporary',
    `meeting_date` VARCHAR(20)  NULL,
    `host`         VARCHAR(50)  NULL,
    `participants` VARCHAR(500) NULL,
    `status`       VARCHAR(20)  NULL COMMENT 'ongoing|ended|cancelled',
    `is_deleted`   TINYINT(1)   NOT NULL DEFAULT 0,
    `created_at`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
