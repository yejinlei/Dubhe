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

package org.dubhe.biz.log.entity;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.MagicNumConstant;

import java.io.Serializable;

/**
 * @description 日志对象封装类
 * @date 2020-06-29
 */
@Data
@Accessors(chain = true)
public class LogInfo implements Serializable {

    private static final long serialVersionUID = 5250395474667395607L;

    @JSONField(ordinal = MagicNumConstant.ONE)
    private String traceId;

    @JSONField(ordinal = MagicNumConstant.TWO)
    private String type;

    @JSONField(ordinal = MagicNumConstant.THREE)
    private String level;

    @JSONField(ordinal = MagicNumConstant.FOUR)
    private String location;

    @JSONField(ordinal = MagicNumConstant.FIVE)
    private String time = DateUtil.now();

    @JSONField(ordinal = MagicNumConstant.SIX)
    private Object info;

    public void setInfo(Object info) {
        this.info = info;
    }
}
