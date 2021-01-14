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

package org.dubhe.data.dao.provider;

import java.util.Collection;
import java.util.Map;

/**
 * @description File sql构建类
 * @date 2020-04-10
 */
public class FileProvider {

    /**
     * 获取数据集状态列表
     *
     * @param para       查询参数
     * @return String    sql
     */
    public String listStatistics(Map<String, Object> para) {
        Collection<Long> ids = (Collection) para.get("ids");
        StringBuffer sql = new StringBuffer(
                "select f.dataset_id, f.`status`, count(1) c from data_file f where f.dataset_id in (");

        ids.forEach(id -> sql.append(id + ","));
        sql.deleteCharAt(sql.length() - 1);
        sql.append(") group by f.`status`, f.dataset_id ");
        return sql.toString();
    }

}
