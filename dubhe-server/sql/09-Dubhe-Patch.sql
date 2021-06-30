-- Patch 补丁sql，使用存储过程实现幂等，可放心多次执行
use dubhe-cloud-prod;
-- 包含原boot项目需要加入的表和数据

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

-- auth服务 系统客户端认证数据初始化
INSERT INTO `oauth_client_details` (`client_id`, `resource_ids`, `client_secret`, `scope`, `authorized_grant_types`, `web_server_redirect_uri`, `authorities`, `access_token_validity`, `refresh_token_validity`, `additional_information`, `autoapprove`) VALUES
	('dubhe-client', NULL, '$2a$10$RUYBRsyV2jpG7pvg/VNus.YHVebzfRen3RGeDe1LVEIJeHYe2F1YK', 'all', 'authorization_code,password,refresh_token', 'http://localhost:8866/oauth/callback', NULL, 3600, 2592000, NULL, NULL);

-- 模型服务 添加打包字段
alter table `pt_model_info`
	add `packaged` tinyint default 0 not null comment '模型是否已经打包，0未打包，1打包完成';

alter table `pt_model_info`
	add `tags` json null comment 'tag信息';

-- 新增 表menu 度量管理菜单
INSERT INTO menu (id, cache, component, component_name, hidden, icon, name, path, permission, pid,sort, type, layout, create_time, update_time, create_user_id, update_user_id,deleted)
VALUES ('97', '', NULL, NULL, '', 'icon_huabanfuben1', '模型炼知', 'atlas', NULL, '0', '70', '0', NULL,'2020-11-20 09:08:12', '2020-11-20 09:08:12', '1', '1', '');
INSERT INTO menu (id, cache, component, component_name, hidden, icon, name, path, permission, pid,sort, type, layout, create_time, update_time, create_user_id, update_user_id,deleted)
VALUES ('1064', '', 'atlas/measure', 'Measure', '', 'icon_huabanfuben1', '度量管理', 'measure', 'atlas:measure', '97', '71', '1','BaseLayout', '2020-11-20 11:03:20', '2020-11-20 11:03:20', '1', '1', '');
INSERT INTO menu (id, cache, component, component_name, hidden, icon, name, path, permission, pid,sort, type, layout, create_time, update_time, create_user_id, update_user_id,deleted)
VALUES ('1065', '', 'atlas/graphVisual', 'AtlasGraphVisual', '', 'icon_huabanfuben1', '图谱可视化', 'graphvisual', NULL,'97', '72', '1', 'BaseLayout', '2020-11-20 11:07:14', '2020-11-20 11:07:14', '1', '1', '');
INSERT INTO menu (id, cache, component, component_name, hidden, icon, name, path, permission, pid,sort, type, layout, create_time, update_time, create_user_id, update_user_id,deleted)
VALUES ('1066', '', 'atlas/graphList', 'AtlasGraph', '', 'icon_huabanfuben1', '图谱列表', 'graph', NULL, '97', '73', '1','BaseLayout', '2020-11-20 11:08:02', '2020-11-20 11:08:02', '1', '1', '');

-- 度量表pt_measure新增字段
ALTER TABLE pt_measure
    ADD dataset_id BIGINT NOT NULL COMMENT '数据集id' AFTER name;
ALTER TABLE pt_measure
    ADD dateset_url VARCHAR(32) NULL COMMENT '数据集url' AFTER dataset_id;
ALTER TABLE pt_measure
    ADD model_urls TEXT NULL COMMENT '模型url' AFTER dataset_url;
ALTER TABLE pt_measure
    ADD measure_status TINYINT(1) NOT NULL default 0  COMMENT '度量文件生成状态，0：生成中，1：生成成功，2：生成失败' AFTER model_urls;

-- 资源回收
ALTER TABLE `recycle_task`
	CHANGE COLUMN `recycle_note` `recycle_note` VARCHAR(512) NULL DEFAULT NULL COMMENT '回收说明' AFTER `update_time`,
	ADD COLUMN `remark` VARCHAR(512) NULL DEFAULT NULL COMMENT '备注' AFTER `recycle_note`;
ALTER TABLE `recycle_task`
	ADD COLUMN `recycle_response` VARCHAR(512) NULL DEFAULT NULL COMMENT '回收响应信息' AFTER `remark`;
ALTER TABLE `recycle_task`
	CHANGE COLUMN `recycle_status` `recycle_status` TINYINT(4) NULL DEFAULT '0' COMMENT '回收任务状态(0:待删除，1:已删除，2:删除失败，3：删除中，4：还原中，5：已还原)' AFTER `recycle_delay_date`;
ALTER TABLE `recycle_task`
	ADD COLUMN `restore_custom` VARCHAR(64) NULL DEFAULT NULL COMMENT '还原定制化方式' AFTER `recycle_response`;

CREATE TABLE `recycle` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
	`recycle_module` VARCHAR(32) NOT NULL COMMENT '回收模块',
	`recycle_delay_date` DATE NULL DEFAULT NULL COMMENT '回收日期',
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


-- 数据集文件标注表
create table data_file_annotation IF NOT EXISTS data_file_annotation
(
    id bigint auto_increment
    primary key,
    dataset_id bigint not null comment '数据集ID',
    label_id bigint not null comment '标签ID',
    version_file_id bigint not null comment '版本文件ID',
    prediction double default 0 null comment '预测值',
    create_user_id bigint null,
    create_time datetime default CURRENT_TIMESTAMP not null,
    update_user_id bigint null,
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    deleted bit default b'0' not null,
    file_name varchar(255) null comment '文件名称'
    )
    comment '数据集文件标注表';

create index label_dataset_id_indx
	on data_file_annotation (label_id, dataset_id);

create index version_file_index
	on data_file_annotation (version_file_id);


-- 菜单新增 back_to ext_config 字段
alter table menu add column back_to varchar(255) default null comment '上级菜单';

alter table menu add column ext_config varchar(255) default null comment '扩展配置';

-- data_dataset_label 新增 create_time  update_time deleted 字段
alter table data_dataset_label add column `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
alter table data_dataset_label add column `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间';
alter table data_dataset_label add column `deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '删除(0正常，1已删除)';
alter table data_dataset_label add column create_user_id bigint(20) DEFAULT NULL COMMENT '创建人id';
alter table data_dataset_label add column update_user_id bigint(20) DEFAULT NULL COMMENT '修改人id';

-- 初始化回收站菜单数据
INSERT INTO menu ( pid, type, name, icon, path, component, component_name, layout, permission, hidden, cache, sort, create_user_id, update_user_id, deleted, back_to, ext_config) VALUES ( 90, 1, '回收站', 'shuju1', 'recycle', 'system/recycle/index', 'SystemRecycle', 'BaseLayout', 'system:recycle', false, false, 999, 1, 1,  false, null, null);

-- 模型优化新增模型对应版本id字段
ALTER TABLE `model_opt_task` ADD COLUMN `model_branch_id` BIGINT(20) NULL DEFAULT NULL COMMENT '模型对应版本id' AFTER `model_id`;
ALTER TABLE `model_opt_task_instance` ADD COLUMN `model_branch_id` BIGINT(20) NULL DEFAULT NULL COMMENT '模型对应版本id' AFTER `model_id`;

-- 云端Serving新增模型对应版本id字段
ALTER TABLE `serving_model_config` ADD COLUMN `model_branch_id` BIGINT(20) NULL DEFAULT NULL COMMENT '模型对应版本id' AFTER `model_id`;
ALTER TABLE `serving_batch` ADD COLUMN `model_branch_id` BIGINT(20) NULL DEFAULT NULL COMMENT '模型对应版本id' AFTER `model_id`;

-- 数据集模块新增数据集来源ID字段
alter table data_dataset
    add column source_id bigint(20) default null comment '数据集来源ID';

-- 数据集删除名称唯一索引
drop index idx_name_unique on data_dataset;

-- 算法管理模块新增算法用途'模型优化'
insert into `pt_auxiliary_info` (`origin_user_id`,`type`,`aux_info`) values (0,'algorithem_usage','模型优化');

-- 修改菜单栏'模型开发'为'算法开发'
update menu set name='算法开发'  where id=30;

-- 重新建立度量表索引
drop index measure_unidex on pt_measure;
create index user_id
    on pt_measure (create_user_id);


-- 删除无效预置算法
delete from pt_train_algorithm where algorithm_name in ('OneFlow预置算法-resnet50','OneFlow预置算法-bert','OneFlow预置算法-alexnet','OneFlow预置算法-mobilenetv2','OneFlow预置算法-dcgan','OneFlow预置算法-vgg16','OneFlow预置算法-insightface','OneFlow预置算法-yolov3','OneFlow预置算法-deep_and_wide');

-- 标签组新增操作类型，标签组类型字段
alter table data_label_group
    add column operate_type int(11) default null comment '操作类型';
alter table data_label_group
    add column label_group_type int(1) default 0 comment '标签组数据类型  0:视觉  1:文本';



-- 菜单新增数据集相关路由
INSERT INTO menu ( pid, type, name, icon, path, component, component_name, layout, permission, hidden, cache, sort, create_user_id, update_user_id, deleted, back_to, ext_config) VALUES ( 10, 1, '图像语义分割', null, 'datasets/segmentation/:datasetId', 'dataset/annotate', 'SegmentationDataset', 'DatasetLayout', null, true, false, 19, 1, 1,  false, null, '{"test": 1}');
INSERT INTO menu ( pid, type, name, icon, path, component, component_name, layout, permission, hidden, cache, sort, create_user_id, update_user_id, deleted, back_to, ext_config) VALUES ( 10, 1, '图像语义分割', null, 'datasets/segmentation/:datasetId/file/:fileId', 'dataset/annotate', 'SegmentationDatasetFile', 'DatasetLayout', null, true, false, 18, 1, 1, false, null, null);
INSERT INTO menu ( pid, type, name, icon, path, component, component_name, layout, permission, hidden, cache, sort, create_user_id, update_user_id, deleted, back_to, ext_config) VALUES ( 10, 1, '医学影像阅读', 'beauty', 'datasets/medical/viewer/:medicalId', 'dataset/medical/viewer', 'DatasetMedicalViewer', 'FullpageLayout', null, true, false, 999, 1, 1,  false, null, null);
INSERT INTO menu ( pid, type, name, icon, path, component, component_name, layout, permission, hidden, cache, sort, create_user_id, update_user_id, deleted, back_to, ext_config) VALUES ( 10, 1, '数据集管理', 'shujuguanli', 'datasets', 'dataset/fork', 'DatasetFork', 'BaseLayout', null, false, false, 17, 1, 1,  false, null, null);
INSERT INTO menu ( pid, type, name, icon, path, component, component_name, layout, permission, hidden, cache, sort, create_user_id, update_user_id, deleted, back_to, ext_config) VALUES ( 10, 1, '医疗影像数据集', null, 'datasets/medical', 'dataset/medical/list', 'DatasetMedical', 'BaseLayout', null, true, false, 25, 1, 1, false, null, null);
INSERT INTO menu ( pid, type, name, icon, path, component, component_name, layout, permission, hidden, cache, sort, create_user_id, update_user_id, deleted, back_to, ext_config) VALUES ( 10, 1, '数据集场景选择', null, 'datasets/entrance', 'dataset/entrance', 'Entrance', 'BaseLayout', null, true, false, 20, 1, 1,  false, null, null);
INSERT INTO menu ( pid, type, name, icon, path, component, component_name, layout, permission, hidden, cache, sort, create_user_id, update_user_id, deleted, back_to, ext_config) VALUES ( 10, 1, '文本分类', null, 'datasets/textclassify/:datasetId', 'dataset/nlp/textClassify', 'TextClassify', 'DetailLayout', '', true, false, 26, 1, 1,  false, null, null);
INSERT INTO menu ( pid, type, name, icon, path, component, component_name, layout, permission, hidden, cache, sort, create_user_id, update_user_id, deleted, back_to, ext_config) VALUES ( 10, 1, '文本标注', null, 'datasets/text/annotation/:datasetId', 'dataset/nlp/annotation', 'TextAnnotation', 'DetailLayout', null, true, false, 27, 1, 1,  false, null, null);
INSERT INTO menu ( pid, type, name, icon, path, component, component_name, layout, permission, hidden, cache, sort, create_user_id, update_user_id, deleted, back_to, ext_config) VALUES ( 10, 1, '导入表格', null, 'datasets/table/import', 'dataset/tableImport', 'TableImport', 'DetailLayout', null, true, false, 999, 1, 1,  false, null, '{}');


-- 管理员角色新增数据集相关权限
INSERT INTO roles_menus (role_id, menu_id) value (1, (select id from menu where path = 'datasets/segmentation/:datasetId'));
INSERT INTO roles_menus (role_id, menu_id) value (1, (select id from menu where path = 'datasets/segmentation/:datasetId/file/:fileId'));
INSERT INTO roles_menus (role_id, menu_id) value (1, (select id from menu where path = 'datasets/medical/viewer/:medicalId'));
INSERT INTO roles_menus (role_id, menu_id) value (1, (select id from menu where path = 'datasets'));
INSERT INTO roles_menus (role_id, menu_id) value (1, (select id from menu where path = 'datasets/medical'));
INSERT INTO roles_menus (role_id, menu_id) value (1, (select id from menu where path = 'datasets/entrance'));
INSERT INTO roles_menus (role_id, menu_id) value (1, (select id from menu where path = 'datasets/textclassify/:datasetId'));
INSERT INTO roles_menus (role_id, menu_id) value (1, (select id from menu where path = 'datasets/text/annotation/:datasetId'));
INSERT INTO roles_menus (role_id, menu_id) value (1, (select id from menu where path = 'datasets/table/import'));

-- 注册角色新增数据集相关权限
INSERT INTO roles_menus (role_id, menu_id) value (2, (select id from menu where path = 'datasets/segmentation/:datasetId'));
INSERT INTO roles_menus (role_id, menu_id) value (2, (select id from menu where path = 'datasets/segmentation/:datasetId/file/:fileId'));
INSERT INTO roles_menus (role_id, menu_id) value (2, (select id from menu where path = 'datasets/medical/viewer/:medicalId'));
INSERT INTO roles_menus (role_id, menu_id) value (2, (select id from menu where path = 'datasets'));
INSERT INTO roles_menus (role_id, menu_id) value (2, (select id from menu where path = 'datasets/medical'));
INSERT INTO roles_menus (role_id, menu_id) value (2, (select id from menu where path = 'datasets/entrance'));
INSERT INTO roles_menus (role_id, menu_id) value (2, (select id from menu where path = 'datasets/textclassify/:datasetId'));
INSERT INTO roles_menus (role_id, menu_id) value (2, (select id from menu where path = 'datasets/text/annotation/:datasetId'));
INSERT INTO roles_menus (role_id, menu_id) value (2, (select id from menu where path = 'datasets/table/import'));

update menu set name = '视觉/文本数据集' where name = '视觉文本数据集';


-- 新增表格导入菜单sql
INSERT INTO menu (pid, type, name, icon, path, component, component_name, layout, permission, hidden, cache, sort, create_user_id, update_user_id,  deleted, back_to, ext_config) VALUES ( 10, 1, '导入表格', null, 'datasets/table/import', 'dataset/tableImport', 'TableImport', 'DetailLayout', null, true, false, 999, 1, 1, false, null, '{}');
INSERT INTO roles_menus (role_id, menu_id) value (1, (select id from menu where path = 'datasets/table/import'));
INSERT INTO roles_menus (role_id, menu_id) value (2, (select id from menu where path = 'datasets/table/import'));




-- 修改回收日期数据类型
alter table recycle modify recycle_delay_date DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '回收日期';

-- Serving 增加镜像字段
ALTER TABLE `serving_model_config` ADD COLUMN `image` VARCHAR(255) NULL DEFAULT NULL COMMENT '镜像' AFTER `model_name`;

ALTER TABLE `serving_batch` ADD COLUMN `image` VARCHAR(255) NULL DEFAULT NULL COMMENT '镜像';
-- Serving 修改字段类型
ALTER TABLE `serving_model_config`
	ADD COLUMN `script_path` VARCHAR(255) NULL DEFAULT NULL COMMENT '推理脚本路径' AFTER `image`,
	ADD COLUMN `use_script` BIT(1) NULL DEFAULT b'0' COMMENT '是否使用脚本' AFTER `script_path`;
-- Serving 修改字段类型
ALTER TABLE `serving_batch`
	ADD COLUMN `script_path` VARCHAR(255) NULL DEFAULT NULL COMMENT '推理脚本路径' AFTER `image`,
	ADD COLUMN `use_script` BIT(1) NULL DEFAULT b'0' COMMENT '是否使用脚本' AFTER `script_path`;
ALTER TABLE serving_model_config MODIFY COLUMN deploy_params json NULL DEFAULT NULL COMMENT '部署参数';
alter table serving_batch drop model_config_path;
alter table serving_batch drop reasoning_script_path;

-- 修改pt_model_info表允许model_description字段为空
ALTER TABLE `pt_model_info`
	MODIFY COLUMN model_description varchar(255) null comment '模型描述';

-- 添加可视化任务菜单
INSERT INTO `menu`(`id`, `pid`, `type`, `name`, `icon`, `path`, `component`, `component_name`, `layout`, `hidden`, `permission`) VALUES
(45, 40, 1, '可视化任务', 'mobanguanli', 'visual', 'trainingJob/trainingVisualList', 'TrainVisual', 'BaseLayout', b'0', 'training:visual');

-- 算法表添加inference字段
alter table pt_train_algorithm
    add column inference tinyint(1) default 0 not null comment '算法文件是否可推理（1可推理，0不可推理）';

ALTER TABLE `serving_model_config`
	ADD COLUMN `image_name` VARCHAR(255) NULL DEFAULT NULL COMMENT '镜像名称' AFTER `image`,
	ADD COLUMN `image_tag` VARCHAR(255) NULL DEFAULT NULL COMMENT '镜像版本' AFTER `image_name`;

ALTER TABLE `serving_batch`
	ADD COLUMN `image_name` VARCHAR(255) NULL DEFAULT NULL COMMENT '镜像名称' COLLATE 'utf8_general_ci' AFTER `image`,
	ADD COLUMN `image_tag` VARCHAR(255) NULL DEFAULT NULL COMMENT '镜像版本' COLLATE 'utf8_general_ci' AFTER `image_name`;


ALTER TABLE `serving_batch`
	ADD COLUMN `algorithm_id` INT NULL DEFAULT NULL COMMENT '算法ID' AFTER `use_script`;

ALTER TABLE `serving_model_config`
    ADD COLUMN `algorithm_id` INT NULL DEFAULT NULL COMMENT '算法ID' AFTER `use_script`;

ALTER TABLE `serving_batch`
	ADD COLUMN `origin_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '资源用有人ID';

ALTER TABLE `serving_info`
	ADD COLUMN `origin_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '资源用有人ID';

ALTER TABLE `model_opt_task`
	ADD COLUMN `origin_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '资源用有人ID';

ALTER TABLE `model_opt_task_instance`
	ADD COLUMN `origin_user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '资源用有人ID';

-- 添加模型格式表pt_model_type
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

-- 修改dict_detail表frame_type值为小写
update dict_detail set label='oneflow'  where dict_id=6 and sort=1;
update dict_detail set label='tensorflow'  where dict_id=6 and sort=2;
update dict_detail set label='pytorch'  where dict_id=6 and sort=3;
update dict_detail set label='keras'  where dict_id=6 and sort=4;
update dict_detail set label='caffe'  where dict_id=6 and sort=5;
update dict_detail set label='blade'  where dict_id=6 and sort=6;
INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (6, 'mxnet', '7', 7);
-- 添加dict_detail表model_type值
INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (7, 'pb', '9', 9);
INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (7, 'ckpt', '10', 10);
INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (7, 'pkt', '11', 11);
INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (7, 'pt', '12', 12);
INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (7, 'h5(HDF5)', '13', 13);
INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (7, 'caffemodel', '14', 14);
INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (7, 'params', '15', 15);
INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (7, 'json', '16', 16);

-- 初始化模型格式表pt_model_type
INSERT INTO `pt_model_type`(`frame_type`, `model_type`) VALUES (1, '1');
INSERT INTO `pt_model_type`(`frame_type`, `model_type`) VALUES (2, '1,9,10');
INSERT INTO `pt_model_type`(`frame_type`, `model_type`) VALUES (3, '8,11,12');
INSERT INTO `pt_model_type`(`frame_type`, `model_type`) VALUES (4, '13');
INSERT INTO `pt_model_type`(`frame_type`, `model_type`) VALUES (5, '4,14');
INSERT INTO `pt_model_type`(`frame_type`, `model_type`) VALUES (6, '6');
INSERT INTO `pt_model_type`(`frame_type`, `model_type`) VALUES (7, '15,16');

-- 添加模型后缀名表pt_model_suffix
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
-- 初始化模型后缀名表pt_model_suffix
INSERT INTO `pt_model_suffix`(`model_type`) VALUES (1);
INSERT INTO `pt_model_suffix`(`model_type`, `model_suffix`) VALUES (2, '.pb');
INSERT INTO `pt_model_suffix`(`model_type`, `model_suffix`) VALUES (3, '.h5');
INSERT INTO `pt_model_suffix`(`model_type`, `model_suffix`) VALUES (4, '.caffeprototxt');
INSERT INTO `pt_model_suffix`(`model_type`) VALUES (5);
INSERT INTO `pt_model_suffix`(`model_type`) VALUES (6);
INSERT INTO `pt_model_suffix`(`model_type`) VALUES (7);
INSERT INTO `pt_model_suffix`(`model_type`, `model_suffix`) VALUES (8, '.pth');
INSERT INTO `pt_model_suffix`(`model_type`, `model_suffix`) VALUES (9, '.pb');
INSERT INTO `pt_model_suffix`(`model_type`, `model_suffix`) VALUES (10, '.ckpt');
INSERT INTO `pt_model_suffix`(`model_type`, `model_suffix`) VALUES (11, '.pkt');
INSERT INTO `pt_model_suffix`(`model_type`, `model_suffix`) VALUES (12, '.pt');
INSERT INTO `pt_model_suffix`(`model_type`, `model_suffix`) VALUES (13, '.h5');
INSERT INTO `pt_model_suffix`(`model_type`, `model_suffix`) VALUES (14, '.caffemodel');
INSERT INTO `pt_model_suffix`(`model_type`, `model_suffix`) VALUES (15, '.params');
INSERT INTO `pt_model_suffix`(`model_type`, `model_suffix`) VALUES (16, '.json');

-- 添加dict_detail表model_type值
INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (7, 'Directory', '17', 17);
-- 修改模型格式表pt_model_type
update pt_model_type set model_type='1,17'  where frame_type=1;
update pt_model_type set model_type='1,13'  where frame_type=4;
-- 修改镜像标签字段数据长度
alter table pt_image modify image_tag varchar(64) not null comment '镜像版本';

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

-- 初始化用户组和权限组菜单
INSERT INTO menu (pid, type, name, icon, path, component, component_name, layout, permission, hidden, cache, sort, create_user_id, update_user_id, create_time, update_time, deleted, back_to, ext_config) VALUES (90, 1, '用户组管理', 'tuanduiguanli-tuanduiguanli', 'userGroup', 'system/userGroup', 'UserGroup', 'BaseLayout', 'system:userGroup', false, false, 91, 3, 3, '2021-05-11 09:32:32', '2021-05-11 09:32:32', false, null, '{}');
INSERT INTO menu (pid, type, name, icon, path, component, component_name, layout, permission, hidden, cache, sort, create_user_id, update_user_id, create_time, update_time, deleted, back_to, ext_config) VALUES (90, 1, '权限管理', 'fuwuguanli', 'authCode', 'system/authCode', 'AuthCode', 'BaseLayout', 'authCode', false, false, 92, 1, 3, '2021-05-19 09:16:32', '2021-05-19 09:19:44', false, null, '{}');

-- 初始化管理员和注册用户 用户组菜单权限
INSERT INTO roles_menus (role_id, menu_id) value (1, (select id from menu where path = 'userGroup'));
INSERT INTO roles_menus (role_id, menu_id) value (2, (select id from menu where path = 'userGroup'));

-- 初始化管理员和注册用户 权限组菜单权限
INSERT INTO roles_menus (role_id, menu_id) value (1, (select id from menu where path = 'authCode'));
INSERT INTO roles_menus (role_id, menu_id) value (2, (select id from menu where path = 'authCode'));

-- 修改训练主表字段
alter table pt_train_job change out_path model_path varchar(128) default '' null comment '训练模型输出路径';
alter table pt_train_job change log_path out_path varchar(128) default '' null comment '训练输出路径';
-- 修改算法表字段
alter table pt_train_algorithm change is_train_out is_train_model_out tinyint(1) default 1 null comment '是否输出训练结果:1是，0否';
alter table pt_train_algorithm change is_train_log is_train_out tinyint(1) default 1 null comment '是否输出训练信息:1是，0否';

-- 增加转预置任务字段
ALTER TABLE `data_task` ADD COLUMN `version_name` varchar(255) NULL COMMENT '转预置版本号';
ALTER TABLE `data_task` ADD COLUMN `target_id` bigint(20) NULL COMMENT '目标数据集id';

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

-- 补充集群状态 pod状态
INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (4, '调度中', 'Pending', 2);
INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (4, '运行完成', 'Succeeded', 3);
INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (4, '已删除', 'Deleted', 4);
INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (4, '运行失败', 'Failed', 5);
INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (4, '未知状态', 'Unknown', 6);

-- 修改notebook表字段
alter table notebook MODIFY COLUMN cpu_num INT(11) NOT NULL DEFAULT '0' COMMENT 'CPU数量(核)';
alter table notebook MODIFY COLUMN gpu_num INT(11) NOT NULL DEFAULT '0' COMMENT 'GPU数量（核）';
alter table notebook MODIFY COLUMN mem_num INT(11) NOT NULL DEFAULT '0' COMMENT '内存大小（M）';
alter table notebook MODIFY COLUMN disk_mem_num INT(11) NOT NULL DEFAULT '0' COMMENT '硬盘内存大小（M）';

-- 训练状态对应的详情信息
ALTER TABLE `pt_train_job`
	ADD COLUMN `status_detail` json NULL DEFAULT NULL COMMENT '状态对应的详情信息';
-- notebook状态对应的详情信息
ALTER TABLE `notebook`
	ADD COLUMN `status_detail` json NULL DEFAULT NULL COMMENT '状态对应的详情信息';

-- serving_batch状态对应的详情信息
ALTER TABLE `serving_batch`
    ADD COLUMN `status_detail` json NULL DEFAULT NULL COMMENT '状态对应的详情信息' AFTER `status`  ;
-- serving_info状态对应的详情信息
ALTER TABLE `serving_info`
    ADD COLUMN `status_detail` json NULL DEFAULT NULL COMMENT '状态对应的详情信息' AFTER `status`  ;
-- model_opt_task_instance状态对应的详情信息
ALTER TABLE `model_opt_task_instance`
    ADD COLUMN `status_detail` json NULL DEFAULT NULL COMMENT '状态对应的详情信息' AFTER `status`  ;

-- 补充模型来源
INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`) VALUES (9, '模型转换', '3', 4);

-- 菜单新增资源规格相关路由
INSERT INTO `menu`( `pid`, `type`, `name`, `icon`, `path`, `component`, `component_name`, `layout`, `hidden`, `permission`) VALUES
(90, 1, '资源规格管理','xunlianzhunbei','resources','system/resources','Resources','BaseLayout',b'0','system:specs');

-- 初始化资源规格
-- 初始化notebook资源规格
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('1CPU2GB内存',0,1,1,0,2048,500);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('2CPU4GB内存',0,1,2,0,2048,500);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('1CPU2GB内存 1GPU',1,1,1,1,2048,2048);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('1CPU2GB内存 2GPU',1,1,1,2,2048,2048);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('2CPU4GB内存 1GPU',1,1,2,1,4096,2048);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('2CPU4GB内存 2GPU',1,1,2,2,4096,2048);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('4CPU16GB内存 2GPU',1,1,16,2,16000,4096);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('4CPU16GB内存 4GPU',1,1,16,4,16000,4096);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('8CPU64GB内存 2GPU',1,1,8,2,64000,4096);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('16CPU128GB内存 4GPU',1,1,16,4,128000,50000);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('32CPU256GB内存 8GPU',1,1,32,8,256000,50000);
-- 初始化train资源规格
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('1CPU2GB内存',0,2,1,0,2048,500);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('2CPU4GB内存',0,2,2,0,2048,500);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('1CPU2GB内存 1GPU',1,2,1,1,2048,2048);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('1CPU2GB内存 2GPU',1,2,1,2,2048,2048);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('2CPU4GB内存 1GPU',1,2,2,1,4096,2048);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('2CPU4GB内存 2GPU',1,2,2,2,4096,2048);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('4CPU16GB内存 2GPU',1,2,16,2,16000,4096);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('4CPU16GB内存 4GPU',1,2,16,4,16000,4096);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('8CPU64GB内存 2GPU',1,2,8,2,64000,4096);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('16CPU128GB内存 4GPU',1,2,16,4,128000,50000);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('32CPU256GB内存 8GPU',1,2,32,8,256000,50000);
-- 初始化serving资源规格
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('1CPU2GB内存',0,3,1,0,2048,500);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('2CPU4GB内存',0,3,2,0,2048,500);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('1CPU2GB内存 1GPU',1,3,1,1,2048,2048);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('1CPU2GB内存 2GPU',1,3,1,2,2048,2048);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('2CPU4GB内存 1GPU',1,3,2,1,4096,2048);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('2CPU4GB内存 2GPU',1,3,2,2,4096,2048);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('4CPU16GB内存 2GPU',1,3,16,2,16000,4096);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('4CPU16GB内存 4GPU',1,3,16,4,16000,4096);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('8CPU64GB内存 2GPU',1,3,8,2,64000,4096);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('16CPU128GB内存 4GPU',1,3,16,4,128000,50000);
INSERT INTO resource_specs(specs_name,resources_pool_type,module,cpu_num,gpu_num,mem_num,workspace_request) value
('32CPU256GB内存 8GPU',1,3,32,8,256000,50000);

--删除无效的资源规格表
drop table notebook_model;
drop table pt_train_job_specs;

-- 补充度量管理菜单权限标识
update menu set permission='atlas:measure' where name='度量管理';

-- 控制台
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) VALUES (0, '控制台', 1, 1);
-- 控制台-用户管理操作权限初始化
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) select id, '用户管理', 1, 1 from permission where name='控制台';
select @pid := @@IDENTITY;
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '创建用户', 'system:user:create', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '编辑用户', 'system:user:edit', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '删除用户', 'system:user:delete', 1, 1);
-- 控制台-用户组管理操作权限初始化
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) select id, '用户组管理', 1, 1 from permission where name='控制台';
select @pid := @@IDENTITY;
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '创建用户组', 'system:userGroup:create', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '编辑用户组', 'system:userGroup:edit', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '删除用户组', 'system:userGroup:delete', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '编辑成员', 'system:userGroup:editUser', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '批量修改角色', 'system:userGroup:editUserRole', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '批量激活锁定', 'system:userGroup:editUserState', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '批量删除用户', 'system:userGroup:deleteUser', 1, 1);
-- 控制台-权限管理操作权限初始化
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) select id, '权限管理', 1, 1 from permission where name='控制台';
select @pid := @@IDENTITY;
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '创建权限组', 'system:authCode:create', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '编辑权限组', 'system:authCode:edit', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '删除权限`组', 'system:authCode:delete', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '创建权限', 'system:permission:create', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '编辑权限', 'system:permission:edit', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '删除权限', 'system:permission:delete', 1, 1);
-- 控制台-角色管理操作权限初始化
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) select id, '角色管理', 1, 1 from permission where name='控制台';
select @pid := @@IDENTITY;
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '创建角色', 'system:role:create', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '编辑角色', 'system:role:edit', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '删除角色', 'system:role:delete', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '权限分配', 'system:role:auth', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '菜单分配', 'system:role:menu', 1, 1);
-- 控制台-菜单管理操作权限初始化
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) select id, '菜单管理', 1, 1 from permission where name='控制台';
select @pid := @@IDENTITY;
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '删除菜单', 'system:menu:create', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '编辑菜单', 'system:menu:edit', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '创建菜单', 'system:menu:delete', 1, 1);
-- 控制台-字典管理操作权限初始化
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) select id, '字典管理', 1, 1 from permission where name='控制台';
select @pid := @@IDENTITY;
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '创建字典', 'system:dict:create', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '编辑字典', 'system:dict:edit', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '删除字典', 'system:dict:delete', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '字典详情-创建', 'system:dictDetail:create', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '字典详情-修改', 'system:dictDetail:edit', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '字典详情-删除', 'system:dictDetail:delete', 1, 1);
-- 控制台-资源规格管理操作权限初始化
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) select id, '资源规格管理', 1, 1 from permission where name='控制台';
select @pid := @@IDENTITY;
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '创建资源规格', 'system:specs:create', 1, 1 );
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '修改资源规格', 'system:specs:edit', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '删除资源规格', 'system:specs:delete', 1, 1);

-- 算法开发
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) VALUES (0, '算法开发', 1, 1);
-- 算法开发-算法管理操作权限初始化
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) select id, '算法管理', 1, 1 from permission where name='算法开发';
select @pid := @@IDENTITY;
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '创建算法', 'development:algorithm:create', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '修改算法', 'development:algorithm:edit', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '删除算法', 'development:algorithm:delete', 1, 1);
-- 算法开发-notebook操作权限初始化
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) select id, 'notebook', 1, 1 from permission where name='算法开发';
select @pid := @@IDENTITY;
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '创建notebook', 'notebook:create', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '修改notebook', 'notebook:update', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '打开notebook', 'notebook:open', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '启动notebook', 'notebook:start', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '停止notebook', 'notebook:stop', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '删除notebook', 'notebook:delete', 1, 1);

-- 模型管理
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) VALUES (0, '模型管理', 1, 1);
-- 模型管理-模型列表操作权限初始化
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) select id, '模型列表', 1, 1 from permission where name='模型管理';
select @pid := @@IDENTITY;
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '模型列表-创建', 'model:model:create', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '模型列表-修改', 'model:model:edit', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '模型列表-删除', 'model:model:delete', 1, 1);
-- 模型管理-模型优化操作权限初始化
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) select id, '模型优化', 1, 1 from permission where name='模型管理';
select @pid := @@IDENTITY;
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '创建任务', 'model:optimize:createTask', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '提交任务', 'model:optimize:submitTask', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '修改任务', 'model:optimize:editTask', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '删除任务', 'model:optimize:deleteTask', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '提交任务实例', 'model:optimize:submitTaskInstance', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '取消任务实例实例', 'model:optimize:cancelTaskInstance', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '删除任务实例', 'model:optimize:deleteTaskInstance', 1, 1);
-- 模型管理-模型列表历史版本-模型版本管理操作权限初始化
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) select id, '模型版本管理', 1, 1 from permission where name='模型管理';
select @pid := @@IDENTITY;
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '模型版本管理-创建', 'model:branch:create', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '模型版本管理-删除', 'model:branch:delete', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '模型版本管理-转预置', 'model:branch:convertPreset', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '模型版本管理-转onnx', 'model:branch:convertOnnx', 1, 1);

-- 训练管理
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) VALUES (0, '训练管理', 1, 1);
-- 训练管理-镜像管理操作权限初始化
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) select id, '镜像管理', 1, 1 from permission where name='训练管理';
select @pid := @@IDENTITY;
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '上传镜像', 'training:image:upload', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '修改镜像', 'training:image:edit', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '删除镜像', 'training:image:delete', 1, 1);
-- 训练管理-训练任务操作权限初始化
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) select id, '训练任务', 1, 1 from permission where name='训练管理';
select @pid := @@IDENTITY;
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '创建训练', 'training:job:create', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '修改训练', 'training:job:update', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '删除训练', 'training:job:delete', 1, 1);

-- 云端serving
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) VALUES (0, '云端serving', 1, 1);
-- 云端serving-在线服务操作权限初始化
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) select id, '在线服务', 1, 1 from permission where name='云端serving';
select @pid := @@IDENTITY;
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '创建服务', 'serving:online:create', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '修改服务', 'serving:online:edit', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '删除服务', 'serving:online:delete', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '启动服务', 'serving:online:start', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '停止服务', 'serving:online:stop', 1, 1);
-- 云端serving-批量服务操作权限初始化
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) select id, '批量服务', 1, 1 from permission where name='云端serving';
select @pid := @@IDENTITY;
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '创建批量任务', 'serving:batch:create', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '修改批量服务', 'serving:batch:edit', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '删除批量服务', 'serving:batch:delete', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '启动批量服务', 'serving:batch:start', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '停止批量服务', 'serving:batch:stop', 1, 1);

-- 模型炼知
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) VALUES (0, '模型炼知', 1, 1);
-- 模型炼知-度量管理操作权限初始化
insert into `permission` (`pid`, `name`, `create_user_id`, `update_user_id`) select id, '度量管理', 1, 1 from permission where name='模型炼知';
select @pid := @@IDENTITY;
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '创建度量', 'atlas:measure:create', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '编辑度量', 'atlas:measure:edit', 1, 1);
insert into `permission` (`pid`, `name`, `permission`, `create_user_id`, `update_user_id`) values (@pid, '删除度量', 'atlas:measure:delete', 1, 1);

-- 管理员角色操作权限初始化
insert into auth(id, auth_code, description, create_user_id, update_user_id) values (1, 'admin权限组', '默认全部操作权限', 1, 1);
insert into auth_permission (auth_id, permission_id) select 1, id from permission;
INSERT INTO `roles_auth` (role_id, auth_id) values (1, 1);

-- 修改menu表模型版本管理权限标识
update menu set permission='model:branch' where name='模型版本管理';

-- 修改任务参数模型输出路径、训练输出路径
alter table pt_train_param change out_path model_path varchar(128) default '' null comment '模型输出路径';
alter table pt_train_param change log_path out_path varchar(128) default '' null comment '输出路径';