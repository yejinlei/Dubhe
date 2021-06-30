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

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.redis.utils.RedisUtils;
import org.dubhe.k8s.constant.RedisConstants;
import org.dubhe.k8s.dao.K8sTaskMapper;
import org.dubhe.k8s.domain.bo.K8sTaskBO;
import org.dubhe.k8s.domain.entity.K8sTask;
import org.dubhe.k8s.enums.K8sTaskStatusEnum;
import org.dubhe.k8s.service.K8sTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @description k8s任务服务实现类
 * @date 2020-08-31
 */
@Service
public class K8sTaskServiceImpl implements K8sTaskService {

    @Autowired
    K8sTaskMapper k8sTaskMapper;
    @Autowired
    private RedisUtils redisUtils;
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
            addRedisDelayTask(k8sTask);
            return k8sTaskMapper.insertOrUpdate(k8sTask);
        }else {
            k8sTask.setId(oldK8sTaskList.get(0).getId());
            k8sTask.setDeleted(false);
            addRedisDelayTask(k8sTask);
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
        addRedisDelayTask(k8sTask);
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
        LambdaQueryWrapper<K8sTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(K8sTask::getNamespace,k8sTask.getNamespace());
        queryWrapper.eq(K8sTask::getResourceName,k8sTask.getResourceName());

        return k8sTaskMapper.selectList(queryWrapper);
    }

    /**
     * 查询
     * @param k8sTaskBO k8s任务参数
     * @return List<k8sTask> k8s任务类集合
     */
    @Override
    public List<K8sTask> selectUnexecutedTask(K8sTaskBO k8sTaskBO) {
        if (k8sTaskBO == null){
            return new ArrayList<>();
        }
        return k8sTaskMapper.selectUnexecutedTask(k8sTaskBO);
    }

    @Override
    public List<K8sTask> selectUnexecutedTask() {
        K8sTaskBO k8sTaskBO = new K8sTaskBO();
        Long curUnixTime = System.currentTimeMillis()/ MagicNumConstant.ONE_THOUSAND;
        k8sTaskBO.setMaxApplyUnixTime(curUnixTime);
        k8sTaskBO.setMaxStopUnixTime(curUnixTime);
        k8sTaskBO.setApplyStatus(K8sTaskStatusEnum.UNEXECUTED.getStatus());
        k8sTaskBO.setStopStatus(K8sTaskStatusEnum.UNEXECUTED.getStatus());
        LogUtil.info(LogEnum.BIZ_K8S,"selectUnexecutedTask {}", JSON.toJSONString(k8sTaskBO));
        return selectUnexecutedTask(k8sTaskBO);
    }

    /**
     * 添加redis延时队列
     * @param k8sTask
     * @return
     */
    @Override
    public boolean addRedisDelayTask(K8sTask k8sTask) {
        if (k8sTask == null || StringUtils.isEmpty(k8sTask.getNamespace()) || StringUtils.isEmpty(k8sTask.getResourceName())){
            return false;
        }
        boolean success = true;
        if (k8sTask.getApplyUnixTime() != null && k8sTask.getApplyUnixTime() > 0){
            success &= redisUtils.zAdd(RedisConstants.DELAY_APPLY_ZSET_KEY,k8sTask.getApplyUnixTime(), String.format(RedisConstants.DELAY_ZSET_VALUE,k8sTask.getNamespace(),k8sTask.getResourceName()));
        }
        if (k8sTask.getStopUnixTime() != null && k8sTask.getStopUnixTime() > 0){
            success &= redisUtils.zAdd(RedisConstants.DELAY_STOP_ZSET_KEY,k8sTask.getStopUnixTime(),String.format(RedisConstants.DELAY_ZSET_VALUE,k8sTask.getNamespace(),k8sTask.getResourceName()));
        }
        return success;
    }

    /**
     * 加载任务到延时队列
     */
    @Override
    public void loadTaskToRedis(){
        selectUnexecutedTask().forEach(x->addRedisDelayTask(x));
    }

    /**
     * 根据namespace 和 resourceName 删除
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return boolean
     */
    @Override
    public boolean deleteByNamespaceAndResourceName(String namespace, String resourceName) {
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(resourceName)){
            return false;
        }
        return k8sTaskMapper.deleteByNamespaceAndResourceName(namespace,resourceName,true) > 0 ? true: false;
    }
}
