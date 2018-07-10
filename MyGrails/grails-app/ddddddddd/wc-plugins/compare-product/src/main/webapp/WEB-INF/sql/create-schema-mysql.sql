CREATE TABLE IF NOT EXISTS `custom_properties` (  `id` bigint(20) NOT NULL AUTO_INCREMENT,  `product_id` bigint(20) NOT NULL,  `idx` bigint(20) NOT NULL,  `label` varchar(255) NOT NULL,  `description` longtext NOT NULL,  PRIMARY KEY (`id`),  UNIQUE KEY `UKf1d642d8f7af8340663a31f255dc` (`product_id`,`label`),  CONSTRAINT `FKapf5vup2rewcaokq1kqjotw1x` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;