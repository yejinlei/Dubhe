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

package org.onebrain.operator.controller;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.cache.Lister;
import lombok.extern.slf4j.Slf4j;
import org.onebrain.operator.action.handler.DistributeTrainActionHandler;
import org.onebrain.operator.crd.DistributeTrain;
import org.onebrain.operator.crd.DistributeTrainList;
import org.onebrain.operator.crd.DoneableDistributeTrain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.TimeUnit;

/**
 * @description 分布式训练控制器
 * @date 2020-06-16
 */
@Slf4j
public class DistributeTrainController {

    @Autowired
    private KubernetesClient client;

    /**
     * 分布式训练informer
     */
    private SharedIndexInformer<DistributeTrain> distributeTrainSharedIndexInformer;

    /**
     * 分布式训练k8s访问客户端
     */
    private MixedOperation<DistributeTrain, DistributeTrainList, DoneableDistributeTrain, Resource<DistributeTrain, DoneableDistributeTrain>> distributeTrainClient;

    /**
     * 分布式训练lister
     */
    private Lister<DistributeTrain> distributeTrainLister;

    @Autowired
    @Qualifier("addActionHandler")
    private DistributeTrainActionHandler addActionHandler;

    @Autowired
    @Qualifier("deleteActionHandler")
    private DistributeTrainActionHandler deleteActionHandler;

    public DistributeTrainController(MixedOperation<DistributeTrain, DistributeTrainList, DoneableDistributeTrain, Resource<DistributeTrain, DoneableDistributeTrain>> distributeTrainClient, SharedIndexInformer<DistributeTrain> distributeTrainSharedIndexInformer, String namespace) {
        this.distributeTrainSharedIndexInformer = distributeTrainSharedIndexInformer;
        this.distributeTrainClient = distributeTrainClient;
        this.distributeTrainLister = new Lister<>(distributeTrainSharedIndexInformer.getIndexer());
    }

    /**
     * 添加事件监听器
     */
    public void create() {
        distributeTrainSharedIndexInformer.addEventHandler(new ResourceEventHandler<DistributeTrain>() {
            /**
             * 处理添加事件
             * @param distributeTrain 分布式训练信息
             */
            @Override
            public void onAdd(DistributeTrain distributeTrain) {
                log.info("add distributeTrain named 【{}】 in namespace 【{}】", distributeTrain.getMetadata().getName(), distributeTrain.getMetadata().getNamespace());
                addActionHandler.handlerAction(distributeTrain);
            }

            /**
             * 处理更内心事件
             * @param distributeTrain 旧的 分布式训练信息
             * @param newDistributeTrain 新的 分布式训练信息
             */
            @Override
            public void onUpdate(DistributeTrain distributeTrain, DistributeTrain newDistributeTrain) {
                log.info("update distributeTrain named 【{}】 in namespace 【{}】", distributeTrain.getMetadata().getName(), distributeTrain.getMetadata().getNamespace());
            }

            /**
             * 处理删除事件
             * @param distributeTrain 分布式训练信息
             * @param b 是否为未知事件
             */
            @Override
            public void onDelete(DistributeTrain distributeTrain, boolean b) {
                log.info("delete distributeTrain named 【{}】 in namespace 【{}】", distributeTrain.getMetadata().getName(), distributeTrain.getMetadata().getNamespace());
                deleteActionHandler.handlerAction(distributeTrain);
            }
        });
    }

    /**
     * 运行
     */
    @Async
    public void run() {
        log.info("Starting DistributeTrain controller");
        try {
            //分布式训练信息同步
            while (!distributeTrainSharedIndexInformer.hasSynced()){
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("run error:【{}】",e);
        }
        log.info("DistributeTrain controller is Running");
    }
}
