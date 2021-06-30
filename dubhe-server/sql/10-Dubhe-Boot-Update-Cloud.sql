SET SQL_SAFE_UPDATES = 0;
delimiter //
DROP PROCEDURE IF EXISTS  fourthEditionProc ;
CREATE PROCEDURE fourthEditionProc( )
    BEGIN
    DECLARE t_error INTEGER DEFAULT 0;
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION SET t_error=1;
		set autocommit=0;
        START TRANSACTION;
-- data_dataset
        alter table data_dataset add column source_id bigint(20) default null comment '数据集来源ID';

        alter table data_dataset modify `data_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '数据类型:0图片，1视频，2文本';
        alter table data_dataset modify `annotate_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '标注类型：2分类,1目标检测,5目标跟踪';
        alter table data_dataset modify `status` int(11) NOT NULL DEFAULT '0' COMMENT '101:未标注  102:手动标注中  103:自动标注中  104:自动标注完成  105:标注完成  201:目标跟踪中  202:目标跟踪完成  203:目标跟踪失败  301:未采样  302:采样中  303:采样失败  401:增强中  402:导入中';
        alter table data_dataset modify `origin_user_id` bigint(20) DEFAULT NULL COMMENT '资源拥有人id';
        alter table data_dataset drop key `idx_name_unique`;

-- data_dataset_label
        alter table data_dataset_label add column `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
        alter table data_dataset_label add column `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间';
        alter table data_dataset_label add column `deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '删除(0正常，1已删除)';
        alter table data_dataset_label add column create_user_id bigint(20) DEFAULT NULL COMMENT '创建人id';
        alter table data_dataset_label add column update_user_id bigint(20) DEFAULT NULL COMMENT '修改人id';

-- data_dataset_version
        alter table data_dataset_version modify `data_conversion` int(1) NOT NULL DEFAULT '0' COMMENT '数据转换；0：图片未复制；1：未转换；2：已转换；3：无法转换; 4:标注未复制';
        alter table data_dataset_version modify `origin_user_id` bigint(19) DEFAULT '0' COMMENT '资源拥有人id';

-- data_dataset_version_file
        alter table `data_dataset_version_file` drop key `annotation_status`;
        alter table `data_dataset_version_file` drop key `dataset_id`;

-- data_file
         alter table `data_file` drop `md5`;
         alter table `data_file` modify  `origin_user_id` bigint(19) DEFAULT NULL COMMENT '资源拥有者ID';
         alter table `data_file` add column `es_transport` int(1) DEFAULT '0' COMMENT '是否上传至es';
         alter table `data_file` add column `exclude_header` smallint(6) NOT NULL DEFAULT '1' COMMENT 'table数据导入时，是否排除文件头';
         alter table `data_file` add key `es_transport` (`es_transport`) ;

-- data_file_annotation
        alter table `data_file_annotation` add column `file_name` varchar(255) DEFAULT NULL COMMENT '文件名称';
        alter table `data_file_annotation` add KEY `label_dataset_id_index` (`label_id`,`dataset_id`);

-- data_label_group
        alter table `data_label_group` modify `origin_user_id` bigint(20) DEFAULT NULL COMMENT '资源用有人ID' ;

-- data_task
        alter table `data_task` modify `type` smallint(3) DEFAULT NULL COMMENT '任务类型 0.自动标注 1.ofrecord 2.imageNet 3.数据增强 4.目标跟踪 5.视频采样 6.医学标注 7.文本分类 8.重新自动标注 10.csv导入';
        alter table `data_task` add column `merge_column` varchar(255) DEFAULT NULL COMMENT 'csv合并列';

-- k8s_resource
        ALTER TABLE `k8s_resource` DROP KEY `kind_namespace_name_uniq` ;

-- k8s_task
        ALTER TABLE `k8s_task` DROP KEY `resource_name_namespace` ;
        ALTER TABLE `k8s_task` DROP KEY `apply_status` ;
        ALTER TABLE `k8s_task` DROP KEY `stop_status` ;

-- menu
        alter table menu add column back_to varchar(255) default null comment '上级菜单';
        alter table menu add column ext_config varchar(255) default null comment '扩展配置';

-- 初始化回收站菜单数据
        INSERT INTO menu ( pid, type, name, icon, path, component, component_name, layout, permission, hidden, cache, sort, create_user_id, update_user_id, deleted, back_to, ext_config) VALUES ( 90, 1, '回收站', 'shuju1', 'recycle', 'system/recycle/index', 'SystemRecycle', 'BaseLayout', 'system:recycle', false, false, 999, 1, 1,  false, null, null);
        INSERT INTO menu ( cache, component, component_name, hidden, icon, name, path, permission, pid,sort, type, layout, create_time, update_time, create_user_id, update_user_id,deleted)
        VALUES ( '', NULL, NULL, '', 'icon_huabanfuben1', '模型炼知', 'atlas', NULL, '0', '70', '0', NULL,'2020-11-20 09:08:12', '2020-11-20 09:08:12', '1', '1', '');
        INSERT INTO menu ( cache, component, component_name, hidden, icon, name, path, permission, pid,sort, type, layout, create_time, update_time, create_user_id, update_user_id,deleted)
        VALUES ( '', 'atlas/measure', 'Measure', '', 'icon_huabanfuben1', '度量管理', 'measure', NULL, '97', '71', '1','BaseLayout', '2020-11-20 11:03:20', '2020-11-20 11:03:20', '1', '1', '');
        INSERT INTO menu ( cache, component, component_name, hidden, icon, name, path, permission, pid,sort, type, layout, create_time, update_time, create_user_id, update_user_id,deleted)
        VALUES ( '', 'atlas/graphVisual', 'AtlasGraphVisual', '', 'icon_huabanfuben1', '图谱可视化', 'graphvisual', NULL,'97', '72', '1', 'BaseLayout', '2020-11-20 11:07:14', '2020-11-20 11:07:14', '1', '1', '');
        INSERT INTO menu ( cache, component, component_name, hidden, icon, name, path, permission, pid,sort, type, layout, create_time, update_time, create_user_id, update_user_id,deleted)
        VALUES ( '', 'atlas/graphList', 'AtlasGraph', '', 'icon_huabanfuben1', '图谱列表', 'graph', NULL, '97', '73', '1','BaseLayout', '2020-11-20 11:08:02', '2020-11-20 11:08:02', '1', '1', '');

-- model_opt_task
        alter table model_opt_task add column `model_branch_id` bigint(20) DEFAULT NULL COMMENT '模型对应版本id';

-- model_opt_task_instance
        alter table model_opt_task_instance add column `model_branch_id` bigint(20) DEFAULT NULL COMMENT '模型对应版本id';



/*Table structure for table `oauth_access_token` */

DROP TABLE IF EXISTS `oauth_access_token`;

CREATE TABLE `oauth_access_token` (
  `token_id` varchar(256) DEFAULT NULL,
  `token` longblob,
  `authentication_id` varchar(256) DEFAULT NULL,
  `user_name` varchar(256) DEFAULT NULL,
  `client_id` varchar(256) DEFAULT NULL,
  `authentication` blob,
  `refresh_token` varchar(256) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='auth token存储表';

/*Table structure for table `oauth_client_details` */

DROP TABLE IF EXISTS `oauth_client_details`;

CREATE TABLE `oauth_client_details` (
  `client_id` varchar(256) NOT NULL,
  `resource_ids` varchar(256) DEFAULT NULL,
  `client_secret` varchar(256) DEFAULT NULL,
  `scope` varchar(256) DEFAULT NULL,
  `authorized_grant_types` varchar(256) DEFAULT NULL,
  `web_server_redirect_uri` varchar(256) DEFAULT NULL,
  `authorities` varchar(256) DEFAULT NULL,
  `access_token_validity` int(11) DEFAULT NULL,
  `refresh_token_validity` int(11) DEFAULT NULL,
  `additional_information` varchar(4096) DEFAULT NULL,
  `autoapprove` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`client_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户端权限配置表';

/*Table structure for table `oauth_refresh_token` */

DROP TABLE IF EXISTS `oauth_refresh_token`;

CREATE TABLE `oauth_refresh_token` (
  `token_id` varchar(256) DEFAULT NULL,
  `token` blob,
  `authentication` blob
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='权限token刷新表';

/*Table structure for table `process_node` */

DROP TABLE IF EXISTS `process_node`;

CREATE TABLE `process_node` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `fun_type` smallint(6) NOT NULL DEFAULT '0' COMMENT '节点功能类型 0-数据节点 1-计算节点',
  `data_type` smallint(6) NOT NULL DEFAULT '0' COMMENT '数据类型 0-csv文件数据 1-mysql数据',
  `desc` varchar(255) DEFAULT NULL COMMENT '节点描述',
  `config` text COMMENT '参数',
  `name` varchar(50) NOT NULL COMMENT '节点名称',
  `operation` smallint(6) DEFAULT '0' COMMENT '读写类型 0-读数据 1-写数据 2-数据处理',
  `deleted` bit(1) DEFAULT NULL,
  `create_user_id` bigint(20) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_user_id` bigint(20) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `program_execute_command` varchar(255) DEFAULT NULL COMMENT '程序执行命令',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

/*Table structure for table `process_processor` */

DROP TABLE IF EXISTS `process_processor`;

CREATE TABLE `process_processor` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '处理器名称',
  `desc` varchar(255) DEFAULT NULL COMMENT '适用场景',
  `bean_name` varchar(255) DEFAULT NULL COMMENT '转换方法bean name',
  `program_path` varchar(255) DEFAULT NULL COMMENT '程序地址',
  `deleted` bit(1) DEFAULT NULL,
  `create_user_id` bigint(20) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_user_id` bigint(20) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

/*Table structure for table `process_task` */

DROP TABLE IF EXISTS `process_task`;

CREATE TABLE `process_task` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) NOT NULL COMMENT '任务名称',
  `type` smallint(6) NOT NULL DEFAULT '0' COMMENT '任务类型 0-数据采集 1-数据清洗 2-数据采集和清洗 ',
  `processor_id` int(11) NOT NULL COMMENT '执行器ID',
  `flow_config` text COMMENT 'flow配置',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '开始时间',
  `status` smallint(6) NOT NULL DEFAULT '0' COMMENT '状态 0-未执行 1-执行中 2-暂停中 3-执行失败 4-完成',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `create_user_id` mediumtext NOT NULL COMMENT '创建人',
  `update_user_id` mediumtext COMMENT '更新人',
  `deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `process_task_name_uindex` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='用途：数据处理任务管理\n负责人：王伟\n创建日期：2021/03/08';

/*Table structure for table `process_task_log` */

DROP TABLE IF EXISTS `process_task_log`;

CREATE TABLE `process_task_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id` int(11) NOT NULL COMMENT '任务ID',
  `file_path` varchar(50) DEFAULT NULL COMMENT '日志文件地址',
  `create_user_id` bigint(20) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_user_id` bigint(20) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- pt_measure
        alter table `pt_measure` add column `dataset_id` bigint(20) NOT NULL COMMENT '数据集id';
        alter table `pt_measure` add column `dateset_url` varchar(32) DEFAULT NULL COMMENT '数据集url';
        alter table `pt_measure` add column `dataset_url` varchar(32) DEFAULT NULL COMMENT '数据集url';
        alter table `pt_measure` add column `model_urls` varchar(512) DEFAULT NULL COMMENT '模型url';
        alter table `pt_measure` add column `measure_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '度量文件生成状态，0：生成中，1：生成成功，2：生成失败';
        alter table `pt_measure` drop key `measure_unidex`;
        alter table `pt_measure` add key `user_id` (`create_user_id`);

-- pt_model_branch
        alter table `pt_measure` modify `origin_user_id` bigint(20) DEFAULT NULL COMMENT '数据拥有人id';

-- pt_model_info
        alter table `pt_model_info` add `packaged` tinyint default 0 not null comment '模型是否已经打包，0未打包，1打包完成';
        alter table `pt_model_info` add `tags` json null comment 'tag信息';

-- pt_train_param
         alter table `pt_train_param`  drop column `train_job_specs_id`;

DROP TABLE IF EXISTS `recycle`;

CREATE TABLE `recycle` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `recycle_module` varchar(32) NOT NULL COMMENT '回收模块',
  `recycle_delay_date` date DEFAULT NULL COMMENT '回收日期',
  `recycle_custom` varchar(64) DEFAULT NULL COMMENT '回收定制化方式',
  `recycle_status` tinyint(4) DEFAULT '0' COMMENT '回收任务状态(0:待删除，1:已删除，2:删除失败，3：删除中，4：还原中，5：已还原)',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `update_user_id` bigint(20) DEFAULT NULL COMMENT '修改人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `recycle_note` varchar(512) DEFAULT NULL COMMENT '回收说明',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `recycle_response` varchar(512) DEFAULT NULL COMMENT '回收响应信息',
  `restore_custom` varchar(64) DEFAULT NULL COMMENT '还原定制化方式',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除(0正常，1已删除)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=923 DEFAULT CHARSET=utf8mb4 COMMENT='垃圾回收任务主表';

/*Table structure for table `recycle_detail` */

DROP TABLE IF EXISTS `recycle_detail`;

CREATE TABLE `recycle_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `recycle_id` bigint(20) NOT NULL COMMENT '垃圾回收任务主表ID',
  `recycle_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '回收类型(0文件，1数据库表数据)',
  `recycle_condition` text NOT NULL COMMENT '回收条件(回收表数据sql、回收文件绝对路径)',
  `recycle_status` tinyint(4) DEFAULT '0' COMMENT '回收任务状态(0:待删除，1:已删除，2:删除失败，3：删除中)',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `update_user_id` bigint(20) DEFAULT NULL COMMENT '修改人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `recycle_note` varchar(512) DEFAULT NULL COMMENT '回收说明',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `recycle_response` varchar(512) DEFAULT NULL COMMENT '回收响应信息',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除(0正常，1已删除)',
  PRIMARY KEY (`id`),
  KEY `recycle_task_main_id` (`recycle_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1605 DEFAULT CHARSET=utf8mb4 COMMENT='垃圾回收任务详情表';

-- recycle_task
        alter table `recycle_task`  add column `remark` varchar(512) DEFAULT NULL COMMENT '备注';
        alter table `recycle_task`  add column `recycle_response` varchar(512) DEFAULT NULL COMMENT '回收响应信息';
        alter table `recycle_task`  add column `restore_custom` varchar(64) DEFAULT NULL COMMENT '还原定制化方式';


-- serving_batch
        alter table `serving_batch`  add column `model_branch_id` bigint(20) DEFAULT NULL COMMENT '模型分支id';

-- serving_model_config
        alter table `serving_model_config`  add column `model_branch_id` bigint(20) DEFAULT NULL COMMENT '模型对应版本id';

DROP TABLE IF EXISTS `tadl_algorithm`;

CREATE TABLE `tadl_algorithm` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `name` varchar(64) NOT NULL COMMENT '算法名称',
  `model_type` int(11) NOT NULL COMMENT '模型类别',
  `algorithm_path` varchar(255) NOT NULL COMMENT '算法文件路径',
  `algorithm_version_id` bigint(20) DEFAULT NULL COMMENT '算法版本id',
  `description` varchar(255) DEFAULT NULL COMMENT '算法描述',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '删除(0：正常，1：删除)',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `tadl_algorithm_name_uindex` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COMMENT='tadl 算法表';

/*Table structure for table `tadl_algorithm_stage` */

DROP TABLE IF EXISTS `tadl_algorithm_stage`;

CREATE TABLE `tadl_algorithm_stage` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '算法阶段id',
  `name` varchar(64) NOT NULL COMMENT '阶段名称',
  `stage_order` tinyint(4) NOT NULL COMMENT '阶段排序',
  `algorithm_id` bigint(20) DEFAULT NULL COMMENT '算法id',
  `algorithm_version_id` bigint(20) DEFAULT NULL COMMENT '算法版本',
  `dataset_name` varchar(64) DEFAULT NULL COMMENT '数据集ID',
  `dataset_id` bigint(20) DEFAULT NULL COMMENT '数据集id',
  `dataset_version` bigint(20) DEFAULT NULL COMMENT '数据集版本id',
  `dataset_path` varchar(255) NOT NULL COMMENT '数据集路径',
  `python_version` varchar(64) DEFAULT NULL COMMENT 'command命令所使用的python环境',
  `algorithm_file` varchar(64) DEFAULT NULL COMMENT 'command命令所使用py文件',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除（0正常，1删除）',
  `gpu_num` tinyint(4) DEFAULT NULL COMMENT 'gpu数量',
  `max_trial_num` int(11) NOT NULL COMMENT '默认最大运行次数',
  `max_execution_time` bigint(20) NOT NULL COMMENT '当前阶段默认最大执行时间',
  `trial_concurrent_num` int(11) NOT NULL COMMENT 'trial默认并发数量',
  `max_execution_time_unit` varchar(64) DEFAULT NULL COMMENT '最大运行时间单位\n年（y）\n月（m）\n周（w）\n日（d）\n小时（h）\n分钟（min）\n秒（s）\n毫秒（ms）\n微秒（us）\n纳秒（ns）\n皮秒（ps）\n飞秒（fs）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `tadl_algorithm_version` */

DROP TABLE IF EXISTS `tadl_algorithm_version`;

CREATE TABLE `tadl_algorithm_version` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '算法版本id',
  `algorithm_id` bigint(20) DEFAULT NULL COMMENT '算法id',
  `version_name` varchar(64) DEFAULT NULL COMMENT '版本名称',
  `description` varchar(255) DEFAULT NULL COMMENT '版本说明',
  `version_source` varchar(64) DEFAULT NULL COMMENT '版本来源',
  `default_metric` varchar(64) NOT NULL COMMENT '默认主要指标',
  `oneshot` bit(1) NOT NULL COMMENT '是否oneshot（0不是，1是）',
  `algorithm_type` varchar(50) NOT NULL COMMENT '算法类型',
  `data_conversion` int(11) NOT NULL DEFAULT '0',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除(0正常，1删除)',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `platform` varchar(64) NOT NULL COMMENT '算法框架',
  `platform_version` varchar(64) NOT NULL COMMENT '算法框架版本',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='tadl 算法版本表';

/*Table structure for table `tadl_experiment` */

DROP TABLE IF EXISTS `tadl_experiment`;

CREATE TABLE `tadl_experiment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) NOT NULL COMMENT '实验名称',
  `description` varchar(255) DEFAULT NULL COMMENT '实验描述',
  `algorithm_id` bigint(20) NOT NULL COMMENT '算法id',
  `algorithm_version_id` bigint(20) NOT NULL COMMENT '算法版本id',
  `model_type` int(11) DEFAULT NULL COMMENT '模型类型',
  `experiment_path` varchar(255) DEFAULT NULL COMMENT '实验路径',
  `image_id` bigint(20) NOT NULL COMMENT '镜像id',
  `status` int(11) NOT NULL COMMENT '实验状态：（\n待运行：101，\n等待中：102，\n运行中：103，\n已暂停：104，\n终止中：105，\n已终止：106，\n已完成：107，\n运行失败：108\n）',
  `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '启动时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `deleted` bit(1) DEFAULT b'0' COMMENT '是否删除（0正常，1删除）',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `tadl_experiment_name_uindex` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='tadl实验表';

/*Table structure for table `tadl_experiment_stage` */

DROP TABLE IF EXISTS `tadl_experiment_stage`;

CREATE TABLE `tadl_experiment_stage` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `experiment_id` bigint(20) NOT NULL COMMENT '实验id',
  `stage_id` varchar(64) NOT NULL COMMENT '阶段ID',
  `stage_name` varchar(64) DEFAULT NULL COMMENT '阶段名称',
  `stage_order` tinyint(4) NOT NULL DEFAULT '0' COMMENT '阶段在实验中所处的先后顺序',
  `resource_id` int(11) DEFAULT NULL COMMENT '实验资源配置id',
  `resource_value` varchar(64) DEFAULT NULL COMMENT '实验资源值',
  `max_trial_num` int(11) NOT NULL COMMENT '最大trail次数',
  `trial_concurrent_num` int(11) NOT NULL COMMENT 'trail并发数量',
  `max_execution_time` bigint(20) NOT NULL DEFAULT '0' COMMENT '最大运行时间（秒）',
  `status` int(11) DEFAULT NULL COMMENT '实验阶段状态状态 (101:待运行，102：运行中，103：已完成，104：运行失败)',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除（0正常，1删除）',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '启动时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `max_execution_time_unit` varchar(64) DEFAULT NULL COMMENT '最大运行时间单位\n年（y）\n月（m）\n周（w）\n日（d）\n小时（h）\n分钟（min）\n秒（s）\n毫秒（ms）\n微秒（us）\n纳秒（ns）\n皮秒（ps）\n飞秒（fs）',
  `skip_stage_id` bigint(20) DEFAULT NULL COMMENT '跳过当前阶段，所属阶段id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COMMENT='tadl实验阶段表';

/*Table structure for table `tadl_trial` */

DROP TABLE IF EXISTS `tadl_trial`;

CREATE TABLE `tadl_trial` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `experiment_id` bigint(20) NOT NULL COMMENT '试验id',
  `stage_id` varchar(64) DEFAULT NULL COMMENT '阶段',
  `name` varchar(64) NOT NULL COMMENT 'trial名称',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `model_path` varchar(255) DEFAULT NULL COMMENT '模型生成地址',
  `status` int(11) NOT NULL COMMENT 'trial状态 (101:待运行，102：运行中，103：已完成，104：运行失败)',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除(0正常，1删除)',
  `sequence` int(11) NOT NULL COMMENT '顺序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1 COMMENT='tadl 试验详情表';

/*Table structure for table `tadl_trial_data` */

DROP TABLE IF EXISTS `tadl_trial_data`;

CREATE TABLE `tadl_trial_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `experiment_id` bigint(20) NOT NULL COMMENT '实验id',
  `stage_id` bigint(20) NOT NULL COMMENT '阶段id',
  `trial_id` bigint(20) NOT NULL COMMENT 'trial id',
  `type` varchar(64) NOT NULL COMMENT '指标类型',
  `sequence` int(11) DEFAULT NULL COMMENT '序列',
  `category` varchar(64) DEFAULT NULL COMMENT '类别',
  `value` decimal(10,0) NOT NULL COMMENT '最优数据',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '修改时间',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除（0正常，1删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8 COMMENT='tadl triall运行结果表';

/*Table structure for table `oauth_access_token` */

DROP TABLE IF EXISTS `oauth_access_token`;

CREATE TABLE `oauth_access_token` (
  `token_id` varchar(256) DEFAULT NULL,
  `token` longblob,
  `authentication_id` varchar(256) DEFAULT NULL,
  `user_name` varchar(256) DEFAULT NULL,
  `client_id` varchar(256) DEFAULT NULL,
  `authentication` blob,
  `refresh_token` varchar(256) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='auth token存储表';

/*Table structure for table `oauth_client_details` */

DROP TABLE IF EXISTS `oauth_client_details`;

CREATE TABLE `oauth_client_details` (
  `client_id` varchar(256) NOT NULL,
  `resource_ids` varchar(256) DEFAULT NULL,
  `client_secret` varchar(256) DEFAULT NULL,
  `scope` varchar(256) DEFAULT NULL,
  `authorized_grant_types` varchar(256) DEFAULT NULL,
  `web_server_redirect_uri` varchar(256) DEFAULT NULL,
  `authorities` varchar(256) DEFAULT NULL,
  `access_token_validity` int(11) DEFAULT NULL,
  `refresh_token_validity` int(11) DEFAULT NULL,
  `additional_information` varchar(4096) DEFAULT NULL,
  `autoapprove` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`client_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户端权限配置表';

/*Table structure for table `oauth_refresh_token` */

DROP TABLE IF EXISTS `oauth_refresh_token`;

CREATE TABLE `oauth_refresh_token` (
  `token_id` varchar(256) DEFAULT NULL,
  `token` blob,
  `authentication` blob
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='权限token刷新表';

/*Table structure for table `process_node` */

DROP TABLE IF EXISTS `process_node`;

CREATE TABLE `process_node` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `fun_type` smallint(6) NOT NULL DEFAULT '0' COMMENT '节点功能类型 0-数据节点 1-计算节点',
  `data_type` smallint(6) NOT NULL DEFAULT '0' COMMENT '数据类型 0-csv文件数据 1-mysql数据',
  `desc` varchar(255) DEFAULT NULL COMMENT '节点描述',
  `config` text COMMENT '参数',
  `name` varchar(50) NOT NULL COMMENT '节点名称',
  `operation` smallint(6) DEFAULT '0' COMMENT '读写类型 0-读数据 1-写数据 2-数据处理',
  `deleted` bit(1) DEFAULT NULL,
  `create_user_id` bigint(20) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_user_id` bigint(20) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `program_execute_command` varchar(255) DEFAULT NULL COMMENT '程序执行命令',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

/*Table structure for table `process_processor` */

DROP TABLE IF EXISTS `process_processor`;

CREATE TABLE `process_processor` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '处理器名称',
  `desc` varchar(255) DEFAULT NULL COMMENT '适用场景',
  `bean_name` varchar(255) DEFAULT NULL COMMENT '转换方法bean name',
  `program_path` varchar(255) DEFAULT NULL COMMENT '程序地址',
  `deleted` bit(1) DEFAULT NULL,
  `create_user_id` bigint(20) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_user_id` bigint(20) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

/*Table structure for table `process_task` */

DROP TABLE IF EXISTS `process_task`;

CREATE TABLE `process_task` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) NOT NULL COMMENT '任务名称',
  `type` smallint(6) NOT NULL DEFAULT '0' COMMENT '任务类型 0-数据采集 1-数据清洗 2-数据采集和清洗 ',
  `processor_id` int(11) NOT NULL COMMENT '执行器ID',
  `flow_config` text COMMENT 'flow配置',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '开始时间',
  `status` smallint(6) NOT NULL DEFAULT '0' COMMENT '状态 0-未执行 1-执行中 2-暂停中 3-执行失败 4-完成',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `create_user_id` mediumtext NOT NULL COMMENT '创建人',
  `update_user_id` mediumtext COMMENT '更新人',
  `deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `process_task_name_uindex` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='用途：数据处理任务管理\n负责人：王伟\n创建日期：2021/03/08';

/*Table structure for table `process_task_log` */

DROP TABLE IF EXISTS `process_task_log`;

CREATE TABLE `process_task_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id` int(11) NOT NULL COMMENT '任务ID',
  `file_path` varchar(50) DEFAULT NULL COMMENT '日志文件地址',
  `create_user_id` bigint(20) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_user_id` bigint(20) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- 模型炼知生成度量Json入口函数
	INSERT INTO `dict`(`id`, `name`, `remark`) VALUES (38, 'entry_name', '模型炼知生成度量Json入口函数');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'alexnet', 'alexnet', '1');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'resnet18', 'resnet18', '2');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'resnet34', 'resnet34', '3');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'resnet50', 'resnet50', '4');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'resnet101', 'resnet101', '5');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'resnet152', 'resnet152', '6');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'resnext50_32x4d', 'resnext50_32x4d', '7');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'resnext101_32x8d', 'resnext101_32x8d', '8');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'wide_resnet50_2', 'wide_resnet50_2', '9');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'wide_resnet101_2', 'wide_resnet101_2', '10');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'vgg11', 'vgg11', '11');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'vgg13', 'vgg13', '12');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'vgg16', 'vgg16', '13');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'vgg19', 'vgg19', '14');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'vgg11_bn', 'vgg11_bn', '15');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'vgg13_bn', 'vgg13_bn', '16');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'vgg16_bn', 'vgg16_bn', '17');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'vgg19_bn', 'vgg19_bn', '18');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'squeezenet1_0', 'squeezenet1_0', '19');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'squeezenet1_1', 'squeezenet1_1', '20');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'inception_v3', 'inception_v3', '21');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'densenet121', 'densenet121', '22');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'densenet169', 'densenet169', '23');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'densenet201', 'densenet201', '24');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'densenet161', 'densenet161', '25');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'googlenet', 'googlenet', '26');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'mobilenet_v2', 'mobilenet_v2', '27');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'mnasnet0_5', 'mnasnet0_5', '28');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'mnasnet0_75', 'mnasnet0_75', '29');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'mnasnet1_0', 'mnasnet1_0', '30');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'mnasnet1_3', 'mnasnet1_3', '31');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'shufflenet_v2_x0_5', 'shufflenet_v2_x0_5', '32');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'shufflenet_v2_x1_0', 'shufflenet_v2_x1_0', '33');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'shufflenet_v2_x1_5', 'shufflenet_v2_x1_5', '34');
	INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (38, 'shufflenet_v2_x2_0', 'shufflenet_v2_x2_0', '35');



	-- auth服务 系统客户端认证数据初始化
	INSERT INTO `oauth_client_details` (`client_id`, `resource_ids`, `client_secret`, `scope`, `authorized_grant_types`, `web_server_redirect_uri`, `authorities`, `access_token_validity`, `refresh_token_validity`, `additional_information`, `autoapprove`) VALUES
		('dubhe-client', NULL, '$2a$10$RUYBRsyV2jpG7pvg/VNus.YHVebzfRen3RGeDe1LVEIJeHYe2F1YK', 'all', 'authorization_code,password,refresh_token', 'http://localhost:8866/oauth/callback', NULL, 3600, 2592000, NULL, NULL);

	IF t_error = 1 THEN
				ROLLBACK;
			ELSE
				COMMIT;
			END IF;
	   select t_error;
	END ;
call fourthEditionProc();//
DROP PROCEDURE IF EXISTS  fourthEditionProc ;
delimiter ;