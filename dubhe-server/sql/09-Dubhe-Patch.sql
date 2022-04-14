use `dubhe-cloud-prod`;

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



ALTER TABLE `notebook`
    ADD COLUMN `pip_site_package_path` VARCHAR(255) NULL COMMENT 'pip包路径';


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


ALTER TABLE
  `pt_job_param`
add
  column notebook_name varchar(255) default null null comment 'notebook名称';