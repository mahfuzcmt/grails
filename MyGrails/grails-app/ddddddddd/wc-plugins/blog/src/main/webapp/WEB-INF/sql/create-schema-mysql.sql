CREATE TABLE IF NOT EXISTS `blog_category` (  `id` bigint(20) NOT NULL AUTO_INCREMENT,  `is_in_trash` tinyint(1) NOT NULL,  `created` datetime(6) NOT NULL,  `base_url` varchar(255) DEFAULT NULL,  `updated` datetime(6) NOT NULL,  `is_disposable` tinyint(1) NOT NULL,  `image` varchar(255) DEFAULT NULL,  `url` varchar(100) NOT NULL,  `name` varchar(100) NOT NULL,  `cloud_config_id` bigint(20) DEFAULT NULL,  `description` varchar(2000) DEFAULT NULL,  PRIMARY KEY (`id`),  UNIQUE KEY `UK_1n7aoi389dbrrg6ml32agpo4w` (`url`),  UNIQUE KEY `UK_5mr0vev5nyhxsdx8gv42mwbqq` (`name`),  KEY `FK25l8o33d7idpqry8w6a7k0bxt` (`cloud_config_id`),  CONSTRAINT `FK25l8o33d7idpqry8w6a7k0bxt` FOREIGN KEY (`cloud_config_id`) REFERENCES `cloud_config` (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `blog_post` (  `id` bigint(20) NOT NULL AUTO_INCREMENT,  `visibility` varchar(255) NOT NULL,  `is_in_trash` tinyint(1) NOT NULL,  `date` datetime(6) NOT NULL,  `visible_to` varchar(255) DEFAULT NULL,  `content` longtext,  `author_id` bigint(20) DEFAULT NULL,  `created` datetime(6) NOT NULL,  `base_url` varchar(255) DEFAULT NULL,  `updated` datetime(6) NOT NULL,  `is_published` tinyint(1) NOT NULL,  `is_disposable` tinyint(1) NOT NULL,  `image` varchar(255) DEFAULT NULL,  `url` varchar(100) NOT NULL,  `name` varchar(100) NOT NULL,  `cloud_config_id` bigint(20) DEFAULT NULL,  PRIMARY KEY (`id`),  UNIQUE KEY `UK_b7mf3v2h8rmnl8tfryr1h9167` (`url`),  UNIQUE KEY `UK_kxp5hcbcfcm7lenpslklfyb98` (`name`),  KEY `FK8o1lnl885e9c2fjkxqvp35vx5` (`author_id`),  KEY `FKgiv6gcspcg6gtjtdbpvdwqaaw` (`cloud_config_id`),  CONSTRAINT `FKgiv6gcspcg6gtjtdbpvdwqaaw` FOREIGN KEY (`cloud_config_id`) REFERENCES `cloud_config` (`id`),  CONSTRAINT `FK8o1lnl885e9c2fjkxqvp35vx5` FOREIGN KEY (`author_id`) REFERENCES `operator` (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `blog_category_posts` (  `blog_post_id` bigint(20) NOT NULL,  `blog_category_id` bigint(20) NOT NULL,  KEY `FK2mh3oykprr0j02vh9y5shwaqg` (`blog_category_id`),  KEY `FKj5yfu69qwu24f1f1ib1a6jdfp` (`blog_post_id`),  CONSTRAINT `FKj5yfu69qwu24f1f1ib1a6jdfp` FOREIGN KEY (`blog_post_id`) REFERENCES `blog_post` (`id`),  CONSTRAINT `FK2mh3oykprr0j02vh9y5shwaqg` FOREIGN KEY (`blog_category_id`) REFERENCES `blog_category` (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `blog_comment` (  `id` bigint(20) NOT NULL AUTO_INCREMENT,  `created` datetime(6) NOT NULL,  `updated` datetime(6) NOT NULL,  `is_spam` tinyint(1) NOT NULL,  `name` varchar(100) DEFAULT NULL,  `post_id` bigint(20) NOT NULL,  `content` varchar(1000) NOT NULL,  `status` varchar(255) NOT NULL,  `email` varchar(50) DEFAULT NULL,  PRIMARY KEY (`id`),  KEY `FKeh1bvld0i4iq1rnw951g518l8` (`post_id`),  CONSTRAINT `FKeh1bvld0i4iq1rnw951g518l8` FOREIGN KEY (`post_id`) REFERENCES `blog_post` (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `blog_post_customer` (  `blog_post_customers_id` bigint(20) DEFAULT NULL,  `customer_id` bigint(20) DEFAULT NULL,  KEY `FK9qk28sfr5tb40nrsu6nhp14n` (`customer_id`),  KEY `FK8dmy15a5h0upm186b3gwsi2bm` (`blog_post_customers_id`),  CONSTRAINT `FK8dmy15a5h0upm186b3gwsi2bm` FOREIGN KEY (`blog_post_customers_id`) REFERENCES `blog_post` (`id`),  CONSTRAINT `FK9qk28sfr5tb40nrsu6nhp14n` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `blog_post_customer_group` (  `blog_post_groups_id` bigint(20) DEFAULT NULL,  `customer_group_id` bigint(20) DEFAULT NULL,  KEY `FK508ntdjrpn5e51kw09s4rqfcj` (`customer_group_id`),  KEY `FK5r7rmd0y9fy83gqhtibefweds` (`blog_post_groups_id`),  CONSTRAINT `FK5r7rmd0y9fy83gqhtibefweds` FOREIGN KEY (`blog_post_groups_id`) REFERENCES `blog_post` (`id`),  CONSTRAINT `FK508ntdjrpn5e51kw09s4rqfcj` FOREIGN KEY (`customer_group_id`) REFERENCES `customer_group` (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `blog_post_meta_tag` (  `blog_post_meta_tags_id` bigint(20) DEFAULT NULL,  `meta_tag_id` bigint(20) DEFAULT NULL,  KEY `FKlt1o08hxhsfifqstdemound77` (`meta_tag_id`),  KEY `FKbgs299ggvech9icuihguxylpj` (`blog_post_meta_tags_id`),  CONSTRAINT `FKbgs299ggvech9icuihguxylpj` FOREIGN KEY (`blog_post_meta_tags_id`) REFERENCES `blog_post` (`id`),  CONSTRAINT `FKlt1o08hxhsfifqstdemound77` FOREIGN KEY (`meta_tag_id`) REFERENCES `meta_tag` (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;