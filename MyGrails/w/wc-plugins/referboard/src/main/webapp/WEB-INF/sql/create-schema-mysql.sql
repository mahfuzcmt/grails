CREATE TABLE IF NOT EXISTS `referboard_tracking_info` (  `id` bigint(20) NOT NULL AUTO_INCREMENT,  `json_data` longtext NOT NULL,  `order_id` bigint(20) NOT NULL,  PRIMARY KEY (`id`),  UNIQUE KEY `UK_dnf1xs3rfteefv3w2ws8imsgq` (`order_id`),  CONSTRAINT `FK15udbfralku1ujf76nb15q010` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;