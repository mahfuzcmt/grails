CREATE TABLE IF NOT EXISTS `form_field` (  `id` bigint(20) NOT NULL AUTO_INCREMENT,  `uuid` varchar(255) NOT NULL,  `validation` varchar(255) DEFAULT NULL,  `placeholder` varchar(255) DEFAULT NULL,  `title` varchar(100) DEFAULT NULL,  `name` varchar(100) NOT NULL,  `value` varchar(500) DEFAULT NULL,  `type` varchar(255) NOT NULL,  `label` varchar(255) DEFAULT NULL,  `clazz` varchar(100) DEFAULT NULL,  PRIMARY KEY (`id`),  UNIQUE KEY `UK_tg7bganr9vgimu11s8fuep434` (`uuid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `field_condition` (  `id` bigint(20) NOT NULL AUTO_INCREMENT,  `target_option` varchar(255) NOT NULL,  `dependent_fielduuid` varchar(255) NOT NULL,  `action` varchar(255) NOT NULL,  `form_field_id` bigint(20) NOT NULL,  PRIMARY KEY (`id`),  KEY `FKaxoebj4yeiw07qc2r5ba0e1eu` (`form_field_id`),  CONSTRAINT `FKaxoebj4yeiw07qc2r5ba0e1eu` FOREIGN KEY (`form_field_id`) REFERENCES `form_field` (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `form` (  `id` bigint(20) NOT NULL AUTO_INCREMENT,  `is_in_trash` tinyint(1) NOT NULL,  `success_message` varchar(500) DEFAULT NULL,  `email_subject` varchar(255) DEFAULT NULL,  `email_bcc` varchar(255) DEFAULT NULL,  `term_condition_text` varchar(500) DEFAULT NULL,  `term_condition` varchar(500) DEFAULT NULL,  `email_to` varchar(255) DEFAULT NULL,  `is_term_condition_text_enabled` tinyint(1) NOT NULL,  `created` datetime(6) NOT NULL,  `updated` datetime(6) NOT NULL,  `email_cc` varchar(255) DEFAULT NULL,  `failure_message` varchar(500) DEFAULT NULL,  `is_disposable` tinyint(1) NOT NULL,  `sender_email_fielduuid` varchar(255) DEFAULT NULL,  `is_term_condition_enabled` tinyint(1) NOT NULL,  `reset_enabled` tinyint(1) NOT NULL,  `after_handler` varchar(1000) DEFAULT NULL,  `name` varchar(255) NOT NULL,  `submission_count` int(11) NOT NULL,  `action_url` varchar(500) DEFAULT NULL,  `action_type` varchar(255) NOT NULL,  `use_captcha` tinyint(1) NOT NULL,  `before_handler` varchar(1000) DEFAULT NULL,  `clazz` varchar(255) DEFAULT NULL,  `submit_button_label` varchar(255) NOT NULL,  PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `form_extra_prop` (  `id` bigint(20) NOT NULL AUTO_INCREMENT,  `value` varchar(255) NOT NULL,  `type` varchar(255) NOT NULL,  `label` varchar(100) DEFAULT NULL,  `extra_value` varchar(255) DEFAULT NULL,  PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `form_field_form_extra_prop` (  `form_field_extras_id` bigint(20) DEFAULT NULL,  `form_extra_prop_id` bigint(20) DEFAULT NULL,  KEY `FK8sulwtn255wbum833al0738dr` (`form_extra_prop_id`),  KEY `FK29rwt43y7u8cdabxtsjj9tai2` (`form_field_extras_id`),  CONSTRAINT `FK29rwt43y7u8cdabxtsjj9tai2` FOREIGN KEY (`form_field_extras_id`) REFERENCES `form_field` (`id`),  CONSTRAINT `FK8sulwtn255wbum833al0738dr` FOREIGN KEY (`form_extra_prop_id`) REFERENCES `form_extra_prop` (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `form_field_form_field` (  `form_field_fields_id` bigint(20) DEFAULT NULL,  `form_field_id` bigint(20) DEFAULT NULL,  KEY `FK3thne6r37vc6cuhx06jmjw57e` (`form_field_id`),  KEY `FK75mj7rr3luifrymtpy8y0ln3u` (`form_field_fields_id`),  CONSTRAINT `FK75mj7rr3luifrymtpy8y0ln3u` FOREIGN KEY (`form_field_fields_id`) REFERENCES `form_field` (`id`),  CONSTRAINT `FK3thne6r37vc6cuhx06jmjw57e` FOREIGN KEY (`form_field_id`) REFERENCES `form_field` (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `form_form_field` (  `form_fields_id` bigint(20) DEFAULT NULL,  `form_field_id` bigint(20) DEFAULT NULL,  KEY `FKo88yts7mhgjdtahvkuik3n8sc` (`form_field_id`),  KEY `FKsyu8gie0sjv89i5akxlbbn597` (`form_fields_id`),  CONSTRAINT `FKsyu8gie0sjv89i5akxlbbn597` FOREIGN KEY (`form_fields_id`) REFERENCES `form` (`id`),  CONSTRAINT `FKo88yts7mhgjdtahvkuik3n8sc` FOREIGN KEY (`form_field_id`) REFERENCES `form_field` (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `form_submission` (  `id` bigint(20) NOT NULL AUTO_INCREMENT,  `form_id` bigint(20) NOT NULL,  `ip` varchar(255) NOT NULL,  `submitted` datetime(6) NOT NULL,  PRIMARY KEY (`id`),  KEY `FKs1mua9sngjjjxss662bpwpoe9` (`form_id`),  CONSTRAINT `FKs1mua9sngjjjxss662bpwpoe9` FOREIGN KEY (`form_id`) REFERENCES `form` (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `form_submission_data` (  `id` bigint(20) NOT NULL AUTO_INCREMENT,  `form_submission_id` bigint(20) NOT NULL,  `is_file` tinyint(1) NOT NULL,  `field_value` longtext,  `field_name` varchar(200) NOT NULL,  `submitted_data_list_idx` int(11) DEFAULT NULL,  PRIMARY KEY (`id`),  KEY `FKcx27uo10ctvwas50ts9mn4sqa` (`form_submission_id`),  CONSTRAINT `FKcx27uo10ctvwas50ts9mn4sqa` FOREIGN KEY (`form_submission_id`) REFERENCES `form_submission` (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;