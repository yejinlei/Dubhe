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

package org.dubhe.service.impl;

import org.dubhe.dao.HarborProjectMapper;
import org.dubhe.service.HarborProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description :Harbor镜像服务 实现类
 * @Date 2020-06-01
 */
@Service
public class HarborProjectServiceImpl implements HarborProjectService {

    @Autowired
    private HarborProjectMapper harborProjectMapper;

    /**
     * 查询 Harbor 镜像
     *
     * @param source
     * @return java.util.List<java.lang.String> Harbor镜像列表
     */
    @Override
    public List<String> getHarborProjects(int source) {
        return harborProjectMapper.findByCreateSource(source);
    }
}
