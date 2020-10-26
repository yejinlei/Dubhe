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

package org.dubhe.data.service.http;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import org.dubhe.base.DataResponseBody;
import org.dubhe.data.domain.bo.EnhanceTaskSplitBO;
import org.dubhe.enums.LogEnum;
import org.dubhe.utils.LogUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @description 数据集增强
 * @date 2020-06-28
 */
@Service
public class EnhanceHttpService {

    @Value("${data.enhance.endpoint}img_process")
    private String endpoint;

    /**
     * 调用算法接口执行数据增强
     *
     * @param enhanceTaskSplitBO    增强任务
     * @return String               返回信息
     */
    public String enhance(EnhanceTaskSplitBO enhanceTaskSplitBO) {
        try {
            String param = JSON.toJSONString(enhanceTaskSplitBO);
            LogUtil.info(LogEnum.BIZ_DATASET, "data enhance request url {}, param {}", endpoint, param);
            String response = HttpUtil.post(endpoint, param);
            LogUtil.info(LogEnum.BIZ_DATASET, "data enhance response {}", response);
            DataResponseBody respObj = JSON.parseObject(response, DataResponseBody.class);
            return respObj.getData().toString();
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "request enhance service error {}", e);
        }
        return null;
    }

}
