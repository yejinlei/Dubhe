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

package org.dubhe.k8s.api.impl;

import io.fabric8.kubernetes.api.model.Toleration;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.batch.Job;
import org.dubhe.k8s.api.NodeApi;
import org.dubhe.k8s.api.ResourceIisolationApi;
import org.dubhe.k8s.domain.cr.DistributeTrain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @description
 * @date 2021-05-20
 */
@Service
public class ResourceIisolationApiImpl implements ResourceIisolationApi {
    @Autowired
    private NodeApi nodeApi;

    /**
     * 添加 node资源隔离信息 Toleration 和 node Selector
     *
     * @param job
     */
    @Override
    public void addIisolationInfo(Job job) {
        Toleration toleration = nodeApi.getNodeIsolationToleration();
        if (toleration == null){
            return;
        }
        if (null == job.getSpec().getTemplate().getSpec().getNodeSelector()){
            job.getSpec().getTemplate().getSpec().setNodeSelector(nodeApi.getNodeIsolationNodeSelector());
        }else {
            job.getSpec().getTemplate().getSpec().getNodeSelector().putAll(nodeApi.getNodeIsolationNodeSelector());
        }
        if (null == job.getSpec().getTemplate().getSpec().getTolerations()){
            job.getSpec().getTemplate().getSpec().setTolerations(Arrays.asList(toleration));
        }else {
            job.getSpec().getTemplate().getSpec().getTolerations().addAll(Arrays.asList(toleration));
        }
    }

    /**
     * 添加 node资源隔离信息 Toleration 和 node Selector
     *
     * @param deployment
     */
    @Override
    public void addIisolationInfo(Deployment deployment) {
        Toleration toleration = nodeApi.getNodeIsolationToleration();
        if (toleration == null){
            return;
        }
        if (null == deployment.getSpec().getTemplate().getSpec().getNodeSelector()){
            deployment.getSpec().getTemplate().getSpec().setNodeSelector(nodeApi.getNodeIsolationNodeSelector());
        }else {
            deployment.getSpec().getTemplate().getSpec().getNodeSelector().putAll(nodeApi.getNodeIsolationNodeSelector());
        }
        if (null == deployment.getSpec().getTemplate().getSpec().getTolerations()){
            deployment.getSpec().getTemplate().getSpec().setTolerations(Arrays.asList(toleration));
        }else {
            deployment.getSpec().getTemplate().getSpec().getTolerations().addAll(Arrays.asList(toleration));
        }
    }

    /**
     * 添加 node资源隔离信息 Toleration 和 node Selector
     *
     * @param statefulSet
     */
    @Override
    public void addIisolationInfo(StatefulSet statefulSet) {
        Toleration toleration = nodeApi.getNodeIsolationToleration();
        if (toleration == null){
            return;
        }
        if (null == statefulSet.getSpec().getTemplate().getSpec().getNodeSelector()){
            statefulSet.getSpec().getTemplate().getSpec().setNodeSelector(nodeApi.getNodeIsolationNodeSelector());
        }else {
            statefulSet.getSpec().getTemplate().getSpec().getNodeSelector().putAll(nodeApi.getNodeIsolationNodeSelector());
        }
        if (null == statefulSet.getSpec().getTemplate().getSpec().getTolerations()){
            statefulSet.getSpec().getTemplate().getSpec().setTolerations(Arrays.asList(toleration));
        }else {
            statefulSet.getSpec().getTemplate().getSpec().getTolerations().addAll(Arrays.asList(toleration));
        }
    }

    /**
     * 添加 node资源隔离信息 Toleration 和 node Selector
     *
     * @param distributeTrain
     */
    @Override
    public void addIisolationInfo(DistributeTrain distributeTrain) {
        Toleration toleration = nodeApi.getNodeIsolationToleration();
        if (toleration == null){
            return;
        }
        distributeTrain.getSpec().addNodeSelector(nodeApi.getNodeIsolationNodeSelector());
        distributeTrain.getSpec().addTolerations(Arrays.asList(toleration));
    }
}
