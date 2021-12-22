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
package org.dubhe.admin.async;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.dubhe.admin.client.SystemNamespaceClient;
import org.dubhe.admin.dao.UserConfigMapper;
import org.dubhe.admin.dao.UserGpuConfigMapper;
import org.dubhe.admin.domain.entity.UserConfig;
import org.dubhe.admin.domain.entity.UserGpuConfig;
import org.dubhe.biz.base.dto.NamespaceDeleteDTO;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @description 异步清理用户资源
 * @date 2021-11-25
 */
@Component
public class CleanupUserResourcesAsync {

    @Autowired
    private UserConfigMapper userConfigMapper;

    @Autowired
    private UserGpuConfigMapper userGpuConfigMapper;

    @Autowired
    private SystemNamespaceClient systemNamespaceClient;

    @Async("adminExecutor")
    public void  cleanUserResource(Set<Long> ids, String accessToken){
        //删除用户资源配置
        QueryWrapper<UserConfig> userConfigWrapper = new QueryWrapper<>();
        userConfigWrapper.in("user_id",ids);
        userConfigMapper.delete(userConfigWrapper);
        QueryWrapper<UserGpuConfig> userGpuConfigWrapper = new QueryWrapper<>();
        userGpuConfigWrapper.in("user_id",ids);
        userGpuConfigMapper.delete(userGpuConfigWrapper);
        //删除用户namespace
        NamespaceDeleteDTO namespaceDeleteDTO = new NamespaceDeleteDTO();
        namespaceDeleteDTO.setIds(ids);
        DataResponseBody dataResponseBody = systemNamespaceClient.deleteNamespace(namespaceDeleteDTO, accessToken);
        if (!dataResponseBody.succeed()) {
            throw new BusinessException("远程调用k8s删除namespace失败");
        }
    }

}