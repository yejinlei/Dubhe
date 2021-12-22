package org.dubhe.k8s.api;

import org.dubhe.k8s.domain.resource.BizService;

import java.util.List;
import java.util.Map;

/**
 * @description k8s Service接口
 * @date 2021-10-27
 */
public interface ServiceApi {

    /**
     * 查询命名空间下所有service
     *
     * @param namespace 命名空间
     * @return List<BizService> Service业务类集合
     */
    List<BizService> getWithNameSpace(String namespace);

    /**
     * 根据resourceName 获取service对应k8s中labels
     *
     * @param resourceName 资源名称
     * @return Map<String, String> map
     */
    Map<String, String> getLabels(String resourceName);
}
