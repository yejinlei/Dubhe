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

package org.onebrain.operator.context;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.VersionInfo;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import io.fabric8.kubernetes.client.utils.Utils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.onebrain.operator.properties.KubeProperties;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * @description k8s上下文
 * @date 2020-09-23
 */
@Slf4j
@Getter
public class KubeContext implements ApplicationContextAware {

    private static final String AUTO = "auto";

    private ApplicationContext applicationContext;

    private KubernetesClient client;

    private Config config;


    public KubeContext(KubeProperties kubeProperties) {
        String configSource = kubeProperties.getKubeconfig();
        try {
            if(AUTO.equals(configSource)){
                //在集群内部可自动侦测
                log.info("kubernetes client is in cluster mode");
                client = new DefaultKubernetesClient();
                config = client.getConfiguration();
            }else{
                if(configSource.startsWith(StrUtil.SLASH)){
                    log.info("read kubeconfig from file system:{}", configSource);
                    System.setProperty(Config.KUBERNETES_KUBECONFIG_FILE, configSource);
                }else{
                    log.info("read kubeconfig from classpath:{}", configSource);
                    final String testKubeconfigFile = Utils.filePath(getClass().getResource(StrUtil.SLASH + configSource));
                    //修改环境变量，重新指定kubeconfig读取位置
                    System.setProperty(Config.KUBERNETES_KUBECONFIG_FILE, testKubeconfigFile);
                }
                client = new DefaultKubernetesClient();
                config = client.getConfiguration();
            }

            //打印集群信息
            log.info("ApiVersion   : {}", client.getApiVersion());
            log.info("MasterUrl    : {}", client.getMasterUrl());
            if(log.isDebugEnabled()){
                VersionInfo versionInfo = client.getVersion();
                log.debug("Version details of this Kubernetes cluster :-");
                log.debug("Major        : {}", versionInfo.getMajor());
                log.debug("Minor        : {}", versionInfo.getMinor());
                log.debug("GitVersion   : {}", versionInfo.getGitVersion());
                log.debug("GitCommit    : {}", versionInfo.getGitCommit());
                log.debug("BuildDate    : {}", versionInfo.getBuildDate());
                log.debug("GitTreeState : {}", versionInfo.getGitTreeState());
                log.debug("Platform     : {}", versionInfo.getPlatform());
                log.debug("GoVersion    : {}", versionInfo.getGoVersion());
            }
        }catch (Exception e){
            client = null;
            log.error("初始化 K8sUtils 失败！", e);
            e.printStackTrace();
        }
    }

    /**
     * 导出成yaml字符串
     * @param resource k8s元数据
     * @return
     */
    public String convertToYaml(HasMetadata resource) {
        try {
            return SerializationUtils.dumpAsYaml(resource);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("can not transform resource to yaml");
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
