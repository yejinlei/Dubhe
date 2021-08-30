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
import org.dubhe.k8s.annotation.K8sValidation;
import org.dubhe.k8s.enums.ValidationTypeEnum;
import org.dubhe.biz.base.utils.StringUtils;

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
     * 任务身份标签,用于标识任务身份
     */
    private String taskIdentifyLabel;
    /**
     * 延时创建时间，单位：分钟
     ***/
    private Integer delayCreateTime;
    /**
     * 定时删除时间，相对于实际创建时间，单位：分钟
     **/
    private Integer delayDeleteTime;
    /**
     * 文件存储服务挂载 key：pod内挂载路径  value：文件存储路径及配置
     **/
    private Map<String,PtMountDirBO> fsMounts;

    /**
     * 设置文件存储挂载
     * @param mountPath pod内挂载路径
     * @param dir 文件存储服务路径
     * @return
     */
    public DistributeTrainBO putFsMounts(String mountPath,String dir){
        if (StringUtils.isNotEmpty(mountPath) && StringUtils.isNotEmpty(dir)){
            if (fsMounts == null){
                fsMounts = new HashMap<>(MagicNumConstant.EIGHT);
            }
            fsMounts.put(mountPath,new PtMountDirBO(dir));
        }
        return this;
    }

    /**
     * 设置文件存储挂载
     * @param mountPath pod内挂载路径
     * @param dir 文件存储服务路径及配置
     * @return
     */
    public DistributeTrainBO putFsMounts(String mountPath,PtMountDirBO dir){
        if (StringUtils.isNotEmpty(mountPath) && dir != null){
            if (fsMounts == null){
                fsMounts = new HashMap<>(MagicNumConstant.EIGHT);
            }
            fsMounts.put(mountPath,dir);
        }
        return this;
    }

    /**
     * 获取 文件存储服务路径列表
     * @return
     */
    public List<String> getDirList(){
        if (CollectionUtil.isNotEmpty(fsMounts)){
            return fsMounts.values().stream().map(PtMountDirBO::getDir).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
