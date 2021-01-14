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
import org.dubhe.base.MagicNumConstant;
import org.dubhe.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description 模型压缩 Job BO
 * @date 2020-05-31
 */
@Data
public class PtModelOptimizationJobBO extends PtJobBO {
    /**
     * 执行命令
     **/
    private List<String> cmdLines;

    /**nfs挂载 key：pod内挂载路径  value：nfs路径及配置**/
    private Map<String,PtMountDirBO> nfsMounts;

    public List<String> getDirList(){
        if (CollectionUtil.isNotEmpty(nfsMounts)){
            return nfsMounts.values().stream().map(PtMountDirBO::getDir).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public PtModelOptimizationJobBO putNfsMounts(String mountPath,String dir){
        if (StringUtils.isNotEmpty(mountPath) && StringUtils.isNotEmpty(dir)){
            if (nfsMounts == null){
                nfsMounts = new HashMap<>(MagicNumConstant.TWO);
            }
            nfsMounts.put(mountPath,new PtMountDirBO(dir));
        }
        return this;
    }

    public PtModelOptimizationJobBO putNfsMounts(String mountPath,PtMountDirBO dir){
        if (StringUtils.isNotEmpty(mountPath) && dir != null){
            if (nfsMounts == null){
                nfsMounts = new HashMap<>(MagicNumConstant.TWO);
            }
            nfsMounts.put(mountPath,dir);
        }
        return this;
    }
}
