INSERT INTO `menu` VALUES (1085, 0, 0, '自动机器学习', 'jinhangzhongshiyan', 'tadl', NULL, NULL, NULL, 'TADL', b'0', b'0', 35, 1, 3, '2021-03-31 08:09:20', '2021-09-14 10:18:05', b'0', NULL, NULL);
INSERT INTO `menu` VALUES (1086, 1085, 1, '实验详情', NULL, 'experiment/:experimentId', 'tadl/detail', 'ExperimentDetail', 'DetailLayout', NULL, b'1', b'0', 999, 1, 1, '2021-03-31 08:23:56', '2021-03-31 08:36:36', b'0', NULL, '{}');
INSERT INTO `menu` VALUES (1087, 1085, 1, '实验管理', NULL, 'list', 'tadl/list/index', 'TadlList', 'BaseLayout', 'tadl', b'0', b'0', 36, 3, 14, '2021-03-31 09:51:04', '2021-09-14 10:18:29', b'0', NULL, '{}');
INSERT INTO `menu` VALUES (1088, 1085, 1, 'TadlForm', NULL, 'form', 'tadl/formPage', 'TadlForm', 'SubpageLayout', 'tadl', b'1', b'0', 999, 3, 3, '2021-03-31 09:51:59', '2021-03-31 09:52:04', b'0', NULL, '{}');
INSERT INTO `menu` VALUES (1089, 1085, 1, '搜索策略', 'zoom', 'searchstrategy', 'tadl/strategy/index', 'SearchStrategy', 'BaseLayout', 'tadl', b'0', b'0', 37, 14, 14, '2021-03-31 10:40:50', '2021-03-31 11:39:59', b'0', NULL, '{}');
UPDATE menu SET menu.name ='云端部署' WHERE menu.name = '云端Serving';
INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (9, '自动机器学习', '4', 5);
 ALTER TABLE `model_opt_task` add
ALTER TABLE `model_opt_task` add
  column gpu_type varchar(255) null comment 'GPU类型(例如：nvidia)' ,
add
  column gpu_model varchar(255) null comment 'GPU型号(例如：v100)',
add
  column k8s_label_key varchar(255) null comment 'k8s GPU资源标签key值(例如：nvidia.com/gpu)',
add
  column `pool_specs_info` varchar(255) null  comment '规格信息',
add
  column  `resources_pool_node` VARCHAR(255) null comment '节点个数' ,
add
  column  `resources_pool_type` TINYINT(4) null comment '节点类型(0为CPU，1为GPU)',
add
  column  `resources_pool_specs` VARCHAR(255) null comment '节点规格'  ;

 ALTER TABLE `model_opt_task_instance` add
  column gpu_type varchar(255) null comment 'GPU类型(例如：nvidia)' ,
add
  column gpu_model varchar(255) null comment 'GPU型号(例如：v100)',
add
  column k8s_label_key varchar(255) null comment 'k8s GPU资源标签key值(例如：nvidia.com/gpu)',
add
  column `pool_specs_info` varchar(255) null  comment '规格信息',
add
  column  `resources_pool_node` VARCHAR(255) null comment '节点个数' ,
add
  column  `resources_pool_type` TINYINT(4) null comment '节点类型(0为CPU，1为GPU)',
add
  column  `resources_pool_specs` VARCHAR(255) null comment '节点规格'  ;


ALTER TABLE `serving_model_config` add
  column gpu_type varchar(255) null comment 'GPU类型(例如：nvidia)' ,
add
  column gpu_model varchar(255) null comment 'GPU型号(例如：v100)',
add
  column k8s_label_key varchar(255) null comment 'k8s GPU资源标签key值(例如：nvidia.com/gpu)';


ALTER TABLE `serving_batch` add
  column gpu_type varchar(255) null comment 'GPU类型(例如：nvidia)' ,
add
  column gpu_model varchar(255) null comment 'GPU型号(例如：v100)',
add
  column k8s_label_key varchar(255) null comment 'k8s GPU资源标签key值(例如：nvidia.com/gpu)';


-- serving GPU类型，型号，k8s GPU资源标签key值初始化
 UPDATE
  `serving_model_config`
SET
  `gpu_type` = 'nvidia',
  `gpu_model` = 'v100',
  `k8s_label_key` = 'nvidia.com/gpu';
-- serving_batch GPU类型，型号，k8s GPU资源标签key值初始化
 UPDATE
  `serving_batch`
SET
  `gpu_type` = 'nvidia',
  `gpu_model` = 'v100',
  `k8s_label_key` = 'nvidia.com/gpu';

-- model_opt_task GPU类型，型号，k8s GPU资源标签key值初始化
  UPDATE
  `model_opt_task`
  SET
    `gpu_type` = 'nvidia',
    `gpu_model` = 'v100',
    `k8s_label_key` = 'nvidia.com/gpu';

-- model_opt_task_instance GPU类型，型号，k8s GPU资源标签key值初始化
UPDATE
  `model_opt_task_instance`
SET
  `gpu_type` = 'nvidia',
  `gpu_model` = 'v100',
  `k8s_label_key` = 'nvidia.com/gpu';




CREATE TABLE if not exists `tadl_algorithm` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `name` varchar(64) NOT NULL COMMENT '算法名称',
  `model_type` int(11) NOT NULL COMMENT '模型类别',
  `algorithm_version_id` bigint(20) DEFAULT NULL COMMENT '算法版本id',
  `description` varchar(255) DEFAULT NULL COMMENT '算法描述',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '删除(0：正常，1：删除)',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `default_metric` varchar(64) NOT NULL COMMENT '默认主要指标',
  `one_shot` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否oneshot',
  `algorithm_type` varchar(50) NOT NULL COMMENT '算法类型',
  `platform` varchar(64) NOT NULL COMMENT '算法框架',
  `platform_version` varchar(64) NOT NULL COMMENT '算法框架版本',
  `gpu` bit(1) DEFAULT b'0' COMMENT '是否支持gpu计算（0支持，1不支持）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='tadl 算法表';


 CREATE TABLE if not exists `tadl_algorithm_stage` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '算法阶段id',
  `name` varchar(64) NOT NULL COMMENT '阶段名称',
  `stage_order` tinyint(4) NOT NULL COMMENT '阶段排序',
  `algorithm_id` bigint(20) DEFAULT NULL COMMENT '算法id',
  `algorithm_version_id` bigint(20) DEFAULT NULL COMMENT '算法版本',
  `dataset_name` varchar(64) DEFAULT NULL COMMENT '数据集ID',
  `dataset_id` bigint(20) DEFAULT NULL COMMENT '数据集id',
  `dataset_version` varchar(64) DEFAULT NULL COMMENT '数据集版本id',
  `dataset_path` varchar(255) NOT NULL COMMENT '数据集路径',
  `python_version` varchar(64) DEFAULT NULL COMMENT 'command命令所使用的python环境',
  `execute_script` varchar(64) DEFAULT NULL COMMENT 'command命令所使用py文件',
  `multi_gpu` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否支持多卡训练（0支持，1不支持）',
  `max_trial_num` int(11) NOT NULL COMMENT '默认最大运行次数',
  `max_exec_duration` decimal(20,4) NOT NULL COMMENT '当前阶段默认最大执行时间',
  `trial_concurrent_num` int(11) NOT NULL COMMENT 'trial默认并发数量',
  `max_exec_duration_unit` varchar(64) DEFAULT NULL COMMENT '最大运行时间单位\n年（y）\n月（m）\n周（w）\n日（d）\n小时（h）\n分钟（min）\n秒（s）\n毫秒（ms）\n微秒（us）\n纳秒（ns）\n皮秒（ps）\n飞秒（fs）',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL COMMENT '是否删除（0正常，1删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='tadl算法阶段表';


 CREATE TABLE  if not exists `tadl_algorithm_version` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '算法版本id',
  `algorithm_id` bigint(20) DEFAULT NULL COMMENT '算法id',
  `version_name` varchar(64) DEFAULT NULL COMMENT '版本名称',
  `description` varchar(255) DEFAULT NULL COMMENT '版本说明',
  `version_source` varchar(64) DEFAULT NULL COMMENT '版本来源',
  `data_conversion` int(11) NOT NULL DEFAULT '0',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除(0正常，1删除)',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='tadl 算法版本表';



 CREATE TABLE if not exists `tadl_experiment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) NOT NULL COMMENT '实验名称',
  `description` varchar(255) DEFAULT NULL COMMENT '实验描述',
  `algorithm_id` bigint(20) NOT NULL COMMENT '算法id',
  `algorithm_version_id` bigint(20) NOT NULL COMMENT '算法版本id',
  `model_type` int(11) DEFAULT NULL COMMENT '模型类型',
  `status` int(11) NOT NULL COMMENT '实验状态：（\n待运行：101，\n等待中：102，\n运行中：103，\n已暂停：104，\n已完成：202，\n运行失败：203\n）',
  `status_detail` json DEFAULT NULL COMMENT '状态对应的详情信息',
  `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '启动时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `deleted` bit(1) DEFAULT b'0' COMMENT '是否删除（0正常，1删除）',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`),
  KEY `tadl_experiment_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='tadl实验表'
;

 CREATE TABLE if not exists `tadl_experiment_stage` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `experiment_id` bigint(20) NOT NULL COMMENT '实验id',
  `algorithm_stage_id` varchar(64) NOT NULL COMMENT '算法阶段ID',
  `stage_name` varchar(64) DEFAULT NULL COMMENT '阶段名称',
  `stage_order` tinyint(4) NOT NULL DEFAULT '0' COMMENT '阶段在实验中所处的先后顺序',
  `resource_id` int(11) DEFAULT NULL COMMENT '实验资源配置id',
  `resource_Name` varchar(255) DEFAULT NULL COMMENT '实验资源值',
  `max_trial_num` int(11) NOT NULL COMMENT '最大trail次数',
  `trial_concurrent_num` int(11) NOT NULL COMMENT 'trail并发数量',
  `max_exec_duration` decimal(20,4) NOT NULL DEFAULT '0.0000' COMMENT '最大运行时间',
  `status` int(11) DEFAULT NULL COMMENT '实验阶段状态状态 (101:待运行，102：运行中，201：已完成，202：运行失败)',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除（0正常，1删除）',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '启动时间',
  `begin_time` timestamp NULL DEFAULT NULL COMMENT '每次开始运行的时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `max_exec_duration_unit` varchar(64) DEFAULT NULL COMMENT '最大运行时间单位\n年（y）\n月（m）\n周（w）\n日（d）\n小时（h）\n分钟（min）\n秒（s）\n毫秒（ms）\n微秒（us）\n纳秒（ns）\n皮秒（ps）\n飞秒（fs）',
  `run_time` bigint(32) DEFAULT '0' COMMENT '暂停前已经运行的时间',
  `gpu_type` varchar(255) DEFAULT NULL COMMENT 'GPU类型(例如：nvidia)',
  `gpu_model` varchar(255) DEFAULT NULL COMMENT 'GPU型号(例如：v100)',
  `k8s_label_key` varchar(255) DEFAULT NULL COMMENT 'k8s GPU资源标签key值(例如：nvidia.com/gpu)',
  PRIMARY KEY (`id`),
  KEY `tadl_experiment_stage_experiment_id_stage_order_index` (`experiment_id`,`stage_order`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='tadl实验阶段表'
;

  CREATE TABLE if not exists `tadl_trial` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `experiment_id` bigint(20) NOT NULL COMMENT '试验id',
  `stage_id` varchar(64) DEFAULT NULL COMMENT '实验阶段ID',
  `name` varchar(64) NOT NULL COMMENT 'trial名称',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `status` int(11) NOT NULL COMMENT 'trial状态 (101:待运行，102：等待中，103：运行中，201：已完成，202：运行失败，203：未知)',
  `resource_name` varchar(64) DEFAULT NULL COMMENT 'k8s实验资源值',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除(0正常，1删除)',
  `sequence` int(11) NOT NULL COMMENT '顺序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1 COMMENT='tadl 试验详情表'
;
CREATE TABLE if not exists `tadl_trial_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `experiment_id` bigint(20) NOT NULL COMMENT '实验id',
  `stage_id` bigint(20) NOT NULL COMMENT '实验阶段ID',
  `trial_id` bigint(20) NOT NULL COMMENT 'trial id',
  `type` varchar(64) DEFAULT NULL COMMENT '指标类型',
  `sequence` int(11) DEFAULT NULL COMMENT '序列',
  `category` varchar(64) DEFAULT NULL COMMENT '类别',
  `value` decimal(17,14) DEFAULT NULL COMMENT '最优数据',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除（0正常，1删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='tadl triall运行结果表'
;