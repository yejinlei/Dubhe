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
import org.dubhe.data.constant.DatasetLabelEnum;
import org.dubhe.data.domain.bo.TaskSplitBO;
import org.dubhe.enums.LogEnum;
import org.dubhe.utils.LogUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @description 调用自动标注服务
 * @date 2020-04-10
 */
@Service
public class AnnotationHttpService {

    @Value("${data.annotation.endpoint}")
    private String baseUrl;

    @Value("${data.annotation.endpoint}auto_annotate")
    private String annotateUrl;

    @Value("${data.imageNet.endpoint}auto_annotate")
    private String imageNetUrl;

    /**
     * http调用标注算法
     *
     * @param request 任务详情
     * @return String 算法服务响应
     */
    public String annotate(TaskSplitBO request) {
        try {
            String param = JSON.toJSONString(request);
            String requestUrl = annotateUrl;
            if (DatasetLabelEnum.IMAGE_NET.getType().equals(request.getLabelType())) {
                requestUrl = imageNetUrl;
            }
            LogUtil.info(LogEnum.BIZ_DATASET, "request annotate:{}, param:{}", requestUrl, param);
            String resp = HttpUtil.post(requestUrl, param);
            LogUtil.info(LogEnum.BIZ_DATASET, "resp:{}", resp);
            DataResponseBody respObj = JSON.parseObject(resp, DataResponseBody.class);
            return (String) respObj.getData();
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "request annotation service error. error:", e);
        }
        return null;
    }

}
