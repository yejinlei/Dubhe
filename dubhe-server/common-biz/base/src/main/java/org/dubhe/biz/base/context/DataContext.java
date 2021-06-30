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

package org.dubhe.biz.base.context;


import org.dubhe.biz.base.dto.CommonPermissionDataDTO;

/**
 * @description 共享上下文数据集信息
 * @date 2020-11-25
 */
public class DataContext {

    /**
     * 私有化构造参数
     */
    private DataContext() {
    }

    private static final ThreadLocal<CommonPermissionDataDTO> CONTEXT = new ThreadLocal<>();

    /**
     * 存放数据集信息
     *
     * @param datasetVO
     */
    public static void set(CommonPermissionDataDTO datasetVO) {
        CONTEXT.set(datasetVO);
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    public static CommonPermissionDataDTO get() {
        return CONTEXT.get();
    }

    /**
     * 清除当前线程内引用，防止内存泄漏
     */
    public static void remove() {
        CONTEXT.remove();
    }

}
