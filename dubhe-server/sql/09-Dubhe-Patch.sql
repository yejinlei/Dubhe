use `dubhe-cloud-prod`;

INSERT INTO `menu` (`id`, `pid`, `type`, `name`, `icon` , `path`, `component`, `component_name`, `layout`, `permission` , `back_to`, `ext_config`, `hidden`, `cache`, `sort` , `create_user_id`, `update_user_id`, `deleted`) VALUES
(1085, 0, 0, '自动机器学习', 'jinhangzhongshiyan' , 'tadl', NULL, NULL, NULL, 'TADL' , NULL, NULL, b'0', b'0', 35 , 1, 3, b'0'),
(1086, 1085, 1, '实验详情', NULL , 'experiment/:experimentId', 'tadl/detail', 'ExperimentDetail', 'DetailLayout', NULL , NULL, '{}', b'1', b'0', 999 , 1, 1, b'0'),
(1087, 1085, 1, '实验管理', NULL, 'list', 'tadl/list/index', 'TadlList', 'BaseLayout', 'tadl', NULL, '{}', b'0', b'0', 36, 3, 14,  b'0'),
(1088, 1085, 1, 'TadlForm', NULL, 'form', 'tadl/formPage', 'TadlForm', 'SubpageLayout', 'tadl',NULL, '{}', b'1', b'0', 999, 3, 3,  b'0'),
(1089, 1085, 1, '搜索策略', 'zoom', 'searchstrategy', 'tadl/strategy/index', 'SearchStrategy', 'BaseLayout', 'tadl', NULL, '{}', b'0', b'0', 37, 14, 14, b'0');

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
	`deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '删除(0：正常，1：删除)',
	`update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	`default_metric` VARCHAR(64) NOT NULL COMMENT '默认主要指标' COLLATE 'utf8_general_ci',
	`one_shot` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否oneshot',
	`algorithm_type` VARCHAR(50) NOT NULL COMMENT '算法类型' COLLATE 'utf8_general_ci',
	`platform` VARCHAR(64) NOT NULL COMMENT '算法框架' COLLATE 'utf8_general_ci',
	`platform_version` VARCHAR(64) NOT NULL COMMENT '算法框架版本' COLLATE 'utf8_general_ci',
	`gpu` BIT(1) NULL DEFAULT b'0' COMMENT '是否支持gpu计算（0支持，1不支持）',
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
	`multi_gpu` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否支持多卡训练（0支持，1不支持）',
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
	`deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除(0正常，1删除)',
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
	`deleted` BIT(1) NULL DEFAULT 'b'0\'' COMMENT '是否删除（0正常，1删除）',
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
	`deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除（0正常，1删除）',
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
	`deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除(0正常，1删除)',
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
	`deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除（0正常，1删除）',
	PRIMARY KEY (`id`) USING BTREE
)
COMMENT='tadl trial 运行结果表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
;

INSERT INTO pt_image (project_name, image_resource, image_status, image_name, image_url
                     , image_tag, remark, create_user_id, update_user_id, deleted
                     , origin_user_id)
VALUES ('notebook', 1, 1, 'jupyterlab', 'notebook/jupyterlab:oneflow-0.1.102-py36-0713'
       , 'oneflow-0.1.102-py36-0713', '预置算法', 1, 1, 0
       , 0);

INSERT INTO pt_image (project_name, image_resource, image_status, image_name, image_url
                     , image_tag, remark, create_user_id, update_user_id, deleted
                     , origin_user_id)
VALUES ('notebook', 1, 1, 'jupyterlab', 'notebook/jupyterlab:oneflow-0.1.102-py36-0713'
       , 'oneflow-0.1.102-py36-0713', '预置算法', 1, 1, 0
       , 0);

ALTER TABLE `pt_train_param`
    ADD COLUMN algorithm_usage varchar(255) NULL DEFAULT '' COMMENT '算法用途',
	ADD COLUMN val_algorithm_usage varchar(255) NULL DEFAULT '' COMMENT '验证数据集算法用途';

ALTER TABLE `pt_job_param`
    ADD COLUMN algorithm_usage varchar(255) NULL DEFAULT '' COMMENT '算法用途',
	ADD COLUMN val_algorithm_usage varchar(255) NULL DEFAULT '' COMMENT '验证数据集算法用途';

INSERT INTO `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`
                         , `deleted`)
SELECT id, '编辑用户配置'
     , 'system:user:configEdit', 1, 1, b'0'
FROM `permission`
WHERE `name` = '用户管理';

UPDATE `permission`
SET `pid` = 1
WHERE `id` = 2;

CREATE TABLE `user_config` (
                               `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
                               `user_id` bigint(20) NOT NULL COMMENT '用户id',
                               `notebook_delay_delete_time` int(10) NOT NULL COMMENT '定时任务延迟删除 Notebook 时间配置，单位：小时',
                               `cpu_limit` int(10) NOT NULL COMMENT '用户CPU资源限制配置，单位：核',
                               `memory_limit` int(10) NOT NULL COMMENT '用户内存资源限制配置，单位：Gi',
                               `gpu_limit` int(10) NOT NULL COMMENT '用户显卡资源限制配置，单位：块',
                               `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人',
                               `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人',
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除(0正常，1已删除)',
                               PRIMARY KEY USING BTREE (`id`),
                               UNIQUE KEY `user_id_unique` USING BTREE (`user_id`),
                               KEY `user_id` USING BTREE (`create_user_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 51 CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMMENT '用户配置表';

INSERT INTO `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`
                         , `deleted`)
SELECT id, '查询用户资源信息'
     , 'system:user:resourceInfo', 1, 1, b'0'
FROM `permission`
WHERE `name` = '用户管理';

DELETE FROM dict
WHERE id IN (35, 36);

DELETE FROM dict_detail
WHERE dict_id IN (35, 36);

INSERT INTO `pt_image` (`project_name`, `image_resource`, `image_status`, `image_name`, `image_url`
                       , `image_tag`, `remark`, `create_user_id`, `update_user_id`, `deleted`
                       , `origin_user_id`)
VALUES ('notebook', '0', '1', 'notebook', 'notebook/notebook:v2'
       , 'v2', '', '1', '1', '0'
       , '0');

ALTER TABLE `notebook`
    ADD COLUMN `pip_site_package_path` VARCHAR(255) NULL COMMENT 'pip包路径';

-- 20210728151053420
-- 专业版终端业务表
-- 20210728151053420
-- 专业版终端业务表
CREATE TABLE IF NOT EXISTS `terminal` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` varchar(255) DEFAULT NULL COMMENT '名称',
    `image_name` varchar(127) DEFAULT '' COMMENT '镜像名',
    `image_tag` varchar(64) DEFAULT '' COMMENT '镜像版本',
    `image_url` varchar(255) DEFAULT '' COMMENT '镜像全路径',
    `data_source_name` varchar(127) DEFAULT NULL COMMENT '数据集名称',
    `data_source_path` varchar(127) DEFAULT NULL COMMENT '数据集路径',
    `running_node` tinyint(3) UNSIGNED DEFAULT '0' COMMENT '运行节点数',
    `total_node` tinyint(3) UNSIGNED DEFAULT '0' COMMENT '服务总节点数',
    `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '服务状态：0-异常，1-保存中，2-运行中,3-已停止',
    `status_detail` json DEFAULT NULL COMMENT '状态对应的详情信息',
    `description` varchar(255) DEFAULT NULL COMMENT '描述',
    `same_info` bit(1) DEFAULT b'0' COMMENT '节点规格是否相同:0相同 1:不同',
    `last_start_time` timestamp NULL DEFAULT NULL COMMENT '上次启动执行时间',
    `last_stop_time` timestamp NULL DEFAULT NULL COMMENT '上次停止执行时间',
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user_id` bigint(20) DEFAULT NULL,
    `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_user_id` bigint(20) DEFAULT NULL,
    `deleted` bit(1) DEFAULT b'0',
    `origin_user_id` bigint(20) DEFAULT NULL COMMENT '资源拥有人ID',
    PRIMARY KEY USING BTREE (`id`),
    KEY `deleted` (`deleted`)
    ) ENGINE = InnoDB CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMMENT '专业版终端业务表';

-- 专业版终端业务详情表
-- 专业版终端业务详情表
CREATE TABLE IF NOT EXISTS `terminal_info` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `terminal_id` bigint(20) NOT NULL COMMENT 'terminal主键',
    `name` varchar(255) DEFAULT NULL COMMENT '名称',
    `k8s_resource_name` varchar(64) DEFAULT NULL COMMENT 'k8s 资源名称',
    `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '服务状态：0-异常，1-调度中，2-运行中,3-已停止',
    `status_detail` json DEFAULT NULL COMMENT '状态对应的详情信息',
    `ssh` varchar(64) DEFAULT NULL COMMENT 'ssh命令',
    `ssh_password` varchar(64) DEFAULT NULL COMMENT 'ssh 密码',
    `cpu_num` int(11) NOT NULL DEFAULT '0' COMMENT 'CPU数量(核)',
    `gpu_num` int(11) NOT NULL DEFAULT '0' COMMENT 'GPU数量（核）',
    `mem_num` int(11) NOT NULL DEFAULT '0' COMMENT '内存大小（M）',
    `disk_mem_num` int(11) NOT NULL DEFAULT '0' COMMENT '磁盘大小（M）',
    `pod_ip` varchar(64) DEFAULT NULL COMMENT 'pod ip',
    `ssh_port` int(11) DEFAULT NULL COMMENT 'ssh端口',
    `ssh_user` varchar(32) DEFAULT NULL COMMENT 'ssh 用户',
    `master_flag` bit(1) DEFAULT b'0' COMMENT '是否master节点:0 否 1:是',
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user_id` bigint(20) DEFAULT NULL,
    `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `update_user_id` bigint(20) DEFAULT NULL,
    `deleted` bit(1) DEFAULT b'0',
    `origin_user_id` bigint(20) DEFAULT NULL COMMENT '资源拥有人ID',
    PRIMARY KEY USING BTREE (`id`),
    KEY `status` (`status`),
    KEY `deleted` (`deleted`)
    ) ENGINE = InnoDB CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMMENT '专业版终端业务详情表';

INSERT INTO `menu` (`pid`, `type`, `name`, `icon`, `path`
                   , `component`, `component_name`, `layout`, `permission`, `hidden`
                   , `cache`, `sort`, `create_user_id`, `update_user_id`, `deleted`
                   , `back_to`, `ext_config`)
VALUES (0, 0, '天枢专业版', 'terminal', 'terminal'
       , NULL, NULL, NULL, 'terminal', b'0'
       , b'0', 80, 3, 3, b'0'
       , NULL, NULL),
       (1084, 1, '终端概览', 'overview', 'overview'
       , 'terminal/overview', 'TerminalOverview', 'BaseLayout', 'terminal:terminals', b'0'
       , b'0', 81, 3, 3, b'0'
       , NULL, '{}'),
       (1084, 1, '远程连接', 'remote', 'remote'
       , 'terminal/remote', 'TerminalRemote', 'BaseLayout', 'terminal:remote', b'0'
       , b'0', 82, 3, 3, b'0'
       , NULL, '{}');

ALTER TABLE `pt_image`
    ADD COLUMN `ssh_pwd` varchar(64) COMMENT '镜像ssh密码',
	ADD COLUMN `ssh_user` varchar(64) COMMENT '镜像ssh用户';

INSERT INTO `pt_image` (`project_name`, `image_resource`, `image_status`, `image_name`, `image_url`
                       , `image_tag`, `remark`, `create_user_id`, `update_user_id`, `deleted`
                       , `origin_user_id`)
VALUES ('notebook', '0', '1', 'notebook', 'notebook/notebook:v2'
       , 'v2', '', '1', '1', '0'
       , '0');

create table k8s_task_identify
(
    id             bigint auto_increment primary key,
    task_id        bigint                             not null comment '任务id',
    task_name      varchar(64)                        not null comment '任务名称',
    create_user_id bigint                             null comment '创建人id',
    update_user_id bigint                             null comment '修改人id',
    create_time    datetime default CURRENT_TIMESTAMP null,
    update_time    datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    deleted        bit      default b'0'              null comment '删除标记 0正常，1已删除'
);

-- pt_job_param表添加algorithm_usage字段，val_algorithm_usage字段
ALTER TABLE
  `pt_job_param`
add
  column algorithm_usage varchar(255) default '' null comment '算法用途',
add
  column val_algorithm_usage varchar(255) default '' null comment '验证数据集算法用途';
-- pt_train_param表添加algorithm_usage字段，val_algorithm_usage字段
ALTER TABLE
  `pt_train_param`
add
  column algorithm_usage varchar(255) default '' null comment '算法用途',
add
  column val_algorithm_usage varchar(255) default '' null comment '验证数据集算法用途';

-- pt_job_param表添加notebook_name字段
ALTER TABLE
  `pt_job_param`
add
  column notebook_name varchar(255) default null null comment 'notebook名称';