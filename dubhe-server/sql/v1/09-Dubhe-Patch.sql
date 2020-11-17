-- 热更脚本

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

-- 变更存储过程分隔符";"为"//"
delimiter //

-- 若迭代为第一版本或第二版本，则初始化版本数据
CREATE PROCEDURE initialVersionDataProc()
BEGIN
   SELECT COUNT(*) into @i FROM system_version;
    IF @i=0 THEN
    INSERT INTO `system_version` (`id`, `version`) VALUES (1, 1);
    END IF;
END;
CALL initialVersionDataProc(); //
DROP PROCEDURE IF EXISTS initialVersionDataProc; //


-- 第二版本补丁（执行以下存储过程）
CREATE PROCEDURE secondEditionProc()
BEGIN
start transaction; -- 整个存储过程指定为一个事务
	SELECT Max(version) into @y FROM system_version;
    IF @y=1 THEN
    -- -----------------------------------------------------------------------------------------------------------------
    -- DDL

    -- data_label_group  表 新建
    create table if not exists data_label_group
    (
        id             bigint primary key auto_increment,
        name           varchar(255) default ''                not null COMMENT '标签组名称',
        create_user_id bigint                                 null,
        create_time    datetime     default CURRENT_TIMESTAMP not null,
        update_user_id bigint                                 null,
        update_time    datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
        deleted        bit          default b'0'              not null,
        remark         varchar(255)     null comment '描述',
        type tinyint(1) NOT NULL DEFAULT 0  comment '类型 0: private 私有标签组,  1:public 公开标签组',
        origin_user_id  bigint         NULL COMMENT '资源用有人ID'
    )
        comment '标签组' charset = utf8mb4;

    -- data_sequence  自定义获取主键ID表
    create table data_sequence
    (
        id            int         not null
            primary key,
        business_code varchar(50) not null,
        start         int         not null,
        step          int         not null
    )
        comment '自定义获取主键ID表' charset = utf8mb4;

    -- data_group_label  标签组标签中间表表
    create table if not exists data_group_label
    (
        id             bigint primary key auto_increment,
        label_id bigint     NULL COMMENT '标签Id',
        label_group_id bigint     NULL COMMENT '标签组Id',
        create_user_id bigint                                 null,
        create_time    datetime     default CURRENT_TIMESTAMP not null,
        update_user_id bigint                                 null,
        update_time    datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
        deleted        bit          default b'0'              not null
    )
        comment '标签组标签中间表' charset = utf8mb4;

    -- 修改data_dataset起始自增ID为101
    alter table data_dataset AUTO_INCREMENT=101;

    -- data_file frame_interval字段取消非空
    ALTER TABLE `data_file`
        MODIFY COLUMN `frame_interval` int(11) NULL DEFAULT 0 COMMENT '帧间隔';

    -- data_file 表 新增 width height 字段
    ALTER TABLE `data_file`
        ADD COLUMN `width`  int default null null comment '图片宽';
    ALTER TABLE `data_file`
        ADD COLUMN `height` int default null null comment '图片高';

    -- data_dataset 表 新增 is_top 字段
    ALTER TABLE `data_dataset`
        ADD COLUMN `is_top` tinyint(1) NULL COMMENT '是否为置顶';

    -- data_task 表 修改 status 字段
    alter table `data_task`
        modify column `status` tinyint(4) not null default 0 comment '任务状态 0.待分配 1.分配中 2.进行中 3.已完成 4.失败';

    -- data_task 表 修改 labels 字段
    alter table `data_task`
        modify column `labels` text character set utf8 collate utf8_general_ci not null comment '该自动标注任务使用的标签数组，json串形式';

    -- data_task 表 新增 failed 字段
    alter table `data_task`
        add column `failed` int(11) not null default 0 comment '失败文件数量';

    -- data_task 表 新增 dataset_id 字段
    alter table `data_task`
        add column `dataset_id` bigint(20)null default null comment '数据集id';

    -- data_task 表 新增 type 字段
    alter table `data_task`
        add column `type` smallint(3) null default null comment '任务类型 0.自动标注 1.ofrecord 2.imagenet 3.数据增强 4.目标跟踪 5.视频采样';

    -- data_task 表 新增 dataset_version_id 字段
    alter table `data_task`
        add column `dataset_version_id` bigint(20) null default null comment '数据集版本id';

    -- data_task 表 新增 enhance_type 字段
    alter table `data_task`
        add column `enhance_type` text character set utf8 collate utf8_general_ci null comment '增强类型数组';

    -- data_task 表 新增 url 字段
    alter table `data_task`
        add column `url` varchar(255) character set utf8 collate utf8_general_ci null default null comment '视频文件url';

    -- data_task 表 新增 frame_interval 字段
    alter table `data_task`
        add column `frame_interval` int(11) null default null comment '视频帧间隔' after `url`;

    -- data_dataset_version data_file 表 新增 origin_user_id   字段
    ALTER TABLE `data_dataset_version`
        ADD COLUMN `origin_user_id` bigint NULL COMMENT '主表资源用户id';

    -- data_file 表 新增 origin_user_id   字段
    ALTER TABLE `data_file`
        ADD COLUMN `origin_user_id` bigint NULL COMMENT '主表资源用户id';

    -- data_dataset 表 新增 origin_user_id   字段
    ALTER TABLE `data_dataset`
        ADD COLUMN `origin_user_id` bigint NULL COMMENT '主表资源用户id';

    -- data_dataset_version_file  表 修改 annotation_status 字段
    alter table data_dataset_version_file modify annotation_status tinyint unsigned default 0 not null comment '101:未标注  102:手动标注中  103:自动标注完成  104:标注完成  105:标注未识别  201:目标跟踪完成';

    -- data_dataset  表 修改 status 字段
    alter table data_dataset modify status int default 0 not null comment '101:未标注  102:手动标注中  103:自动标注中  104:自动标注完成  105:标注完成  201:目标跟踪中  202:目标跟踪完成  203:目标跟踪失败  301:未采样  302:采样中  303:采样失败  401:增强中  402:导入中';

    -- data_dataset  表 新建 label_group_id  字段
    ALTER TABLE `data_dataset` ADD COLUMN `label_group_id` bigint NULL COMMENT '标签组ID';

    -- data_label_group  表 新建 operate_type  字段
    ALTER TABLE `data_label_group`ADD COLUMN `operate_type` int NULL COMMENT '操作类型 1:Json编辑器操作类型 2:自定义操作类型 3:导入操作类型';

    create index select_file on data_dataset_version_file (dataset_id,status,annotation_status,version_name,file_id);




    -- 修改表字段和属性
    alter table pt_train_job
        change train_job_specs_id train_job_specs_name varchar(32) null comment '训练规格名称';

    -- 增加算法创建状态字段
    alter table pt_train_algorithm
        add algorithm_status tinyint(1) default 0 not null comment '算法上传状态，0：创建中，1：创建成功，2：创建失败' after algorithm_source;


    -- 分布式训练 --
    ALTER TABLE `pt_train_job`
        ADD COLUMN `train_type` TINYINT(1) ZEROFILL NULL DEFAULT '0' COMMENT '训练类型 0：普通训练，1：分布式训练' AFTER `train_status`;

    -- 训练任务模板规格id-->name
    alter table pt_train_param
        change train_job_specs_id train_job_specs_name varchar(32) null comment '训练规格名称';

    -- 增加验证数据来源名称字段
    alter table pt_train_job
        add column val_data_source_name varchar(127)  comment '验证数据来源名称';

    -- 增加验证数据来源路径字段
    alter table pt_train_job
        add column val_data_source_path varchar(255)  comment '验证数据来源路径';

    -- 增加是否验证数据集字段
    alter table pt_train_job
        add column val_type tinyint(1) default 0 comment '是否验证数据集';

    -- 增加模型id
    alter  table pt_train_job
        add column model_id bigint comment '模型id';

    -- 是否加载模型
    alter  table pt_train_job
        add column model_type tinyint(1) comment '是否加载模型';

    -- 增加模型来源
    alter  table pt_train_job
        add column model_resource tinyint(1) comment '模型来源';

    -- 增加模型名称
    alter  table pt_train_job
        add column model_name varchar(255) comment '模型名称';

    -- 增加模型加载路径
    alter  table pt_train_job
        add column model_load_dir varchar(255) comment '模型加载路径';

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

    -- pt_job_param 表 新增 delay_create_time delay_delete_time 字段 训练延时启动，自动停止 --
    ALTER TABLE `pt_job_param`
        ADD COLUMN `delay_create_time` timestamp NULL COMMENT '训练延时启动时间';
    ALTER TABLE `pt_job_param`
        ADD COLUMN `delay_delete_time` timestamp NULL COMMENT '训练自动停止时间';

    -- 增加模型id
    alter  table pt_train_param
        add column model_id bigint comment '模型id';

    -- 是否加载模型
    alter  table pt_train_param
        add column model_type tinyint(1) comment '是否加载模型';

    -- 增加模型来源
    alter  table pt_train_param
        add column model_resource tinyint(1) comment '模型来源';

    -- 增加模型名称
    alter  table pt_train_param
        add column model_name varchar(255) comment '模型名称';

    -- 增加模型加载路径
    alter  table pt_train_param
        add column model_load_dir varchar(255) comment '模型加载路径';

    -- 垃圾回收任务表
        create table recycle_task
        (
            id                 bigint auto_increment comment '主键'
                primary key,
            recycle_module     varchar(32)                          not null comment '回收模块',
            recycle_type       tinyint(1) default 0                 not null comment '回收类型(0文件，1数据库表数据)',
            recycle_custom     varchar(64)                          null comment '回收定制化方式',
            recycle_condition  text                                 not null comment '回收条件(回收表数据sql、回收文件绝对路径)',
            recycle_delay_date date                                 null comment '回收日期',
            recycle_status     tinyint    default 0                 null comment '回收状态(0:待回收，1:已回收，2:回收失败)',
            create_user_id     bigint                               null comment '创建人ID',
            update_user_id     bigint                               null comment '修改人ID',
            create_time        datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
            update_time        datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间',
            recycle_note       varchar(512)                         null comment '回收备注',
            deleted            tinyint(1) default 0                 not null comment '删除(0正常，1已删除)'
        )
        comment '垃圾回收任务表' charset = utf8mb4;

    -- 模型开发数据数据权限修改
    ALTER TABLE `notebook`
        ALTER `user_id` DROP DEFAULT;
    ALTER TABLE `notebook`
        CHANGE COLUMN `user_id` `origin_user_id` BIGINT(20) NOT NULL COMMENT '所属用户ID' AFTER `id`;

    -- 训练开发数据数据权限修改
    ALTER TABLE `pt_image`
        ADD COLUMN `origin_user_id` bigint NULL COMMENT '数据拥有人id';
    ALTER TABLE `pt_train`
        ADD COLUMN `origin_user_id` bigint NULL COMMENT '数据拥有人id';
    ALTER TABLE `pt_train_algorithm`
        ADD COLUMN `origin_user_id` bigint NULL COMMENT '数据拥有人id';
    ALTER TABLE `pt_train_job`
        ADD COLUMN `origin_user_id` bigint NULL COMMENT '数据拥有人id';
    ALTER TABLE `pt_train_param`
        ADD COLUMN `origin_user_id` bigint NULL COMMENT '数据拥有人id';

    -- 算法用途数据权限修改
    ALTER TABLE `pt_auxiliary_info` CHANGE COLUMN `user_id` `origin_user_id`  BIGINT(20) NOT NULL COMMENT '数据拥有人id';


    -- 模型开发数据数据权限修改
    ALTER TABLE `pt_model_info`
        ADD COLUMN `origin_user_id` bigint NULL COMMENT '数据拥有人id';
    ALTER TABLE `pt_model_branch`
        ADD COLUMN `origin_user_id` bigint NULL COMMENT '数据拥有人id';

    -- 分布式训练 --
    ALTER TABLE `pt_train_param`
        ADD COLUMN `train_type` TINYINT(1) ZEROFILL NULL DEFAULT '0' COMMENT '训练类型 0：普通训练，1：分布式训练';
    -- -----------------------------------------------------------------------------------------------------------------
    -- DML


    -- 修复历史数据 data_dataset 表 赋值 origin_user_id  字段 2020.09.01 沈阳--
    update data_dataset  set origin_user_id  = create_user_id  where  id >0;

    -- 修复历史数据 data_dataset_version  表 赋值 origin_user_id
    update data_dataset_version ddv inner join (select id,create_user_id from data_dataset ) dd on ddv.dataset_id = dd.id set ddv.origin_user_id = dd.create_user_id where ddv.id > 0;

    -- 修复历史数据  data_file 表 赋值 origin_user_id
    update data_file ddv inner join
        (select id,create_user_id from data_dataset ) dd on
                ddv.dataset_id = dd.id set ddv.origin_user_id = dd.create_user_id where ddv.id > 0;

    -- 修复历史数据  data_dataset_version_file 字段 annotation_status
    update data_dataset_version_file set annotation_status =
    (case when annotation_status = 0 || annotation_status = 101 then 101
          when annotation_status = 1 || annotation_status = 102 then 102
          when annotation_status = 2 || annotation_status = 103 then 103
          when annotation_status = 3 || annotation_status = 104 then 104
          when annotation_status = 4 || annotation_status = 201 then 201
    else annotation_status end);


    -- 修复历史数据  data_dataset 字段 status
    update data_dataset set status =
    (case when status = 0 || status = 101 then 101
          when status = 1 || status = 102 then 102
          when status = 2 || status = 103 then 103
          when status = 3 || status = 104 then 104
          when status = 4 || status = 105 then 105
          when status = 5 || status = 301 then 301
          when status = 6 || status = 202 then 202
          when status = 7 || status = 302 then 302
          when status = 8 || status = 401 then 401
        else status end);

    -- 修复历史数据  data_file 字段 status
    update data_file
    set status =
            (case
                 when status = 0 || status = 101 then 101
                 when status = 1 || status = 102 then 102
                 when status = 2 || status = 103 then 103
                 when status = 3 || status = 104 then 104
                 when status = 4 || status = 201 then 201
                 else status end);

    -- 修复历史数据 data_label 1-80 标签为已删除
    update data_label set deleted = 1 where id <81;

    -- 修复历史数据 修改表 data_dataset label_group_id字段 coco标签组
    update data_dataset set label_group_id = 1 where id in (
        select dataset_id from data_dataset_label where label_id  in (
            select id from data_label where id <= 160 and id>=81
        )
        group by dataset_id
    );


    -- 修复历史数据 修改表 data_dataset label_group_id字段 imagenet标签组
    update data_dataset set label_group_id = 1 where id in (
        select dataset_id from data_dataset_label where label_id  in (
            select id from data_label where id <= 1160 and id>=161
        )
        group by dataset_id
    );




     -- 新增 表data_sequence 默认配置
    INSERT INTO data_sequence (id, business_code, start, step) VALUES (1, 'DATA_FILE', 1, 5000);
    INSERT INTO data_sequence (id, business_code, start, step) VALUES (2, 'DATA_VERSION_FILE', 1, 5000);

    -- 新增 表menu 标签组默认菜单
    INSERT INTO menu (id, cache, component, component_name, hidden,  icon, name, path, permission, pid, sort, type, layout, create_time, update_time, create_user_id, update_user_id, deleted) VALUES (100, false, 'labelGroup/labelGroupForm', 'LabelGroupEdit', true, null, '编辑标签组', 'labelgroup/edit', null, 10, 24, 1, 'SubpageLayout', current_timestamp, current_timestamp, 1, 1, false);
    INSERT INTO menu (id, cache, component, component_name, hidden,  icon, name, path, permission, pid, sort, type, layout, create_time, update_time, create_user_id, update_user_id, deleted) VALUES (101,false, 'labelGroup/labelGroupForm', 'LabelGroupDetail', true, null, '标签组详情', 'labelgroup/detail', null, 10, 23, 1, 'SubpageLayout', current_timestamp, current_timestamp, 1, 1, false);
    INSERT INTO menu (id, cache, component, component_name, hidden,  icon, name, path, permission, pid, sort, type, layout, create_time, update_time, create_user_id, update_user_id, deleted) VALUES (102,false, 'labelGroup/labelGroupForm', 'LabelGroupCreate', true, null, '创建标签组', 'labelgroup/create', null, 10, 22, 1, 'SubpageLayout', current_timestamp, current_timestamp, 1, 1, false);
    INSERT INTO menu (id, cache, component, component_name, hidden,  icon, name, path, permission, pid, sort, type, layout, create_time, update_time, create_user_id, update_user_id, deleted) VALUES (103,false, 'labelGroup/index',           'LabelGroup', false,  'mobanguanli', '标签组管理', 'labelgroup', '', 10, 21, 1, 'BaseLayout', current_timestamp, current_timestamp, 1, 1, false);

    -- 新增 表roles_menus 管理员角色与标签组菜单权限关系
    INSERT INTO roles_menus (role_id, menu_id) VALUES (1, 100);
    INSERT INTO roles_menus (role_id, menu_id) VALUES (1, 101);
    INSERT INTO roles_menus (role_id, menu_id) VALUES (1, 102);
    INSERT INTO roles_menus (role_id, menu_id) VALUES (1, 103);

    -- 新增 表roles_menus 注册用户角色与标签组菜单权限关系
    INSERT INTO roles_menus (role_id, menu_id) VALUES (2, 100);
    INSERT INTO roles_menus (role_id, menu_id) VALUES (2, 101);
    INSERT INTO roles_menus (role_id, menu_id) VALUES (2, 102);
    INSERT INTO roles_menus (role_id, menu_id) VALUES (2, 103);



    -- 新增默认数据 表data_label_group 新增默认数据预置标签组
    INSERT INTO data_label_group (id, name, create_user_id, create_time, update_user_id, update_time, deleted, remark, type, origin_user_id) VALUES (1, 'COCO', 0, current_timestamp, null, current_timestamp, false, 'test', 1, 0);
    INSERT INTO data_label_group (id, name, create_user_id, create_time, update_user_id, update_time, deleted, remark, type, origin_user_id) VALUES (2, 'ImageNet', 0, current_timestamp, null, current_timestamp, false, 'test', 1, 0);


    -- 新增默认数据 表data_group_label 新增预置标签组和标签之间的关系
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (81, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (82, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (83, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (84, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (85, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (86, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (87, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (88, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (89, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (90, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (91, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (92, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (93, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (94, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (95, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (96, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (97, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (98, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (99, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (100, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (101, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (102, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (103, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (104, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (105, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (106, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (107, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (108, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (109, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (110, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (111, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (112, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (113, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (114, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (115, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (116, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (117, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (118, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (119, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (120, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (121, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (122, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (123, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (124, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (125, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (126, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (127, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (128, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (129, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (130, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (131, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (132, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (133, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (134, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (135, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (136, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (137, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (138, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (139, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (140, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (141, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (142, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (143, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (144, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (145, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (146, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (147, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (148, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (149, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (150, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (151, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (152, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (153, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (154, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (155, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (156, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (157, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (158, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (159, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (160, 1, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (161, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (162, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (163, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (164, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (165, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (166, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (167, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (168, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (169, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (170, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (171, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (172, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (173, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (174, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (175, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (176, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (177, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (178, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (179, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (180, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (181, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (182, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (183, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (184, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (185, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (186, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (187, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (188, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (189, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (190, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (191, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (192, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (193, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (194, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (195, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (196, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (197, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (198, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (199, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (200, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (201, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (202, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (203, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (204, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (205, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (206, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (207, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (208, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (209, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (210, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (211, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (212, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (213, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (214, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (215, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (216, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (217, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (218, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (219, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (220, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (221, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (222, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (223, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (224, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (225, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (226, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (227, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (228, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (229, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (230, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (231, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (232, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (233, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (234, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (235, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (236, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (237, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (238, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (239, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (240, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (241, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (242, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (243, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (244, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (245, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (246, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (247, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (248, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (249, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (250, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (251, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (252, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (253, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (254, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (255, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (256, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (257, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (258, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (259, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (260, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (261, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (262, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (263, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (264, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (265, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (266, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (267, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (268, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (269, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (270, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (271, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (272, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (273, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (274, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (275, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (276, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (277, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (278, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (279, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (280, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (281, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (282, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (283, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (284, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (285, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (286, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (287, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (288, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (289, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (290, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (291, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (292, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (293, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (294, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (295, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (296, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (297, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (298, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (299, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (300, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (301, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (302, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (303, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (304, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (305, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (306, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (307, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (308, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (309, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (310, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (311, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (312, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (313, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (314, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (315, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (316, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (317, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (318, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (319, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (320, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (321, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (322, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (323, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (324, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (325, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (326, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (327, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (328, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (329, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (330, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (331, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (332, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (333, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (334, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (335, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (336, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (337, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (338, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (339, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (340, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (341, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (342, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (343, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (344, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (345, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (346, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (347, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (348, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (349, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (350, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (351, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (352, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (353, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (354, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (355, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (356, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (357, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (358, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (359, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (360, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (361, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (362, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (363, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (364, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (365, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (366, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (367, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (368, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (369, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (370, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (371, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (372, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (373, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (374, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (375, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (376, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (377, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (378, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (379, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (380, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (381, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (382, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (383, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (384, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (385, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (386, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (387, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (388, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (389, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (390, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (391, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (392, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (393, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (394, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (395, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (396, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (397, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (398, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (399, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (400, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (401, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (402, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (403, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (404, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (405, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (406, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (407, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (408, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (409, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (410, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (411, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (412, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (413, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (414, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (415, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (416, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (417, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (418, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (419, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (420, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (421, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (422, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (423, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (424, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (425, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (426, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (427, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (428, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (429, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (430, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (431, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (432, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (433, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (434, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (435, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (436, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (437, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (438, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (439, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (440, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (441, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (442, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (443, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (444, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (445, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (446, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (447, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (448, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (449, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (450, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (451, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (452, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (453, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (454, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (455, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (456, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (457, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (458, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (459, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (460, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (461, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (462, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (463, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (464, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (465, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (466, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (467, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (468, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (469, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (470, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (471, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (472, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (473, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (474, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (475, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (476, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (477, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (478, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (479, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (480, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (481, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (482, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (483, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (484, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (485, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (486, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (487, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (488, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (489, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (490, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (491, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (492, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (493, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (494, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (495, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (496, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (497, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (498, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (499, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (500, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (501, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (502, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (503, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (504, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (505, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (506, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (507, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (508, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (509, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (510, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (511, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (512, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (513, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (514, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (515, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (516, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (517, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (518, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (519, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (520, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (521, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (522, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (523, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (524, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (525, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (526, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (527, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (528, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (529, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (530, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (531, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (532, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (533, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (534, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (535, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (536, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (537, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (538, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (539, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (540, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (541, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (542, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (543, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (544, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (545, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (546, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (547, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (548, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (549, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (550, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (551, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (552, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (553, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (554, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (555, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (556, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (557, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (558, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (559, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (560, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (561, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (562, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (563, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (564, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (565, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (566, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (567, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (568, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (569, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (570, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (571, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (572, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (573, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (574, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (575, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (576, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (577, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (578, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (579, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (580, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (581, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (582, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (583, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (584, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (585, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (586, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (587, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (588, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (589, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (590, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (591, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (592, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (593, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (594, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (595, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (596, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (597, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (598, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (599, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (600, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (601, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (602, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (603, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (604, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (605, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (606, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (607, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (608, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (609, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (610, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (611, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (612, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (613, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (614, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (615, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (616, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (617, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (618, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (619, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (620, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (621, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (622, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (623, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (624, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (625, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (626, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (627, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (628, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (629, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (630, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (631, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (632, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (633, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (634, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (635, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (636, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (637, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (638, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (639, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (640, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (641, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (642, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (643, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (644, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (645, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (646, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (647, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (648, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (649, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (650, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (651, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (652, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (653, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (654, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (655, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (656, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (657, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (658, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (659, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (660, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (661, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (662, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (663, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (664, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (665, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (666, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (667, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (668, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (669, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (670, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (671, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (672, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (673, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (674, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (675, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (676, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (677, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (678, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (679, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (680, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (681, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (682, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (683, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (684, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (685, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (686, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (687, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (688, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (689, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (690, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (691, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (692, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (693, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (694, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (695, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (696, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (697, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (698, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (699, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (700, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (701, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (702, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (703, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (704, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (705, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (706, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (707, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (708, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (709, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (710, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (711, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (712, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (713, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (714, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (715, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (716, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (717, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (718, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (719, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (720, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (721, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (722, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (723, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (724, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (725, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (726, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (727, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (728, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (729, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (730, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (731, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (732, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (733, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (734, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (735, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (736, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (737, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (738, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (739, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (740, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (741, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (742, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (743, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (744, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (745, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (746, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (747, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (748, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (749, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (750, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (751, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (752, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (753, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (754, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (755, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (756, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (757, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (758, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (759, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (760, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (761, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (762, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (763, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (764, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (765, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (766, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (767, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (768, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (769, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (770, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (771, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (772, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (773, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (774, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (775, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (776, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (777, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (778, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (779, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (780, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (781, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (782, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (783, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (784, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (785, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (786, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (787, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (788, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (789, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (790, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (791, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (792, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (793, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (794, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (795, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (796, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (797, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (798, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (799, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (800, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (801, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (802, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (803, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (804, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (805, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (806, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (807, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (808, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (809, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (810, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (811, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (812, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (813, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (814, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (815, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (816, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (817, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (818, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (819, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (820, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (821, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (822, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (823, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (824, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (825, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (826, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (827, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (828, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (829, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (830, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (831, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (832, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (833, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (834, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (835, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (836, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (837, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (838, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (839, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (840, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (841, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (842, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (843, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (844, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (845, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (846, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (847, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (848, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (849, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (850, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (851, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (852, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (853, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (854, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (855, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (856, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (857, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (858, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (859, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (860, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (861, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (862, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (863, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (864, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (865, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (866, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (867, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (868, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (869, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (870, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (871, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (872, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (873, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (874, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (875, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (876, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (877, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (878, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (879, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (880, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (881, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (882, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (883, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (884, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (885, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (886, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (887, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (888, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (889, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (890, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (891, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (892, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (893, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (894, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (895, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (896, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (897, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (898, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (899, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (900, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (901, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (902, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (903, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (904, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (905, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (906, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (907, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (908, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (909, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (910, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (911, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (912, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (913, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (914, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (915, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (916, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (917, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (918, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (919, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (920, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (921, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (922, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (923, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (924, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (925, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (926, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (927, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (928, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (929, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (930, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (931, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (932, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (933, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (934, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (935, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (936, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (937, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (938, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (939, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (940, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (941, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (942, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (943, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (944, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (945, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (946, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (947, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (948, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (949, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (950, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (951, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (952, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (953, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (954, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (955, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (956, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (957, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (958, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (959, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (960, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (961, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (962, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (963, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (964, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (965, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (966, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (967, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (968, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (969, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (970, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (971, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (972, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (973, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (974, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (975, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (976, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (977, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (978, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (979, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (980, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (981, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (982, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (983, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (984, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (985, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (986, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (987, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (988, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (989, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (990, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (991, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (992, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (993, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (994, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (995, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (996, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (997, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (998, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (999, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1000, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1001, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1002, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1003, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1004, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1005, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1006, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1007, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1008, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1009, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1010, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1011, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1012, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1013, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1014, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1015, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1016, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1017, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1018, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1019, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1020, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1021, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1022, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1023, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1024, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1025, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1026, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1027, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1028, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1029, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1030, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1031, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1032, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1033, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1034, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1035, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1036, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1037, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1038, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1039, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1040, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1041, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1042, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1043, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1044, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1045, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1046, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1047, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1048, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1049, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1050, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1051, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1052, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1053, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1054, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1055, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1056, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1057, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1058, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1059, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1060, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1061, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1062, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1063, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1064, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1065, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1066, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1067, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1068, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1069, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1070, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1071, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1072, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1073, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1074, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1075, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1076, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1077, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1078, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1079, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1080, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1081, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1082, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1083, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1084, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1085, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1086, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1087, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1088, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1089, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1090, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1091, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1092, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1093, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1094, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1095, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1096, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1097, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1098, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1099, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1100, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1101, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1102, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1103, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1104, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1105, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1106, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1107, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1108, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1109, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1110, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1111, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1112, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1113, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1114, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1115, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1116, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1117, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1118, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1119, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1120, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1121, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1122, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1123, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1124, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1125, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1126, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1127, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1128, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1129, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1130, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1131, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1132, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1133, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1134, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1135, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1136, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1137, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1138, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1139, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1140, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1141, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1142, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1143, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1144, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1145, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1146, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1147, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1148, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1149, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1150, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1151, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1152, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1153, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1154, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1155, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1156, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1157, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1158, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1159, 2, 1, current_timestamp, 1, current_timestamp, false);
    INSERT INTO data_group_label (label_id, label_group_id, create_user_id, create_time, update_user_id, update_time, deleted) VALUES (1160, 2, 1, current_timestamp, 1, current_timestamp, false);






    -- 处理脏规格数据
    update pt_train_job
    set train_job_specs_name=case train_job_specs_name
                                 when '1' then '1Core2GB'
                                 when '2' then '1Core4GB 1TITAN V'
                                 when '3' then '2Core4GB'
                                 when '4' then '2Core4GB 1TITAN V'
                                 when '5' then '4Core8GB 1TITAN V'
                                 when '6' then '8Core16GB 4TITAN V'
                                 when '7' then '8Core16GB 1TITAN V'
                                 when '8' then '8Core32GB 4TITAN V'
        end
    where train_job_specs_name is not null;


    -- 同步修改算法表存量数据算法状态(默认为创建成功)
    update pt_train_algorithm
    set algorithm_status=1;

    -- 处理脏数据
    update pt_train_param
    set train_job_specs_name=case train_job_specs_name
                                 when '1' then '1Core2GB'
                                 when '2' then '1Core4GB 1TITAN V'
                                 when '3' then '2Core4GB'
                                 when '4' then '2Core4GB 1TITAN V'
                                 when '5' then '4Core8GB 1TITAN V'
                                 when '6' then '8Core16GB 4TITAN V'
                                 when '7' then '8Core16GB 1TITAN V'
                                 when '8' then '8Core32GB 4TITAN V'
        end
    where train_job_specs_name is not null;


    -- pt_train_job表增加训练任务"失败信息"字段 train_msg
    alter table pt_train_job
        add train_msg varchar(128) null comment '训练信息(失败信息)';


    -- 训练开发数据数据权限修改
    UPDATE `pt_image` SET origin_user_id = create_user_id ;
    UPDATE `pt_image` SET origin_user_id=0 WHERE image_resource=1;
    UPDATE `pt_train` SET origin_user_id = create_user_id ;
    UPDATE `pt_train_algorithm` SET origin_user_id = create_user_id ;
    UPDATE `pt_train_algorithm` SET origin_user_id=0 WHERE algorithm_source=2;
    UPDATE `pt_train_job` SET origin_user_id = create_user_id ;
    UPDATE `pt_train_param` SET origin_user_id = create_user_id ;

    -- 下面4条数据顺序不能改变
    UPDATE `pt_auxiliary_info` SET origin_user_id = 0 where is_default= 1;
    ALTER TABLE `pt_auxiliary_info` DROP is_default;
    DROP index `inx_user_id_type` ON `pt_auxiliary_info` ;
    CREATE index `inx_user_id_type` ON `pt_auxiliary_info` (`origin_user_id`, `type`) USING BTREE;

    -- 模型开发数据数据权限修改
    UPDATE `pt_model_info` SET origin_user_id = create_user_id ;
    UPDATE `pt_model_info` SET origin_user_id=0   WHERE model_resource=1;
    UPDATE `pt_model_branch` SET origin_user_id = create_user_id ;

    -- GPU规格
    INSERT INTO `dict`(`id`, `name`, `remark`)
    VALUES (36, 'gpu_specs', 'gpu规格');
    INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`)
    VALUES (36, '1Core4GB 1TITAN V', '{"cpuNum": 1000, "gpuNum": 1, "memNum": 4000, "workspaceRequest": "100Mi"}', 1);
    INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`)
    VALUES (36, '2Core4GB 1TITAN V', '{"cpuNum": 2000, "gpuNum": 1, "memNum": 4000, "workspaceRequest": "500Mi"}', 2);
    INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`)
    VALUES (36, '4Core8GB 1TITAN V', '{"cpuNum": 4000, "gpuNum": 1, "memNum": 8000, "workspaceRequest": "500Mi"}', 3);
    INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`)
    VALUES (36, '8Core16GB 4TITAN V', '{"cpuNum": 8000, "gpuNum": 4, "memNum": 16000, "workspaceRequest": "500Mi"}', 6);
    INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`)
    VALUES (36, '8Core16GB 1TITAN V', '{"cpuNum": 8000, "gpuNum": 1, "memNum": 16000, "workspaceRequest": "500Mi"}', 5);
    INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`)
    VALUES (36, '8Core32GB 4TITAN V', '{"cpuNum": 8000, "gpuNum": 4, "memNum": 32000, "workspaceRequest": "500Mi"}', 7);
    INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`)
    VALUES (36, '4Core8GB 2TITAN V', '{"cpuNum": 4000, "gpuNum": 2, "memNum": 8000, "workspaceRequest": "500Mi"}', 4);

    -- CPU规格
    INSERT INTO `dict`(`id`, `name`, `remark`)
    VALUES (35, 'cpu_specs', 'cpu规格');
    INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`)
    VALUES (35, '1Core2GB', '{"cpuNum": 1000, "gpuNum": 0, "memNum": 2000, "workspaceRequest": "100Mi"}', 1);
    INSERT INTO `dict_detail`(`dict_id`, `label`, `value`, `sort`)
    VALUES (35, '2Core4GB', '{"cpuNum": 4000, "{"cpuNum": 2000, "gpuNum": 0, "memNum": 4000, "workspaceRequest": "100Mi"}',
            2);

    -- 系统版本变更为第二版本
    INSERT INTO `system_version` (`id`, `version`) VALUES (2, 2);
    END IF;
commit; -- 提交
END;
CALL secondEditionProc(); //
DROP PROCEDURE IF EXISTS secondEditionProc; //
delimiter ;