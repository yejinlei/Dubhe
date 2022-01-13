INSERT INTO `menu` VALUES (1085, 0, 0, '自动机器学习', 'jinhangzhongshiyan', 'tadl', NULL, NULL, NULL, 'TADL', b'0', b'0', 35, 1, 3, '2021-03-31 08:09:20', '2021-09-14 10:18:05', b'0', NULL, NULL);
INSERT INTO `menu` VALUES (1086, 1085, 1, '实验详情', NULL, 'experiment/:experimentId', 'tadl/detail', 'ExperimentDetail', 'DetailLayout', NULL, b'1', b'0', 999, 1, 1, '2021-03-31 08:23:56', '2021-03-31 08:36:36', b'0', NULL, '{}');
INSERT INTO `menu` VALUES (1087, 1085, 1, '实验管理', NULL, 'list', 'tadl/list/index', 'TadlList', 'BaseLayout', 'tadl', b'0', b'0', 36, 3, 14, '2021-03-31 09:51:04', '2021-09-14 10:18:29', b'0', NULL, '{}');
INSERT INTO `menu` VALUES (1088, 1085, 1, 'TadlForm', NULL, 'form', 'tadl/formPage', 'TadlForm', 'SubpageLayout', 'tadl', b'1', b'0', 999, 3, 3, '2021-03-31 09:51:59', '2021-03-31 09:52:04', b'0', NULL, '{}');
INSERT INTO `menu` VALUES (1089, 1085, 1, '搜索策略', 'zoom', 'searchstrategy', 'tadl/strategy/index', 'SearchStrategy', 'BaseLayout', 'tadl', b'0', b'0', 37, 14, 14, '2021-03-31 10:40:50', '2021-03-31 11:39:59', b'0', NULL, '{}');
UPDATE menu SET menu.name ='云端部署' WHERE menu.name = '云端Serving'
INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (9, '自动机器学习', '4', 5);

create table if not exists `tadl_algorithm` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
	`name` VARCHAR(64) NOT NULL COMMENT '算法名称' COLLATE 'utf8_general_ci',
	`model_type` INT(11) NOT NULL COMMENT '模型类别',
	`algorithm_version_id` BIGINT(20) NULL DEFAULT NULL COMMENT '算法版本id',
	`description` VARCHAR(255) NULL DEFAULT NULL COMMENT '算法描述' COLLATE 'utf8_general_ci',
	`create_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '创建人',
	`update_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '更新人',
	`create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`deleted` BIT(1) NOT NULL DEFAULT 'b\'0\'' COMMENT '删除(0：正常，1：删除)',
	`update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	`default_metric` VARCHAR(64) NOT NULL COMMENT '默认主要指标' COLLATE 'utf8_general_ci',
	`one_shot` BIT(1) NOT NULL DEFAULT 'b\'0\'' COMMENT '是否oneshot',
	`algorithm_type` VARCHAR(50) NOT NULL COMMENT '算法类型' COLLATE 'utf8_general_ci',
	`platform` VARCHAR(64) NOT NULL COMMENT '算法框架' COLLATE 'utf8_general_ci',
	`platform_version` VARCHAR(64) NOT NULL COMMENT '算法框架版本' COLLATE 'utf8_general_ci',
	`gpu` BIT(1) NULL DEFAULT 'b\'0\'' COMMENT '是否支持gpu计算（0支持，1不支持）',
	PRIMARY KEY (`id`) USING BTREE
)
COMMENT='tadl 算法表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;

create table if not exists  `tadl_algorithm_stage` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '算法阶段id',
	`name` VARCHAR(64) NOT NULL COMMENT '阶段名称' COLLATE 'utf8_general_ci',
	`stage_order` TINYINT(4) NOT NULL COMMENT '阶段排序',
	`algorithm_id` BIGINT(20) NULL DEFAULT NULL COMMENT '算法id',
	`algorithm_version_id` BIGINT(20) NULL DEFAULT NULL COMMENT '算法版本',
	`dataset_name` VARCHAR(64) NULL DEFAULT NULL COMMENT '数据集ID' COLLATE 'utf8_general_ci',
	`dataset_id` BIGINT(20) NULL DEFAULT NULL COMMENT '数据集id',
	`dataset_version` VARCHAR(64) NULL DEFAULT NULL COMMENT '数据集版本id' COLLATE 'utf8_general_ci',
	`dataset_path` VARCHAR(255) NOT NULL COMMENT '数据集路径' COLLATE 'utf8_general_ci',
	`python_version` VARCHAR(64) NULL DEFAULT NULL COMMENT 'command命令所使用的python环境' COLLATE 'utf8_general_ci',
	`execute_script` VARCHAR(64) NULL DEFAULT NULL COMMENT 'command命令所使用py文件' COLLATE 'utf8_general_ci',
	`multi_gpu` BIT(1) NOT NULL DEFAULT 'b\'0\'' COMMENT '是否支持多卡训练（0支持，1不支持）',
	`max_trial_num` INT(11) NOT NULL COMMENT '默认最大运行次数',
	`max_exec_duration` DECIMAL(20,4) NOT NULL COMMENT '当前阶段默认最大执行时间',
	`trial_concurrent_num` INT(11) NOT NULL COMMENT 'trial默认并发数量',
	`max_exec_duration_unit` VARCHAR(64) NULL DEFAULT NULL COMMENT '最大运行时间单位\n年（y）\n月（m）\n周（w）\n日（d）\n小时（h）\n分钟（min）\n秒（s）\n毫秒（ms）\n微秒（us）\n纳秒（ns）\n皮秒（ps）\n飞秒（fs）' COLLATE 'utf8_general_ci',
	`create_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '创建人',
	`create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '更新人',
	`update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	`deleted` BIT(1) NOT NULL COMMENT '是否删除（0正常，1删除）',
	PRIMARY KEY (`id`) USING BTREE
)
COMMENT='tadl算法阶段表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;
create table if not exists  `tadl_algorithm_version` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '算法版本id',
	`algorithm_id` BIGINT(20) NULL DEFAULT NULL COMMENT '算法id',
	`version_name` VARCHAR(64) NULL DEFAULT NULL COMMENT '版本名称' COLLATE 'utf8_general_ci',
	`description` VARCHAR(255) NULL DEFAULT NULL COMMENT '版本说明' COLLATE 'utf8_general_ci',
	`version_source` VARCHAR(64) NULL DEFAULT NULL COMMENT '版本来源' COLLATE 'utf8_general_ci',
	`data_conversion` INT(11) NOT NULL DEFAULT '0',
	`create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`deleted` BIT(1) NOT NULL DEFAULT 'b\'0\'' COMMENT '是否删除(0正常，1删除)',
	`create_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '创建人',
	`update_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '更新人',
	`update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`) USING BTREE
)
COMMENT='tadl 算法版本表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;
create table if not exists  `tadl_experiment` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
	`name` VARCHAR(64) NOT NULL COMMENT '实验名称' COLLATE 'utf8_general_ci',
	`description` VARCHAR(255) NULL DEFAULT NULL COMMENT '实验描述' COLLATE 'utf8_general_ci',
	`algorithm_id` BIGINT(20) NOT NULL COMMENT '算法id',
	`algorithm_version_id` BIGINT(20) NOT NULL COMMENT '算法版本id',
	`model_type` INT(11) NULL DEFAULT NULL COMMENT '模型类型',
	`status` INT(11) NOT NULL COMMENT '实验状态：（\n待运行：101，\n等待中：102，\n运行中：103，\n已暂停：104，\n已完成：202，\n运行失败：203\n）',
	`status_detail` JSON NULL DEFAULT NULL COMMENT '状态详情',
	`update_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '更新人',
	`create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	`start_time` TIMESTAMP NULL DEFAULT NULL COMMENT '启动时间',
	`end_time` TIMESTAMP NULL DEFAULT NULL COMMENT '结束时间',
	`deleted` BIT(1) NULL DEFAULT 'b\'0\'' COMMENT '是否删除（0正常，1删除）',
	`create_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '创建人',
	PRIMARY KEY (`id`) USING BTREE,
	INDEX `tadl_experiment_name` (`name`) USING BTREE
)
COMMENT='tadl实验表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;
create table if not exists  `tadl_experiment_stage` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
	`experiment_id` BIGINT(20) NOT NULL COMMENT '实验id',
	`algorithm_stage_id` VARCHAR(64) NOT NULL COMMENT '算法阶段ID' COLLATE 'utf8_general_ci',
	`stage_name` VARCHAR(64) NULL DEFAULT NULL COMMENT '阶段名称' COLLATE 'utf8_general_ci',
	`stage_order` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '阶段在实验中所处的先后顺序',
	`resource_id` INT(11) NULL DEFAULT NULL COMMENT '实验资源配置id',
	`resource_Name` VARCHAR(255) NULL DEFAULT NULL COMMENT '实验资源值' COLLATE 'utf8_general_ci',
	`max_trial_num` INT(11) NOT NULL COMMENT '最大trail次数',
	`trial_concurrent_num` INT(11) NOT NULL COMMENT 'trail并发数量',
	`max_exec_duration` DECIMAL(20,4) NOT NULL DEFAULT '0.0000' COMMENT '最大运行时间',
	`status` INT(11) NULL DEFAULT NULL COMMENT '实验阶段状态状态 (101:待运行，102：运行中，201：已完成，202：运行失败)',
	`update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	`create_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '创建人',
	`update_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '更新人',
	`create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`deleted` BIT(1) NOT NULL DEFAULT 'b\'0\'' COMMENT '是否删除（0正常，1删除）',
	`start_time` TIMESTAMP NULL DEFAULT NULL COMMENT '启动时间',
	`begin_time` TIMESTAMP NULL DEFAULT NULL COMMENT '每次开始运行的时间',
	`end_time` TIMESTAMP NULL DEFAULT NULL COMMENT '结束时间',
	`max_exec_duration_unit` VARCHAR(64) NULL DEFAULT NULL COMMENT '最大运行时间单位\n年（y）\n月（m）\n周（w）\n日（d）\n小时（h）\n分钟（min）\n秒（s）\n毫秒（ms）\n微秒（us）\n纳秒（ns）\n皮秒（ps）\n飞秒（fs）' COLLATE 'utf8_general_ci',
	`run_time` BIGINT(32) NULL DEFAULT '0' COMMENT '暂停前已经运行的时间',
	PRIMARY KEY (`id`) USING BTREE,
	INDEX `tadl_experiment_stage_experiment_id_stage_order_index` (`experiment_id`, `stage_order`) USING BTREE
)
COMMENT='tadl实验阶段表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;
create table if not exists  `tadl_trial` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
	`experiment_id` BIGINT(20) NOT NULL COMMENT '试验id',
	`stage_id` VARCHAR(64) NULL DEFAULT NULL COMMENT '实验阶段ID' COLLATE 'latin1_swedish_ci',
	`name` VARCHAR(64) NOT NULL COMMENT 'trial名称' COLLATE 'latin1_swedish_ci',
	`start_time` TIMESTAMP NULL DEFAULT NULL COMMENT '开始时间',
	`end_time` TIMESTAMP NULL DEFAULT NULL COMMENT '结束时间',
	`status` INT(11) NOT NULL COMMENT 'trial状态 (101:待运行，102：等待中，103：运行中，201：已完成，202：运行失败，203：未知)',
	`resource_name` VARCHAR(64) NULL DEFAULT NULL COMMENT 'k8s实验资源值' COLLATE 'latin1_swedish_ci',
	`update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	`create_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '创建人',
	`update_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '更新人',
	`create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`deleted` BIT(1) NOT NULL DEFAULT 'b\'0\'' COMMENT '是否删除(0正常，1删除)',
	`sequence` INT(11) NOT NULL COMMENT '顺序',
	PRIMARY KEY (`id`) USING BTREE
)
COMMENT='tadl 实验详情表'
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;
create table if not exists  `tadl_trial_data` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
	`experiment_id` BIGINT(20) NOT NULL COMMENT '实验id',
	`stage_id` BIGINT(20) NOT NULL COMMENT '实验阶段ID',
	`trial_id` BIGINT(20) NOT NULL COMMENT 'trial id',
	`type` VARCHAR(64) NULL DEFAULT NULL COMMENT '指标类型' COLLATE 'utf8_general_ci',
	`sequence` INT(11) NULL DEFAULT NULL COMMENT '序列',
	`category` VARCHAR(64) NULL DEFAULT NULL COMMENT '类别' COLLATE 'utf8_general_ci',
	`value` DECIMAL(17,14) NULL DEFAULT NULL COMMENT '最优数据',
	`create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
	`create_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '创建人',
	`update_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '更新人',
	`deleted` BIT(1) NOT NULL DEFAULT 'b\'0\'' COMMENT '是否删除（0正常，1删除）',
	PRIMARY KEY (`id`) USING BTREE
)
COMMENT='tadl trial 运行结果表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;