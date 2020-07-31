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

package org.dubhe.k8s.config;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpHost;
import org.dubhe.enums.LogEnum;
import org.dubhe.k8s.api.DeploymentApi;
import org.dubhe.k8s.api.JupyterResourceApi;
import org.dubhe.k8s.api.LimitRangeApi;
import org.dubhe.k8s.api.LogMonitoringApi;
import org.dubhe.k8s.api.MetricsApi;
import org.dubhe.k8s.api.ModelOptJobApi;
import org.dubhe.k8s.api.NamespaceApi;
import org.dubhe.k8s.api.NodeApi;
import org.dubhe.k8s.api.PersistentVolumeClaimApi;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.api.ResourceQuotaApi;
import org.dubhe.k8s.api.TrainJobApi;
import org.dubhe.k8s.api.impl.DeploymentApiImpl;
import org.dubhe.k8s.api.impl.JupyterResourceApiImpl;
import org.dubhe.k8s.api.impl.LimitRangeApiImpl;
import org.dubhe.k8s.api.impl.LogMonitoringApiImpl;
import org.dubhe.k8s.api.impl.MetricsApiImpl;
import org.dubhe.k8s.api.impl.ModelOptJobApiImpl;
import org.dubhe.k8s.api.impl.NamespaceApiImpl;
import org.dubhe.k8s.api.impl.NodeApiImpl;
import org.dubhe.k8s.api.impl.PersistentVolumeClaimApiImpl;
import org.dubhe.k8s.api.impl.PodApiImpl;
import org.dubhe.k8s.api.impl.ResourceQuotaApiImpl;
import org.dubhe.k8s.api.impl.TrainJobApiImpl;
import org.dubhe.k8s.cache.ResourceCache;
import org.dubhe.k8s.properties.ClusterProperties;
import org.dubhe.k8s.utils.K8sUtils;
import org.dubhe.utils.LogUtil;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

import static org.apache.http.HttpVersion.HTTP;
import static org.dubhe.base.MagicNumConstant.ZERO;
import static org.dubhe.constant.SymbolConstant.COLON;
import static org.dubhe.constant.SymbolConstant.COMMA;

/**
 * @description load kubeconfig
 * @date 2020-04-09
 */
@Configuration
@EnableConfigurationProperties(ClusterProperties.class)
public class K8sConfig {

    @Autowired
    private ClusterProperties clusterProperties;

    @Value("${k8s.elasticsearch.hostlist}")
    private String hostlist;

    @Bean
    public K8sUtils k8sUtils() throws IOException {
        LogUtil.debug(LogEnum.BIZ_K8S, "ClusterProperties======{}", JSONObject.toJSONString(clusterProperties));
        if (clusterProperties == null) {
            return null;
        }
        final String kubeconfig = clusterProperties.getKubeconfig();
        LogUtil.debug(LogEnum.BIZ_K8S, "ClusterProperties.getKubeconfig()======{}", clusterProperties.getKubeconfig());
        final String url = clusterProperties.getUrl();
        LogUtil.debug(LogEnum.BIZ_K8S, "ClusterProperties.getUrl()======{}", clusterProperties.getKubeconfig());
        if (StrUtil.isEmpty(url) && StrUtil.isEmpty(kubeconfig)) {
            return null;
        }
        return new K8sUtils(clusterProperties);
    }

    @Bean
    public JupyterResourceApi jupyterResourceApi(K8sUtils k8sUtils) {
        return new JupyterResourceApiImpl(k8sUtils);
    }

    @Bean
    public TrainJobApi jupyterJobApi(K8sUtils k8sUtils) {
        return new TrainJobApiImpl(k8sUtils);
    }

    @Bean
    public PodApi podApi(K8sUtils k8sUtils) {
        return new PodApiImpl(k8sUtils);
    }

    @Bean
    public NamespaceApi namespaceApi(K8sUtils k8sUtils) {
        return new NamespaceApiImpl(k8sUtils);
    }

    @Bean
    public NodeApi nodeApi(K8sUtils k8sUtils) {
        return new NodeApiImpl(k8sUtils);
    }

    @Bean
    public LimitRangeApi limitRangeApi(K8sUtils k8sUtils) {
        return new LimitRangeApiImpl(k8sUtils);
    }

    @Bean
    public ResourceQuotaApi resourceQuotaApi(K8sUtils k8sUtils) {
        return new ResourceQuotaApiImpl(k8sUtils);
    }

    @Bean
    public PersistentVolumeClaimApi persistentVolumeClaimApi(K8sUtils k8sUtils) {
        return new PersistentVolumeClaimApiImpl(k8sUtils);
    }

    @Bean
    public LogMonitoringApi logMonitoringApi(K8sUtils k8sUtils) {
        return new LogMonitoringApiImpl(k8sUtils);
    }

    @Bean
    public ResourceCache resourceCache() {
        return new ResourceCache();
    }

    @Bean
    public MetricsApi metricsApi(K8sUtils k8sUtils) {
        return new MetricsApiImpl(k8sUtils);
    }

    @Bean
    public DeploymentApi deploymentApi(K8sUtils k8sUtils) {
        return new DeploymentApiImpl(k8sUtils);
    }

    @Bean
    public ModelOptJobApi jobApi(K8sUtils k8sUtils) {
        return new ModelOptJobApiImpl(k8sUtils);
    }

    @Bean
    public RestHighLevelClient restHighLevelClient(){

        String[] hosts = hostlist.split(COMMA);
        HttpHost[] httpHostArray = new HttpHost[hosts.length];
        for(int i=ZERO;i<hosts.length;i++){
            String item = hosts[i];
            httpHostArray[i] = new HttpHost(item.split(COLON)[ZERO], Integer.parseInt(item.split(COLON)[1]), HTTP);
        }
        return new RestHighLevelClient(RestClient.builder(httpHostArray));
    }
}
