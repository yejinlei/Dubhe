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
package org.dubhe.k8s.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.dubhe.k8s.dao.K8sTaskMapper;
import org.dubhe.k8s.domain.bo.K8sTaskBO;
import org.dubhe.k8s.domain.entity.K8sTask;
import org.dubhe.k8s.service.K8sTaskService;
import org.dubhe.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @description k8s任务服务实现类
 * @date 2020-8-31
 */
@Service
public class K8sTaskServiceImpl implements K8sTaskService {

    @Autowired
    K8sTaskMapper k8sTaskMapper;
    /**
     * 创建或者更新任务
     *
     * @param k8sTask 对象
     * @return int 插入数量
     */
    @Override
    public int createOrUpdateTask(K8sTask k8sTask) {
        if (k8sTask == null){
            return 0;
        }
        List<K8sTask> oldK8sTaskList = selectByNamespaceAndResourceName(k8sTask);
        if (CollectionUtils.isEmpty(oldK8sTaskList)){
            return k8sTaskMapper.insert(k8sTask);
        }else {
            k8sTask.setId(oldK8sTaskList.get(0).getId());
            k8sTask.setDeleted(false);
            return update(k8sTask);
        }
    }
    /**
     * 修改任务
     * @param k8sTask k8s任务
     * @return int 更新数量
     */
    @Override
    public int update(K8sTask k8sTask) {
        if (k8sTask == null){
            return 0;
        }
        if (k8sTask.getId() != null){
            return k8sTaskMapper.updateById(k8sTask);
        }
        List<K8sTask> oldK8sTaskList = selectByNamespaceAndResourceName(k8sTask);
        if (!CollectionUtils.isEmpty(oldK8sTaskList)){
            k8sTask.setId(oldK8sTaskList.get(0).getId());
            k8sTask.setDeleted(false);
            return k8sTaskMapper.updateById(k8sTask);
        }
        return 0;
    }

    /**
     * 根据namesapce 和 resourceName 查询
     * @param k8sTask
     * @return
     */
    @Override
    public List<K8sTask> selectByNamespaceAndResourceName(K8sTask k8sTask){
        if (k8sTask == null || StringUtils.isEmpty(k8sTask.getNamespace()) || StringUtils.isEmpty(k8sTask.getResourceName())){
            return null;
        }
        K8sTask select = new K8sTask();
        select.setNamespace(k8sTask.getNamespace());
        select.setResourceName(k8sTask.getResourceName());
        QueryWrapper<K8sTask> queryK8sTaskJonWrapper = new QueryWrapper<>(select);
        return k8sTaskMapper.selectList(queryK8sTaskJonWrapper);
    }

    /**
     * 查询
     * @param k8sTaskBO k8s任务参数
     * @return List<k8sTask> k8s任务类集合
     */
    @Override
    public List<K8sTask> seleteUnexecutedTask(K8sTaskBO k8sTaskBO) {
        if (k8sTaskBO == null){
            return new ArrayList<>();
        }
        return k8sTaskMapper.seleteUnexecutedTask(k8sTaskBO);
    }
}
