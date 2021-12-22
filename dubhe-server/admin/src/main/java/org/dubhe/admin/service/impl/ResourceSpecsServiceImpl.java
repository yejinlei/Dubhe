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
import org.dubhe.admin.client.SystemNodeClient;
import org.dubhe.admin.dao.ResourceSpecsMapper;
import org.dubhe.admin.dao.UserGpuConfigMapper;
import org.dubhe.admin.domain.dto.*;
import org.dubhe.admin.domain.entity.ResourceSpecs;
import org.dubhe.admin.domain.entity.UserGpuConfig;
import org.dubhe.admin.domain.vo.ResourceSpecsQueryVO;
import org.dubhe.admin.service.ResourceSpecsService;
import org.dubhe.admin.service.UserService;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.dto.QueryResourceSpecsDTO;
import org.dubhe.biz.base.dto.QueryUserK8sResourceDTO;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.QueryResourceSpecsVO;
import org.dubhe.biz.base.vo.QueryUserResourceSpecsVO;
import org.dubhe.biz.base.vo.UserConfigVO;
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
 * @description CPU, GPU, 内存等资源规格管理
 * @date 2021-05-27
 */
@Service
public class ResourceSpecsServiceImpl implements ResourceSpecsService {

    @Autowired
    private ResourceSpecsMapper resourceSpecsMapper;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private UserService userService;

    @Autowired
    private SystemNodeClient systemNodeClient;

    @Autowired
    private UserGpuConfigMapper userGpuConfigMapper;

    /**
     *  查询资源规格
     * @param resourceSpecsQueryDTO 查询资源规格请求实体
     * @return List<ResourceSpecs> resourceSpecs 资源规格列表
     */
    @Override
    public Map<String, Object> getResourceSpecs(ResourceSpecsQueryDTO resourceSpecsQueryDTO) {
        Page page = resourceSpecsQueryDTO.toPage();
        //排序字段
        String sort = null == resourceSpecsQueryDTO.getSort() ? StringConstant.ID : resourceSpecsQueryDTO.getSort();
        QueryWrapper<ResourceSpecs> queryResourceSpecsWrapper = new QueryWrapper<>();
        queryResourceSpecsWrapper.like(resourceSpecsQueryDTO.getSpecsName() != null, "specs_name", resourceSpecsQueryDTO.getSpecsName())
                .eq(resourceSpecsQueryDTO.getResourcesPoolType() != null, "resources_pool_type", resourceSpecsQueryDTO.getResourcesPoolType())
                .eq(resourceSpecsQueryDTO.getModule() != null, "module", resourceSpecsQueryDTO.getModule());
        if (resourceSpecsQueryDTO.getMultiGpu() != null) {
            if (resourceSpecsQueryDTO.getMultiGpu()) {
                queryResourceSpecsWrapper.gt("gpu_num", MagicNumConstant.ONE);
            } else {
                queryResourceSpecsWrapper.eq("gpu_num", MagicNumConstant.ONE);
            }
        }
        if (StringConstant.SORT_ASC.equals(resourceSpecsQueryDTO.getOrder())) {
            queryResourceSpecsWrapper.orderByAsc(StringUtils.humpToLine(sort));
        } else {
            queryResourceSpecsWrapper.orderByDesc(StringUtils.humpToLine(sort));
        }
        Page<ResourceSpecs> pageResourceSpecsResult = resourceSpecsMapper.selectPage(page, queryResourceSpecsWrapper);
        //结果集处理
        //查询结果数
        page.setTotal(pageResourceSpecsResult.getTotal());
        List<ResourceSpecs> resourceSpecs = pageResourceSpecsResult.getRecords();
        List<ResourceSpecsQueryVO> resourceSpecsQueryVOS = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(resourceSpecs)) {
            resourceSpecsQueryVOS = resourceSpecs.stream().map(x -> {
                ResourceSpecsQueryVO resourceSpecsQueryVO = new ResourceSpecsQueryVO();
                BeanUtils.copyProperties(x, resourceSpecsQueryVO);
                return resourceSpecsQueryVO;
            }).collect(Collectors.toList());
        }
        return PageUtil.toPage(page, resourceSpecsQueryVOS);
    }

    /**
     *  新增资源规格
     * @param resourceSpecsCreateDTO  新增资源规格实体
     * @return List<Long> 新增资源规格id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> create(ResourceSpecsCreateDTO resourceSpecsCreateDTO) {
        UserContext curUser = userContextService.getCurUser();
        //规格名称校验
        QueryWrapper<ResourceSpecs> specsWrapper = new QueryWrapper<>();
        specsWrapper.eq("specs_name", resourceSpecsCreateDTO.getSpecsName()).eq("module", resourceSpecsCreateDTO.getModule());
        if (resourceSpecsMapper.selectCount(specsWrapper) > 0) {
            LogUtil.error(LogEnum.SYS_ERR, "The module: {} resourceSpecs name ({}) already exists", resourceSpecsCreateDTO.getModule(), resourceSpecsCreateDTO.getSpecsName());
            throw new BusinessException("规格名称已存在");
        }
        ResourceSpecs resourceSpecs = new ResourceSpecs();
        BeanUtils.copyProperties(resourceSpecsCreateDTO, resourceSpecs);
        resourceSpecs.setCreateUserId(curUser.getId());
        if (resourceSpecsCreateDTO.getGpuNum() > 0) {
            resourceSpecs.setResourcesPoolType(true);
        }
        try {
            resourceSpecsMapper.insert(resourceSpecs);
        } catch (Exception e) {
            LogUtil.error(LogEnum.SYS_ERR, "The user {} saved the ResourceSpecs parameters ResourceSpecsCreateDTO: {} was not successful. Failure reason :{}", curUser, resourceSpecsCreateDTO, e);
            throw new BusinessException("内部错误");
        }
        return Collections.singletonList(resourceSpecs.getId());
    }

    /**
     *  修改资源规格
     * @param resourceSpecsUpdateDTO  修改资源规格实体
     * @return List<Long> 修改资源规格id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> update(ResourceSpecsUpdateDTO resourceSpecsUpdateDTO) {
        UserContext curUser = userContextService.getCurUser();
        ResourceSpecs resourceSpecs = new ResourceSpecs();
        resourceSpecs.setId(resourceSpecsUpdateDTO.getId()).setUpdateUserId(curUser.getId());
        if (resourceSpecsUpdateDTO.getSpecsName() != null) {
            //规格名称校验
            QueryWrapper<ResourceSpecs> specsWrapper = new QueryWrapper<>();
            specsWrapper.eq("specs_name", resourceSpecsUpdateDTO.getSpecsName()).eq("module", resourceSpecsUpdateDTO.getModule()).ne("id", resourceSpecsUpdateDTO.getId());
            if (resourceSpecsMapper.selectCount(specsWrapper) > 0) {
                LogUtil.error(LogEnum.SYS_ERR, "The module: {} resourceSpecs name ({}) already exists", resourceSpecsUpdateDTO.getModule(), resourceSpecsUpdateDTO.getSpecsName());
                throw new BusinessException("规格名称已存在");
            }
            resourceSpecs.setSpecsName(resourceSpecsUpdateDTO.getSpecsName());
        }
        if (resourceSpecsUpdateDTO.getCpuNum() != null) {
            resourceSpecs.setCpuNum(resourceSpecsUpdateDTO.getCpuNum());
        }
        if (resourceSpecsUpdateDTO.getGpuNum() != null) {
            resourceSpecs.setGpuNum(resourceSpecsUpdateDTO.getGpuNum());
            if (resourceSpecsUpdateDTO.getGpuNum() > 0) {
                resourceSpecs.setResourcesPoolType(true);
            } else {
                resourceSpecs.setResourcesPoolType(false);
            }
        }
        if (resourceSpecsUpdateDTO.getMemNum() != null) {
            resourceSpecs.setMemNum(resourceSpecsUpdateDTO.getMemNum());
        }
        if (resourceSpecsUpdateDTO.getWorkspaceRequest() != null) {
            resourceSpecs.setWorkspaceRequest(resourceSpecsUpdateDTO.getWorkspaceRequest());
        }
        try {
            resourceSpecsMapper.updateById(resourceSpecs);
        } catch (Exception e) {
            LogUtil.error(LogEnum.SYS_ERR, "The user {} updated the ResourceSpecs parameters resourceSpecsUpdateDTO: {} was not successful. Failure reason :{}", curUser, resourceSpecsUpdateDTO, e);
            throw new BusinessException("内部错误");
        }
        return Collections.singletonList(resourceSpecs.getId());
    }

    /**
     *  资源规格删除
     * @param resourceSpecsDeleteDTO 资源规格删除id集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(ResourceSpecsDeleteDTO resourceSpecsDeleteDTO) {
        UserContext curUser = userContextService.getCurUser();
        Set<Long> idList = resourceSpecsDeleteDTO.getIds();
        try {
            resourceSpecsMapper.deleteBatchIds(idList);
        } catch (Exception e) {
            LogUtil.error(LogEnum.SYS_ERR, "The user {} Deleted the ResourceSpecs parameters resourceSpecsDeleteDTO: {} was not successful. Failure reason :{}", curUser, resourceSpecsDeleteDTO, e);
            throw new BusinessException("内部错误");
        }
    }

    /**
     * 查询资源规格
     * @param queryResourceSpecsDTO 查询资源规格请求实体
     * @return QueryResourceSpecsVO 资源规格返回结果实体类
     */
    @Override
    public QueryResourceSpecsVO queryResourceSpecs(QueryResourceSpecsDTO queryResourceSpecsDTO) {
        QueryWrapper<ResourceSpecs> queryResourceSpecsWrapper = new QueryWrapper<>();
        queryResourceSpecsWrapper.eq("specs_name", queryResourceSpecsDTO.getSpecsName())
                .eq("module", queryResourceSpecsDTO.getModule());
        ResourceSpecs resourceSpecs = resourceSpecsMapper.selectOne(queryResourceSpecsWrapper);
        if (resourceSpecs == null) {
            throw new BusinessException("资源规格不存在或已被删除");
        }
        QueryResourceSpecsVO queryResourceSpecsVO = new QueryResourceSpecsVO();
        BeanUtils.copyProperties(resourceSpecs, queryResourceSpecsVO);
        return queryResourceSpecsVO;
    }


    /**
     * 查询用户资源规格
     * @param queryUserResourceSpecsDTO 查询用户资源规格请求实体
     * @return List<QueryUserResourceSpecsVO> 用户资源规格返回结果实体类集合
     */
    @Override
    public List<QueryUserResourceSpecsVO> getUserResourceSpecs(QueryUserResourceSpecsDTO queryUserResourceSpecsDTO) {
        Long userId;
        if (queryUserResourceSpecsDTO.getUserId() == null) {
            userId = userContextService.getCurUser().getId();
        } else {
            userId = queryUserResourceSpecsDTO.getUserId();
        }
        UserConfigVO userConfig = userService.findUserConfig(userId);
        if (queryUserResourceSpecsDTO.getResourcesPoolNode() == null) {
            queryUserResourceSpecsDTO.setResourcesPoolNode(MagicNumConstant.ONE);
        }
        QueryWrapper<ResourceSpecs> queryResourceSpecsWrapper = new QueryWrapper<>();
        queryResourceSpecsWrapper.eq("module", queryUserResourceSpecsDTO.getModule()).eq("resources_pool_type", queryUserResourceSpecsDTO.getResourcesPoolType())
                .le("cpu_num", userConfig.getCpuLimit()).le("mem_num", userConfig.getMemoryLimit() * MagicNumConstant.ONE_THOUSAND_TWENTY_FOUR);
        if (queryUserResourceSpecsDTO.getResourcesPoolType()) {
            if (queryUserResourceSpecsDTO.getGpuModel() == null || queryUserResourceSpecsDTO.getK8sLabelKey() == null) {
                throw new BusinessException("传参错误");
            }
            UserGpuConfig userGpuConfig = userGpuConfigMapper.selectOne(new QueryWrapper<>(new UserGpuConfig().setUserId(userId).setGpuModel(queryUserResourceSpecsDTO.getGpuModel())
                    .setK8sLabelKey(queryUserResourceSpecsDTO.getK8sLabelKey())).last(" limit 1 "));
            Integer gpuLimit = null;
            if (userGpuConfig != null) {
                gpuLimit = userGpuConfig.getGpuLimit();
            }
            // 如果老用户未初始化GPU配置，则设置默认配置
            if (userGpuConfig == null && userGpuConfigMapper.selectCountByUserId(userId) == 0) {
                UserGpuConfig preUserGpuConfig = userGpuConfigMapper.selectOne(new QueryWrapper<>(new UserGpuConfig().setUserId(0L).setGpuModel(queryUserResourceSpecsDTO.getGpuModel()).setK8sLabelKey(queryUserResourceSpecsDTO.getK8sLabelKey())));
                if (preUserGpuConfig != null) {
                    gpuLimit = preUserGpuConfig.getGpuLimit();
                }
            }
            if (gpuLimit != null) {
                queryResourceSpecsWrapper.le("gpu_num", gpuLimit);
            }
        }

        if (queryUserResourceSpecsDTO.getMultiGpu() != null) {
            if (queryUserResourceSpecsDTO.getMultiGpu()) {
                queryResourceSpecsWrapper.gt("gpu_num", MagicNumConstant.ONE);
            } else {
                queryResourceSpecsWrapper.eq("gpu_num", MagicNumConstant.ONE);
            }
        }

        queryResourceSpecsWrapper.orderByAsc("cpu_num");
        List<ResourceSpecs> resourceSpecs = resourceSpecsMapper.selectList(queryResourceSpecsWrapper);
        List<QueryUserResourceSpecsVO> queryUserResourceSpecsVOS = new ArrayList<>();
        List<QueryUserK8sResourceDTO> QueryUserK8sResources = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(resourceSpecs)) {
            QueryUserK8sResources = resourceSpecs.stream().map(x -> {
                QueryUserK8sResourceDTO queryUserK8sResourceDTO = new QueryUserK8sResourceDTO();
                BeanUtils.copyProperties(x, queryUserK8sResourceDTO);
                queryUserK8sResourceDTO.setUserId(userId).setResourcesPoolNode(queryUserResourceSpecsDTO.getResourcesPoolNode());
                if (queryUserResourceSpecsDTO.getResourcesPoolType()) {
                    queryUserK8sResourceDTO.setGpuModel(queryUserResourceSpecsDTO.getGpuModel()).setK8sLabelKey(queryUserResourceSpecsDTO.getK8sLabelKey());
                }
                return queryUserK8sResourceDTO;
            }).collect(Collectors.toList());
        }
        //过滤k8s集群资源
        if (CollectionUtils.isNotEmpty(QueryUserK8sResources)) {
            DataResponseBody<List<QueryUserResourceSpecsVO>> dataResponseBody = systemNodeClient.queryUserK8sResource(QueryUserK8sResources);
            if (!dataResponseBody.succeed()) {
                throw new BusinessException("dubhe-k8s服务调用异常，查询集群资源是否可用失败");
            }
            return dataResponseBody.getData();
        }
        return queryUserResourceSpecsVOS;
    }

    /**
     * 查询资源规格
     * @param id 资源规格id
     * @return QueryResourceSpecsVO 资源规格返回结果实体类
     */
    @Override
    public QueryResourceSpecsVO queryTadlResourceSpecs(Long id) {
        LogUtil.info(LogEnum.BIZ_SYS,"Query resource specification information with resource id:{}",id);
        ResourceSpecs resourceSpecs = resourceSpecsMapper.selectById(id);
        LogUtil.info(LogEnum.BIZ_SYS,"Obtain resource specification information:{} ",resourceSpecs);
        if (resourceSpecs == null) {
            throw new BusinessException("资源规格不存在或已被删除");
        }
        QueryResourceSpecsVO queryResourceSpecsVO = new QueryResourceSpecsVO();
        BeanUtils.copyProperties(resourceSpecs, queryResourceSpecsVO);
        LogUtil.info(LogEnum.BIZ_SYS,"Return resource specification information :{} ",queryResourceSpecsVO);
        return queryResourceSpecsVO;
    }
}