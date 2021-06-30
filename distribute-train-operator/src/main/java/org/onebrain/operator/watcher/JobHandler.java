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

package org.onebrain.operator.watcher;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.onebrain.operator.constants.KubeConstants;
import org.onebrain.operator.redis.RedisService;
import org.onebrain.operator.redis.key.OperatorKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.onebrain.operator.constants.CrdConstants.CRD_KIND;

/**
 * @description Job处理器
 * @date 2020-09-24
 */
@Data
@Slf4j
@Component
public class JobHandler {

    public static final String FINISHED = "finished";
    public static final String PENDING = "pending";
    @Autowired
    private RedisService redis;

    @Autowired
    private KubernetesClient client;

    /**
     * 处理Job
     *
     * @param job
     */
    public void handleJob(Job job) {
        log.info("handleJob=>job : 【{}】", job.getMetadata().getName());

        //筛选出DistributeTrain下的job
        List<OwnerReference> ownerReferences = job.getMetadata().getOwnerReferences();
        if (CollectionUtil.isEmpty(ownerReferences) || !CRD_KIND.equals(ownerReferences.get(0).getKind())) {
            return;
        }

        String key = job.getMetadata().getUid();
        if (StrUtil.equals(redis.get(OperatorKey.CR_JOB, key), FINISHED)) {
            return;
        }

        try {
            redis.set(OperatorKey.CR_JOB, key, PENDING);

            final Integer parallelism = job.getSpec().getParallelism();
            final Integer backoffLimit = job.getSpec().getBackoffLimit();
            //成功 或者 失败达到最大次数
            if (job.getStatus() != null
                    && ((job.getStatus().getFailed() != null && job.getStatus().getFailed() + 1 >= backoffLimit)
                    || (job.getStatus().getSucceeded() != null && parallelism.equals(job.getStatus().getSucceeded())))) {
                //得到DistributeTrain的Statefulset
                String dtName = ownerReferences.get(0).getName();
                String namespace = job.getMetadata().getNamespace();

                List<StatefulSet> statefulsetList = client.apps().statefulSets()
                        .inNamespace(namespace)
                        .withLabel(KubeConstants.DISTRIBUTE_TRAIN_LABEL, dtName)
                        .list().getItems();

                if (CollectionUtil.isEmpty(statefulsetList)) {
                    log.info("jobWatcher: statefulset of 【{}】 not exists", dtName);
                    return;
                }

                //缩容Statefulset的replica到0
                StatefulSet statefulSet = statefulsetList.get(0);
                statefulSet.getSpec().setReplicas(0);
                client.resource(statefulSet).createOrReplace();
                log.info("jobWatcher: reduce replicas of 【{}】 to zero", dtName);

                redis.set(OperatorKey.CR_JOB, key, "finished");
            }

        } catch (Exception e) {
            redis.set(OperatorKey.CR_JOB, key, "error");
            log.error("handle job error:【{}】", e);
        }
    }
}
