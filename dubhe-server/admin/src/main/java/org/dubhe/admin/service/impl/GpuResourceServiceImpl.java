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
package org.dubhe.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.collections4.CollectionUtils;
import org.dubhe.admin.dao.GpuResourceMapper;
import org.dubhe.admin.domain.dto.*;
import org.dubhe.admin.domain.entity.GpuResource;
import org.dubhe.admin.domain.vo.GpuResourceQueryVO;
import org.dubhe.admin.service.GpuResourceService;
import org.dubhe.admin.service.UserService;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.vo.UserConfigVO;
import org.dubhe.biz.base.vo.UserGpuConfigVO;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description GPU资源管理
 * @date 2021-08-20
 */
@Service
public class GpuResourceServiceImpl implements GpuResourceService {

    @Autowired
    private GpuResourceMapper gpuResourceMapper;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private UserService userService;

    /**
     *  查询GPU资源
     * @param gpuResourceQueryDTO 查询GPU资源请求实体
     * @return List<GpuResource> gpuResourceSpecs GPU资源列表
     */
    @Override
    public Map<String, Object> getGpuResource(GpuResourceQueryDTO gpuResourceQueryDTO) {
        Page page = gpuResourceQueryDTO.toPage();
        //排序字段
        String sort = null == gpuResourceQueryDTO.getSort() ? StringConstant.ID : gpuResourceQueryDTO.getSort();
        QueryWrapper<GpuResource> queryResourceWrapper = new QueryWrapper<>();
        queryResourceWrapper.eq(gpuResourceQueryDTO.getGpuType() != null, "gpu_type", gpuResourceQueryDTO.getGpuType());
        if (StringConstant.SORT_ASC.equals(gpuResourceQueryDTO.getOrder())) {
            queryResourceWrapper.orderByAsc(StringUtils.humpToLine(sort));
        } else {
            queryResourceWrapper.orderByDesc(StringUtils.humpToLine(sort));
        }
        Page<GpuResource> pageGpuResourceResult = gpuResourceMapper.selectPage(page, queryResourceWrapper);
        //结果集处理
        //查询结果数
        page.setTotal(pageGpuResourceResult.getTotal());
        List<GpuResource> gpuResource = pageGpuResourceResult.getRecords();
        List<GpuResourceQueryVO> gpuResourceQueryVOS = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(gpuResource)) {
            gpuResourceQueryVOS = gpuResource.stream().map(x -> {
                GpuResourceQueryVO gpuResourceQueryVO = new GpuResourceQueryVO();
                BeanUtils.copyProperties(x, gpuResourceQueryVO);
                return gpuResourceQueryVO;
            }).collect(Collectors.toList());
        }
        return PageUtil.toPage(page, gpuResourceQueryVOS);
    }

    /**
     *  新增GPU资源
     * @param gpuResourceCreateDTO  新增GPU资源实体
     * @return List<Long> 新增GPU资源id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> create(GpuResourceCreateDTO gpuResourceCreateDTO) {
        UserContext curUser = userContextService.getCurUser();
        //GPU资源校验
        QueryWrapper<GpuResource> resourceWrapper = new QueryWrapper<>();
        resourceWrapper.eq("gpu_type", gpuResourceCreateDTO.getGpuType())
                .eq("gpu_model", gpuResourceCreateDTO.getGpuModel());
        if (gpuResourceMapper.selectCount(resourceWrapper) > 0) {
            throw new BusinessException("GPU资源已存在");
        }
        GpuResource gpuResource = new GpuResource();
        BeanUtils.copyProperties(gpuResourceCreateDTO, gpuResource);
        try {
            gpuResourceMapper.insert(gpuResource);
        } catch (Exception e) {
            LogUtil.error(LogEnum.SYS_ERR, "The user: {} saved the GpuResource parameters GpuResourceCreateDTO: {} was not successful. Failure reason: {}", curUser.getUsername(), gpuResourceCreateDTO, e);
            throw new BusinessException("内部错误");
        }
        return Collections.singletonList(gpuResource.getId());
    }

    /**
     *  修改GPU资源
     * @param gpuResourceUpdateDTO  修改GPU资源实体
     * @return List<Long> 修改GPU资源id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> update(GpuResourceUpdateDTO gpuResourceUpdateDTO) {
        UserContext curUser = userContextService.getCurUser();
        GpuResource gpuResource = new GpuResource();
        gpuResource.setId(gpuResourceUpdateDTO.getId());
        //规格名称校验
        QueryWrapper<GpuResource> resourceWrapper = new QueryWrapper<>();
        resourceWrapper.eq("gpu_type", gpuResourceUpdateDTO.getGpuType())
                .eq("gpu_model", gpuResourceUpdateDTO.getGpuModel()).ne("id", gpuResourceUpdateDTO.getId());
        if (gpuResourceMapper.selectCount(resourceWrapper) > 0) {
            throw new BusinessException("GPU资源已存在");
        }
        gpuResource.setGpuType(gpuResourceUpdateDTO.getGpuType()).setGpuModel(gpuResourceUpdateDTO.getGpuModel()).setK8sLabelKey(gpuResourceUpdateDTO.getK8sLabelKey());
        try {
            gpuResourceMapper.updateById(gpuResource);
        } catch (Exception e) {
            LogUtil.error(LogEnum.SYS_ERR, "The user: {} updated the GpuResource parameters gpuResourceUpdateDTO: {} was not successful. Failure reason :{}", curUser.getUsername(), gpuResourceUpdateDTO, e);
            throw new BusinessException("内部错误");
        }
        return Collections.singletonList(gpuResource.getId());
    }

    /**
     *  GPU资源删除
     * @param gpuResourceDeleteDTO GPU资源删除id集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(GpuResourceDeleteDTO gpuResourceDeleteDTO) {
        UserContext curUser = userContextService.getCurUser();
        Set<Long> idList = gpuResourceDeleteDTO.getIds();
        try {
            gpuResourceMapper.deleteBatchIds(idList);
        } catch (Exception e) {
            LogUtil.error(LogEnum.SYS_ERR, "The user: {} Deleted the ResourceSpecs parameters resourceSpecsDeleteDTO: {} was not successful. Failure reason :{}", curUser.getUsername(), gpuResourceDeleteDTO, e);
            throw new BusinessException("内部错误");
        }
    }

    /**
     *  查询GPU类型
     * @return List<string>  GPU类型列表
     */
    @Override
    public List<String> getGpuType() {
        //查询GPU类型
        QueryWrapper<GpuResource> queryGpuModelWrapper = new QueryWrapper<>();
        queryGpuModelWrapper.orderByDesc("id");
        List<GpuResource> gpuResources = gpuResourceMapper.selectList(queryGpuModelWrapper);
        List<String> gpuTypes = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(gpuResources)) {
            gpuTypes = gpuResources.stream().map(GpuResource::getGpuType).distinct().collect(Collectors.toList());
        }
        return gpuTypes;
    }

    /**
     *  查询用户GPU类型
     * @return List<string>  GPU类型列表
     */
    @Override
    public Set<String> getUserGpuType() {
        UserContext curUser = userContextService.getCurUser();
        UserConfigVO userConfig = userService.findUserConfig(curUser.getId());
        Set<String> userGpuTypes = new HashSet<>();
        if (CollectionUtils.isNotEmpty(userConfig.getGpuResources())) {
            for (UserGpuConfigVO userGpuConfig : userConfig.getGpuResources()) {
                if (userGpuConfig.getGpuLimit() > 0) {
                    userGpuTypes.add(userGpuConfig.getGpuType());
                }
            }
        }
        return userGpuTypes;
    }

    /**
     *  根据用户GPU类型查询用户GPU资源
     * @return List<GpuResource>  用户GPU资源列表
     */
    @Override
    public List<GpuResource> getUserGpuResource(UserGpuResourceQueryDTO userGpuResourceQueryDTO) {
        UserContext curUser = userContextService.getCurUser();
        UserConfigVO userConfig = userService.findUserConfig(curUser.getId());
        Set<String> userGpuModels = new HashSet<>();
        if (CollectionUtils.isNotEmpty(userConfig.getGpuResources())) {
            for (UserGpuConfigVO userGpuConfig : userConfig.getGpuResources()) {
                if (userGpuConfig.getGpuLimit() > 0 && userGpuResourceQueryDTO.getGpuType().equals(userGpuConfig.getGpuType())) {
                    userGpuModels.add(userGpuConfig.getGpuModel());
                }
            }
        }
        QueryWrapper<GpuResource> queryGpuModelWrapper = new QueryWrapper<>();
        queryGpuModelWrapper.orderByDesc("id").eq("gpu_type", userGpuResourceQueryDTO.getGpuType()).in("gpu_model", userGpuModels);
        return gpuResourceMapper.selectList(queryGpuModelWrapper);
    }
}