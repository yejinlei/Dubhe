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

package org.dubhe.biz.base.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @description 业务模块
 * @date 2020-05-25
 */
@Getter
public enum BizEnum {

    /**
     * 模型开发
     */
    NOTEBOOK("模型开发", "notebook", 0),
    /**
     * 算法管理
     */
    ALGORITHM("算法管理", "algorithm", 1),
    /**
     * 模型管理
     */
    MODEL("模型管理", "model", 2),
    /**
     * 模型优化
     */
    MODEL_OPT("模型优化管理","modelopt",6),
    /**
     * 云端Serving-在线服务
     */
    SERVING("云端Serving", "serving", 3),
    /**
     * 批量服务
     */
    BATCH_SERVING("批量服务", "batchserving", 4),
    /**
     * 度量管理
     */
    MEASURE("度量管理", "measure", 5),
    /**
     * 专业版终端
     */
    TERMINAL("专业版终端", "terminal", 7),
    /**
     * TADL
     */
    TADL("TADL服务", "tadl", 8);

    /**
     * 业务模块名称
     */
    private String bizName;
    /**
     * 业务模块名称
     */
    private String bizCode;
    /**
     * 业务源代号
     */
    private Integer createResource;

    BizEnum(String bizName, String bizCode, Integer createResource) {
        this.createResource = createResource;
        this.bizName = bizName;
        this.bizCode = bizCode;
    }

    private static final Map<Integer, BizEnum> RESOURCE_ENUM_MAP = new HashMap<Integer, BizEnum>() {
        {
            for (BizEnum enums : BizEnum.values()) {
                put(enums.getCreateResource(), enums);
            }
        }
    };

    /**
     * 根据createResource获取BizEnum
     * @param createResource
     * @return
     */
    public static BizEnum getByCreateResource(int createResource) {
        return RESOURCE_ENUM_MAP.get(createResource);
    }


}
