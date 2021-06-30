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

package org.dubhe.servinggateway.service;

import org.dubhe.servinggateway.domain.vo.GatewayRouteQueryVO;

import java.util.List;

/**
 * @description 在线服务网关路由接口
 * @date 2020-09-11
 */
public interface GatewayRouteService {
    /**
     * 获取所有可用的路有信息
     *
     * @return List<GatewayRoute> 可用的路由信息
     */
    List<GatewayRouteQueryVO> findActiveRoutes();

    /**
     * 根据id获取可用的路由信息
     *
     * @param id 主键
     * @return GatewayRoute 路由信息
     */
    GatewayRouteQueryVO findActiveById(Long id);
}
