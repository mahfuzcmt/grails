CREATE TABLE IF NOT EXISTS `tenant_info` (
  `name` varchar(200) NOT NULL,
  `db_name` varchar(300) NOT NULL,
  `username` varchar(300) NOT NULL,
  `password` varchar(300) NOT NULL,
  `host` varchar(200) NOT NULL,
  `server` varchar(45) NOT NULL DEFAULT 'mysql',
  `id` varchar(20) NOT NULL DEFAULT '00000000',
  `active` BIT(1) NOT NULL DEFAULT b'1',
  PRIMARY KEY (`name`,`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `db_properties` (
  `tenant_name` varchar(200) NOT NULL,
  `value` varchar(300) NOT NULL,
  `key` varchar(300) NOT NULL,
  KEY `FK__instance_info` (`tenant_name`),
  CONSTRAINT `FK__instance_info` FOREIGN KEY (`tenant_name`) REFERENCES `tenant_info` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tenant_configs` (
  `tenant_name` varchar(200) NOT NULL,
  `value` varchar(300) NOT NULL,
  `key` varchar(300) NOT NULL,
  KEY `FK__instance_info_2` (`tenant_name`),
  CONSTRAINT `FK__instance_info_2` FOREIGN KEY (`tenant_name`) REFERENCES `tenant_info` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tenant_alias` (
	`tenant_name` VARCHAR(200) NOT NULL,
	`alias` VARCHAR(300) NOT NULL,
	CONSTRAINT `FK__tenant_info` FOREIGN KEY (`tenant_name`) REFERENCES `tenant_info` (`name`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;