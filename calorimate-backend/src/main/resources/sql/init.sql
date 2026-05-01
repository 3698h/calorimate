CREATE DATABASE IF NOT EXISTS calorimate DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE calorimate;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    `height` DOUBLE DEFAULT NULL COMMENT '身高(cm)',
    `weight` DOUBLE DEFAULT NULL COMMENT '体重(kg)',
    `age` INT DEFAULT NULL COMMENT '年龄',
    `gender` VARCHAR(10) DEFAULT NULL COMMENT '性别(male/female)',
    `target_calories` DOUBLE DEFAULT 2000 COMMENT '每日目标卡路里',
    `role` VARCHAR(20) DEFAULT 'user' COMMENT '角色(user/admin)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

DROP TABLE IF EXISTS `food`;
CREATE TABLE `food` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '食物ID',
    `name` VARCHAR(100) NOT NULL COMMENT '食物名称',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '食物分类',
    `calories` DOUBLE NOT NULL COMMENT '卡路里(kcal)',
    `protein` DOUBLE DEFAULT NULL COMMENT '蛋白质(g)',
    `fat` DOUBLE DEFAULT NULL COMMENT '脂肪(g)',
    `carbs` DOUBLE DEFAULT NULL COMMENT '碳水化合物(g)',
    `unit` VARCHAR(20) DEFAULT '份' COMMENT '计量单位',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食物表';

DROP TABLE IF EXISTS `diet_log`;
CREATE TABLE `diet_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `food_id` BIGINT NOT NULL COMMENT '食物ID',
    `meal_type` VARCHAR(20) NOT NULL COMMENT '餐次(breakfast/lunch/dinner/snack)',
    `servings` DOUBLE DEFAULT 1 COMMENT '份数',
    `calories` DOUBLE NOT NULL COMMENT '摄入卡路里',
    `log_date` DATE NOT NULL COMMENT '记录日期',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_date` (`user_id`, `log_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮食记录表';

INSERT INTO `food` (`name`, `category`, `calories`, `protein`, `fat`, `carbs`, `unit`) VALUES
('米饭', '主食', 116, 2.6, 0.3, 25.6, '100g'),
('面条', '主食', 137, 4.0, 0.5, 28.0, '100g'),
('馒头', '主食', 221, 7.0, 1.1, 47.0, '100g'),
('鸡蛋', '蛋类', 144, 13.3, 8.8, 2.8, '100g'),
('鸡胸肉', '肉类', 133, 31.0, 1.2, 0.5, '100g'),
('猪肉', '肉类', 242, 13.2, 20.5, 1.5, '100g'),
('牛肉', '肉类', 190, 26.0, 8.0, 0, '100g'),
('苹果', '水果', 53, 0.2, 0.2, 13.5, '100g'),
('香蕉', '水果', 89, 1.1, 0.3, 22.8, '100g'),
('牛奶', '饮品', 66, 3.3, 3.6, 5.0, '100ml'),
('可乐', '饮品', 42, 0, 0, 10.6, '100ml'),
('西兰花', '蔬菜', 36, 4.1, 0.6, 4.3, '100g'),
('番茄', '蔬菜', 18, 0.9, 0.2, 3.9, '100g'),
('豆腐', '豆制品', 81, 8.1, 3.7, 4.2, '100g'),
('鱼肉', '海鲜', 104, 18.0, 3.0, 0, '100g'),
('虾', '海鲜', 87, 18.6, 0.8, 0, '100g'),
('玉米', '主食', 112, 4.0, 1.2, 22.8, '100g'),
('红薯', '主食', 86, 1.6, 0.1, 20.1, '100g'),
('酸奶', '饮品', 72, 3.4, 2.7, 9.3, '100g'),
('面包', '主食', 266, 8.3, 1.7, 50.6, '100g');

INSERT INTO `user` (`username`, `password`, `height`, `weight`, `age`, `gender`, `target_calories`, `role`) VALUES
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PsLJfGqKCQ1FHVCKGO2Hq', 175, 70, 28, 'male', 2000, 'admin');
