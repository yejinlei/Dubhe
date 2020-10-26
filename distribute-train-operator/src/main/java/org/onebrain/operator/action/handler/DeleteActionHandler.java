/**
 * Copyright 2020 Zhejiang Lab & The OneFlow Authors. All Rights Reserved.
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

package org.onebrain.operator.action.handler;

import cn.hutool.core.collection.CollectionUtil;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetList;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobList;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.onebrain.operator.constants.KubeConstants;
import org.onebrain.operator.crd.DistributeTrain;
import org.onebrain.operator.redis.RedisService;
import org.onebrain.operator.redis.key.OperatorKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description 删除事件的处理器
 * @date 2020-09-23
 */
@Component("deleteActionHandler")
@Slf4j
public class DeleteActionHandler implements DistributeTrainActionHandler {

    @Autowired
    private KubernetesClient client;

    @Autowired
    private RedisService redis;

    /**
     * 处理删除事件
     * @param distributeTrain 分布式训练信息
     */
    @Override
    public void handlerAction(DistributeTrain distributeTrain) {
        log.info("handlerAction=>distributeTrain : 【{}】", distributeTrain);
        String namespace = distributeTrain.getMetadata().getNamespace();
        String parentName = distributeTrain.getMetadata().getName();
        // namespace+parentName(分布式训练名称) 确定相应的资源
        //删除job
        JobList jobList = client.batch().jobs().inNamespace(namespace).withLabel(KubeConstants.DISTRIBUTE_TRAIN_LABEL, parentName).list();
        if(CollectionUtil.isNotEmpty(jobList.getItems())){
            for (Job item : jobList.getItems()) {
                client.batch().jobs().delete(item);
            }
            log.info("delete job in distributeTrain 【{}】", parentName);
        }
        //删除statefullSete
        StatefulSetList statefulSetList = client.apps().statefulSets().inNamespace(namespace).withLabel(KubeConstants.DISTRIBUTE_TRAIN_LABEL, parentName).list();
        if(CollectionUtil.isNotEmpty(statefulSetList.getItems())){
            for (StatefulSet item : statefulSetList.getItems()) {
                client.apps().statefulSets().delete(item);
            }
            log.info("delete statefulSet in distributeTrain 【{}】", parentName);
        }
        //删除service
        ServiceList svcList = client.services().inNamespace(namespace).withLabel(KubeConstants.DISTRIBUTE_TRAIN_LABEL, parentName).list();
        if(CollectionUtil.isNotEmpty(svcList.getItems())){
            for (Service item : svcList.getItems()) {
                client.services().delete(item);
            }
            log.info("delete svc in distributeTrain 【{}】", parentName);
        }
        //删除redis里记录的分布式训练信息
        redis.del(OperatorKey.CR, distributeTrain.getMetadata().getUid());
        log.info("delete distributeTrain 【{}】 successfully", parentName);
    }
}
