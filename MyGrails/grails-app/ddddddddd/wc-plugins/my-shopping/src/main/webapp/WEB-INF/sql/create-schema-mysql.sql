CREATE TABLE IF NOT EXISTS `my_shopping_mapping` (  `id` bigint(20) NOT NULL AUTO_INCREMENT,  `my_shopping_category` varchar(255) NOT NULL,  `path` varchar(255) NOT NULL,  `category_id` bigint(20) NOT NULL,  PRIMARY KEY (`id`),  UNIQUE KEY `UK_clq3tpna86eh9g1ljg0prbx0t` (`category_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;