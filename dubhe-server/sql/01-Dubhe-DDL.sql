-- DDL 脚本
use `dubhe-cloud-prod`;

-- 原boot单体项目 DDL 脚本内容
CREATE TABLE IF NOT EXISTS `data_dataset` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL,
    `remark` varchar(255) DEFAULT NULL,
    `type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '类型 0: private 私有数据,  1:team  团队数据  2:public 公开数据',
    `team_id` bigint(20) DEFAULT NULL,
    `uri` varchar(255) DEFAULT '' COMMENT '数据集存储位置',
    `create_user_id` bigint(20) DEFAULT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_user_id` bigint(20) DEFAULT NULL,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` bit(1) DEFAULT NULL,
    `data_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '数据类型:0图片，1视频，2文本',
    `annotate_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '标注类型：2分类,1目标检测,5目标跟踪',
    `labels` varchar(255) NOT NULL DEFAULT '' COMMENT '标签集合，以逗号分隔',
    `status` int(11) NOT NULL DEFAULT '0' COMMENT '101:未标注  102:手动标注中  103:自动标注中  104:自动标注完成  105:标注完成  201:目标跟踪中  202:目标跟踪完成  203:目标跟踪失败  301:未采样  302:采样中  303:采样失败  401:增强中  402:导入中',
    `current_version_name` varchar(16) DEFAULT NULL COMMENT '当前版本号',
    `is_import` tinyint(1) DEFAULT '0' COMMENT '是否用户导入',
    `archive_url` varchar(255) DEFAULT NULL COMMENT '用户导入数据集压缩包地址',
    `decompress_state` tinyint(2) DEFAULT '0' COMMENT '解压状态: 0未解压 1解压中 2解压完成 3解压失败',
    `decompress_fail_reason` varchar(255) DEFAULT NULL COMMENT '解压失败原因',
    `is_top` tinyint(1) DEFAULT NULL COMMENT '是否为置顶',
    `origin_user_id` bigint(20) DEFAULT NULL COMMENT '资源拥有人id',
    `label_group_id` bigint(20) DEFAULT NULL COMMENT '标签组ID',
    `source_id` bigint(20) DEFAULT NULL COMMENT '数据集源ID',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT = 1 COMMENT='数据集管理';

CREATE TABLE IF NOT EXISTS `data_dataset_label`  (
   `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
   `dataset_id` bigint(20) UNSIGNED NOT NULL,
   `label_id` bigint(20) UNSIGNED NOT NULL,
   `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
   `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除(0正常，1已删除)',
   `create_user_id` bigint(20) NULL DEFAULT NULL COMMENT '创建人id',
   `update_user_id` bigint(20) NULL DEFAULT NULL COMMENT '修改人id',
   PRIMARY KEY (`id`) USING BTREE,
   UNIQUE INDEX `dataset_id`(`dataset_id`, `label_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT='数据集标签';

CREATE TABLE IF NOT EXISTS `data_dataset_version` (
    `id` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `dataset_id` bigint(19) DEFAULT NULL COMMENT '数据集ID',
    `team_id` bigint(19) DEFAULT NULL COMMENT '团队ID',
    `create_user_id` bigint(19) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_user_id` bigint(19) DEFAULT NULL COMMENT '修改人',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '数据集版本删除标记0正常，1已删除',
    `version_name` varchar(8) NOT NULL COMMENT '版本号',
    `version_note` varchar(50) NOT NULL COMMENT '版本说明',
    `version_source` varchar(32) DEFAULT NULL COMMENT '来源版本号',
    `version_url` varchar(255) DEFAULT NULL COMMENT '版本信息存储url',
    `data_conversion` int(1) NOT NULL DEFAULT '0' COMMENT '数据转换；0：未复制；1：已复制;2:转换完成,3:转换失败',
    `origin_user_id` bigint(20) DEFAULT NULL COMMENT '资源用有人ID',
    `of_record` tinyint(1) DEFAULT '0' COMMENT '是否生成ofRecord文件',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `unique_version` (`dataset_id`,`version_name`) COMMENT '数据集版本号唯一'
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='数据集版本表';

CREATE TABLE IF NOT EXISTS `data_dataset_version_file` (
     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
     `dataset_id` bigint(20) DEFAULT NULL COMMENT '数据集ID',
     `version_name` varchar(8) DEFAULT NULL COMMENT '数据集版本',
     `file_id` bigint(20) DEFAULT NULL COMMENT '文件ID',
     `status` tinyint(1) NOT NULL DEFAULT '2' COMMENT '状态 0: 新增 1:删除 2:正常',
     `annotation_status` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '101:未标注  102:手动标注中  103:自动标注完成  104:标注完成  105:标注未识别  201:目标跟踪完成',
     `backup_status` tinyint(3) unsigned DEFAULT '0' COMMENT '数据集状态备份,版本切换使用',
     `changed` bit(1) DEFAULT b'0' COMMENT '0 - 未改变,1 - 改变',
     `file_name` varchar(255) DEFAULT '' COMMENT '文件名称',
     PRIMARY KEY (`id`) USING BTREE,
     KEY `select_status` (`dataset_id`,`version_name`) USING BTREE,
     KEY `dataset_id_annotation_status` (`dataset_id`,`annotation_status`,`version_name`) USING BTREE,
     KEY `select_file` (`dataset_id`,`status`,`annotation_status`,`version_name`,`file_id`),
     KEY `file_state_annotation_finished` (`dataset_id`,`file_id`,`version_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='数据集版本文件关系表';

CREATE TABLE IF NOT EXISTS `data_file` (
     `id` bigint(20) unsigned zerofill NOT NULL AUTO_INCREMENT COMMENT 'ID',
     `name` varchar(255) NOT NULL DEFAULT '' COMMENT '文件名',
     `status` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '状态:0-未标注，1-标注中，2-自动标注完成，3-已标注完成,4-目标追踪完成',
     `dataset_id` bigint(20) DEFAULT NULL COMMENT '数据集id',
     `url` varchar(255) NOT NULL DEFAULT '' COMMENT '资源访问路径',
     `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建用户ID',
     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新用户ID',
     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
     `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '0正常，1已删除',
     `file_type` tinyint(4) DEFAULT '0' COMMENT '文件类型  0-图片,1-视频',
     `pid` bigint(20) DEFAULT '0' COMMENT '父文件id',
     `frame_interval` int(11) DEFAULT '0' COMMENT '帧间隔',
     `enhance_type` smallint(3) DEFAULT NULL COMMENT '增强类型',
     `width` int(11) DEFAULT NULL COMMENT '图片宽',
     `height` int(11) DEFAULT NULL COMMENT '图片高',
     `origin_user_id` bigint(19) DEFAULT NULL COMMENT '资源拥有者ID',
     `es_transport` int(1) DEFAULT '0' COMMENT '是否上传至es',
     `exclude_header` smallint(6) NOT NULL DEFAULT '1' COMMENT 'table数据导入时，是否排除文件头',
     PRIMARY KEY (`id`) USING BTREE,
     UNIQUE KEY `name_uniq` (`name`,`dataset_id`,`deleted`) USING BTREE,
     KEY `deleted` (`deleted`) USING BTREE,
     KEY `dataset_upt_time` (`dataset_id`,`update_time`) USING BTREE,
     KEY `uuid` (`url`,`deleted`) USING BTREE,
     KEY `status` (`dataset_id`,`status`,`deleted`) USING BTREE,
     KEY `es_transport` (`es_transport`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='文件信息';

CREATE TABLE IF NOT EXISTS `data_file_annotation` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `dataset_id` bigint(20) NOT NULL COMMENT '数据集ID',
    `label_id` bigint(20) NOT NULL COMMENT '标签ID',
    `version_file_id` bigint(20) NOT NULL COMMENT '版本文件ID',
    `prediction` double DEFAULT '0' COMMENT '预测值',
    `create_user_id` bigint(20) DEFAULT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_user_id` bigint(20) DEFAULT NULL,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` bit(1) NOT NULL DEFAULT b'0',
    `file_name` varchar(255) DEFAULT NULL COMMENT '文件名称',
    `status` tinyint(1) DEFAULT '0' COMMENT '状态 0: 新增 1:删除 2:正常',
    `invariable` tinyint(1) DEFAULT '0' COMMENT '是否为版本标注信息0：否 1：是',
    PRIMARY KEY (`id`),
    KEY `version_file_index` (`version_file_id`) USING BTREE,
    KEY `label_dataset_id_index` (`label_id`,`dataset_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='数据集文件标注表';

CREATE TABLE IF NOT EXISTS `data_group_label` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `label_id` bigint(20) DEFAULT NULL COMMENT '标签Id',
    `label_group_id` bigint(20) DEFAULT NULL COMMENT '标签组Id',
    `create_user_id` bigint(20) DEFAULT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_user_id` bigint(20) DEFAULT NULL,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` bit(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='标签组标签中间表';

CREATE TABLE IF NOT EXISTS `data_label` (
      `id` bigint(20) NOT NULL AUTO_INCREMENT,
      `name` varchar(255) NOT NULL DEFAULT '',
      `color` varchar(7) NOT NULL DEFAULT '#000000',
      `create_user_id` bigint(20) DEFAULT NULL,
      `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
      `update_user_id` bigint(20) DEFAULT NULL,
      `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      `deleted` bit(1) NOT NULL DEFAULT b'0',
      `type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '标签类型 0:自定义标签 1:自动标注标签 2:ImageNet 3: MS COCO',
      PRIMARY KEY (`id`) USING BTREE,
      KEY `dataset` (`name`,`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='数据集标签';

CREATE TABLE IF NOT EXISTS `data_label_group` (
      `id` bigint(20) NOT NULL AUTO_INCREMENT,
      `name` varchar(255) NOT NULL DEFAULT '' COMMENT '标签组名称',
      `create_user_id` bigint(20) DEFAULT NULL,
      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
      `update_user_id` bigint(20) DEFAULT NULL,
      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      `deleted` bit(1) NOT NULL DEFAULT b'0',
      `remark` varchar(255) DEFAULT NULL COMMENT '描述',
      `type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '类型 0: private 私有标签组,  1:public 公开标签组',
      `origin_user_id` bigint(20) DEFAULT NULL COMMENT '资源拥有人',
      `operate_type` int(11) DEFAULT NULL COMMENT '操作类型 1:Json编辑器操作类型 2:自定义操作类型 3:导入操作类型',
      `label_group_type` int(1) NOT NULL DEFAULT '0' COMMENT '标签组数据类型  0:视觉  1:文本',
      PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='标签组';

CREATE TABLE IF NOT EXISTS `data_lesion_slice` (
     `id` bigint(20) NOT NULL AUTO_INCREMENT,
     `create_user_id` bigint(20) DEFAULT NULL,
     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
     `update_user_id` bigint(20) DEFAULT NULL,
     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
     `lesion_order` int(11) NOT NULL COMMENT '序号',
     `slice_desc` varchar(255) DEFAULT NULL COMMENT '病灶层面',
     `medicine_id` bigint(20) DEFAULT NULL COMMENT '数据集ID',
     `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '0正常，1已删除',
     `draw_info` text,
     `origin_user_id` bigint(19) DEFAULT NULL COMMENT '资源拥有者ID',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='数据病变切片';

CREATE TABLE IF NOT EXISTS `data_medicine` (
     `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
     `name` varchar(255) NOT NULL COMMENT '数据集名称',
     `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建用户ID',
     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新用户ID',
     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
     `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '0-正常 1-已删除',
     `status` smallint(4) DEFAULT NULL COMMENT '状态 101-未标注 103-自动标注中 104-自动标注完成 105-完成',
     `patient_id` varchar(50) DEFAULT NULL COMMENT '检查号(CT号或CR、DR号)',
     `study_instance_uid` varchar(64) DEFAULT NULL COMMENT '研究实例UID',
     `series_instance_uid` varchar(64) DEFAULT NULL COMMENT '序列实例UID',
     `modality` varchar(50) DEFAULT NULL COMMENT '模式',
     `body_part_examined` varchar(50) DEFAULT NULL COMMENT '部位',
     `merge_annotation` text COMMENT '自动标注内容合并后结果',
     `remark` varchar(255) DEFAULT NULL,
     `origin_user_id` bigint(20) NOT NULL COMMENT '资源拥有人',
     `type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '类型 0: private 私有数据,  1:team  团队数据  2:public 公开数据',
     `annotate_type` smallint(5) NOT NULL DEFAULT '0' COMMENT '标注类型: 1.器官分割 2.病灶检测之肺结节检测',
     PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='医学数据集';

CREATE TABLE IF NOT EXISTS `data_medicine_file` (
      `id` bigint(20) unsigned zerofill NOT NULL AUTO_INCREMENT COMMENT 'ID',
      `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建用户ID',
      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新用户ID',
      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
      `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '0-正常 1-已删除',
      `medicine_id` bigint(20) DEFAULT NULL COMMENT '数据集ID',
      `name` varchar(225) DEFAULT NULL COMMENT '文件名称',
      `url` varchar(255) DEFAULT NULL COMMENT '文件地址',
      `instance_number` smallint(5) DEFAULT NULL COMMENT '实例序号',
      `sop_instance_uid` varchar(64) DEFAULT NULL COMMENT 'SOP实例UID',
      `origin_user_id` bigint(20) DEFAULT NULL COMMENT '资源拥有人',
      `status` tinyint(4) DEFAULT NULL COMMENT '状态 101-未标注 103-自动标注完成 104-完成',
      `image_position_patient` double(11,1) DEFAULT NULL,
      PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='医学数据集';

CREATE TABLE IF NOT EXISTS `data_sequence` (
     `id` int(11) NOT NULL,
     `business_code` varchar(50) NOT NULL,
     `start` int(11) NOT NULL,
     `step` int(11) NOT NULL,
     PRIMARY KEY (`id`),
     UNIQUE KEY `business_code_unique` (`business_code`) USING BTREE
) ENGINE=InnoDB COMMENT='自定义获取主键ID表';

CREATE TABLE IF NOT EXISTS `data_task`  (
  `id` bigint(20) UNSIGNED ZEROFILL NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `total` int(11) NOT NULL DEFAULT 0 COMMENT '任务需要处理的文件总数',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '任务状态 0.待分配 1.分配中 2.进行中 3.已完成 4.失败',
  `finished` int(11) NOT NULL DEFAULT 0 COMMENT '已完成的文件数',
  `failed` int(11) NOT NULL DEFAULT 0 COMMENT '失败文件数量',
  `files` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '文件id数组',
  `dataset_id` bigint(20) NULL DEFAULT NULL COMMENT '数据集ID',
  `annotate_type` tinyint(4) NOT NULL DEFAULT 0 COMMENT '标注类型：0分类,1目标检测',
  `data_type` tinyint(4) NOT NULL DEFAULT 0 COMMENT '数据类型:0图片，1视频',
  `labels` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '该自动标注任务使用的标签数组，json串形式',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '0正常，1已删除',
  `datasets` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '数据集id数组',
  `create_user_id` bigint(20) NULL DEFAULT NULL COMMENT '创建用户ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `type` smallint(3) NULL DEFAULT NULL COMMENT '任务类型 0.自动标注 1.ofrecord 2.imageNet 3.数据增强 4.目标跟踪 5.视频采样',
  `dataset_version_id` bigint(20) NULL DEFAULT NULL COMMENT '数据集版本ID',
  `enhance_type` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '增强类型数组',
  `url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '视频文件url',
  `frame_interval` int(11) NULL DEFAULT NULL COMMENT '视频帧间隔',
  `merge_column` varchar(255) DEFAULT NULL COMMENT 'csv合并列',
  `version_name` varchar(255) DEFAULT NULL COMMENT '转预置版本号',
  `target_id` bigint(20) NULL DEFAULT NULL COMMENT '目标数据集id',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `deleted` (`deleted`) USING BTREE,
  KEY `ds_status` (`datasets`,`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '标注任务信息';

create table if not exists dict
(
    id          bigint auto_increment              primary key,
    name        varchar(255)                       not null,
    remark      varchar(255)                       null,
    create_time datetime default CURRENT_TIMESTAMP null,
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    charset = utf8;

create table if not exists dict_detail
(
    id          bigint auto_increment              primary key,
    dict_id     bigint                             null,
    label       varchar(255)                       not null,
    value       varchar(255)                       not null,
    sort        bigint   default 999               null,
    create_time datetime default CURRENT_TIMESTAMP null,
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    charset = utf8;

create table harbor_project
(
	id bigint unsigned auto_increment comment '主键ID'
		primary key,
	image_name varchar(100) not null comment '镜像名称',
	create_resource tinyint default 0 not null comment '0 - NOTEBOOK模型管理  1- ALGORITHM算法管理',
	create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
	create_user_id bigint null comment '创建用户ID',
	update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
	update_user_id bigint null comment '更新用户ID',
	deleted bit default b'0' null comment '0正常，1已删除',
	sync_status tinyint(1) default 0 null comment '同步状态(默认为0，同步成功为1)',
	constraint image_name
		unique (image_name)
)
comment 'harbor project表' charset=utf8;

create table if not exists log
(
    id               bigint auto_increment
        primary key,
    browser          varchar(255) null,
    create_time      datetime     null,
    description      varchar(255) null,
    exception_detail text         null,
    log_type         varchar(255) null,
    method           varchar(255) null,
    params           text         null,
    request_ip       varchar(255) null,
    time             bigint       null,
    username         varchar(255) null
)
    charset = utf8;

create table if not exists menu
(
    id             bigint   auto_increment            primary key,
    pid            bigint   default 0                 not null  COMMENT '上级菜单ID',
    type           int      default 0                 not null  COMMENT '菜单类型: 0目录，1页面，2权限，3外链' ,
    name           varchar(255)                       null      COMMENT '名称',
    icon           varchar(255)                       null      COMMENT '菜单图标',
    path           varchar(255)                       null      COMMENT '路径或外链URL',
    component      varchar(255)                       null      COMMENT '组件路径',
    component_name varchar(255)                       null      COMMENT '路由名称',
    layout         varchar(255)                       null      COMMENT '页面布局类型',
    permission     varchar(255)                       null      COMMENT '权限标识',
    back_to     varchar(255)                       null      COMMENT '上级菜单',
    ext_config     varchar(255)                       null      COMMENT '扩展配置',
    hidden         bit      default b'0'              null      COMMENT '菜单栏不显示',
    cache          bit      default b'0'              null      COMMENT '路由缓存 keep-alive',
    sort           bigint   default 999               null      COMMENT '菜单排序',
    create_user_id bigint(20) DEFAULT NULL COMMENT '创建人id',
    update_user_id bigint(20) DEFAULT NULL COMMENT '修改人id',
    create_time    datetime default CURRENT_TIMESTAMP null,
    update_time    datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    deleted bit(1) DEFAULT 0 COMMENT '删除标记 0正常，1已删除'
)
    charset = utf8;


create table if not exists `notebook` (
	`id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
	`origin_user_id` BIGINT(20) NOT NULL COMMENT '所属用户ID',
	`name` VARCHAR(100) NOT NULL COMMENT 'notebook名称(供K8S使用)',
	`notebook_name` VARCHAR(100) NULL DEFAULT NULL COMMENT 'notebook名称(供前端使用)',
	`description` VARCHAR(255) NULL DEFAULT NULL COMMENT '描述',
	`url` VARCHAR(255) NULL DEFAULT NULL COMMENT '访问 notebook 在 Jupyter 里所需的url',
	`total_run_min` INT(11) NOT NULL DEFAULT '0' COMMENT '运行总时间(分钟)',
	`cpu_num` INT(11) NOT NULL DEFAULT '0' COMMENT 'CPU数量(核)',
	`gpu_num` INT(11) NOT NULL DEFAULT '0' COMMENT 'GPU数量（核）',
	`mem_num` INT(11) NOT NULL DEFAULT '0' COMMENT '内存大小（M）',
	`disk_mem_num` INT(11) NOT NULL DEFAULT '0' COMMENT '硬盘内存大小（M）',
	`status` TINYINT(4) NOT NULL DEFAULT '1' COMMENT '0运行，1停止, 2删除, 3启动中，4停止中，5删除中，6运行异常（暂未启用）',
	`status_detail` json default null comment '状态对应的详情信息',
	`last_start_time` TIMESTAMP NULL DEFAULT NULL COMMENT '上次启动执行时间',
	`last_operation_timeout` BIGINT(20) NULL DEFAULT NULL COMMENT '上次操作对应超时时间点（20200603121212）',
	`k8s_status_code` VARCHAR(100) NULL DEFAULT NULL COMMENT 'k8s响应状态码',
	`create_resource` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '0 - notebook 创建 1- 其它系统创建',
	`k8s_status_info` VARCHAR(255) NULL DEFAULT NULL COMMENT 'k8s响应状态信息',
	`k8s_namespace` VARCHAR(255) NOT NULL COMMENT 'k8s中namespace',
	`k8s_resource_name` VARCHAR(255) NOT NULL COMMENT 'k8s中资源名称',
	`k8s_image_name` VARCHAR(255) NOT NULL COMMENT 'k8s中jupyter的镜像名称',
	`k8s_pvc_path` VARCHAR(255) NOT NULL COMMENT 'k8s中pvc存储路径',
	`k8s_mount_path` VARCHAR(255) NOT NULL DEFAULT '/notebook' COMMENT 'k8s中容器路径',
	`create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`create_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '创建用户ID',
	`update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	`update_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '更新用户ID',
	`deleted` BIT(1) NULL DEFAULT b'0' COMMENT '0正常，1已删除',
	`data_source_name` VARCHAR(255) NULL DEFAULT NULL COMMENT '数据集名称',
	`data_source_path` VARCHAR(255) NULL DEFAULT NULL COMMENT '数据集路劲',
	`algorithm_id` BIGINT(20) NULL DEFAULT '0' COMMENT '算法ID',
	PRIMARY KEY (`id`),
	INDEX `status` (`status`),
	INDEX `user_id` (`origin_user_id`),
	INDEX `name` (`name`),
	INDEX `last_operation_timeout` (`last_operation_timeout`),
	INDEX `k8s_namespace` (`k8s_namespace`),
	INDEX `k8s_resource_name` (`k8s_resource_name`)
)
COMMENT='notebook数据表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;

create table if not exists pt_dev_envs
(
    id             bigint auto_increment comment '主键'
        primary key,
    name           varchar(255)     not null comment '名称',
    remark         varchar(255)     null comment '描述',
    type           varchar(255)     not null comment '类型 ',
    cpu_num        int              not null comment 'CPU数量',
    gpu_num        int              not null comment 'GPU数量',
    mem_num        int              not null comment '内存大小单位M',
    pod_num        int              not null comment 'POD数量',
    status         varchar(255)     not null comment '状态 对应k8s的状态',
    dataset_id     bigint           null comment '数据集ID',
    image_id       bigint           null comment '镜像ID',
    storage_id     bigint           null comment '存储ID',
    duration       int              null comment '时长',
    start_time     datetime         null comment '开始时间',
    close_time     datetime         null comment '释放时间',
    create_time    datetime         null comment '创建时间',
    update_time    datetime         null comment '修改时间',
    create_user_id bigint           null comment '创建人ID',
    update_user_id bigint           null comment '修改人ID',
    team_id        bigint           null comment '团队ID',
    deleted        bit default b'0' null comment '0正常，1已删除'
)
    charset = utf8;

create table if not exists pt_job_param
(
    id             bigint auto_increment comment '主键id'
        primary key,
    train_job_id   bigint                  not null comment '训练作业jobId',
    algorithm_id   bigint                  not null comment '算法来源id',
    run_params     json                    null comment '运行参数(算法来源为我的算法时为调优参数，算法来源为预置算法时为运行参数)',
    param_f1       varchar(255) default '' null comment 'F1值',
    param_callback varchar(255) default '' null comment '召回率',
    param_precise  varchar(255) default '' null comment '精确率',
    param_accuracy varchar(255) default '' null comment '准确率',
    create_user_id bigint                  null comment '创建人',
    deleted        tinyint(1)   default 0  null comment '删除(0正常，1已删除)',
    create_time    timestamp               null comment '创建时间',
    update_user_id bigint                  null comment '更新人',
    update_time    timestamp               null comment '更新时间',
    run_command    varchar(255) default '' null COMMENT '运行命令',
    image_name     varchar(127) default '' null COMMENT '镜像名称',
    delay_create_time    timestamp         null comment '创建时间',
    delay_delete_time    timestamp         null comment '创建时间'

)
    comment 'job运行参数及结果表' charset = utf8mb4;

create table if not exists pt_model_branch
(
    id               bigint auto_increment comment '主键'
        primary key,
    parent_id        bigint                             null comment '父ID',
    version          varchar(8)                         not null comment '版本号',
    url              varchar(255)                       not null comment '模型地址',
    model_path       varchar(255)                       not null comment '模型存储地址',
    model_source     tinyint(3)                         not null comment '模型来源（用户上传、平台生成、优化后导入）',
    create_user_id   bigint                             null comment '创建用户ID',
    update_user_id    bigint                            null comment '更新用户ID',
    team_id          bigint                             null comment '团队ID',
    create_time      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间',
    deleted          bit      default b'0'              not null comment '0 正常，1 已删除',
    algorithm_id     bigint                             null comment '算法ID',
    algorithm_name   varchar(255)                       null comment '算法名称',
    algorithm_source tinyint(1)                         null comment '算法来源(1为我的算法，2为预置算法)',
    status           tinyint(3)                         null comment '文件拷贝状态(0文件拷贝中，1文件拷贝成功，2文件拷贝失败)',
    origin_user_id   bigint                             null comment '资源用有人ID'
)
    comment '分支管理' charset = utf8;

create table if not exists pt_model_info
(
    id                bigint auto_increment comment '主键'
        primary key,
    name              varchar(255)                       not null comment '模型名称',
    frame_type        tinyint                            not null comment '框架类型',
    model_format      tinyint                            not null comment '模型文件的格式（后缀名）',
    model_description varchar(255)                       null comment '模型描述',
    model_type        varchar(255)                       not null comment '模型分类',
    url               varchar(255)                       null comment '模型地址',
    model_version     varchar(8)                         null comment '模型版本',
    create_user_id    bigint                             null comment '创建用户ID',
    update_user_id    bigint                             null comment '更新用户ID',
    team_id           bigint                             null comment '组ID',
    deleted           bit      default b'0'              not null comment '0 正常，1 已删除',
    create_time       datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time       datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间',
    model_resource    tinyint  default 0                 null comment '模型是否为预置模型（0默认模型，1预置模型）',
    total_num         bigint   default 0                 null comment '模型版本总的个数',
    origin_user_id    bigint                             null comment '资源用有人ID',
    tags              json                               null comment 'tag信息',
    packaged          tinyint  default 0                 not null comment '模型是否已经打包，0未打包，1打包完成'
)
    comment '模型管理' charset = utf8;

create table if not exists pt_model_type
(
    id                bigint auto_increment comment '主键'
        primary key,
    frame_type        tinyint                            not null comment '框架类型',
    model_type        varchar(255)                       not null comment '模型文件的格式',
    create_user_id    bigint                             null comment '创建用户ID',
    update_user_id    bigint                             null comment '更新用户ID',
    deleted           bit      default b'0'              not null comment '0 正常，1 已删除',
    create_time       datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time       datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间',
    origin_user_id    bigint                             null comment '资源用有人ID'
)
    comment '模型格式' charset = utf8;

create table if not exists pt_model_suffix
(
    id                bigint auto_increment comment '主键'
        primary key,
    model_type        tinyint                            not null comment '模型文件的格式',
    model_suffix      varchar(255)  default ''           not null comment '模型文件的格式对应后缀名',
    create_user_id    bigint                             null comment '创建用户ID',
    update_user_id    bigint                             null comment '更新用户ID',
    deleted           bit      default b'0'              not null comment '0 正常，1 已删除',
    create_time       datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time       datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间',
    origin_user_id    bigint                             null comment '资源用有人ID'
)
    comment '模型后缀名' charset = utf8;

create table if not exists pt_project_template
(
    id             bigint auto_increment comment '主键'
        primary key,
    name           varchar(255)     not null comment '名称',
    remark         varchar(255)     null comment '描述',
    type           varchar(255)     not null comment '类型 ',
    dataset_id     bigint           null comment '数据集ID',
    image_id       bigint           null comment '镜像ID',
    code_url       varchar(255)     null comment '代码地址',
    cmd            varchar(255)     not null comment '命令行',
    create_time    datetime         not null comment '创建时间',
    update_time    datetime         null comment '修改时间',
    create_user_id bigint           null comment '创建人ID',
    update_user_id bigint           null comment '修改人ID',
    team_id        bigint           null comment '团队ID',
    deleted        bit default b'0' null comment '0正常，1已删除'
)
    charset = utf8;

create table if not exists pt_storage
(
    id             bigint auto_increment comment '主键'
        primary key,
    name           varchar(255)     not null comment '名称',
    type           varchar(255)     not null comment '类型 ',
    size           int              not null comment '存储大小，单位M',
    storage_class  varchar(255)     null comment '对应k8s pvc的 storageClass',
    create_time    datetime         not null comment '创建时间',
    create_user_id bigint           null comment '创建人ID',
    update_user_id bigint           null comment '修改人ID',
    update_time    datetime         null comment '修改时间',
    team_id        bigint           null comment '团队ID',
    deleted        bit default b'0' null comment '0正常，1已删除'
)
    charset = utf8;

create table if not exists pt_train
(
    id             bigint auto_increment comment '主键id'
        primary key,
    train_name     varchar(64)          not null comment '训练作业名',
    version_num    int(8)     default 1 not null comment '训练作业job有效版本数量',
    total_num      int(8)     default 1 not null comment '训练作业总版本数',
    deleted        tinyint(1) default 0 not null comment '删除(0正常，1已删除)',
    create_user_id bigint               null     comment '创建人',
    create_time    timestamp            null     comment '创建时间',
    update_user_id bigint               null     comment '更新人',
    update_time    timestamp            null     comment '更新时间',
    train_key      varchar(32)          null,
    origin_user_id bigint               null     comment '资源拥有者ID'
)
    comment '训练作业主表' charset = utf8mb4;

create index idx_user_id
    on pt_train (create_user_id);

create table if not exists pt_train_algorithm
(
    id                 bigint auto_increment comment '主键'
        primary key,
    algorithm_name     varchar(255)            not null comment '算法名称',
    description        varchar(255) default '' null comment '算法描述',
    algorithm_source   tinyint(1)              not null comment '算法来源(1为我的算法，2为预置算法)',
    algorithm_status   tinyint(1)   default 0  not null comment '算法上传状态，0：创建中，1：创建成功，2：创建失败',
    code_dir           varchar(255) default '' null comment '代码目录',
    run_command        varchar(255) default '' null comment '运行命令',
    run_params         json                    null comment '运行参数',
    algorithm_usage    varchar(255) default '' null comment '算法用途',
    accuracy           varchar(255) default '' null comment '算法精度',
    p4_inference_speed int                     null comment 'P4推理速度（ms）',
    create_user_id     bigint                  null comment '创建人',
    create_time        timestamp               null comment '创建时间',
    update_user_id     bigint                  null comment '更新人',
    update_time        timestamp               null comment '更新时间',
    deleted            tinyint(1)   default 0  not null comment '删除(0正常，1已删除)',
    image_name         varchar(127)            null,
    is_train_model_out       tinyint(1)   default 1  null comment '是否输出训练结果:1是，0否',
    is_train_out      tinyint(1)   default 1  null comment '是否输出作业信息:1是，0否',
    is_visualized_log  tinyint(1)   default 0  null comment '是否输出可视化日志:1是，0否',
    inference          tinyint(1)   default 0  not null comment '算法文件是否可推理（1可推理，0不可推理）',
     origin_user_id    bigint                  null comment '资源拥有者ID'
)
    comment '训练算法表' charset = utf8mb4;

create table if not exists pt_train_job
(
    id                   bigint auto_increment comment '主键id'
        primary key,
    train_id             bigint                                 not null comment '训练作业id',
    train_version        varchar(32)                            not null comment 'job版本',
    parent_train_version varchar(32)                            null comment 'job父版本',
    job_name             varchar(64)                            not null comment '任务名称',
    description          varchar(255) default ''                null comment '描述',
    runtime              varchar(32)  default ''                null comment '运行时长',
    model_path             varchar(128) default ''                null comment '训练输出位置',
    out_path             varchar(128) default ''                null comment '作业输出路径',
    resources_pool_type  tinyint(1)   default 0                 not null comment '类型(0为CPU，1为GPU)',
    resources_pool_specs varchar(128)                           null comment '规格',
    resources_pool_node  int(8)       default 1                 not null comment '节点个数',
    train_status         tinyint(1)   default 0                 not null comment '训练作业job状态, 0为待处理，1为运行中，2为运行完成，3为失败，4为停止，5为未知，6为删除，7为创建失败)',
    status_detail        json         default                   null comment '状态对应的详情信息',
    `train_type` TINYINT(1) UNSIGNED ZEROFILL NULL DEFAULT '0' COMMENT '训练类型 0：普通训练，1：分布式训练',
    deleted              tinyint(1)   default 0                 null comment '删除(0正常，1已删除)',
    create_user_id       bigint                                 null comment '创建人',
    create_time          timestamp                              null comment '创建时间',
    update_user_id       bigint                                 null comment '更新人',
    update_time          timestamp                              null comment '更新时间',
    visualized_log_path  varchar(128) default ''                null comment '可视化日志路径',
    data_source_name     varchar(127)                           null comment '数据集名称',
    data_source_path     varchar(127)                           null comment '数据集路径',
    train_job_specs_name varchar(32)                            null comment '训练规格名称',
    k8s_job_name         varchar(70)                            null comment 'k8s创建好的job名称',
    val_data_source_name varchar(127)                           null comment '验证数据集名称',
    val_data_source_path varchar(255)                           null comment '验证数据集路径',
    val_type             tinyint(1)   default 0                 null comment '是否验证数据集',
    origin_user_id       bigint                                 null comment '资源拥有者ID',
    train_msg            varchar(128)                           null comment '训练信息(失败信息)',
    model_id             bigint                                 null comment '模型id',
    model_branch_id      bigint                                 null comment '模型对应版本id',
    model_resource       tinyint(1)                             null comment '模型类型(0我的模型1预置模型2炼知模型)',
    teacher_model_ids    varchar(255)                           null comment '教师模型ids',
    student_model_ids    varchar(255)                           null comment '学生模型ids',
    constraint inx_tran_id_version
        unique (train_id, train_version)
)
    comment '训练作业job表' charset = utf8mb4;

create index inx_create_user_id
    on pt_train_job (create_user_id);

create table if not exists pt_train_param
(
    id                   bigint auto_increment comment '主键id'
        primary key,
    param_name           varchar(128)            not null comment '任务参数名称',
    description          varchar(256) default '' null comment '描述',
    algorithm_id         bigint                  not null comment '算法id',
    out_path             varchar(128) default '' null comment '输出路径',
    run_params           json                    null comment '运行参数(算法来源为我的算法时为调优参数，算法来源为预置算法时为运行参数)',
    algorithm_source     tinyint(1)   default 1  not null comment '算法来源(1为我的算法，2为预置算法)',
    log_path             varchar(128) default '' null comment '日志输出路径',
    resources_pool_type  tinyint(1)   default 0  not null comment '类型(0为CPU，1为GPU)',
    resources_pool_specs varchar(128)            null comment '规格',
    resources_pool_node  int(8)       default 1  not null comment '节点个数',
    deleted              tinyint(1)   default 0  null comment '删除(0正常，1已删除)',
    create_user_id       bigint                  null comment '创建人',
    create_time          timestamp               null comment '创建时间',
    update_user_id       bigint                  null comment '更新人',
    update_time          timestamp               null comment '更新时间',
    data_source_name     varchar(127)            null comment '数据集名称',
    data_source_path     varchar(127)            null comment '数据集路径',
    run_command          varchar(255) default '' null COMMENT '运行命令',
    image_name           varchar(127) default '' null COMMENT '镜像名称',
    train_job_specs_name  varchar(32)            null COMMENT '训练规格名称',
    origin_user_id       bigint                  null comment '资源拥有者ID',
    `train_type`         TINYINT(1) UNSIGNED ZEROFILL NULL DEFAULT '0' COMMENT '训练类型 0：普通训练，1：分布式训练',
    val_data_source_name varchar(127)            null comment '验证数据集名称',
    val_data_source_path varchar(127)            null comment '验证数据集路径',
    val_type             tinyint(1)   default 0  null comment '是否验证数据集(0否，1是)',
    model_id             bigint                                 null comment '模型id',
    model_branch_id      bigint                                 null comment '模型对应版本id',
    model_resource       tinyint(1)                             null comment '模型类型(0我的模型1预置模型2炼知模型)',
    teacher_model_ids    varchar(255)                           null comment '教师模型ids',
    student_model_ids    varchar(255)                           null comment '学生模型ids'
)
    comment '任务参数表' charset = utf8mb4;

create table if not exists role
(
    id          bigint auto_increment
        primary key,
    name        varchar(255)                       not null,
    permission  varchar(255)                       null,
    remark      varchar(255)                       null,
        create_user_id bigint(20) DEFAULT NULL COMMENT '创建人id',
    update_user_id bigint(20) DEFAULT NULL COMMENT '修改人id',
    deleted bit(1) DEFAULT 0 COMMENT '删除标记 0正常，1已删除',
    create_time datetime default CURRENT_TIMESTAMP null,
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    charset = utf8;

create table if not exists roles_menus
(
    role_id bigint not null,
    menu_id bigint not null,
    primary key (role_id, menu_id)
)
    charset = utf8;

create table if not exists service
(
    id             int auto_increment comment '主键'
        primary key,
    model_id       int          null comment '模型id',
    model_version  int          null comment '模板版本号',
    status         int          null comment '状态',
    config         text         null comment '配置信息',
    yaml_path      varchar(255) null comment 'yaml配置信息',
    create_user_id int          null comment '创建人',
    create_time    datetime     null comment '创建时间',
    update_user_id datetime     null comment '更新人',
    update_time    datetime     null comment '更新时间'
)
    comment '服务管理' charset = utf8;

create table if not exists service_monitor
(
    id          int auto_increment comment '主键'
        primary key,
    service_id  int      null comment '服务id',
    system_info text     null comment '占用系统信息',
    api_info    text     null comment '接口信息',
    create_time datetime null comment '创建时间'
)
    comment '服务监控信息' charset = utf8;

create table if not exists team
(
    id          bigint auto_increment
        primary key,
    create_time datetime     null,
    enabled     bit          not null,
    name        varchar(255) not null
)
    charset = utf8;

create table if not exists teams_users_roles
(
    id      bigint auto_increment
        primary key,
    role_id bigint null,
    team_id bigint null,
    user_id bigint null
)
    charset = utf8;

create table if not exists user
(
    id                       bigint auto_increment
        primary key,
    email                    varchar(255)                       null,
    enabled                  bit                                not null,
    last_password_reset_time datetime                           null,
    nick_name                varchar(255)                       null,
    password                 varchar(255)                       null,
    phone                    varchar(255)                       null,
    sex                      varchar(255)                       null,
    username                 varchar(255)                       null,
    remark                   varchar(255)                       null,
    create_time              datetime default CURRENT_TIMESTAMP null,
    update_time              datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,

    create_user_id bigint(20) DEFAULT NULL COMMENT '创建人id',
    update_user_id bigint(20) DEFAULT NULL COMMENT '修改人id',
    deleted bit(1) DEFAULT 0 COMMENT '删除标记 0正常，1已删除',

    avatar_id                bigint                             null
)
    charset = utf8;

create table if not exists user_avatar
(
    id          bigint auto_increment
        primary key,
    path        varchar(255)                       null,
    real_name   varchar(255)                       null,
    size        varchar(255)                       null,
        create_user_id bigint(20) DEFAULT NULL COMMENT '创建人id',
    update_user_id bigint(20) DEFAULT NULL COMMENT '修改人id',
    deleted bit(1) DEFAULT 0 COMMENT '删除标记 0正常，1已删除',
    create_time datetime default CURRENT_TIMESTAMP null,
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    charset = utf8;

create table if not exists users_roles
(
    user_id bigint not null comment '用户ID',
    role_id bigint not null comment '角色ID',
    primary key (user_id, role_id)
)
    comment '用户角色关联' charset = utf8;


create table if not exists  pt_auxiliary_info (
	id                bigint(20)     not null auto_increment comment '主键id',
	origin_user_id    bigint(20)     not null                comment '资源拥有者id',
	type              varchar(20)    not null                comment '类型',
	aux_info         varchar(50)     null     default NULL   comment '辅助信息',
	deleted              tinyint(1)   default 0                 null comment '删除(0正常，1已删除)',
    create_user_id       bigint                                 null comment '创建人',
    create_time          timestamp                              null comment '创建时间',
    update_user_id       bigint                                 null comment '更新人',
    update_time          timestamp                              null comment '更新时间',
	primary key (`id`),
	index `inx_user_id_type` (`origin_user_id`, `type`) USING BTREE
)
comment='用户的辅助信息表，通过类型进行区分' charset = utf8;

-- 新建镜像表
create table pt_image
(
    id             int(8) auto_increment comment '主键'
        primary key,
    project_name   varchar(100)         not null comment '项目名',
    image_resource tinyint(1)           not null comment '镜像来源(0:我的镜像，1:预置镜像)',
    image_status   tinyint(1)           not null comment '镜像状态(0:制作中,1:制作成功,2:制作失败)',
    image_name     varchar(64)          not null comment '镜像名称',
    image_url      varchar(255)         null comment '镜像地址',
    image_tag      varchar(64)          not null comment '镜像版本',
    remark         varchar(1024)        null comment '镜像描述',
    create_user_id bigint               null comment '创建人',
    create_time    timestamp            null comment '创建时间',
    update_user_id bigint               null comment '更新人',
    update_time    timestamp            null comment '更新时间',
    deleted        tinyint(1) default 0 null comment '删除(0正常，1已删除)',
    origin_user_id bigint               null comment '资源拥有者ID'
)
    comment '镜像表' charset = utf8mb4;

-- k8s资源表
CREATE TABLE  if not exists k8s_resource
(
    id                   bigint auto_increment
        primary key,
    kind   		   varchar(32)          not null comment '资源类型',
    namespace 	   varchar(64)          not null comment '命名空间',
    name   		   varchar(64)          not null comment '名称',
    resource_name  varchar(64)          not null comment '资源名称',
    env      	   varchar(32)          null comment '环境',
    business       varchar(32)          null comment '所属业务模块',
    create_user_id bigint               null comment '创建人',
    create_time    timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    update_user_id bigint               null comment '更新人',
    update_time    timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted        tinyint(1) default 0 null comment '删除(0正常，1已删除)',
	INDEX name (name),
	INDEX resource_name (resource_name),
	constraint kind_namespace_name_uniq unique (kind,namespace,name) comment '资源唯一'
)
    comment 'k8s资源表' charset = utf8mb4;

-- k8s任务表
CREATE TABLE  if not exists k8s_task
(
    id                   bigint auto_increment
        primary key,
    namespace 	   varchar(64)                 not null comment '命名空间',
    resource_name  varchar(64)                 not null comment '资源名称',
	task_yaml  json                            not null comment '资源清单',
    business       varchar(32)                 null comment '所属业务模块',
	apply_unix_time  bigint   default 0        not null comment '资源创建unix时间(精确到秒)',
	apply_display_time        timestamp        null comment '资源创建展示时间',
    apply_status   tinyint(1) default 0        not null comment '状态(0无需操作，1未创建，2已创建)',
    stop_unix_time   bigint   default 0        not null comment '资源停止unix时间(精确到秒)',
	stop_display_time         timestamp        null comment '资源停止展示时间',
	stop_status    tinyint(1) default 0        not null comment '状态(0无需操作，1未停止，2已停止)',
	create_time    timestamp  default CURRENT_TIMESTAMP null comment '创建时间',
	create_user_id bigint(20) default 0        null comment '创建用户ID',
    update_time    timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    update_user_id bigint(20) default 0        null comment '更新用户ID',
    deleted bit(1) default b'0' comment '0正常，1已删除',
	INDEX apply_unix_time(apply_unix_time),
	INDEX stop_unix_time(stop_unix_time),
	KEY `apply_status` (`apply_status`) USING BTREE,
    KEY `stop_status` (`stop_status`) USING BTREE,
	UNIQUE resource_name_namespace (resource_name,namespace) comment '唯一索引'
)
    comment 'k8s任务表' charset = utf8mb4;

    -- 垃圾回收任务表
CREATE TABLE `recycle_task` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
	`recycle_module` VARCHAR(32) NOT NULL COMMENT '回收模块',
	`recycle_type` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '回收类型(0文件，1数据库表数据)',
	`recycle_custom` VARCHAR(64) NULL DEFAULT NULL COMMENT '回收定制化方式',
	`recycle_condition` TEXT NOT NULL COMMENT '回收条件(回收表数据sql、回收文件绝对路径)',
	`recycle_delay_date` DATE NULL DEFAULT NULL COMMENT '回收日期',
	`recycle_status` TINYINT(4) NULL DEFAULT '0' COMMENT '回收任务状态(0:待删除，1:已删除，2:删除失败，3：删除中，4：还原中，5：已还原)',
	`create_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '创建人ID',
	`update_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '修改人ID',
	`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
	`recycle_note` VARCHAR(512) NULL DEFAULT NULL COMMENT '回收说明',
	`remark` VARCHAR(512) NULL DEFAULT NULL COMMENT '备注',
	`recycle_response` VARCHAR(512) NULL DEFAULT NULL COMMENT '回收响应信息',
	`restore_custom` VARCHAR(64) NULL DEFAULT NULL COMMENT '还原定制化方式',
	`deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '删除(0正常，1已删除)',
	PRIMARY KEY (`id`)
)
COMMENT='垃圾回收任务表'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;


-- 创建系统版本控制表
CREATE TABLE  if not exists system_version
(
	id INT(10) NOT NULL AUTO_INCREMENT COMMENT '主键',
	version INT(10) NOT NULL DEFAULT '0' COMMENT '版本号',
	create_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	create_user_id BIGINT(20) NULL DEFAULT '0' COMMENT '创建用户ID',
	update_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
	update_user_id BIGINT(20) NULL DEFAULT '0' COMMENT '更新用户ID',
	deleted BIT(1) NULL DEFAULT b'0' COMMENT '0正常，1已删除',
	PRIMARY KEY (`id`) USING BTREE,
	UNIQUE INDEX `version` (`version`) USING BTREE
)
    comment '系统版本控制表' charset = utf8mb4;

   -- 度量管理表
    create table if not exists pt_measure
    (
        id             bigint(20)  NOT NULL AUTO_INCREMENT primary key,
        name           varchar(32) NOT NULL COMMENT '度量名称',
        dataset_id     bigint(20)  NOT NULL COMMENT '数据集id',
        dataset_url    varchar(32)          DEFAULT NULL COMMENT '数据集url',
        model_urls     TEXT         DEFAULT NULL COMMENT '模型url',
        measure_status tinyint(1)  NOT NULL DEFAULT '0' COMMENT '度量文件生成状态，0：生成中，1：生成成功，2：生成失败',
        url            varchar(200)         DEFAULT NULL COMMENT '度量文件路径',
        origin_user_id bigint(20)           DEFAULT NULL COMMENT '资源拥有人ID',
        create_user_id bigint(20)           DEFAULT NULL COMMENT '创建人',
        update_user_id bigint(20)           DEFAULT NULL COMMENT '更新人',
        create_time    datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        update_time    datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        description    varchar(512)         DEFAULT NULL COMMENT '度量描述',
        deleted        tinyint(1)  NOT NULL DEFAULT '0' COMMENT '删除(0正常，1已删除)'
    )
    COMMENT ='度量管理表' charset = utf8mb4;
    create index user_id
    on pt_measure (create_user_id);

-- 批量服务表
CREATE TABLE IF NOT EXISTS serving_batch (
	id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
	name VARCHAR(255) NULL DEFAULT NULL COMMENT '服务名称',
	resource_info VARCHAR(16) NULL DEFAULT NULL COMMENT '资源信息',
	model_resource TINYINT(8) NULL DEFAULT NULL COMMENT '模型来源（1-预置模型，0-我的模型）',
	model_id BIGINT(20) NULL DEFAULT NULL COMMENT '模型id',
	model_branch_id BIGINT(20) NULL DEFAULT NULL COMMENT '模型对应版本id',
	model_address VARCHAR(255) NULL DEFAULT NULL COMMENT '模型地址',
	input_path VARCHAR(255) NULL DEFAULT NULL COMMENT '输入数据目录',
	output_path VARCHAR(255) NULL DEFAULT NULL COMMENT '输出数据目录',
	status VARCHAR(8) NULL DEFAULT NULL COMMENT '服务状态：0为失败，1为部署中，2为运行中，3为停止，4为完成，5为未知)',
	status_detail json DEFAULT NULL COMMENT '状态对应的详情信息',
	progress VARCHAR(255) NULL DEFAULT NULL COMMENT '进度',
	start_time DATETIME NULL DEFAULT NULL COMMENT '任务开始时间',
	end_time DATETIME NULL DEFAULT NULL COMMENT '任务结束时间',
	resources_pool_node VARCHAR(255) NULL DEFAULT NULL COMMENT '节点个数',
	resources_pool_type TINYINT(4) NULL DEFAULT NULL COMMENT '节点类型(0为CPU，1为GPU)',
	resources_pool_specs VARCHAR(255) NULL DEFAULT NULL COMMENT '节点规格',
	pool_specs_info VARCHAR(255) NULL DEFAULT NULL COMMENT '规格信息',
	deploy_params JSON NULL DEFAULT NULL COMMENT '部署参数',
	frame_type TINYINT(4) NULL DEFAULT NULL COMMENT '框架类型',
	description VARCHAR(255) NULL DEFAULT NULL COMMENT '描述',
	image VARCHAR(255) NULL DEFAULT NULL COMMENT '镜像',
	image_name VARCHAR(255) NULL DEFAULT NULL COMMENT '镜像名称',
	image_tag VARCHAR(255) NULL DEFAULT NULL COMMENT '镜像版本',
	algorithm_id INT NULL DEFAULT NULL COMMENT '算法ID',
	use_script BIT(1) NULL DEFAULT b'0' COMMENT '是否使用脚本',
	script_path VARCHAR(255) NULL DEFAULT NULL COMMENT '推理脚本路径',
	origin_user_id BIGINT(20) NULL COMMENT '资源用有人ID',
	create_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
	create_user_id BIGINT(20) NULL DEFAULT NULL,
	update_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	update_user_id BIGINT(20) NULL DEFAULT NULL,
	deleted BIT(1) NULL DEFAULT b'0',
	PRIMARY KEY (id) USING BTREE,
	INDEX model_id (model_id),
	INDEX status (status),
	INDEX deleted (deleted)
)
COMMENT='云端Serving批量服务业务表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
ROW_FORMAT=DYNAMIC
;

-- 在线服务表
CREATE TABLE IF NOT EXISTS serving_info (
	id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
	name VARCHAR(255) NULL DEFAULT NULL COMMENT '服务名称',
	uuid VARCHAR(255) NULL DEFAULT NULL COMMENT '服务请求接口uuid',
	status VARCHAR(8) NULL DEFAULT NULL COMMENT '服务状态：0-异常，1-部署中，2-运行中，3-已停止',
	status_detail json DEFAULT NULL COMMENT '状态对应的详情信息',
	type TINYINT(4) NULL DEFAULT NULL COMMENT '服务类型：0-Restful，1-gRPC',
	model_resource TINYINT(1) NULL DEFAULT NULL COMMENT '模型来源（1-预置模型，0-我的模型）',
	running_node TINYINT(3) UNSIGNED NULL DEFAULT '0' COMMENT '运行节点数',
	total_node TINYINT(3) UNSIGNED NULL DEFAULT '0' COMMENT '服务总节点数',
	description VARCHAR(255) NULL DEFAULT NULL COMMENT '描述',
	origin_user_id BIGINT(20) NULL COMMENT '资源用有人ID',
	create_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
	create_user_id BIGINT(20) NULL DEFAULT NULL,
	update_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	update_user_id BIGINT(20) NULL DEFAULT NULL,
	deleted BIT(1) NULL DEFAULT b'0',
	PRIMARY KEY (id) USING BTREE,
	INDEX uuid (uuid),
	INDEX status (status),
	INDEX type (type),
	INDEX model_resource (model_resource),
	INDEX deleted (deleted)
)
COMMENT='云端Serving在线服务业务表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
ROW_FORMAT=DYNAMIC
;

-- 在线服务模型部署信息表
CREATE TABLE IF NOT EXISTS serving_model_config (
	id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
	serving_id BIGINT(20) NOT NULL COMMENT 'Serving信息id',
	model_id BIGINT(20) NULL DEFAULT NULL COMMENT '模型id',
	model_branch_id BIGINT(20) NULL DEFAULT NULL COMMENT '模型对应版本id',
	model_address VARCHAR(255) NULL DEFAULT NULL COMMENT '模型路径',
	release_rate VARCHAR(255) NULL DEFAULT NULL COMMENT '灰度发布分流（%）',
	resources_pool_type TINYINT(4) NULL DEFAULT NULL COMMENT '节点类型(0为CPU，1为GPU)',
	resources_pool_specs VARCHAR(255) NULL DEFAULT NULL COMMENT '节点规格',
	resources_pool_node VARCHAR(255) NULL DEFAULT NULL COMMENT '节点个数',
	url VARCHAR(255) NULL DEFAULT NULL COMMENT '模型部署url',
	frame_type TINYINT(4) NULL DEFAULT NULL COMMENT '框架类型',
	model_resource TINYINT(4) NULL DEFAULT NULL COMMENT '模型来源(0-我的模型，1-预置模型)',
	resource_info VARCHAR(16) NULL DEFAULT NULL COMMENT '资源信息',
	pool_specs_info VARCHAR(255) NULL DEFAULT NULL COMMENT '规格信息',
	deploy_params JSON NULL DEFAULT NULL COMMENT '部署参数',
	deploy_id VARCHAR(32) NULL DEFAULT NULL COMMENT '部署id(用于回滚)',
	model_version VARCHAR(8) NULL DEFAULT NULL COMMENT '模型版本',
	model_name VARCHAR(255) NULL DEFAULT NULL COMMENT '模型名称',
	ready_replicas TINYINT(4) NULL DEFAULT NULL COMMENT 'deployment已 Running的pod数',
	image VARCHAR(255) NULL DEFAULT NULL COMMENT '镜像',
	image_name VARCHAR(255) NULL DEFAULT NULL COMMENT '镜像名称',
	image_tag VARCHAR(255) NULL DEFAULT NULL COMMENT '镜像版本',
	algorithm_id INT NULL DEFAULT NULL COMMENT '算法ID',
	use_script BIT(1) NULL DEFAULT b'0' COMMENT '是否使用脚本',
	script_path VARCHAR(255) NULL DEFAULT NULL COMMENT '推理脚本路径',
	create_user_id BIGINT(20) NULL DEFAULT NULL,
	create_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
	update_user_id BIGINT(20) NULL DEFAULT NULL,
	update_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	deleted BIT(1) NULL DEFAULT b'0',
	PRIMARY KEY (id) USING BTREE,
	INDEX serving_id (serving_id) USING BTREE,
	INDEX model_id (model_id),
	INDEX deleted (deleted)
)
COMMENT='云端Serving在线服务模型部署业务表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
ROW_FORMAT=DYNAMIC
;

-- 模型优化内置算法、模型、数据集关系表
CREATE TABLE IF NOT EXISTS model_opt_build_in (
	id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
	type TINYINT(4) NOT NULL COMMENT '算法类型（0-剪枝，1-蒸馏，2-量化）',
	algorithm VARCHAR(255) NULL DEFAULT NULL COMMENT '算法名称',
	algorithm_path VARCHAR(255) NULL DEFAULT NULL COMMENT '算法路径',
	dataset VARCHAR(255) NULL DEFAULT NULL COMMENT '数据集名称',
	dataset_path VARCHAR(255) NULL DEFAULT NULL COMMENT '数据集路径',
	model VARCHAR(255) NULL DEFAULT NULL COMMENT '模型名称',
	model_path VARCHAR(255) NULL DEFAULT NULL COMMENT '模型路径',
	create_user_id BIGINT(20) NULL DEFAULT NULL COMMENT '创建用户ID',
	create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	update_user_id BIGINT(20) NULL DEFAULT NULL COMMENT '更新用户ID',
	update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	deleted BIT(1) NULL DEFAULT b'0' COMMENT '0正常，1已删除',
	PRIMARY KEY (id) USING BTREE
)
COMMENT='模型优化内置算法、模型、数据集关系表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
ROW_FORMAT=DYNAMIC
;

-- 模型优化用户数据集表
CREATE TABLE IF NOT EXISTS model_opt_dataset (
	id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
	name VARCHAR(255) NOT NULL COMMENT '名称',
	path VARCHAR(255) NOT NULL COMMENT '路径',
	create_user_id BIGINT(20) NULL DEFAULT NULL COMMENT '创建人',
	create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	update_user_id BIGINT(20) NULL DEFAULT NULL COMMENT '修改人',
	update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
	deleted BIT(1) NOT NULL DEFAULT b'0' COMMENT '0正常，1已删除',
	PRIMARY KEY (id)
)
COMMENT='模型优化用户数据集表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;

-- 模型优化任务表
CREATE TABLE IF NOT EXISTS model_opt_task (
	id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID主键',
	name VARCHAR(100) NOT NULL COMMENT '任务名称',
	description VARCHAR(1024) NULL DEFAULT NULL COMMENT '任务描述',
	is_built_in BIT(1) NOT NULL COMMENT '是否内置',
	model_id BIGINT(20) NULL DEFAULT NULL COMMENT '模型id',
	model_name VARCHAR(255) NULL DEFAULT NULL COMMENT '模型名称',
	model_address VARCHAR(255) NULL DEFAULT NULL COMMENT '模型路径',
	algorithm_id BIGINT(20) NULL DEFAULT NULL COMMENT '优化算法id',
	algorithm_type TINYINT(4) NULL DEFAULT NULL COMMENT '优化算法类型',
	algorithm_name VARCHAR(255) NULL DEFAULT NULL COMMENT '优化算法',
	algorithm_path VARCHAR(255) NULL DEFAULT NULL COMMENT '算法路径',
	dataset_id BIGINT(20) NULL DEFAULT NULL COMMENT '数据集id',
	dataset_name VARCHAR(255) NULL DEFAULT NULL COMMENT '数据集名称',
	dataset_path VARCHAR(255) NULL DEFAULT NULL COMMENT '数据集路径',
	command TEXT NULL COMMENT '运行命令',
	params JSON NULL DEFAULT NULL COMMENT '运行参数',
	origin_user_id BIGINT(20) NULL COMMENT '资源用有人ID',
	create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	create_user_id BIGINT(20) NULL DEFAULT NULL COMMENT '创建人',
	update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	update_user_id BIGINT(20) NULL DEFAULT NULL COMMENT '修改人',
	deleted BIT(1) NOT NULL DEFAULT b'0' COMMENT '删除标志',
	PRIMARY KEY (id) USING BTREE,
	INDEX name (name) USING BTREE,
	INDEX algorithm_type (algorithm_type) USING BTREE,
	INDEX create_time (create_time) USING BTREE
)
COMMENT='模型优化任务表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
ROW_FORMAT=DYNAMIC
;

-- 模型优化任务实例记录表
CREATE TABLE IF NOT EXISTS model_opt_task_instance (
	id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
	task_id BIGINT(20) NOT NULL COMMENT '任务Id',
	task_name VARCHAR(127) NULL DEFAULT NULL COMMENT '任务名称',
	is_built_in BIT(1) NULL DEFAULT NULL COMMENT '是否内置',
	model_id BIGINT(20) NULL DEFAULT NULL COMMENT '模型id',
	model_name VARCHAR(255) NULL DEFAULT NULL COMMENT '模型名称',
	model_address VARCHAR(255) NULL DEFAULT NULL COMMENT '模型路径',
	algorithm_id BIGINT(20) NULL DEFAULT NULL COMMENT '优化算法id',
	algorithm_type TINYINT(4) NULL DEFAULT NULL COMMENT '优化算法类型',
	algorithm_name VARCHAR(255) NULL DEFAULT NULL COMMENT '算法路径',
	algorithm_path VARCHAR(255) NULL DEFAULT NULL COMMENT '使用类型 0-内置 1-我的',
	dataset_id BIGINT(20) NULL DEFAULT NULL COMMENT '数据集id',
	dataset_name VARCHAR(255) NULL DEFAULT NULL COMMENT '数据集名称',
	dataset_path VARCHAR(255) NULL DEFAULT NULL COMMENT '数据集路径',
	start_time DATETIME NULL DEFAULT NULL COMMENT '任务实例开始时间',
	end_time DATETIME NULL DEFAULT NULL COMMENT '任务实例结束时间',
	output_model_dir VARCHAR(255) NULL DEFAULT '0' COMMENT '输出模型路径',
	log_path VARCHAR(255) NULL DEFAULT NULL COMMENT '日志地址',
	status VARCHAR(8) NOT NULL DEFAULT '-1' COMMENT '-1-等待中,0-进行中,1-已完成,2-已取消,3-执行失败',
	status_detail json DEFAULT NULL COMMENT '状态对应的详情信息',
	command TEXT NULL COMMENT '运行命令',
	params JSON NULL DEFAULT NULL COMMENT '运行参数',
	opt_result_before MEDIUMTEXT NULL COMMENT '模型优化前性能参数',
	opt_result_json_path_before VARCHAR(255) NULL DEFAULT NULL COMMENT '模型优化前性能参数json文件路径',
	opt_result_after MEDIUMTEXT NULL COMMENT '模型优化后性能参数',
	opt_result_json_path_after VARCHAR(255) NULL DEFAULT NULL COMMENT '模型优化后性能参数json文件路径',
	origin_user_id BIGINT(20) NULL COMMENT '资源用有人ID',
	create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	create_user_id BIGINT(20) NULL DEFAULT NULL COMMENT '创建人',
	update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	update_user_id BIGINT(20) NULL DEFAULT NULL COMMENT '修改人',
	deleted BIT(1) NOT NULL DEFAULT b'0' COMMENT '删除标识：0-未删除，1-删除',
	PRIMARY KEY (id) USING BTREE,
	INDEX task_id (task_id) USING BTREE,
	INDEX status (status) USING BTREE,
	INDEX name (task_name) USING BTREE,
	INDEX model_type (model_name) USING BTREE,
	INDEX algorithm_type (algorithm_type) USING BTREE,
	INDEX start_time (start_time) USING BTREE,
	INDEX end_time (end_time) USING BTREE
)
COMMENT='模型优化任务实例记录表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
ROW_FORMAT=DYNAMIC
;

CREATE TABLE `recycle` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
	`recycle_module` VARCHAR(32) NOT NULL COMMENT '回收模块',
	`recycle_delay_date` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '回收日期',
	`recycle_custom` VARCHAR(64) NULL DEFAULT NULL COMMENT '回收定制化方式',
	`recycle_status` TINYINT(4) NULL DEFAULT '0' COMMENT '回收任务状态(0:待删除，1:已删除，2:删除失败，3：删除中，4：还原中，5：已还原)',
	`create_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '创建人ID',
	`update_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '修改人ID',
	`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
	`recycle_note` VARCHAR(512) NULL DEFAULT NULL COMMENT '回收说明',
	`remark` VARCHAR(512) NULL DEFAULT NULL COMMENT '备注',
	`recycle_response` VARCHAR(512) NULL DEFAULT NULL COMMENT '回收响应信息',
	`restore_custom` VARCHAR(64) NULL DEFAULT NULL COMMENT '还原定制化方式',
	`deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '删除(0正常，1已删除)',
	PRIMARY KEY (`id`)
)
COMMENT='垃圾回收任务主表'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;

CREATE TABLE `recycle_detail` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
	`recycle_id` BIGINT(20) NOT NULL COMMENT '垃圾回收任务主表ID',
	`recycle_type` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '回收类型(0文件，1数据库表数据)',
	`recycle_condition` TEXT NOT NULL COMMENT '回收条件(回收表数据sql、回收文件绝对路径)',
	`recycle_status` TINYINT(4) NULL DEFAULT '0' COMMENT '回收任务状态(0:待删除，1:已删除，2:删除失败，3：删除中)',
	`create_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '创建人ID',
	`update_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '修改人ID',
	`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
	`recycle_note` VARCHAR(512) NULL DEFAULT NULL COMMENT '回收说明',
	`remark` VARCHAR(512) NULL DEFAULT NULL COMMENT '备注',
	`recycle_response` VARCHAR(512) NULL DEFAULT NULL COMMENT '回收响应信息',
	`deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '删除(0正常，1已删除)',
	PRIMARY KEY (`id`),
	INDEX `recycle_task_main_id` (`recycle_id`)
)
COMMENT='垃圾回收任务详情表'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;

-- auth服务 token存储
CREATE TABLE IF NOT EXISTS `oauth_access_token` (
  `token_id` varchar(256) DEFAULT NULL,
  `token` blob,
  `authentication_id` varchar(256) DEFAULT NULL,
  `user_name` varchar(256) DEFAULT NULL,
  `client_id` varchar(256) DEFAULT NULL,
  `authentication` blob,
  `refresh_token` varchar(256) DEFAULT NULL
) comment 'auth token存储表' ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- auth服务 客户端权限配置
CREATE TABLE IF NOT EXISTS `oauth_client_details` (
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
) comment '客户端权限配置表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- auth服务 权限token刷新
CREATE TABLE IF NOT EXISTS `oauth_refresh_token` (
  `token_id` varchar(256) DEFAULT NULL,
  `token` blob,
  `authentication` blob
) comment '权限token刷新表' ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- 操作权限表
CREATE TABLE if not exists `permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pid` bigint(20) NOT NULL DEFAULT '0' COMMENT '父id',
  `name` varchar(64) DEFAULT NULL COMMENT '菜单/操作按钮名称',
  `permission` varchar(128) DEFAULT NULL COMMENT '权限标识',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `update_user_id` bigint(20) DEFAULT NULL COMMENT '修改人id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) DEFAULT b'0' COMMENT '删除标记 0正常，1已删除',
  PRIMARY KEY (`id`)
)
ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COMMENT='权限表';

-- 角色权限组关联表
create table if not exists `roles_auth`(
    role_id     bigint not null,
    auth_id bigint null
)ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='角色权限关联表';

-- 用户组表
CREATE TABLE if not exists `pt_group`
(
    id             BIGINT(20)                             not null auto_increment primary key,
    name           varchar(32)  default null comment '用户组名称',
    description         varchar(255) default null comment '备注',
    create_user_id bigint                                 null comment '创建人id',
    update_user_id bigint                                 null comment '修改人id',
    create_time    datetime     default CURRENT_TIMESTAMP null,
    update_time    datetime     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    deleted        bit          default b'0'              null comment '删除标记 0正常，1已删除'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC COMMENT ='用户组表';
  create unique index group_name_uindex
	on pt_group (name);

-- 用户组-用户关联表
create  table if not exists `user_group`(
    group_id BIGINT(20)  not null comment '用户组id',
    user_id bigint(20) not null comment '用户id'
)
comment '用户组-用户关联表' charset =utf8;
create index group_user_group_id
    on user_group (group_id);

create unique index group_user_user_id
    on user_group (user_id);

create table if not exists `auth`(
    id             bigint auto_increment
        primary key,
    auth_code      varchar(32)                        not null comment '权限code',
    description    varchar(255)                       null comment '描述',
    create_user_id bigint                             null comment '创建人id',
    update_user_id bigint                             null comment '修改人id',
    create_time    datetime default CURRENT_TIMESTAMP null,
    update_time    datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    deleted        bit      default b'0'              null comment '删除标记 0正常，1已删除'
)
    comment '权限管理表';

create index auth_index_auth_code
    on auth (auth_code);

create table if not exists `auth_permission`
(
    auth_id       bigint not null,
    permission_id bigint not null
)
    comment '权限组-权限关联表';

create table if not exists resource_specs
(
    id                 int auto_increment comment '主键ID'
        primary key,
    specs_name         varchar(128) default ''  not null comment '规格名称',
    resources_pool_type  tinyint(1) default 0   not null comment '规格类型(0为CPU, 1为GPU)',
    module             int                      not null comment '所属业务场景(0:通用，1：dubhe-notebook，2：dubhe-train，3：dubhe-serving)',
    cpu_num            int                      not null comment 'CPU数量,单位：核',
    gpu_num            int                      not null comment 'GPU数量，单位：核',
    mem_num            int                      not null comment '内存大小，单位：M',
    workspace_request  int                      not null comment '工作空间的存储配额，单位：M',
    create_user_id     bigint                   null comment '创建人',
    create_time        timestamp                null comment '创建时间',
    update_user_id     bigint                   null comment '更新人',
    update_time        timestamp                null comment '更新时间',
    deleted            tinyint(1)   default 0   not null comment '删除(0正常，1已删除)'
)
    comment '资源规格';

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
	`deleted` BIT(1) NULL DEFAULT b'0' COMMENT '是否删除（0正常，1删除）',
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
