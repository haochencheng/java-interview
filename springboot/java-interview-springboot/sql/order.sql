CREATE DATABASE if not exists `order` DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `sku`
(
    `id` INT(11) AUTO_INCREMENT,
    `name` varchar(100) NOT NULL ,
    `inventory` INT(11) NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `order`
(
    `id` INT(11)  AUTO_INCREMENT,
    `sku_id` INT(11) NOT NULL,
    `user_id` INT(11) NOT NULL,
    `order_no` VARCHAR(50) NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

-- 测试数据
INSERT INTO `order`.sku (id, inventory, create_time, name) VALUES
(1, 100, '2020-07-16 00:57:16', '测试商品1');

-- 更新库存
update sku set inventory=2000 where id = 1;
