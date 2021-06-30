/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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

package org.dubhe.datasetutil.domain.entity;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @description 日志对象封装类
 * @date 2020-06-29
 */
@Data
@Accessors(chain = true)
public class LogInfo implements Serializable {

    /**
     * id
     */
    @JSONField(ordinal = 1)
    private String traceId;

    /**
     * 类型
     */
    @JSONField(ordinal = 2)
    private String type;

    /**
     * 等级
     */
    @JSONField(ordinal = 3)
    private String level;

    /**
     * 位置
     */
    @JSONField(ordinal = 4)
    private String location;

    /**
     * 时间
     */
    @JSONField(ordinal = 5)
    private String time = DateUtil.now();

    /**
     * 描述
     */
    @JSONField(ordinal = 6)
    private Object info;

    public void setInfo(Object info) {
        this.info = info;
    }
}
