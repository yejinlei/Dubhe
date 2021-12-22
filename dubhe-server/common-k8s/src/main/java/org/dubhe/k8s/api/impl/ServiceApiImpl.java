package org.dubhe.k8s.api.impl;

import cn.hutool.core.collection.CollectionUtil;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.api.ServiceApi;
import org.dubhe.k8s.domain.resource.BizService;
import org.dubhe.k8s.domain.vo.TerminalResourceVO;
import org.dubhe.k8s.utils.BizConvertUtils;
import org.dubhe.k8s.utils.K8sUtils;
import org.dubhe.k8s.utils.LabelUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @description k8s Service接口实现
 * @date 2021-10-27
 */
public class ServiceApiImpl implements ServiceApi {

    private K8sUtils k8sUtils;
    private KubernetesClient client;


    public ServiceApiImpl(K8sUtils k8sUtils) {
        this.k8sUtils = k8sUtils;
        this.client = k8sUtils.getClient();
    }

    /**
     * 查询命名空间下所有service
     *
     * @param namespace 命名空间
     * @return List<BizService> Service业务类集合
     */
    @Override
    public List<BizService> getWithNameSpace(String namespace) {
        try {
            List<BizService> BizServiceList =new ArrayList<>();
            ServiceList svcList = client.services().inNamespace(namespace).list();
            if(CollectionUtil.isEmpty(svcList.getItems())){
                return BizServiceList;
            }
            BizServiceList = BizConvertUtils.toBizServiceList(svcList.getItems());
            LogUtil.info(LogEnum.BIZ_K8S,"Output {}", BizServiceList);
            return BizServiceList;
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "getWithNameSpace error:", e);
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * 根据resourceName 获取service对应k8s中labels
     *
     * @param resourceName 资源名称
     * @return Map<String, String> map
     */
    @Override
    public Map<String, String> getLabels(String resourceName){
        return LabelUtils.withEnvResourceName(resourceName);
    }
}
