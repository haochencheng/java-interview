CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `age` int(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `index_id_name` (`id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;