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

package org.dubhe.k8s.domain.bo;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.k8s.annotation.K8sValidation;
import org.dubhe.k8s.enums.ValidationTypeEnum;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description deployment BO
 * @date 2021-06-30
 */
@Data
@Accessors(chain = true)
public class DeploymentBO {
    /**
     * 命名空间
     **/
    @K8sValidation(ValidationTypeEnum.K8S_RESOURCE_NAME)
    private String namespace;
    /**
     * 资源名称
     **/
    @K8sValidation(ValidationTypeEnum.K8S_RESOURCE_NAME)
    private String resourceName;
    /**
     * Number of desired pods
     */
    private Integer replicas;
    /**
     * GPU数量
     **/
    private Integer gpuNum;
    /**
     * 内存数量 单位Mi
     **/
    private Integer memNum;
    /**
     * CPU数量
     **/
    private Integer cpuNum;
    /**
     * 镜像名称
     **/
    private String image;
    /**
     * 执行命令
     **/
    private List<String> cmdLines;
    /**
     * 文件存储服务挂载 key：pod内挂载路径  value：文件存储路径及配置
     **/
    private Map<String, PtMountDirBO> fsMounts;
    /**
     * 业务标签,用于标识业务模块
     **/
    @K8sValidation(ValidationTypeEnum.K8S_RESOURCE_NAME)
    private String businessLabel;
    /**
     * 任务身份标签,用于标识任务身份
     **/
    private String taskIdentifyLabel;
    /**
     * 端口
     */
    private Set<Integer> ports;

    /**
     * 镜像拉取策略
     * IfNotPresent 默认值
     * Always
     * Never
     */
    private String imagePullPolicy;

    /**
     * 获取nfs路径
     * @return
     */
    public List<String> getDirList(){
        if (CollectionUtil.isNotEmpty(fsMounts)){
            return fsMounts.values().stream().map(PtMountDirBO::getDir).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * 设置nfs挂载
     * @param mountPath 容器内路径
     * @param dir nfs路径
     * @return
     */
    public DeploymentBO putfsMounts(String mountPath, String dir){
        if (StringUtils.isNotEmpty(mountPath) && StringUtils.isNotEmpty(dir)){
            if (fsMounts == null){
                fsMounts = new HashMap<>(MagicNumConstant.TWO);
            }
            fsMounts.put(mountPath,new PtMountDirBO(dir));
        }
        return this;
    }

    /**
     * 设置nfs挂载
     * @param mountPath 容器内路径
     * @param dir nfs路径及配置
     * @return
     */
    public DeploymentBO putfsMounts(String mountPath, PtMountDirBO dir){
        if (StringUtils.isNotEmpty(mountPath) && dir != null){
            if (fsMounts == null){
                fsMounts = new HashMap<>(MagicNumConstant.TWO);
            }
            fsMounts.put(mountPath,dir);
        }
        return this;
    }

    /**
     * 添加端口
     *
     * @param port
     */
    public void addPort(Integer port){
        if (port == null){
            return;
        }
        if (ports == null){
            ports = new HashSet<>();
        }
        ports.add(port);
    }

    /**
     * 添加端口
     *
     * @param ports
     */
    public void addPorts(Set<Integer> ports){
        if (CollectionUtils.isEmpty(ports)){
            return;
        }
        if (ports == null){
            ports = new HashSet<>();
        }
        ports.addAll(ports);
    }
}
