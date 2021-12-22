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

package org.dubhe.dubhek8s.service;


import org.dubhe.biz.base.dto.QueryUserK8sResourceDTO;
import org.dubhe.biz.base.vo.QueryUserResourceSpecsVO;
import org.dubhe.dubhek8s.domain.dto.NodeDTO;
import org.dubhe.dubhek8s.domain.vo.K8sAllResourceVO;
import org.dubhe.k8s.domain.dto.NodeInfoDTO;
import org.dubhe.k8s.domain.dto.NodeIsolationDTO;
import org.dubhe.k8s.domain.entity.K8sNode;
import org.dubhe.k8s.domain.resource.BizNode;

import java.util.List;

/**
 * @description 用来查询node的service层代码
 * @date 2020-06-03
 */
public interface SystemNodeService {

    /**
     * 查询节点封装的数据
     *
     * @param
     * @return List<NodeDTO> NodeDTO集合
     */
    List<NodeDTO> findNodes();

    /**
     * 查询节点封装的数据和隔离信息
     *
     * @param
     * @return List<NodeDTO> NodeDTO集合
     */
    List<NodeDTO> findNodesIsolation();

    /**
     * k8s节点添加资源隔离
     *
     * @param nodeIsolationDTO k8s节点资源隔离DTO
     * @return List<BizNode>
     */
    List<BizNode> addNodeIisolation(NodeIsolationDTO nodeIsolationDTO);

    /**
     * k8s节点删除资源隔离
     *
     * @param nodeIsolationDTO k8s节点资源隔离DTO
     * @return List<BizNode>
     */
    List<BizNode> delNodeIisolation(NodeIsolationDTO nodeIsolationDTO);

    /**
     * 查询用户k8s可用资源
     *
     * @param queryUserK8sResources 用户k8s可用资源查询条件
     * @return QueryUserResourceSpecsVO 用户k8s可用资源
     */
    List<QueryUserResourceSpecsVO> queryUserK8sResource(List<QueryUserK8sResourceDTO> queryUserK8sResources);

    /**
     * k8s节点信息编辑
     *
     * @param nodeInfoDTO k8s节点信息DTO
     * @return K8sNode
     */
    K8sNode editNodeInfo(NodeInfoDTO nodeInfoDTO);

    /**
     * 统计k8s集群各个资源的数量
     *
     * @return K8sAllResourceVO 资源总量统计响应VO
     */
    K8sAllResourceVO findAllResource();
}
