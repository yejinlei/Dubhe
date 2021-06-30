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

package org.onebrain.operator.action.deployer;

import cn.hutool.core.util.RandomUtil;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @description 创建资源的信息的抽象类
 * @date 2020-04-30
 */
@Data
@Accessors(chain = true)
public abstract class AbstractResourceCreateInfo {


    /**
     * 生成随机字符串
     * @param digits 位数
     * @return
     */
    protected static String getRandomStr(Integer digits){
        return RandomUtil.randomString(digits);
    }
}
