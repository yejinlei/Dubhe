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

package org.dubhe.servinggateway.service.impl;

import org.dubhe.servinggateway.dao.GatewayRouteMapper;
import org.dubhe.servinggateway.domain.vo.GatewayRouteQueryVO;
import org.dubhe.servinggateway.service.GatewayRouteService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description 在线服务网关路由实现类
 * @date 2020-09-11
 */
@Service
public class GatewayRouteServiceImpl implements GatewayRouteService {

    @Resource
    private GatewayRouteMapper gatewayRouteMapper;

    /**
     * 获取所有可用的路有信息
     *
     * @return List<GatewayRoute> 可用的路由信息
     */
    @Override
    public List<GatewayRouteQueryVO> findActiveRoutes() {
        return gatewayRouteMapper.findAllActiveRoute();
    }

    /**
     * 根据id获取可用的路由信息
     *
     * @param id 主键
     * @return GatewayRoute 路由信息
     */
    @Override
    public GatewayRouteQueryVO findActiveById(Long id) {
        return gatewayRouteMapper.findActiveById(id);
    }
}
