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

package org.dubhe.servinggateway.dao;

import org.apache.ibatis.annotations.Mapper;
import org.dubhe.servinggateway.domain.vo.GatewayRouteQueryVO;

import java.util.List;

/**
 * @description 在线服务网关路由mapper
 * @date 2020-09-10
 */
@Mapper
public interface GatewayRouteMapper{

    /**
     * @param id 主键
     * @return GatewayRoute 返回路由信息
     */
    GatewayRouteQueryVO findActiveById(Long id);

    /**
     * @return List<GatewayRoute> 可用的路由列表
     */
    List<GatewayRouteQueryVO> findAllActiveRoute();
}
