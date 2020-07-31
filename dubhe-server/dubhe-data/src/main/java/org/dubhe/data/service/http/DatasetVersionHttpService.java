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

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import org.dubhe.base.DataResponseBody;
import org.dubhe.enums.LogEnum;
import org.dubhe.utils.JwtUtils;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @description 调用训练算法查询当前数据集是否在使用
 * @date 2020-06-08
 */
@Service
public class DatasetVersionHttpService {

    @Value("${data.ptversion}api/${server.rest-version}/trainJob/dataSourceStatus")
    private String ptJobUrl;

    /**
     * 调用训练算法查询当前数据集是否在使用
     *
     * @param datasetVersionUrls 数据集版本url
     * @return: boolean          是否正在训练
     */
    public boolean urlStatus(List<String> datasetVersionUrls) {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes()).getRequest();
            String authorization = request.getHeader(JwtUtils.AUTH_HEADER);
            String trainJson = HttpRequest.get(ptJobUrl)
                    .form("dataSourcePath", StringUtils.join(datasetVersionUrls, ","))
                    .charset("UTF-8").header(JwtUtils.AUTH_HEADER, authorization).execute().body();
            Map<String, Boolean> versionStatus = (Map<String, Boolean>) JSONObject.parseObject(trainJson, DataResponseBody.class).getData();
            return !versionStatus.values().contains(false);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "request connection training error", e);
        }
        return false;
    }

}
