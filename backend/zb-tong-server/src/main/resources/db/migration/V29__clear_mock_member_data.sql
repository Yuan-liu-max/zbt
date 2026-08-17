-- V29__clear_mock_member_data.sql — 清除会员等级表中的mock假数据
UPDATE `member_level` SET `member_count` = 0, `total_consumption` = 0;
