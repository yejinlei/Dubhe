/**
 * Copyright 2020 Tianshu AI Platform. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =============================================================
 */

package org.dubhe.biz.base.constant;

/**
 * @description 权限标识，对应 menu 表中的 permission 字段
 * @date 2020-05-14
 */
public final class Permissions {

    /**
     * 数据管理
     */
    public static final String DATA = "hasAuthority('ROLE_data')";

    /**
     * notebook权限
     */
    public static final String NOTEBOOK = "hasAuthority('ROLE_notebook')";
    public static final String NOTEBOOK_CREATE = "hasAuthority('ROLE_notebook:create')";
    public static final String NOTEBOOK_UPDATE = "hasAuthority('ROLE_notebook:update')";
    public static final String NOTEBOOK_OPEN = "hasAuthority('ROLE_notebook:open')";
    public static final String NOTEBOOK_START = "hasAuthority('ROLE_notebook:start')";
    public static final String NOTEBOOK_STOP = "hasAuthority('ROLE_notebook:stop')";
    public static final String NOTEBOOK_DELETE = "hasAuthority('ROLE_notebook:delete')";

    /**
     * 算法管理
     */
    public static final String DEVELOPMENT_ALGORITHM = "hasAuthority('ROLE_development:algorithm')";
    public static final String DEVELOPMENT_ALGORITHM_CREATE = "hasAuthority('ROLE_development:algorithm:create')";
    public static final String DEVELOPMENT_ALGORITHM_EDIT = "hasAuthority('ROLE_development:algorithm:edit')";
    public static final String DEVELOPMENT_ALGORITHM_DELETE = "hasAuthority('ROLE_development:algorithm:delete')";

    /**
     * 训练管理
     */
    public static final String TRAINING_JOB = "hasAuthority('ROLE_training:job')";
    public static final String TRAINING_JOB_CREATE = "hasAuthority('ROLE_training:job:create')";
    public static final String TRAINING_JOB_UPDATE = "hasAuthority('ROLE_training:job:update')";
    public static final String TRAINING_JOB_DELETE = "hasAuthority('ROLE_training:job:delete')";


    public static final String MODEL_MODEL = "hasAuthority('ROLE_model:model')";
    public static final String MODEL_MODEL_CREATE = "hasAuthority('ROLE_model:model:create')";
    public static final String MODEL_MODEL_EDIT = "hasAuthority('ROLE_model:model:edit')";
    public static final String MODEL_MODEL_DELETE = "hasAuthority('ROLE_model:model:delete')";

    /**
     * 模型版本管理
     */
    public static final String MODEL_BRANCH = "hasAuthority('ROLE_model:branch')";
    public static final String MODEL_BRANCH_CREATE = "hasAuthority('ROLE_model:branch:create')";
    public static final String MODEL_BRANCH_DELETE = "hasAuthority('ROLE_model:branch:delete')";
    public static final String MODEL_BRANCH_CONVERT_PRESET = "hasAuthority('ROLE_model:branch:convertPreset')";
    public static final String MODEL_BRANCH_CONVERT_ONNX = "hasAuthority('ROLE_model:branch:convertOnnx')";

    /**
     * 模型优化
     */
    public static final String MODEL_OPTIMIZE = "hasAuthority('ROLE_model:optimize')";
    public static final String MODEL_OPTIMIZE_CREATE = "hasAuthority('ROLE_model:optimize:createTask')";
    public static final String MODEL_OPTIMIZE_SUBMIT_TASK = "hasAuthority('ROLE_model:optimize:submitTask')";
    public static final String MODEL_OPTIMIZE_SUBMIT_TASK_INSTANCE = "hasAuthority('ROLE_model:optimize:submitTaskInstance')";
    public static final String MODEL_OPTIMIZE_CANCEL_TASK_INSTANCE = "hasAuthority('ROLE_model:optimize:cancelTaskInstance')";
    public static final String MODEL_OPTIMIZE_EDIT = "hasAuthority('ROLE_model:optimize:editTask')";
    public static final String MODEL_OPTIMIZE_DELETE_TASK = "hasAuthority('ROLE_model:optimize:deleteTask')";
    public static final String MODEL_OPTIMIZE_DELETE_TASK_INSTANCE = "hasAuthority('ROLE_model:optimize:deleteTaskInstance')";

    /**
     * 控制台
     */
    public static final String SYSTEM_NODE = "hasAuthority('ROLE_system:node')";
    public static final String SYSTEM_LOG = "hasAuthority('ROLE_system:log')";
    public static final String SYSTEM_TEAM = "hasAuthority('ROLE_system:team')";

    /**
     * 云端Serving
     */
    public static final String SERVING = "hasAuthority('ROLE_serving')";
    public static final String SERVING_BATCH = "hasAuthority('ROLE_serving:batch')";
    public static final String SERVING_BATCH_CREATE = "hasAuthority('ROLE_serving:batch:create')";
    public static final String SERVING_BATCH_EDIT = "hasAuthority('ROLE_serving:batch:edit')";
    public static final String SERVING_BATCH_START = "hasAuthority('ROLE_serving:batch:start')";
    public static final String SERVING_BATCH_STOP = "hasAuthority('ROLE_serving:batch:stop')";
    public static final String SERVING_BATCH_DELETE = "hasAuthority('ROLE_serving:batch:delete')";
    public static final String SERVING_DEPLOYMENT = "hasAuthority('ROLE_serving:online')";
    public static final String SERVING_DEPLOYMENT_CREATE = "hasAuthority('ROLE_serving:online:create')";
    public static final String SERVING_DEPLOYMENT_EDIT = "hasAuthority('ROLE_serving:online:edit')";
    public static final String SERVING_DEPLOYMENT_DELETE = "hasAuthority('ROLE_serving:online:delete')";
    public static final String SERVING_DEPLOYMENT_START = "hasAuthority('ROLE_serving:online:start')";
    public static final String SERVING_DEPLOYMENT_STOP = "hasAuthority('ROLE_serving:online:stop')";

    /**
     * 镜像管理
     */
    public static final String IMAGE = "hasAuthority('ROLE_training:image')";
    public static final String IMAGE_UPLOAD = "hasAuthority('ROLE_training:image:upload')";
    public static final String IMAGE_EDIT = "hasAuthority('ROLE_training:image:edit')";
    public static final String IMAGE_DELETE = "hasAuthority('ROLE_training:image:delete')";

    /**
     * 度量管理
     */
    public static final String MEASURE = "hasAuthority('ROLE_atlas:measure')";
    public static final String MEASURE_CREATE = "hasAuthority('ROLE_atlas:measure:create')";
    public static final String MEASURE_EDIT = "hasAuthority('ROLE_atlas:measure:edit')";
    public static final String MEASURE_DELETE = "hasAuthority('ROLE_atlas:measure:delete')";

    /**
     * 控制台：用户组管理
     */
    public static final String USER_GROUP_CREATE = "hasAuthority('ROLE_system:userGroup:create')";
    public static final String USER_GROUP_EDIT = "hasAuthority('ROLE_system:userGroup:edit')";
    public static final String USER_GROUP_DELETE = "hasAuthority('ROLE_system:userGroup:delete')";
    public static final String USER_GROUP_EDIT_USER = "hasAuthority('ROLE_system:userGroup:editUser')";
    public static final String USER_GROUP_EDIT_USER_ROLE = "hasAuthority('ROLE_system:userGroup:editUserRole')";
    public static final String USER_GROUP_EDIT_USER_STATE = "hasAuthority('ROLE_system:userGroup:editUserState')";
    public static final String USER_GROUP_DELETE_USER = "hasAuthority('ROLE_system:userGroup:deleteUser')";

    /**
     * 控制台：用户管理
     */
    public static final String USER_CREATE = "hasAuthority('ROLE_system:user:create')";
    public static final String USER_EDIT = "hasAuthority('ROLE_system:user:edit')";
    public static final String USER_DELETE = "hasAuthority('ROLE_system:user:delete')";
    public static final String USER_DOWNLOAD = "hasAuthority('ROLE_system:user:download')";
    public static final String USER_CONFIG_EDIT = "hasAuthority('ROLE_system:user:configEdit')";
    public static final String USER_RESOURCE_INFO = "hasAuthority('ROLE_system:user:resourceInfo')";

    /**
     * 控制台：角色管理
     */
    public static final String ROLE = "hasAuthority('ROLE_system:role')";
    public static final String ROLE_CREATE = "hasAuthority('ROLE_system:role:create')";
    public static final String ROLE_EDIT = "hasAuthority('ROLE_system:role:edit')";
    public static final String ROLE_DELETE = "hasAuthority('ROLE_system:role:delete')";
    public static final String ROLE_DWONLOAD = "hasAuthority('ROLE_system:role:download')";
    public static final String ROLE_MENU = "hasAuthority('ROLE_system:role:menu')";
    public static final String ROLE_AUTH = "hasAuthority('ROLE_system:role:auth')";

    /**
     * 控制台：权限组管理
     */
    public static final String AUTH_CODE = "hasAuthority('ROLE_system:authCode')";
    public static final String AUTH_CODE_CREATE = "hasAuthority('ROLE_system:authCode:create')";
    public static final String AUTH_CODE_EDIT = "hasAuthority('ROLE_system:authCode:edit')";
    public static final String AUTH_CODE_DELETE = "hasAuthority('ROLE_system:authCode:delete')";

    /**
     * 控制台：权限管理
     */
    public static final String PERMISSION = "hasAuthority('ROLE_system:permission')";
    public static final String PERMISSION_CREATE = "hasAuthority('ROLE_system:permission:create')";
    public static final String PERMISSION_EDIT = "hasAuthority('ROLE_system:permission:edit')";
    public static final String PERMISSION_DELETE = "hasAuthority('ROLE_system:permission:delete')";

    /**
     * 控制台：菜单管理
     */
    public static final String MENU = "hasAuthority('ROLE_system:menu')";
    public static final String MENU_CREATE = "hasAuthority('ROLE_system:menu:create')";
    public static final String MENU_EDIT = "hasAuthority('ROLE_system:menu:edit')";
    public static final String MENU_DELETE = "hasAuthority('ROLE_system:menu:delete')";
    public static final String MENU_DOWNLOAD = "hasAuthority('ROLE_system:menu:download')";

    /**
     * 控制台：字典管理
     */
    public static final String DICT = "hasAuthority('ROLE_system:dict')";
    public static final String DICT_CREATE = "hasAuthority('ROLE_system:dict:create')";
    public static final String DICT_EDIT = "hasAuthority('ROLE_system:dict:edit')";
    public static final String DICT_DELETE = "hasAuthority('ROLE_system:dict:delete')";
    public static final String DICT_DOWNLOAD = "hasAuthority('ROLE_system:dict:download')";

    /**
     * 控制台：字典详情管理
     */
    public static final String DICT_DETAIL_CREATE = "hasAuthority('ROLE_system:dictDetail:create')";
    public static final String DICT_DETAIL_EDIT = "hasAuthority('ROLE_system:dictDetail:edit')";
    public static final String DICT_DETAIL_DELETE = "hasAuthority('ROLE_system:dictDetail:delete')";

    /**
     * 控制台：资源规格管理
     */
    public static final String SPECS_CREATE = "hasAuthority('ROLE_system:specs:create')";
    public static final String SPECS_EDIT = "hasAuthority('ROLE_system:specs:edit')";
    public static final String SPECS_DELETE = "hasAuthority('ROLE_system:specs:delete')";

    /**
     * 专业版：终端
     */
    public static final String TERMINAL_CREATE = "hasAuthority('ROLE_terminal:specs:create')";
    public static final String TERMINAL_RESTART = "hasAuthority('ROLE_terminal:specs:restart')";
    public static final String TERMINAL_PRESAVE = "hasAuthority('ROLE_terminal:specs:save')";
    public static final String TERMINAL_DELETE = "hasAuthority('ROLE_terminal:specs:delete')";
    public static final String TERMINAL_DETAIL = "hasAuthority('ROLE_terminal:specs:detail')";
    public static final String TERMINAL_LIST = "hasAuthority('ROLE_terminal:specs:list')";

    private Permissions() {
    }
}
