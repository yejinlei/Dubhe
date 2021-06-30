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

package org.dubhe.data.service;

import org.dubhe.data.domain.entity.DataSequence;

/**
 * @description 获取序列服务接口
 * @date 2020-09-23
 */
public interface DataSequenceService {
    /**
     * 根据业务编码获取序列号
     * @param businessCode    业务编码
     * @return DataSequence   序列实体
     */
    DataSequence getSequence(String businessCode);
    /**
     * 根据业务编码更新起点
     * @param businessCode  业务编码
     * @return DataSequence 序列实体
     */
    int updateSequenceStart(String businessCode);

    /**
     * 检查表是否存在
     * @param tableName 表名
     * @return boolean  是否存在标识
     */
    boolean checkTableExist(String tableName);

    /**
     * 执行存储过程创建表
     * @param tableId 表名
     */
    void createTable(String tableId);

    /**
     * 扩容可用数量
     *
     * @param businessCode 业务编码
     * @return DataSequence 数据ID序列
     */
    DataSequence expansionUsedNumber(String businessCode);
}
