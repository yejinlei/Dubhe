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
import org.dubhe.base.MagicNumConstant;
import org.dubhe.k8s.annotation.K8sValidation;
import org.dubhe.k8s.enums.ValidationTypeEnum;
import org.dubhe.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description DistributeTrainBO
 * @date 2020-07-08
 */
@Data
@Accessors(chain = true)
public class DistributeTrainBO {
    /**
     * 命名空间
     **/
    @K8sValidation(ValidationTypeEnum.K8S_RESOURCE_NAME)
    private String namespace;
    /**
     * 资源名称
     **/
    @K8sValidation(ValidationTypeEnum.K8S_RESOURCE_NAME)
    private String name;
    /**
     * 多机启动实例数
     **/
    private Integer size;
    /**
     * 镜像名称
     **/
    private String image;
    /**
     * master机器运行时执行命令
     **/
    private String masterCmd;
    /**
     * 每个节点运行使用内存,单位Mi
     **/
    private Integer memNum;
    /**
     * 每个节点运行使用CPU数量，1000代表占用一核心
     **/
    private Integer cpuNum;
    /**
     * 每个节点运行使用GPU数量
     **/
    private Integer gpuNum;
    /**
     * slave机器运行时执行命令
     **/
    private String slaveCmd;
    /**
     * 运行环境变量
     **/
    private Map<String,String> env;
    /**
     * 业务标签,用于标识业务模块
     **/
    private String businessLabel;
    /**
     * 延时创建时间，单位：分钟
     ***/
    private Integer delayCreateTime;
    /**
     * 定时删除时间，相对于实际创建时间，单位：分钟
     **/
    private Integer delayDeleteTime;
    /**
     * nfs挂载 key：pod内挂载路径  value：nfs路径及配置
     **/
    private Map<String,PtMountDirBO> nfsMounts;

    /**
     * 设置nfs挂载
     * @param mountPath pod内挂载路径
     * @param dir nfs路径
     * @return
     */
    public DistributeTrainBO putNfsMounts(String mountPath,String dir){
        if (StringUtils.isNotEmpty(mountPath) && StringUtils.isNotEmpty(dir)){
            if (nfsMounts == null){
                nfsMounts = new HashMap<>(MagicNumConstant.EIGHT);
            }
            nfsMounts.put(mountPath,new PtMountDirBO(dir));
        }
        return this;
    }

    /**
     * 设置nfs挂载
     * @param mountPath pod内挂载路径
     * @param dir nfs路径及配置
     * @return
     */
    public DistributeTrainBO putNfsMounts(String mountPath,PtMountDirBO dir){
        if (StringUtils.isNotEmpty(mountPath) && dir != null){
            if (nfsMounts == null){
                nfsMounts = new HashMap<>(MagicNumConstant.EIGHT);
            }
            nfsMounts.put(mountPath,dir);
        }
        return this;
    }

    /**
     * 获取 nfs路径列表
     * @return
     */
    public List<String> getDirList(){
        if (CollectionUtil.isNotEmpty(nfsMounts)){
            return nfsMounts.values().stream().map(PtMountDirBO::getDir).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
