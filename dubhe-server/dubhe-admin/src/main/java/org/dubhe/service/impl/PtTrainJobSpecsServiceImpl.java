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

package org.dubhe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.dubhe.dao.PtTrainJobSpecsMapper;
import org.dubhe.domain.dto.PtTrainJobSpecsQueryDTO;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.domain.entity.PtTrainJobSpecs;
import org.dubhe.domain.vo.PtTrainJobSpecsQueryVO;
import org.dubhe.enums.LogEnum;
import org.dubhe.service.PtTrainJobSpecsService;
import org.dubhe.utils.JwtUtils;
import org.dubhe.utils.LogUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @description 训练作业规格服务实现类
 * @date 2020-05-06
 */
@Service
public class PtTrainJobSpecsServiceImpl implements PtTrainJobSpecsService {

    @Autowired
    private PtTrainJobSpecsMapper ptTrainJobSpecsMapper;

    /**
     * 规格查询
     *
     * @param ptTrainJobSpecsQueryDTO        查询规格表参数
     * @return List<PtTrainJobSpecsQueryVO>  返回规格查询结果
     **/
    @Override
    public List<PtTrainJobSpecsQueryVO> getTrainJobSpecs(PtTrainJobSpecsQueryDTO ptTrainJobSpecsQueryDTO) {
        UserDTO currentUser = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_TRAIN, "user{}Query job list display, the received parameters are{}", currentUser.getUsername(), ptTrainJobSpecsQueryDTO);
        QueryWrapper<PtTrainJobSpecs> query = new QueryWrapper<>();
        if (ptTrainJobSpecsQueryDTO.getResourcesPoolType() != null) {
            query.eq("resources_pool_type", ptTrainJobSpecsQueryDTO.getResourcesPoolType());
        }
        List<PtTrainJobSpecs> ptTrainJobSpecs = ptTrainJobSpecsMapper.selectList(query);
        List<PtTrainJobSpecsQueryVO> ptTrainJobSpecsQueryList = ptTrainJobSpecs.stream().map(x -> {
            PtTrainJobSpecsQueryVO ptTrainJobSpecsQueryVO = new PtTrainJobSpecsQueryVO();
            BeanUtils.copyProperties(x, ptTrainJobSpecsQueryVO);
            return ptTrainJobSpecsQueryVO;
        }).collect(Collectors.toList());
        return ptTrainJobSpecsQueryList;
    }
}
