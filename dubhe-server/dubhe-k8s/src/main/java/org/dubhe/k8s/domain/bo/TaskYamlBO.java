/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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

import io.fabric8.kubernetes.api.model.HasMetadata;
import lombok.Data;
import org.dubhe.enums.LogEnum;
import org.dubhe.k8s.utils.YamlUtils;
import org.dubhe.utils.LogUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * @description 任务k8s资源列表
 * @date 2020-08-28
 */
@Data
public class TaskYamlBO {
    private List<ResourceYamlBO> yamlList;

    public void append(HasMetadata resource){
        if (yamlList == null){
            yamlList = new LinkedList<ResourceYamlBO>();
        }
        try {
            Method getMethod = resource.getClass().getDeclaredMethod("getKind", null);
            String kind = (String) getMethod.invoke(resource, null);
            HasMetadata obj =  (HasMetadata)resource;
            yamlList.add(new ResourceYamlBO(kind,YamlUtils.dumpAsYaml(obj)));
        } catch (IllegalAccessException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "resource:{} getKind error:{}",resource, e);
        } catch (InvocationTargetException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "resource:{} getKind error:{}",resource, e);
        } catch (NoSuchMethodException e){
            LogUtil.error(LogEnum.BIZ_K8S, "resource:{} not have getKind exception:{}",resource, e);
        }
    }
}
