-- DDL 脚本

create table if not exists data_dataset
(
    id                   bigint auto_increment
        primary key,
    name                 varchar(255)                           not null,
    remark               varchar(255)                           null,
    type                 varchar(255) default '0'               not null comment '类型 0: private 私有数据,  1:team  团队数据  2:public 公开数据',
    team_id              bigint                                 null,
    uri                  varchar(255) default ''                null comment '数据集存储位置',
    create_user_id       bigint                                 null,
    create_time          datetime     default CURRENT_TIMESTAMP not null,
    update_user_id       bigint                                 null,
    update_time          datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    deleted              bit          default b'0'              not null,
    data_type            tinyint      default 0                 not null comment '数据类型:0图片，1视频',
    annotate_type        tinyint      default 0                 not null comment '标注类型：0分类,1目标检测',
    labels varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '标签集合，以逗号分隔',
    status               tinyint      default 0                 not null comment '0:未标注，1:手动标注中，2:自动标注中，3:自动标注完成，4:标注完成，5:未采样，6:目标跟踪完成，7:采样中',
    current_version_name varchar(16)                            null comment '当前版本号',
    `is_import`          tinyint(1)   DEFAULT '0' COMMENT '是否用户导入',
    `archive_url`        varchar(255) DEFAULT NULL COMMENT '用户导入数据集压缩包地址',
    `decompress_state`   tinyint(2)   DEFAULT '0' COMMENT '解压状态: 0未解压 1解压中 2解压完成 3解压失败',
    `decompress_fail_reason` varchar(255) DEFAULT NULL COMMENT '解压失败原因',
    constraint idx_name_unique
        unique (name) comment '数据集名称唯一'
)
    comment '数据集管理' charset = utf8;

create table if not exists data_dataset_label
(
    id         bigint unsigned auto_increment
        primary key,
    dataset_id bigint unsigned not null,
    label_id   bigint unsigned not null,
    constraint dataset_id
        unique (dataset_id, label_id)
)
    charset = utf8;

create table if not exists data_dataset_version
(
    id             bigint(19) auto_increment comment '主键'
        primary key,
    dataset_id     bigint(19)       null comment '数据集ID',
    team_id        bigint(19)       null comment '团队ID',
    create_user_id bigint(19)       null comment '创建人',
    create_time    datetime         not null comment '创建时间',
    update_user_id bigint(19)       null comment '修改人',
    update_time    datetime         null comment '修改时间',
    deleted        bit default b'0' not null comment '数据集版本删除标记0正常，1已删除',
    version_name   varchar(8)       not null comment '版本号',
    version_note   varchar(50)      not null comment '版本说明',
    version_source varchar(32)      null comment '来源版本号',
    version_url    varchar(255)     null comment '版本信息存储url',
    data_conversion int(1) NOT NULL DEFAULT 0 COMMENT '数据转换；0：未复制；1：已复制;2:转换完成,3:转换失败',
    constraint unique_version
        unique (dataset_id, version_name) comment '数据集版本号唯一'
)
    comment '数据集版本表' charset = utf8mb4;

create table if not exists data_dataset_version_file
(
    id                bigint auto_increment comment '主键'
        primary key,
    dataset_id        bigint                     null comment '数据集ID',
    version_name      varchar(8)                 null comment '数据集版本',
    file_id           bigint                     null comment '文件ID',
    status            tinyint(1)       default 2 not null comment '状态 0: 新增 1:删除 2:正常',
    annotation_status tinyint unsigned default 0 not null comment '状态:0-未标注，1-标注中，2-自动标注完成，3-已标注完成,4-目标追踪完成',
    backup_status tinyint(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT '数据集状态备份,版本切换使用',
    changed tinyint(1) NULL DEFAULT 0 COMMENT '0-未改变;1-改变'
)
    comment '数据集版本文件关系表' charset = utf8mb4;

create table if not exists data_file
(
    id             bigint unsigned zerofill auto_increment comment 'ID'
        primary key,
    name           varchar(255)     default ''                not null comment '文件名',
    status         tinyint unsigned default 0                 not null comment '状态:0-未标注，1-标注中，2-自动标注完成，3-已标注完成,4-目标追踪完成',
    dataset_id     bigint                                     null comment '数据集id',
    url            varchar(255)     default ''                not null comment '资源访问路径',
    create_user_id bigint                                     null comment '创建用户ID',
    create_time    datetime         default CURRENT_TIMESTAMP not null comment '创建时间',
    update_user_id bigint                                     null comment '更新用户ID',
    update_time    datetime         default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted        bit              default b'0'              not null comment '0正常，1已删除',
    md5            varchar(255)     default ''                not null comment '文件md5',
    file_type      tinyint          default 0                 null comment '文件类型  0-图片,1-视频',
    pid            bigint           default 0                 null comment '父文件id',
    frame_interval int              default 0                 not null comment '帧间隔',
    enhance_type smallint(3) NULL DEFAULT NULL COMMENT '增强类型',
    constraint name_uniq
        unique (name, dataset_id, deleted)
)
    comment '文件信息' charset = utf8;

create index dataset_upt_time
    on data_file (dataset_id, update_time);

create index deleted
    on data_file (deleted);

create index status
    on data_file (dataset_id, status, deleted);

create index uuid
    on data_file (url, deleted);

create table if not exists data_label
(
    id             bigint primary key auto_increment,
    `name`           varchar(255) default ''                not null,
    color          varchar(7)   default '#000000'         not null,
    create_user_id bigint                                 null,
    create_time    datetime     default CURRENT_TIMESTAMP not null,
    update_user_id bigint                                 null,
    update_time    datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    deleted        bit          default b'0'              not null,
    `type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '标签类型 0:自定义标签 1:自动标注标签 2:ImageNet 3: MS COCO'
)  auto_increment = 11000
    comment '数据集标签' charset = utf8;

create index dataset
    on data_label (name, deleted);

create table if not exists data_task
(
    id             bigint unsigned zerofill auto_increment comment 'ID'
        primary key,
    total          int          default 0                 not null comment '任务需要处理的文件总数',
    status         tinyint      default 1                 not null comment '任务状态，创建即为进行中。1进行中，2已完成',
    finished       int          default 0                 not null comment '已完成的文件数',
    files          varchar(255) default ''                null comment '文件id数组',
    datasets       varchar(255) default ''                null comment '数据集id数组',
    create_user_id bigint                                 null comment '创建用户ID',
    create_time    datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted        bit          default b'0'              not null comment '0正常，1已删除',
    annotate_type  tinyint      default 0                 not null comment '标注类型：0分类,1目标检测',
    data_type      tinyint      default 0                 not null comment '数据类型:0图片，1视频',
    labels         varchar(255)                           not null comment '该自动标注任务使用的标签数组，json串形式'
)
    comment '标注任务信息' charset = utf8;

create index deleted
    on data_task (deleted);

create index ds_status
    on data_task (datasets, status);

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
	`user_id` BIGINT(20) NOT NULL COMMENT '所属用户ID',
	`name` VARCHAR(100) NOT NULL COMMENT 'notebook名称(供K8S使用)',
    `notebook_name` VARCHAR(100) NOT NULL COMMENT 'notebook名称(供前端使用)',
	`description` VARCHAR(255) NULL DEFAULT NULL COMMENT '描述',
	`url` VARCHAR(255) NULL DEFAULT NULL COMMENT '访问 notebook 在 Jupyter 里所需的url',
	`total_run_min` INT(11) NOT NULL DEFAULT '0' COMMENT '运行总时间(分钟)',
	`cpu_num` TINYINT(4) NOT NULL DEFAULT '0' COMMENT 'CPU数量',
	`gpu_num` TINYINT(4) NOT NULL DEFAULT '0' COMMENT 'GPU数量',
	`mem_num` INT(11) NOT NULL DEFAULT '0' COMMENT '内存大小（G）',
	`disk_mem_num` INT(11) NOT NULL DEFAULT '0' COMMENT '硬盘内存大小（G）',
	`create_resource` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '0 - notebook 创建 1- 其它系统创建',
	`status` TINYINT(4) NOT NULL DEFAULT '1' COMMENT '0运行中，1停止, 2删除, 3启动中，4停止中，5删除中，6运行异常（暂未启用）',
	`last_start_time` TIMESTAMP NULL DEFAULT NULL COMMENT '上次启动执行时间',
	`last_operation_timeout` BIGINT(20) NULL DEFAULT NULL COMMENT '上次操作对应超时时间点（20200603121212）',
	`k8s_status_code` VARCHAR(100) NULL DEFAULT NULL COMMENT 'k8s响应状态码',
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
	`algorithm_id` bigint(20) DEFAULT '0' COMMENT '算法ID',
	PRIMARY KEY (`id`),
	INDEX `status` (`status`),
	INDEX `user_id` (`user_id`),
	INDEX `name` (`name`),
	INDEX `last_operation_timeout` (`last_operation_timeout`),
	INDEX `k8s_namespace` (`k8s_namespace`),
	INDEX `k8s_resource_name` (`k8s_resource_name`)
)
COMMENT='notebook数据表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;


create table if not exists notebook_model
(
    id           int auto_increment comment '主键ID'
        primary key,
    model_type   varchar(50)   not null comment '模板类型 CPU GPU',
    cpu_num      int           not null comment 'CPU数量',
    gpu_num      int           not null comment 'GPU数量',
    mem_num      int           not null comment '内存大小',
    spec         varchar(50)   null comment 'GPU规格',
    deleted      int default 0 null comment '0正常，1已删除',
    disk_mem_num int           null comment '硬盘内存大小',
    default_status int(11) DEFAULT '0' COMMENT '默认值，1 为默认 '
)
    comment 'notebook模板';

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
    image_name     varchar(127) default '' null COMMENT '镜像名称'
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
    status           tinyint(3)                         null comment '文件拷贝状态(0文件拷贝中，1文件拷贝成功，2文件拷贝失败)'
)
    comment '分支管理' charset = utf8;

create table if not exists pt_model_info
(
    id                bigint auto_increment comment '主键'
        primary key,
    name              varchar(255)                       not null comment '模型名称',
    frame_type        tinyint                            not null comment '框架类型',
    model_format      tinyint                            not null comment '模型文件的格式（后缀名）',
    model_description varchar(255)                       not null comment '模型描述',
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
    total_num         bigint   default 0                 null comment '模型版本总的个数'
)
    comment '模型管理' charset = utf8;

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
    train_key      varchar(32)          null
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
    is_train_out       tinyint(1)   default 1  null comment '是否输出训练结果:1是，0否',
    is_train_log       tinyint(1)   default 1  null comment '是否输出训练日志:1是，0否',
    is_visualized_log  tinyint(1)   default 0  null comment '是否输出可视化日志:1是，0否'
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
    out_path             varchar(128) default ''                null comment '训练输出位置',
    log_path             varchar(128) default ''                null comment '作业日志路径',
    resources_pool_type  tinyint(1)   default 0                 not null comment '类型(0为CPU，1为GPU)',
    resources_pool_specs varchar(128)                           null comment '规格',
    resources_pool_node  int(8)       default 1                 not null comment '节点个数',
    train_status         tinyint(1)   default 0                 not null comment '训练作业job状态, 0为待处理，1为运行中，2为运行完成，3为失败，4为停止，5为未知，6为删除，7为创建失败)',
    deleted              tinyint(1)   default 0                 null comment '删除(0正常，1已删除)',
    create_user_id       bigint                                 null comment '创建人',
    create_time          timestamp                              null comment '创建时间',
    update_user_id       bigint                                 null comment '更新人',
    update_time          timestamp                              null comment '更新时间',
    visualized_log_path  varchar(128) default ''                null comment '可视化日志路径',
    data_source_name     varchar(127)                           null comment '数据集名称',
    data_source_path     varchar(127)                           null comment '数据集路径',
    train_job_specs_id   int(6)       default null                   COMMENT '训练规格id',
    k8s_job_name         varchar(70)                            null comment 'k8s创建好的job名称',
    constraint inx_tran_id_version
        unique (train_id, train_version)
)
    comment '训练作业job表' charset = utf8mb4;

create index inx_create_user_id
    on pt_train_job (create_user_id);

create table if not exists pt_train_job_specs
(
    id          int(6) auto_increment comment '主键id'
        primary key,
    specs_name  varchar(128) default ''                not null comment '规格名称',
    specs_info  json                                   not null comment '规格信息',
    resources_pool_type  tinyint(1)   default 0        not null comment '规格类型(0为CPU, 1为GPU)',
    deleted              tinyint(1)   default 0                 null comment '删除(0正常，1已删除)',
    create_user_id       bigint                                 null comment '创建人',
    create_time          timestamp                              null comment '创建时间',
    update_user_id       bigint                                 null comment '更新人',
    update_time          timestamp                              null comment '更新时间'
)
    comment '规格表' charset = utf8mb4;

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
    train_job_specs_id   int(6)       default null    COMMENT '训练规格id'
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
	user_id           bigint(20)     not null                comment '用户id',
	type              varchar(20)    not null                comment '类型',
	is_default        tinyint(1)     not null default '0'    comment '是否为默认值',
	aux_info         varchar(50)     null     default NULL   comment '辅助信息',
	deleted              tinyint(1)   default 0                 null comment '删除(0正常，1已删除)',
    create_user_id       bigint                                 null comment '创建人',
    create_time          timestamp                              null comment '创建时间',
    update_user_id       bigint                                 null comment '更新人',
    update_time          timestamp                              null comment '更新时间',
	primary key (`id`),
	index `inx_user_id_type` (`user_id`, `type`) USING BTREE
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
    image_tag      varchar(32)          not null comment '镜像版本',
    remark         varchar(1024)        null comment '镜像描述',
    create_user_id bigint               null comment '创建人',
    create_time    timestamp            null comment '创建时间',
    update_user_id bigint               null comment '更新人',
    update_time    timestamp            null comment '更新时间',
    deleted        tinyint(1) default 0 null comment '删除(0正常，1已删除)'
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
