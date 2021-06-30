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
package org.dubhe.measure.async;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import org.dubhe.biz.base.enums.MeasureStateEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.cloud.remotecall.config.RestTemplateHolder;
import org.dubhe.measure.constant.MeasureConstants;
import org.dubhe.measure.dao.PtMeasureMapper;
import org.dubhe.measure.domain.entity.PtMeasure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @description 异步生成度量文件
 * @date 2021-01-11
 */
@Component
public class GenerateMeasureFileAsync {

    @Autowired
    private RestTemplateHolder restTemplateHolder;

    @Autowired
    private PtMeasureMapper ptMeasureMapper;

    @Value("${model.measuring.url.json}")
    private String modelMeasuringUrlJson;

    /**
     * 异步生成度量json文件
     *
     * @param ptMeasure 度量对象
     * @param measurePath 度量文件存储目录
     */
    @Async(MeasureConstants.MEASURE_EXECUTOR)
    public void generateMeasureFile(PtMeasure ptMeasure, String measurePath) {

        //组装请求生成度量json文件的python服务参数
        JSONObject params = new JSONObject();
        //打包模型的路径集合
        params.put(MeasureConstants.ZOO_SET, StrUtil.split(ptMeasure.getModelUrls(), StrUtil.COMMA));
        //探针数据
        params.put(MeasureConstants.PROBE_SET_ROOT, ptMeasure.getDatasetUrl() + StrUtil.SLASH + "origin");
        params.put(MeasureConstants.EXPORT_PATH, measurePath);
        RestTemplate restTemplate = restTemplateHolder.getRestTemplate();
        try {
            DataResponseBody<List<String>> result = restTemplate.postForObject(modelMeasuringUrlJson, params, DataResponseBody.class);
            if (result != null && !result.succeed()) {
                LogUtil.error(LogEnum.MEASURE, "async generate measure file fail,exception msg {}", result.getMsg());
                updMeasureState(ptMeasure.getId(), StrUtil.EMPTY, false);
                throw new BusinessException("远程调用度量文件服务异常");
            }
            String fileUrl = result.getData().get(0);

            //更新异步生成度量文件结果
            updMeasureState(ptMeasure.getId(), fileUrl, true);
        } catch (Exception e) {
            updMeasureState(ptMeasure.getId(), StrUtil.EMPTY, false);
            LogUtil.error(LogEnum.MEASURE, "async generate measure file fail, exception {}", ptMeasure.getId(), e);
        }
    }


    /**
     * 更新异步生成度量文件结果
     *
     * @param id 度量id
     * @param fileUrl 度量文件路径
     * @param flag 是否生成度量文件
     */
    private void updMeasureState(Long id, String fileUrl, boolean flag) {
        PtMeasure ptMeasure = new PtMeasure();
        ptMeasure.setId(id)
                .setUrl(fileUrl);
        if (flag) {
            ptMeasure.setMeasureStatus(MeasureStateEnum.SUCCESS.getCode());
        } else {
            ptMeasure.setMeasureStatus(MeasureStateEnum.FAIL.getCode());
        }
        try {
            ptMeasureMapper.updateById(ptMeasure);
        } catch (Exception e) {
            LogUtil.error(LogEnum.MEASURE, "pt_measure table update operation failed with id {},exception {}", id, e);
        }
    }
}
