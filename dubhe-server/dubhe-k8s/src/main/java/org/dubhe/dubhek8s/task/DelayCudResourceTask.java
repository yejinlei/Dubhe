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

package org.dubhe.dubhek8s.task;

import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.redis.utils.RedisUtils;
import org.dubhe.k8s.api.DistributeTrainApi;
import org.dubhe.k8s.api.NativeResourceApi;
import org.dubhe.k8s.constant.RedisConstants;
import org.dubhe.k8s.domain.bo.K8sTaskBO;
import org.dubhe.k8s.domain.bo.ResourceYamlBO;
import org.dubhe.k8s.domain.bo.TaskYamlBO;
import org.dubhe.k8s.domain.entity.K8sTask;
import org.dubhe.k8s.enums.K8sKindEnum;
import org.dubhe.k8s.enums.K8sTaskStatusEnum;
import org.dubhe.k8s.service.K8sTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;

/**
 * @description kubernetes 延时创建/删除定时任务
 * @date 2020-09-01
 */
@Component
public class DelayCudResourceTask {
    @Autowired
    private DistributeTrainApi distributeTrainApi;
    @Autowired
    private NativeResourceApi nativeResourceApi;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private K8sTaskService k8sTaskService;
    /**
     * 空闲计数,达到100后从数据库同步延时任务到redis
     */
    private static Integer idleCount = 0;

    /**
     * 每一秒执行一次，延时创建/删除定时任务
     */
    @Scheduled(cron = "0/1 * * * * ?")
    public void delayCudResource(){
        try {
            //休眠 0-1 秒避免 频繁抢锁
            Thread.sleep((long)Math.random()*MagicNumConstant.ONE_THOUSAND);
        } catch (InterruptedException e) {
            LogUtil.error(LogEnum.BIZ_K8S,e);
        }
        String uuid = UUID.randomUUID().toString();
        try {
            if (!redisUtils.getDistributedLock(RedisConstants.DELAY_CUD_RESOURCE_KEY,uuid,RedisConstants.DELAY_CUD_RESOURCE_EXPIRE_TIME)){
                return;
            }
            if (!CollectionUtils.isEmpty(redisUtils.zRangeByScorePop(RedisConstants.DELAY_APPLY_ZSET_KEY,System.currentTimeMillis()/1000)) ||
                    !CollectionUtils.isEmpty(redisUtils.zRangeByScorePop(RedisConstants.DELAY_STOP_ZSET_KEY,System.currentTimeMillis()/1000))){
                delayCud();
            }else {
                idleCount++;
            }
            if (idleCount%MagicNumConstant.ONE_HUNDRED == 0){
                k8sTaskService.loadTaskToRedis();
                idleCount = 1;
            }
        }catch (Exception e){
            LogUtil.error(LogEnum.BIZ_K8S,"delayCudResource error {}",e);
        }finally {
            redisUtils.releaseDistributedLock(RedisConstants.DELAY_CUD_RESOURCE_KEY,uuid);
        }
    }

    /**
     * 处理延时任务
     */
    private void delayCud(){
        K8sTaskBO k8sTaskBO = new K8sTaskBO();
        Long curUnixTime = System.currentTimeMillis()/MagicNumConstant.ONE_THOUSAND;
        k8sTaskBO.setMaxApplyUnixTime(curUnixTime);
        k8sTaskBO.setMaxStopUnixTime(curUnixTime);
        k8sTaskBO.setApplyStatus(K8sTaskStatusEnum.UNEXECUTED.getStatus());
        k8sTaskBO.setStopStatus(K8sTaskStatusEnum.UNEXECUTED.getStatus());

        List<K8sTask> k8sTasksList = k8sTaskService.selectUnexecutedTask(k8sTaskBO);
        if (CollectionUtils.isEmpty(k8sTasksList)){
            return;
        }
        for (K8sTask k8sTask : k8sTasksList){
            TaskYamlBO taskYamlBO = k8sTask.getTaskYamlBO();
            List<ResourceYamlBO> yamlList = taskYamlBO.getYamlList();
            if (CollectionUtils.isEmpty(yamlList)){
                continue;
            }

            boolean needCreate = k8sTask.needCreate(curUnixTime);
            boolean needDelete = k8sTask.needDelete(curUnixTime);
            if (needCreate){
                yamlList.forEach(resourceYamlBO->{
                    String yaml = resourceYamlBO.getYaml();
                    if (K8sKindEnum.DISTRIBUTETRAIN.getKind().equals(resourceYamlBO.getKind())) {
                        distributeTrainApi.create(yaml);
                    } else {
                        nativeResourceApi.create(yaml);
                    }
                    k8sTask.setApplyStatus(K8sTaskStatusEnum.EXECUTED.getStatus());
                    k8sTaskService.update(k8sTask);
                });
            }
            if (needDelete){
                yamlList.forEach(resourceYamlBO->{
                    String yaml = resourceYamlBO.getYaml();
                    if (K8sKindEnum.DISTRIBUTETRAIN.getKind().equals(resourceYamlBO.getKind())) {
                        distributeTrainApi.delete(yaml);
                    } else {
                        nativeResourceApi.delete(yaml);
                    }
                    k8sTask.setStopStatus(K8sTaskStatusEnum.EXECUTED.getStatus());
                    k8sTaskService.update(k8sTask);
                });
            }
            if (k8sTask.overtime(curUnixTime)){
                k8sTask.setApplyStatus(K8sTaskStatusEnum.EXECUTED.getStatus());
                k8sTask.setStopStatus(K8sTaskStatusEnum.EXECUTED.getStatus());
                k8sTaskService.update(k8sTask);
            }
        }
    }
}
