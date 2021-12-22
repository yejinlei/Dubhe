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
package org.dubhe.k8s.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.compress.utils.Lists;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.k8s.dao.K8sCallbackEventMapper;
import org.dubhe.k8s.domain.entity.K8sCallbackEvent;
import org.dubhe.k8s.domain.vo.K8sEventVO;
import org.dubhe.k8s.domain.vo.K8sResourceEventResultVO;
import org.dubhe.k8s.service.K8sCallbackEventService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description:
 * @date: 2021/11/15
 */
@Service
public class K8sCallbackEventServiceImpl implements K8sCallbackEventService {
    @Resource
    private K8sCallbackEventMapper  k8sCallbackEventMapper;

    /**
     * @see K8sCallbackEventService#insertOrUpdate(K8sCallbackEvent)
     */
    @Override
    public boolean insertOrUpdate(K8sCallbackEvent k8sCallbackEvent) {
        QueryWrapper<K8sCallbackEvent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("resource_name", k8sCallbackEvent.getResourceName());
        queryWrapper.eq("container_id", k8sCallbackEvent.getContainerId());
        queryWrapper.eq("deleted", MagicNumConstant.ZERO);
        queryWrapper.eq("event_type", k8sCallbackEvent.getEventType());
        queryWrapper.eq("business_type", k8sCallbackEvent.getBusinessType());
        if (k8sCallbackEventMapper.update(k8sCallbackEvent, queryWrapper) > 0) {
            return true;
        } else {
            return k8sCallbackEventMapper.insert(k8sCallbackEvent) > 0;
        }
    }

    /**
     * @see K8sCallbackEventService#batchQueryByResourceName(List)
     */
    @Override
    public List<K8sResourceEventResultVO> batchQueryByResourceName(List<String> resourceNames) {
        QueryWrapper<K8sCallbackEvent> queryWrapper = new QueryWrapper<K8sCallbackEvent>();
        queryWrapper.in("resource_name", resourceNames);
        queryWrapper.eq("deleted", MagicNumConstant.ZERO);
        List<K8sCallbackEvent> callbackEvents = k8sCallbackEventMapper.selectList(queryWrapper);
        if (CollectionUtil.isEmpty(callbackEvents)) {
            return Lists.newArrayList();
        }

        Map<String, List<K8sCallbackEvent>> k8sCallbackEventMap = new HashMap<>();
        callbackEvents.forEach(e -> {
            if (k8sCallbackEventMap.containsKey(e.getResourceName())) {
                List<K8sCallbackEvent> events = k8sCallbackEventMap.get(e.getResourceName());
                events.add(e);
            } else {
                k8sCallbackEventMap.put(e.getResourceName(), new ArrayList<K8sCallbackEvent>() {{add(e);}});
            }
        });

        return k8sCallbackEventMap.entrySet().stream().map(entry -> {
            K8sResourceEventResultVO resultVO = new K8sResourceEventResultVO();
            resultVO.setResourceName(entry.getKey());
            resultVO.setEventVOList(convert(entry.getValue()));
            return resultVO;
        }).collect(Collectors.toList());
    }

    /**
     * @see K8sCallbackEventService#queryByResourceName(List)
     */
    @Override
    public List<K8sEventVO> queryByResourceName(List<String> resourceNames) {
        QueryWrapper<K8sCallbackEvent> queryWrapper = new QueryWrapper<K8sCallbackEvent>();
        queryWrapper.in("resource_name", resourceNames);
        queryWrapper.eq("deleted", MagicNumConstant.ZERO);
        List<K8sCallbackEvent> callbackEvents = k8sCallbackEventMapper.selectList(queryWrapper);
        if (CollectionUtil.isEmpty(callbackEvents)) {
            return Lists.newArrayList();
        }
        return convert(callbackEvents);
    }

    /**
     * @see K8sCallbackEventService#delete(String, String) 
     */
    @Override
    public boolean delete(String resourceName, String businessType) {
        QueryWrapper<K8sCallbackEvent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("resource_name", resourceName);
        queryWrapper.eq("business_type", businessType);
        return k8sCallbackEventMapper.delete(queryWrapper) > 0;
    }

    /**
     * 数据库Entity转换成VO对象
     *
     * @param callbackEvents
     * @return
     */
    private List<K8sEventVO> convert(List<K8sCallbackEvent> callbackEvents) {
        return callbackEvents.stream().map( e ->
                {
                    K8sEventVO k8sEventVO = new K8sEventVO();
                    k8sEventVO.setType(e.getEventType());
                    k8sEventVO.setMessage(e.getMessage());
                    k8sEventVO.setResourceName(e.getResourceName());
                    k8sEventVO.setStartTime(e.getStartTime());
                    return k8sEventVO;
                }
        ).collect(Collectors.toList());
    }
}
