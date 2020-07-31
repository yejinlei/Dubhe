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
import com.alibaba.fastjson.JSONObject;
import org.dubhe.data.domain.entity.DatasetVersion;
import org.dubhe.data.domain.entity.Label;
import org.dubhe.data.service.DatasetLabelService;
import org.dubhe.enums.LogEnum;
import org.dubhe.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description 调用数据转换服务
 * @date 2020-06-05
 */
@Service
public class ConversionHttpService {

    @Value("${data.ofrecord.endpoint}gen_ofrecord")
    private String conversionUrl;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Autowired
    private DatasetLabelService datasetLabelService;

    /**
     * 向数据转换服务发送请求
     *
     * @param datasetVersion 数据集版本
     */
    public void convert(DatasetVersion datasetVersion) {
        try {
            Map<String, String> datasetLabels = new HashMap<>();
            List<Label> labels = datasetLabelService.listLabelByDatasetId(datasetVersion.getDatasetId());
            labels.forEach(label -> {
                datasetLabels.put(label.getId().toString(), label.getName());
            });
            JSONObject param = new JSONObject();
            param.put("id", datasetVersion.getId());
            String versionUrl = bucketName + File.separator + datasetVersion.getVersionUrl();
            param.put("datasetPath", versionUrl);
            param.put("datasetLabels", datasetLabels);
            LogUtil.info(LogEnum.BIZ_DATASET, "request convert:{}, param:{}", conversionUrl, param);
            HttpUtil.post(conversionUrl, param.toJSONString());
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "request convert service error. error:", e);
        }
    }

}
