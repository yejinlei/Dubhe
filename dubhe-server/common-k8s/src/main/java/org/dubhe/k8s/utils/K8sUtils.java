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

package org.dubhe.k8s.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.k8s.constant.K8sLabelConstants;
import org.dubhe.k8s.properties.ClusterProperties;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @description init K8sUtils
 * @date 2020-04-09
 */
@Getter
public class K8sUtils implements ApplicationContextAware {

    /**
     * K8S通信协议
     **/
    private final static String HTTPS_PREFIX = "https";

    private final static String USER_DIR_SYSTEM_PROPERTY = "user.dir";

    private ApplicationContext applicationContext;

    private KubernetesClient client;
    private Config config;
    private String nfs;
    private String host;
    private String port;

    /**
     * 获取 k8s 连接配置文件
     * @param kubeConfig
     * @return
     * @throws IOException
     */
    private String getKubeconfigFile(String kubeConfig) throws IOException {
        kubeConfig = kubeConfig.startsWith(File.separator) ? kubeConfig : File.separator.concat(kubeConfig);
        String path = System.getProperty(USER_DIR_SYSTEM_PROPERTY) + kubeConfig;
        LogUtil.info(LogEnum.BIZ_K8S, "kubeconfig path:{}", path);
        FileUtil.writeFromStream(new ClassPathResource(kubeConfig).getInputStream(), path);
        return path;
    }

    /**
     * 构造 KubernetesClient
     * @param clusterProperties
     * @throws IOException
     */
    public K8sUtils(ClusterProperties clusterProperties) throws IOException{
        String kubeConfig = clusterProperties.getKubeconfig();
        if (StrUtil.isNotBlank(kubeConfig)) {
            String kubeConfigFile = getKubeconfigFile(kubeConfig);
            //修改环境变量，重新指定kubeconfig读取位置
            System.setProperty(Config.KUBERNETES_KUBECONFIG_FILE, kubeConfigFile);
            client = new DefaultKubernetesClient();
            config = client.getConfiguration();

        } else {
            LogUtil.warn(LogEnum.BIZ_K8S, "can't find kubeconfig in classpath, ignoring");
            String k8sUrl = clusterProperties.getUrl();
            if (k8sUrl.startsWith(HTTPS_PREFIX)) {
                config = new ConfigBuilder().withMasterUrl(k8sUrl)
                        .withTrustCerts(true)
                        .withCaCertData(IOUtils.toString(clusterProperties.getCaCrt().getInputStream(), "UTF-8"))
                        .withClientCertData(Base64.getEncoder().encodeToString(IOUtils.toByteArray(clusterProperties.getClientCrt().getInputStream())))
                        .withClientKeyData(IOUtils.toString(clusterProperties.getClientKey().getInputStream(), "UTF-8"))
                        .build();
            } else {
                config = new ConfigBuilder().withMasterUrl(k8sUrl).build();
            }
            LogUtil.info(LogEnum.BIZ_K8S, "config信息为{}", JSON.toJSONString(config));
            client = new DefaultKubernetesClient(config);
            LogUtil.info(LogEnum.BIZ_K8S, "client为{}", JSON.toJSONString(client));
        }

        nfs = clusterProperties.getNfs();
        host = clusterProperties.getHost();
        port = clusterProperties.getPort();

        //打印集群信息
        LogUtil.info(LogEnum.BIZ_K8S, "ApiVersion   : {}", client.getApiVersion());
        LogUtil.info(LogEnum.BIZ_K8S, "MasterUrl    : {}", client.getMasterUrl());
        LogUtil.info(LogEnum.BIZ_K8S, "NFS Server   : {}", nfs);
        LogUtil.info(LogEnum.BIZ_K8S, "VersionInfo  : {}", JSON.toJSONString(client.getVersion()));
    }

    /**
     * 导出成yaml字符串
     *
     * @param resource 任意对象
     * @return String 转换后的yaml字符串
     */
    public String convertToYaml(Object resource) {
        return YamlUtils.convertToYaml(resource);
    }

    /**
     * 导出成yaml字符串
     *
     * @param resource 任意对象
     * @param filePath 文件路径
     */
    public void dumpToYaml(Object resource, String filePath) {
        YamlUtils.dumpToYaml(resource, filePath);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 获取gpu选择label
     * @param gpuNum
     * @return
     */
    public static Map<String, String> gpuSelector(Integer gpuNum) {
        Map<String, String> gpuSelector = new HashMap<>(2);
        if (gpuNum != null && gpuNum > 0) {
            gpuSelector.put(K8sLabelConstants.NODE_GPU_LABEL_KEY, K8sLabelConstants.NODE_GPU_LABEL_VALUE);
        }
        return gpuSelector;
    }
}
