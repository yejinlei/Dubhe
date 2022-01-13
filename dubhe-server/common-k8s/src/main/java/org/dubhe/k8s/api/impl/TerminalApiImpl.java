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

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.api.NodeApi;
import org.dubhe.k8s.api.PersistentVolumeClaimApi;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.api.ResourceIisolationApi;
import org.dubhe.k8s.api.ResourceQuotaApi;
import org.dubhe.k8s.api.TerminalApi;
import org.dubhe.k8s.api.VolumeApi;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.bo.BuildFsVolumeBO;
import org.dubhe.k8s.domain.bo.BuildServiceBO;
import org.dubhe.k8s.domain.bo.TerminalBO;
import org.dubhe.k8s.domain.vo.PtJupyterDeployVO;
import org.dubhe.k8s.domain.vo.TerminalResourceVO;
import org.dubhe.k8s.domain.vo.VolumeVO;
import org.dubhe.k8s.enums.ImagePullPolicyEnum;
import org.dubhe.k8s.enums.K8sKindEnum;
import org.dubhe.k8s.enums.K8sResponseEnum;
import org.dubhe.k8s.enums.LackOfResourcesEnum;
import org.dubhe.k8s.enums.LimitsOfResourcesEnum;
import org.dubhe.k8s.enums.ServiceTypeENum;
import org.dubhe.k8s.utils.BizConvertUtils;
import org.dubhe.k8s.utils.K8sUtils;
import org.dubhe.k8s.utils.LabelUtils;
import org.dubhe.k8s.utils.ResourceBuildUtils;
import org.dubhe.k8s.utils.YamlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @description 专业版终端接口实现
 * @date 2021-06-29
 */
public class TerminalApiImpl implements TerminalApi {
    private K8sUtils k8sUtils;
    private KubernetesClient client;
    @Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;
    @Autowired
    private VolumeApi volumeApi;
    @Autowired
    private PersistentVolumeClaimApi persistentVolumeClaimApi;
    @Autowired
    private NodeApi nodeApi;
    @Autowired
    private PodApi podApi;
    @Autowired
    private ResourceQuotaApi resourceQuotaApi;
    @Autowired
    private ResourceIisolationApi resourceIisolationApi;

    public TerminalApiImpl(K8sUtils k8sUtils) {
        this.k8sUtils = k8sUtils;
        this.client = k8sUtils.getClient();
    }

    /**
     * 创建
     *
     * @param bo
     * @return BizDeployment
     */
    @Override
    public TerminalResourceVO create(TerminalBO bo) {
        try {
            LogUtil.info(LogEnum.BIZ_K8S, "Params of creating TerminalApiImpl--create:{}", bo);
            //资源配额校验
            LimitsOfResourcesEnum limitsOfResources = resourceQuotaApi.reachLimitsOfResources(bo.getNamespace(), bo.getCpuNum(), bo.getMemNum(), bo.getGpuNum());
            if (!LimitsOfResourcesEnum.ADEQUATE.equals(limitsOfResources)) {
                return new TerminalResourceVO().error(K8sResponseEnum.LACK_OF_RESOURCES.getCode(), limitsOfResources.getMessage());
            }
            LackOfResourcesEnum lack = nodeApi.isAllocatable(bo.getCpuNum(), bo.getMemNum(), bo.getGpuNum());
            if (!LackOfResourcesEnum.ADEQUATE.equals(lack)) {
                return new TerminalResourceVO().error(K8sResponseEnum.LACK_OF_RESOURCES.getCode(), lack.getMessage());
            }
            if (!fileStoreApi.createDirs(bo.getDirList().toArray(new String[MagicNumConstant.ZERO]))) {
                return new TerminalResourceVO().error(K8sResponseEnum.INTERNAL_SERVER_ERROR.getCode(), K8sResponseEnum.INTERNAL_SERVER_ERROR.getMessage());
            }

            //存储卷构建
            VolumeVO volumeVO = volumeApi.buildFsVolumes(new BuildFsVolumeBO(bo.getNamespace(), bo.getResourceName(), bo.getFsMounts()));
            if (!K8sResponseEnum.SUCCESS.getCode().equals(volumeVO.getCode())) {
                return new TerminalResourceVO().error(volumeVO.getCode(), volumeVO.getMessage());
            }

            //共享存储
            Integer ShmMemAmount = bo.getMemNum() == null?MagicNumConstant.BINARY_TEN_EXP:bo.getMemNum()/MagicNumConstant.TWO;
            volumeVO.addShmFsVolume(new Quantity(String.valueOf(ShmMemAmount),K8sParamConstants.MEM_UNIT));

            //名称生成
            String deploymentName = StrUtil.format(K8sParamConstants.RESOURCE_NAME_TEMPLATE, bo.getResourceName(), RandomUtil.randomString(MagicNumConstant.EIGHT));
            String svcName = StrUtil.format(K8sParamConstants.SUB_RESOURCE_NAME_TEMPLATE, bo.getResourceName(), K8sParamConstants.SVC_SUFFIX, RandomUtil.randomString(MagicNumConstant.FIVE));

            //标签生成
            Map<String, String> baseLabels = LabelUtils.getBaseLabels(bo.getResourceName(), bo.getBusinessLabel());
            Map<String, String> podLabels = LabelUtils.getChildLabels(bo.getResourceName(), deploymentName, K8sKindEnum.DEPLOYMENT.getKind(), bo.getBusinessLabel(), bo.getTaskIdentifyLabel());

            //部署deployment
            bo.setImagePullPolicy(ImagePullPolicyEnum.ALWAYS.getPolicy());
            Deployment deployment = ResourceBuildUtils.buildDeployment(bo, volumeVO, deploymentName);
            LogUtil.info(LogEnum.BIZ_K8S, "Ready to deploy {}, yaml信息为{}", deploymentName, YamlUtils.dumpAsYaml(deployment));
            resourceIisolationApi.addIisolationInfo(deployment);
            Deployment deploymentResult = client.apps().deployments().inNamespace(bo.getNamespace()).create(deployment);

            //部署service
            BuildServiceBO buildServiceBO = new BuildServiceBO(bo.getNamespace(), svcName, baseLabels, podLabels, ServiceTypeENum.NODE_PORT.getType());
            if (!CollectionUtils.isEmpty(bo.getPorts())){
                bo.getPorts().forEach(port -> {
                    buildServiceBO.addPort(ResourceBuildUtils.buildServicePort(port, port, SymbolConstant.PORT+SymbolConstant.HYPHEN+port));
                });
            }
            Service service = ResourceBuildUtils.buildService(buildServiceBO);
            LogUtil.info(LogEnum.BIZ_K8S, "Ready to deploy {}, yaml信息为{}", svcName, YamlUtils.dumpAsYaml(service));
            Service serviceResult = client.services().create(service);
            return new TerminalResourceVO(BizConvertUtils.toBizDeployment(deploymentResult),BizConvertUtils.toBizService(serviceResult));
        }catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "TerminalApiImpl.create error, param:{} error:", bo, e);
            return new TerminalResourceVO().error(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 删除
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return PtBaseResult 基础结果类
     */
    @Override
    public PtBaseResult delete(String namespace, String resourceName) {
        try {
            LogUtil.info(LogEnum.BIZ_K8S, "delete Terminal namespace:{} resourceName:{}",namespace,resourceName);
            DeploymentList deploymentList = client.apps().deployments().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).list();
            if (deploymentList == null || deploymentList.getItems().size() == 0){
                return new PtBaseResult();
            }
            persistentVolumeClaimApi.delete(namespace,resourceName);
            persistentVolumeClaimApi.deletePvByResourceName(resourceName);
            Boolean res = client.services().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).delete()
                    && client.apps().deployments().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).delete();
            if (res) {
                return new PtBaseResult();
            } else {
                return K8sResponseEnum.REPEAT.toPtBaseResult();
            }
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "delete error:", e);
            return new PtBaseResult(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 查询
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return
     */
    @Override
    public TerminalResourceVO get(String namespace, String resourceName) {
        try {
            ServiceList svcList = client.services().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).list();
            Service svc = CollectionUtil.isEmpty(svcList.getItems()) ? null : svcList.getItems().get(0);
            DeploymentList deploymentList = client.apps().deployments().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).list();
            Deployment deployment = CollectionUtil.isEmpty(deploymentList.getItems()) ? null : deploymentList.getItems().get(0);
            return new TerminalResourceVO(BizConvertUtils.toBizDeployment(deployment), BizConvertUtils.toBizService(svc));
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "get error:", e);
            return new TerminalResourceVO().error(String.valueOf(e.getCode()), e.getMessage());
        }
    }
}
