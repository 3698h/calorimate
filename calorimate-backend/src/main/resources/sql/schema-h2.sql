CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(100) NOT NULL,
    `openid` VARCHAR(100),
    `avatar_url` VARCHAR(500),
    `height` DOUBLE,
    `weight` DOUBLE,
    `age` INT,
    `gender` VARCHAR(10),
    `birthday` VARCHAR(20),
    `target_calories` DOUBLE,
    `target_weight` DOUBLE,
    `activity_level` VARCHAR(20),
    `goal` VARCHAR(20),
    `vip_level` INT DEFAULT 0,
    `vip_expire_time` TIMESTAMP,
    `role` VARCHAR(20) DEFAULT 'user',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (`username`)
);

CREATE TABLE IF NOT EXISTS `food` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `category` VARCHAR(50),
    `calories` DOUBLE NOT NULL,
    `protein` DOUBLE,
    `fat` DOUBLE,
    `carbs` DOUBLE,
    `unit` VARCHAR(20) DEFAULT '份',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `diet_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `food_id` BIGINT NOT NULL,
    `meal_type` VARCHAR(20) NOT NULL,
    `servings` DOUBLE DEFAULT 1,
    `calories` DOUBLE NOT NULL,
    `log_date` DATE NOT NULL,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_diet_log_user_date ON diet_log(user_id, log_date);
