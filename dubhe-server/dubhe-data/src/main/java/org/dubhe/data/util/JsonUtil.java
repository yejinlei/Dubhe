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

package org.dubhe.data.util;

import com.alibaba.fastjson.JSON;
import org.dubhe.biz.base.utils.StringUtils;

/**
 * @description 校验字符串是否是 json 格式 工具类
 * @date 2020-10-16
 */
public class JsonUtil {

    /**
     * 校验字符串是否是json格式
     *
     * @param json json字符串
     * @return 校验结果 true:是 false:否
     */
    public static boolean isJson(String json) {
        boolean result = false;
        try {
            if(!StringUtils.isEmpty(json)){
                JSON.parse(json);
                result = true;
            }
        } catch (Exception e) {
            result=false;
        }
        return result;
    }

}

