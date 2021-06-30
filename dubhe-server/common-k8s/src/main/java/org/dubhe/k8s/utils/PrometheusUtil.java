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
package org.dubhe.k8s.utils;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.map.HashedMap;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.bo.PrometheusMetricBO;
import org.dubhe.k8s.domain.dto.PodQueryDTO;

import java.util.Map;

/**
 * @description prometheus 工具类
 * @date 2021-02-03
 */
public class PrometheusUtil {
    /**
     * prometheus get查询
     * @param url
     * @param paramMap
     * @return
     */
    public static PrometheusMetricBO getQuery(String url,Map<String, Object> paramMap){
        if (StringUtils.isEmpty(url)){
            return null;
        }
        try {
            String metricStr = HttpUtil.get(url,paramMap);
            if (StringUtils.isEmpty(metricStr)){
                return null;
            }
            return JSON.parseObject(metricStr, PrometheusMetricBO.class);
        }catch (Exception e){
            LogUtil.error(LogEnum.BIZ_K8S, "getQuery url:{} paramMap:{} error:{}", url,paramMap,e.getMessage(),e);
            return null;
        }
    }

    /**
     * 组装参数
     * @param param 查询表达式
     * @param podName pod名称
     * @param podQueryDTO 查询参数
     * @return
     */
    public static Map<String, Object> getQueryParamMap(String param,String podName,PodQueryDTO podQueryDTO){
        Map<String, Object> paramMap = new HashedMap<>(MagicNumConstant.EIGHT);
        if (StringUtils.isEmpty(param) || StringUtils.isEmpty(podName)){
            return paramMap;
        }
        paramMap.put(StringConstant.QUERY, param.replace(K8sParamConstants.POD_NAME_PLACEHOLDER,podName));
        if (podQueryDTO == null){
            return paramMap;
        }
        if (podQueryDTO.getStartTime() != null){
            paramMap.put(StringConstant.START_LOW,podQueryDTO.getStartTime());
        }
        if (podQueryDTO.getEndTime() != null){
            paramMap.put(StringConstant.END_LOW,podQueryDTO.getEndTime());
        }
        if (podQueryDTO.getStep() != null){
            paramMap.put(StringConstant.STEP_LOW,podQueryDTO.getStep());
        }
        return paramMap;
    }

    /**
     * 组装参数
     * @param param 查询表达式
     * @param podName pod名称
     * @return
     */
    public static Map<String, Object> getQueryParamMap(String param,String podName){
        Map<String, Object> paramMap = new HashedMap<>(MagicNumConstant.TWO);
        if (StringUtils.isEmpty(param) || StringUtils.isEmpty(podName)){
            return paramMap;
        }
        paramMap.put(StringConstant.QUERY, param.replace(K8sParamConstants.POD_NAME_PLACEHOLDER,podName));
        return paramMap;
    }
}
