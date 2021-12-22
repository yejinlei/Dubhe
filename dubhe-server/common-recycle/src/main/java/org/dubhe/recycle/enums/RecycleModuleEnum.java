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
package org.dubhe.recycle.enums;

import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.exception.BusinessException;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.dubhe.biz.base.constant.ApplicationNameConst.*;
/**
 * @description 垃圾回收模块枚举
 * @date 2020-09-17
 */
public enum RecycleModuleEnum {


    BIZ_TRAIN(1, "训练任务管理",SERVER_TRAIN),
    BIZ_DATASET(2, "数据集管理",SERVER_DATA),
    BIZ_NOTEBOOK(3, "Notebook",SERVER_NOTEBOOK),
    BIZ_ALGORITHM(4, "算法管理",SERVER_ALGORITHM),
    BIZ_IMAGE(5, "镜像管理",SERVER_IMAGE),
    BIZ_MODEL_OPT(6, "模型优化",SERVER_OPTIMIZE),
    BIZ_MODEL(7, "模型管理",SERVER_MODEL),
    BIZ_DATAMEDICINE(8, "医学影像",SERVER_DATA_DCM),
    BIZ_MEASURE(9, "度量管理",SERVER_MEASURE),
    BIZ_SERVING(10, "云端部署", SERVER_SERVING),
    BIZ_TADL(11,"自动机器学习",SERVER_TADL);

    private Integer value;

    private String desc;

    /**
     * 模块服务名
     */
    private String server;

    RecycleModuleEnum(Integer value, String desc,String server) {
        this.value = value;
        this.desc = desc;
        this.server = server;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * 获取服务名
     * @param value 模块代号
     * @return 服务
     */
    public static String getServer(int value){
        for (RecycleModuleEnum recycleModuleEnum:RecycleModuleEnum.values()){
            if (value == recycleModuleEnum.getValue()){
                if (recycleModuleEnum.server == null){
                    throw new BusinessException(recycleModuleEnum.desc + " 服务未配置服务名！");
                }
                return recycleModuleEnum.server;
            }
        }
        throw new BusinessException(value+" 服务不存在！");
    }

    /**
     * 模块代号，模块名称 映射
     */
    public static final Map<Integer,String> RECYCLE_MODULE_MAP;
    static {
        RECYCLE_MODULE_MAP = new LinkedHashMap<>(MagicNumConstant.SIXTEEN);
        for (RecycleModuleEnum recycleModuleEnum:RecycleModuleEnum.values()){
            RECYCLE_MODULE_MAP.put(recycleModuleEnum.value,recycleModuleEnum.desc);
        }
    }

}
