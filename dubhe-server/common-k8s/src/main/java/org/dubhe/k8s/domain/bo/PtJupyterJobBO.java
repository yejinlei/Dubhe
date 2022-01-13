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
import org.dubhe.k8s.enums.GraphicsCardTypeEnum;
import org.dubhe.k8s.enums.ValidationTypeEnum;
import org.dubhe.biz.base.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description 训练任务 Job BO
 * @date 2020-04-22
 */
@Data
@Accessors(chain = true)
public class PtJupyterJobBO {
    /**命名空间**/
    @K8sValidation(ValidationTypeEnum.K8S_RESOURCE_NAME)
    private String namespace;
    /**资源名称**/
    @K8sValidation(ValidationTypeEnum.K8S_RESOURCE_NAME)
    private String name;
    /**GPU数量,1代表使用一张显卡**/
    private Integer gpuNum;
    /**是否使用gpu true：使用；false：不用**/
    private Boolean useGpu;
    /**内存数量，单位Mi**/
    private Integer memNum;
    /**cpu用量 单位:m 1个核心=1000m**/
    private Integer cpuNum;
    /**镜像名称**/
    private String image;
    /**执行命令**/
    private List<String> cmdLines;

    /**文件存储服务挂载 key：pod内挂载路径  value：文件存储路径及配置**/
    private Map<String,PtMountDirBO> fsMounts;

    /**显卡类型**/
    private GraphicsCardTypeEnum graphicsCardType;
    /**业务标签,用于标识业务模块**/
    private String businessLabel;
    /**额外扩展的标签**/
    private Map<String, String> extraLabelMap;
    /**任务身份标签,用于标识任务身份**/
    private String taskIdentifyLabel;
    /**延时创建时间，单位：分钟**/
    private Integer delayCreateTime;
    /**定时删除时间，相对于实际创建时间，单位：分钟**/
    private Integer delayDeleteTime;
    /**pip包路径**/
    private String pipSitePackagePath;
    /**pip包挂载路径**/
    private String pipSitePackageMountPath;


    public List<String> getDirList(){
        if (CollectionUtil.isNotEmpty(fsMounts)){
            return fsMounts.values().stream().map(PtMountDirBO::getDir).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public PtJupyterJobBO putFsMounts(String mountPath,String dir){
        if (StringUtils.isNotEmpty(mountPath) && StringUtils.isNotEmpty(dir)){
            if (fsMounts == null){
                fsMounts = new HashMap<>(MagicNumConstant.TWO);
            }
            fsMounts.put(mountPath,new PtMountDirBO(dir));
        }
        return this;
    }

    public PtJupyterJobBO putFsMounts(String mountPath,PtMountDirBO dir){
        if (StringUtils.isNotEmpty(mountPath) && dir != null){
            if (fsMounts == null){
                fsMounts = new HashMap<>(MagicNumConstant.TWO);
            }
            fsMounts.put(mountPath,dir);
        }
        return this;
    }
}
