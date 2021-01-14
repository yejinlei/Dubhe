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

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.grpc.ManagedChannel;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.dubhe.annotation.DataPermissionMethod;
import org.dubhe.constant.NumberConstant;
import org.dubhe.constant.ServingConstant;
import org.dubhe.constant.StringConstant;
import org.dubhe.constant.SymbolConstant;
import org.dubhe.dao.PtModelBranchMapper;
import org.dubhe.dao.PtModelInfoMapper;
import org.dubhe.dao.ServingInfoMapper;
import org.dubhe.dao.ServingModelConfigMapper;
import org.dubhe.domain.PtModelBranch;
import org.dubhe.domain.PtModelInfo;
import org.dubhe.domain.dto.PredictParamDTO;
import org.dubhe.domain.dto.ServingInfoCreateDTO;
import org.dubhe.domain.dto.ServingInfoDeleteDTO;
import org.dubhe.domain.dto.ServingInfoDetailDTO;
import org.dubhe.domain.dto.ServingInfoQueryDTO;
import org.dubhe.domain.dto.ServingInfoUpdateDTO;
import org.dubhe.domain.dto.ServingModelConfigDTO;
import org.dubhe.domain.dto.ServingStartDTO;
import org.dubhe.domain.dto.ServingStopDTO;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.domain.entity.ImageInfo;
import org.dubhe.domain.entity.ServingInfo;
import org.dubhe.domain.entity.ServingModelConfig;
import org.dubhe.domain.vo.PredictParamVO;
import org.dubhe.domain.vo.ServingConfigMetricsVO;
import org.dubhe.domain.vo.ServingInfoCreateVO;
import org.dubhe.domain.vo.ServingInfoDeleteVO;
import org.dubhe.domain.vo.ServingInfoDetailVO;
import org.dubhe.domain.vo.ServingInfoQueryVO;
import org.dubhe.domain.vo.ServingInfoUpdateVO;
import org.dubhe.domain.vo.ServingMetricsVO;
import org.dubhe.domain.vo.ServingModelConfigVO;
import org.dubhe.domain.vo.ServingPodMetricsVO;
import org.dubhe.domain.vo.ServingStartVO;
import org.dubhe.domain.vo.ServingStopVO;
import org.dubhe.dto.callback.ServingK8sDeploymentCallbackCreateDTO;
import org.dubhe.enums.BizEnum;
import org.dubhe.enums.DatasetTypeEnum;
import org.dubhe.enums.LogEnum;
import org.dubhe.enums.ServingErrorEnum;
import org.dubhe.enums.ServingRouteEventEnum;
import org.dubhe.enums.ServingStatusEnum;
import org.dubhe.enums.ServingTypeEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.k8s.api.MetricsApi;
import org.dubhe.k8s.domain.dto.PodQueryDTO;
import org.dubhe.k8s.domain.vo.PodVO;
import org.dubhe.k8s.domain.vo.PtPodsVO;
import org.dubhe.k8s.service.PodService;
import org.dubhe.service.ServingLuaScriptService;
import org.dubhe.service.ServingModelConfigService;
import org.dubhe.service.ServingService;
import org.dubhe.task.DeployServingAsyncTask;
import org.dubhe.utils.DateUtil;
import org.dubhe.utils.GrpcClient;
import org.dubhe.utils.JwtUtils;
import org.dubhe.utils.K8sNameTool;
import org.dubhe.utils.K8sUtil;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.NfsUtil;
import org.dubhe.utils.PageUtil;
import org.dubhe.utils.ReflectionUtils;
import org.dubhe.utils.StringUtils;
import org.dubhe.utils.WrapperHelp;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * @description 在线服务管理
 * @date 2020-08-25
 */
@Service
public class ServingServiceImpl implements ServingService {

    @Value("${serving.gateway-uri-postfix}")
    private String GATEWAY_URI_POSTFIX;
    @Value("${k8s.pod.metrics.grafanaUrl}")
    private String k8sPodMetricsGrafanaUrl;
    @Value("${serving.group}")
    private String servingGroup;
    @Resource
    private ServingInfoMapper servingInfoMapper;
    @Resource
    private ServingModelConfigService servingModelConfigService;
    @Resource
    private ServingModelConfigMapper servingModelConfigMapper;
    @Resource
    private PtModelInfoMapper ptModelInfoMapper;
    @Resource
    private PtModelBranchMapper ptModelBranchMapper;
    @Resource
    private DeployServingAsyncTask deployServingAsyncTask;
    @Resource
    private K8sUtil k8sUtil;
    @Resource
    private ServingLuaScriptService servingLuaScriptService;
    @Resource
    private PodService podService;
    @Resource
    private MetricsApi metricsApi;
    @Resource
    private NfsUtil nfsUtil;
    @Resource
    private K8sNameTool k8sNameTool;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public final static List<String> FILE_NAMES;

    static {
        FILE_NAMES = ReflectionUtils.getFieldNames(ServingInfoQueryVO.class);
    }

    /**
     * 查询分页数据
     *
     * @param servingInfoQueryDTO 服务查询参数
     * @return Map<String, Object>云端服务分页对象
     */
    @Override
    public Map<String, Object> query(ServingInfoQueryDTO servingInfoQueryDTO) {
        String name = servingInfoQueryDTO.getName();
        //服务名称或id条件非空
        if (StringUtils.isNotBlank(name)) {
            //整数匹配
            if (StringConstant.PATTERN_NUM.matcher(name).matches()) {
                servingInfoQueryDTO.setId(Long.parseLong(name));
                servingInfoQueryDTO.setName(null);
                Map<String, Object> map = queryServing(servingInfoQueryDTO);
                if (((List<ServingInfoQueryVO>) map.get(StringConstant.RESULT)).size() > NumberConstant.NUMBER_0) {
                    return map;
                } else {
                    servingInfoQueryDTO.setId(null);
                    servingInfoQueryDTO.setName(name);
                }
            }
        }
        return queryServing(servingInfoQueryDTO);
    }

    /**
     * 分页查询在线服务
     *
     * @param servingInfoQueryDTO 查询条件
     * @return Map<String, Object> 分页结果
     */
    public Map<String, Object> queryServing(ServingInfoQueryDTO servingInfoQueryDTO) {
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        assert Objects.nonNull(userDTO);
        LogUtil.info(LogEnum.SERVING, "User {} queried online service list, with the query {}", userDTO.getUsername(), JSONObject.toJSONString(servingInfoQueryDTO));
        QueryWrapper<ServingInfo> wrapper = WrapperHelp.getWrapper(servingInfoQueryDTO);
        wrapper.eq("create_user_id", userDTO.getId());
        if (StringUtils.isNotBlank(servingInfoQueryDTO.getName())) {
            //按名称搜索时不区分大小写
            wrapper.like("lower(name)", servingInfoQueryDTO.getName().toLowerCase());
        }
        Page page = new Page(null == servingInfoQueryDTO.getCurrent() ? NumberConstant.NUMBER_1 : servingInfoQueryDTO.getCurrent(),
                null == servingInfoQueryDTO.getSize() ? NumberConstant.NUMBER_10 : servingInfoQueryDTO.getSize());
        try {
            //排序字段，默认按更新时间降序，否则将驼峰转换为下划线
            String column = servingInfoQueryDTO.getSort() != null && FILE_NAMES.contains(servingInfoQueryDTO.getSort())
                    ? StringUtils.humpToLine(servingInfoQueryDTO.getSort()) : "update_time";
            //排序方式
            boolean isAsc = !StringUtils.isBlank(servingInfoQueryDTO.getOrder()) && !StringUtils.equals(servingInfoQueryDTO.getOrder(), StringConstant.SORT_DESC);
            wrapper.orderBy(true, isAsc, column);
        } catch (Exception e) {
            LogUtil.error(LogEnum.SERVING, "User queried online service with an exception, request info: {}，exception info: {}", JSONObject.toJSONString(servingInfoQueryDTO), e);
            throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
        }
        IPage<ServingInfo> servingInfos = servingInfoMapper.selectPage(page, wrapper);
        List<ServingInfoQueryVO> queryVOList = servingInfos.getRecords().stream().map(servingInfo -> {
            ServingInfoQueryVO servingInfoQueryVO = new ServingInfoQueryVO();
            BeanUtils.copyProperties(servingInfo, servingInfoQueryVO);
            servingInfoQueryVO.setUrl(servingInfo.getUuid() + GATEWAY_URI_POSTFIX);
            Map<String, String> statistics = servingLuaScriptService.countCallsByServingInfoId(servingInfo.getId());
            servingInfoQueryVO.setTotalNum(statistics.getOrDefault("callCount", SymbolConstant.ZERO));
            servingInfoQueryVO.setFailNum(statistics.getOrDefault("failedCount", SymbolConstant.ZERO));
            return servingInfoQueryVO;
        }).collect(Collectors.toList());
        LogUtil.info(LogEnum.SERVING, "User {} queried online service list, online service count = {}", userDTO.getUsername(), queryVOList.size());
        return PageUtil.toPage(page, queryVOList);
    }

    /**
     * 创建服务
     *
     * @param servingInfoCreateDTO 服务创建参数
     * @return ServingInfoCreateVO 服务创建返回对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public ServingInfoCreateVO create(ServingInfoCreateDTO servingInfoCreateDTO) {
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        //参数校验
        assert Objects.nonNull(userDTO);
        checkNameExist(servingInfoCreateDTO.getName(), userDTO.getId());
        //gRPC不支持灰度发布
        if (NumberConstant.NUMBER_1 == servingInfoCreateDTO.getType() && servingInfoCreateDTO.getModelConfigList().size() > NumberConstant.NUMBER_1) {
            throw new BusinessException(ServingErrorEnum.GRPC_PROTOCOL_NOT_SUPPORTED);
        }
        ServingInfo servingInfo = buildServingInfo(servingInfoCreateDTO);
        List<ServingModelConfig> modelConfigList = insertServing(servingInfoCreateDTO, userDTO, servingInfo);
        //异步部署容器
        deployServingAsyncTask.deployServing(userDTO, servingInfo, modelConfigList);
        return new ServingInfoCreateVO(servingInfo.getId(), servingInfo.getStatus());
    }

    /**
     * 创建服务并保存数据
     *
     * @param servingInfoCreateDTO 在线服务信息创建对象
     * @param userDTO              用户信息
     * @param servingInfo          在线服务信息对象
     * @return List<ServingModelConfig> 在线服务模型配置信息列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<ServingModelConfig> insertServing(ServingInfoCreateDTO servingInfoCreateDTO, UserDTO userDTO, ServingInfo servingInfo) {
        int result = servingInfoMapper.insert(servingInfo);
        if (result < NumberConstant.NUMBER_1) {
            LogUtil.error(LogEnum.SERVING, "User {} failed saving service into into the database. Service name: {}", userDTO.getUsername(), servingInfoCreateDTO.getName());
            throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
        }
        return saveModelConfig(servingInfoCreateDTO.getModelConfigList(), servingInfo, userDTO.getUsername());
    }


    /**
     * 校验名称是否存在
     *
     * @param name   服务名称
     * @param userId 用户ID
     */
    public void checkNameExist(String name, Long userId) {
        LambdaQueryWrapper<ServingInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServingInfo::getName, name);
        wrapper.eq(ServingInfo::getCreateUserId, userId);
        int count = servingInfoMapper.selectCount(wrapper);
        if (count > NumberConstant.NUMBER_0) {
            throw new BusinessException(ServingErrorEnum.SERVING_NAME_EXIST);
        }
    }

    /**
     * 构建服务信息
     *
     * @param servingInfoCreateDTO 服务创建参数
     * @return ServingInfo 服务信息
     */
    public ServingInfo buildServingInfo(ServingInfoCreateDTO servingInfoCreateDTO) {
        ServingInfo servingInfo = new ServingInfo();
        BeanUtils.copyProperties(servingInfoCreateDTO, servingInfo);
        servingInfo.setUuid(StringUtils.getUUID());
        servingInfo.setStatus(ServingStatusEnum.IN_DEPLOYMENT.getStatus());
        int totalNode = servingInfoCreateDTO.getModelConfigList().stream().mapToInt(ServingModelConfigDTO::getResourcesPoolNode).sum();
        servingInfo.setTotalNode(totalNode);
        return servingInfo;
    }

    /**
     * 校验资源类型
     *
     * @param servingModelConfig 模型配置
     */
    public void checkResourceType(ServingModelConfig servingModelConfig) {
        // oneflow 暂不支持cpu
        if (servingModelConfig.getFrameType() == NumberConstant.NUMBER_1 && servingModelConfig.getResourcesPoolType() == NumberConstant.NUMBER_0) {
            throw new BusinessException(ServingErrorEnum.CPU_NOT_SUPPORTED_BY_ONEFLOW);
        }
    }

    /**
     * 保存模型配置对象列表
     *
     * @param modelConfigDTOList 模型配置列表
     * @param servingInfo        服务信息
     * @return List<ServingModelConfig> 模型配置对象列表
     */
    List<ServingModelConfig> saveModelConfig(List<ServingModelConfigDTO> modelConfigDTOList, ServingInfo servingInfo, String userName) {
        List<ServingModelConfig> list = new ArrayList<>();
        //生成4位随机字符串，作为部署id
        String deployId = StringUtils.getTimestamp();
        for (ServingModelConfigDTO servingModelConfigDTO : modelConfigDTOList) {
            ServingModelConfig servingModelConfig = new ServingModelConfig();
            BeanUtils.copyProperties(servingModelConfigDTO, servingModelConfig);
            servingModelConfig.setServingId(servingInfo.getId());
            servingModelConfig.setDeployId(deployId);
            PtModelInfo ptModelInfo = ptModelInfoMapper.selectById(servingModelConfig.getModelId());
            //校验框架
            Integer frameType = getFrameType(ptModelInfo, servingModelConfigDTO.getModelAddress());
            servingModelConfig.setFrameType(frameType);
            checkResourceType(servingModelConfig);
            LambdaQueryWrapper<PtModelBranch> wrapper = new LambdaQueryWrapper();
            wrapper.eq(PtModelBranch::getParentId, servingModelConfigDTO.getModelId());
            wrapper.eq(PtModelBranch::getModelAddress, servingModelConfigDTO.getModelAddress());
            PtModelBranch ptModelBranch = ptModelBranchMapper.selectOne(wrapper);
            if (ptModelBranch != null) {
                servingModelConfig.setModelVersion(ptModelBranch.getVersionNum());
            }
            servingModelConfig.setModelName(ptModelInfo.getName());
            String path = k8sUtil.getAbsoluteNfsPath(servingModelConfig.getModelAddress());
            if (nfsUtil.fileOrDirIsEmpty(path)) {
                throw new BusinessException(ServingErrorEnum.MODEL_FILE_NOT_EXIST);
            }
            if (servingModelConfigMapper.insert(servingModelConfig) < NumberConstant.NUMBER_1) {
                throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
            }
            list.add(servingModelConfig);
        }
        return list;
    }

    /**
     * 获取模型框架
     *
     * @param ptModelInfo  模型信息
     * @param modelAddress 模型路径
     * @return 模型框架
     */
    public Integer getFrameType(PtModelInfo ptModelInfo, String modelAddress) {
        String path = k8sUtil.getAbsoluteNfsPath(modelAddress);
        if (ptModelInfo == null || nfsUtil.fileOrDirIsEmpty(path)) {
            throw new BusinessException(ServingErrorEnum.MODEL_FILE_NOT_EXIST);
        }
        if (ptModelInfo.getFrameType() > NumberConstant.NUMBER_4) {
            throw new BusinessException(ServingErrorEnum.MODEL_FRAME_TYPE_NOT_SUPPORTED);
        }
        return ptModelInfo.getFrameType();
    }

    /**
     * 在线服务修改
     *
     * @param servingInfoUpdateDTO 服务对象修改
     * @return ServingInfoUpdateVO 返回修改后的状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public ServingInfoUpdateVO update(ServingInfoUpdateDTO servingInfoUpdateDTO) {
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        assert Objects.nonNull(userDTO);
        ServingInfo servingInfo = checkServingInfoExist(servingInfoUpdateDTO.getId(), userDTO.getId());
        checkRunningNode(servingInfo.getRunningNode());
        //如果修改了任务名，校验新的任务名是否存在
        if (!servingInfo.getName().equals(servingInfoUpdateDTO.getName())) {
            checkNameExist(servingInfoUpdateDTO.getName(), userDTO.getId());
        }
        servingInfo.setName(servingInfoUpdateDTO.getName())
                .setDescription(servingInfoUpdateDTO.getDescription())
                .setType(servingInfoUpdateDTO.getType())
                .setStatus(ServingStatusEnum.IN_DEPLOYMENT.getStatus())
                .setUpdateTime(DateUtil.getCurrentTimestamp());
        servingInfo.setUuid(StringUtils.getUUID());
        int totalNode = servingInfoUpdateDTO.getModelConfigList().stream().mapToInt(ServingModelConfigDTO::getResourcesPoolNode).sum();
        servingInfo.setTotalNode(totalNode);
        Set<Long> oldIds = servingModelConfigService.getIdsByServingId(servingInfoUpdateDTO.getId());
        //删除去掉的模型配置信息
        if (CollectionUtils.isNotEmpty(oldIds)) {
            if (!servingModelConfigService.removeByIds(oldIds)) {
                LogUtil.error(LogEnum.SERVING, "User {} modified online service model config but failed deleting online service model config. Model config ids={}", userDTO.getUsername(), oldIds);
                throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
            }
            List<ServingModelConfig> oldModelConfigList = servingModelConfigService.listByIds(oldIds);
            deployServingAsyncTask.deleteServing(userDTO, servingInfo, oldModelConfigList);
            //删除路由信息
            if (ServingTypeEnum.HTTP.getType().equals(servingInfo.getType())) {
                this.notifyUpdateServingRoute(Collections.emptyList(), oldModelConfigList.stream().map(ServingModelConfig::getId).collect(Collectors.toList()));
            }
        }
        List<ServingModelConfig> modelConfigList = updateServing(servingInfoUpdateDTO, userDTO, servingInfo);
        //异步部署容器
        deployServingAsyncTask.deployServing(userDTO, servingInfo, modelConfigList);
        return new ServingInfoUpdateVO(servingInfo.getId(), servingInfo.getStatus());
    }

    /**
     * 修改服务并保存数据
     *
     * @param servingInfoUpdateDTO 服务对象修改
     * @param userDTO              用户信息
     * @param servingInfo          在线服务信息
     * @return List<ServingModelConfig> 模型配置对象列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<ServingModelConfig> updateServing(ServingInfoUpdateDTO servingInfoUpdateDTO, UserDTO userDTO, ServingInfo servingInfo) {
        int result = servingInfoMapper.updateById(servingInfo);
        if (result < NumberConstant.NUMBER_1) {
            LogUtil.error(LogEnum.SERVING, "User {} failed deleting online service from the database, service id={}", userDTO.getUsername(), servingInfo.getId());
            throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
        }
        return saveModelConfig(servingInfoUpdateDTO.getModelConfigList(), servingInfo, userDTO.getUsername());
    }

    /**
     * 校验是否有节点在运行
     *
     * @param runningNode 运行节点数
     */
    void checkRunningNode(Integer runningNode) {
        if (runningNode != null && runningNode > NumberConstant.NUMBER_0) {
            throw new BusinessException(ServingErrorEnum.OPERATION_NOT_ALLOWED);
        }
    }

    /**
     * 校验服务信息是否存在
     *
     * @param id     服务id
     * @param userId 用户id
     * @return ServingInfo 服务信息对象
     */
    ServingInfo checkServingInfoExist(Long id, Long userId) {
        ServingInfo servingInfo = servingInfoMapper.selectById(id);
        //校验服务是否存在，或是否为当前用户创建
        if (servingInfo == null || (!userId.equals(servingInfo.getCreateUserId()))) {
            throw new BusinessException(ServingErrorEnum.SERVING_INFO_ABSENT);
        }
        return servingInfo;
    }

    /**
     * 在线服务删除
     *
     * @param servingInfoDeleteDTO 服务对象删除
     * @return ServingInfoDeleteVO 返回删除对象的id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServingInfoDeleteVO delete(ServingInfoDeleteDTO servingInfoDeleteDTO) {
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        assert Objects.nonNull(userDTO);
        ServingInfo servingInfo = checkServingInfoExist(servingInfoDeleteDTO.getId(), userDTO.getId());
        List<ServingModelConfig> modelConfigList = getModelConfigByServingId(servingInfo.getId());
        deleteServing(servingInfoDeleteDTO, userDTO, servingInfo);
        Map<String, Object> map = new HashMap<>(NumberConstant.NUMBER_2);
        map.put("serving_id", servingInfo.getId());
        if (!servingModelConfigService.removeByMap(map)) {
            LogUtil.error(LogEnum.SERVING, "User {} failed update online service in the database, service id={}, service name:{}",
                    userDTO.getUsername(), servingInfoDeleteDTO.getId(), servingInfo.getName());
            throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
        }
        deployServingAsyncTask.deleteServing(userDTO, servingInfo, modelConfigList);
        //删除路由信息
        if (ServingTypeEnum.HTTP.getType().equals(servingInfo.getType())) {
            this.notifyUpdateServingRoute(Collections.emptyList(), modelConfigList.stream().map(ServingModelConfig::getId).collect(Collectors.toList()));
        }
        return new ServingInfoDeleteVO(servingInfo.getId());
    }

    /**
     * 删除服务并保存数据
     *
     * @param servingInfoDeleteDTO 服务信息删除对象
     * @param userDTO              用户信息
     * @param servingInfo          在线服务信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteServing(ServingInfoDeleteDTO servingInfoDeleteDTO, UserDTO userDTO, ServingInfo servingInfo) {
        int result = servingInfoMapper.deleteById(servingInfo.getId());
        if (result < NumberConstant.NUMBER_1) {
            LogUtil.error(LogEnum.SERVING, "User {} failed deleting online service from the database, service id={}, service name:{}",
                    userDTO.getUsername(), servingInfoDeleteDTO.getId(), servingInfo.getName());
            throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 在线服务获取详情
     *
     * @param servingInfoDetailDTO 获取服务详情参数
     * @return ServingInfoDetailVO 返回详情对象
     */
    @Override
    public ServingInfoDetailVO getDetail(ServingInfoDetailDTO servingInfoDetailDTO) {
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        assert Objects.nonNull(userDTO);
        ServingInfo servingInfo = checkServingInfoExist(servingInfoDetailDTO.getId(), userDTO.getId());
        Map<String, Object> map = new HashMap<>(NumberConstant.NUMBER_2);
        map.put("serving_id", servingInfo.getId());
        List<ServingModelConfig> servingModelConfigList = servingModelConfigService.listByMap(map);
        ServingInfoDetailVO servingInfoDetailVO = new ServingInfoDetailVO();
        BeanUtils.copyProperties(servingInfo, servingInfoDetailVO);
        List<ServingModelConfigVO> servingModelConfigVOList = new ArrayList<>();
        List<Long> configIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(servingModelConfigList)) {
            for (ServingModelConfig servingModelConfig : servingModelConfigList) {
                ServingModelConfigVO vo = new ServingModelConfigVO();
                BeanUtils.copyProperties(servingModelConfig, vo);
                servingModelConfigVOList.add(vo);
                configIdList.add(servingModelConfig.getId());
            }
        }
        servingInfoDetailVO.setModelConfigList(servingModelConfigVOList);
        // 获取调用次数
        Map<String, String> countCalls = servingLuaScriptService.countCalls(configIdList);
        servingInfoDetailVO.setTotalNum(countCalls.getOrDefault("callCount", SymbolConstant.ZERO));
        servingInfoDetailVO.setFailNum(countCalls.getOrDefault("failedCount", SymbolConstant.ZERO));
        return servingInfoDetailVO;
    }

    /**
     * 启动在线服务
     *
     * @param servingStartDTO 启动服务参数
     * @return ServingStartVO 返回启动后对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServingStartVO start(ServingStartDTO servingStartDTO) {
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        assert Objects.nonNull(userDTO);
        ServingInfo servingInfo = checkServingInfoExist(servingStartDTO.getId(), userDTO.getId());
        checkRunningNode(servingInfo.getRunningNode());
        List<ServingModelConfig> modelConfigList = getModelConfigByServingId(servingInfo.getId());
        servingInfo.setUuid(StringUtils.getUUID());
        servingInfo.setStatus(ServingStatusEnum.IN_DEPLOYMENT.getStatus());
        updateServingStart(userDTO, servingInfo);
        //异步部署容器
        deployServingAsyncTask.deployServing(userDTO, servingInfo, modelConfigList);
        return new ServingStartVO(servingInfo.getId(), servingInfo.getStatus());
    }

    /**
     * 启动服务并保存数据
     *
     * @param userDTO     用户信息
     * @param servingInfo 在线服务信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateServingStart(UserDTO userDTO, ServingInfo servingInfo) {
        int result = servingInfoMapper.updateById(servingInfo);
        if (result < NumberConstant.NUMBER_1) {
            LogUtil.error(LogEnum.SERVING, "User {} failed update online service in the database, service id={}", userDTO.getUsername(), servingInfo.getId());
            throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 在线服务推理
     *
     * @param id    预测服务ID
     * @param url   预测地址
     * @param files 需要预测的图片文件
     * @return String 返回推理结果
     */
    @Override
    public String predict(Long id, String url, MultipartFile[] files) {
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        assert Objects.nonNull(userDTO);
        ServingInfo servingInfo = checkServingInfoExist(id, userDTO.getId());
        //校验服务状态是否为运行中
        if (servingInfo.getRunningNode() == NumberConstant.NUMBER_0) {
            throw new BusinessException(ServingErrorEnum.SERVING_NOT_WORKING);
        }
        if (StringUtils.isBlank(url)) {
            throw new BusinessException(ServingErrorEnum.SERVING_NOT_WORKING);
        }
        //校验推理图片
        if (files == null || files.length == NumberConstant.NUMBER_0) {
            throw new BusinessException(ServingErrorEnum.PREDICT_IMAGE_EMPTY);
        }
        List<ImageInfo> imageInfoList = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            //获取文件后缀名
            assert Objects.nonNull(fileName);
            int begin = fileName.lastIndexOf(SymbolConstant.DOT);
            String suffix = fileName.substring(begin).toLowerCase();
            if (!ServingConstant.IMAGE_FORMAT.contains(suffix)) {
                throw new BusinessException(ServingErrorEnum.IMAGE_FORMAT_ERROR);
            }
            String base64File;
            try {
                base64File = Base64.encodeBase64String(file.getBytes());
            } catch (Exception e) {
                throw new BusinessException(ServingErrorEnum.IMAGE_CONVERT_BASE64_FAIL);
            }
            if (StringUtils.isNotBlank(base64File)) {
                ImageInfo imageInfo = new ImageInfo();
                imageInfo.setImageName(fileName);
                imageInfo.setImageFile(base64File);
                imageInfoList.add(imageInfo);
            }
        }
        if (imageInfoList.isEmpty()) {
            throw new BusinessException(ServingErrorEnum.IMAGE_FORMAT_ERROR);
        }
        ManagedChannel channel = GrpcClient.getChannel(id, url);
        return GrpcClient.getResult(channel, imageInfoList).getJsonResult();
    }

    /**
     * 在线服务停止
     *
     * @param servingStopDTO 停止服务参数
     * @return ServingStopVO 返回停止后对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServingStopVO stop(ServingStopDTO servingStopDTO) {
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        assert Objects.nonNull(userDTO);
        ServingInfo servingInfo = checkServingInfoExist(servingStopDTO.getId(), userDTO.getId());
        //修改状态及可用节点数
        servingInfo.setStatus(ServingStatusEnum.STOP.getStatus());
        servingInfo.setRunningNode(NumberConstant.NUMBER_0);
        List<ServingModelConfig> modelConfigList = getModelConfigByServingId(servingInfo.getId());
        updateServingStop(userDTO, servingInfo, modelConfigList);
        deployServingAsyncTask.deleteServing(userDTO, servingInfo, modelConfigList);
        //删除路由信息
        if (ServingTypeEnum.HTTP.getType().equals(servingInfo.getType())) {
            this.notifyUpdateServingRoute(Collections.emptyList(), modelConfigList.stream().map(ServingModelConfig::getId).collect(Collectors.toList()));
        }
        return new ServingStopVO(servingInfo.getId(), servingInfo.getStatus());
    }

    /**
     * 停止服务并保存数据
     *
     * @param userDTO         用户信息
     * @param servingInfo     在线服务信息
     * @param modelConfigList 模型配置列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateServingStop(UserDTO userDTO, ServingInfo servingInfo, List<ServingModelConfig> modelConfigList) {
        int result = servingInfoMapper.updateById(servingInfo);
        if (result < NumberConstant.NUMBER_1) {
            LogUtil.error(LogEnum.SERVING, "User {} failed stopping the online service and failed to update the service in the database. Service id={}, service name:{}, running node number:{}",
                    userDTO.getUsername(), servingInfo.getId(), servingInfo.getName(), servingInfo.getRunningNode());
            throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
        }
        modelConfigList.forEach(servingModelConfig -> {
            servingModelConfig.setReadyReplicas(NumberConstant.NUMBER_0);
            servingModelConfigMapper.updateById(servingModelConfig);
        });
        if (ServingTypeEnum.GRPC.getType().equals(servingInfo.getType())) {
            GrpcClient.shutdownChannel(servingInfo.getId(), userDTO);
        }
    }

    /**
     * 获取推理参数
     *
     * @param predictParamDTO 获取预测参数服务
     * @return PredictParamVO 返回推理参数对象
     */
    @Override
    public PredictParamVO getPredictParam(PredictParamDTO predictParamDTO) {
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        assert Objects.nonNull(userDTO);
        ServingInfo servingInfo = checkServingInfoExist(predictParamDTO.getId(), userDTO.getId());
        PredictParamVO predictParamVO = new PredictParamVO();
        LambdaQueryWrapper<ServingModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServingModelConfig::getServingId, predictParamDTO.getId());
        List<ServingModelConfig> servingModelConfigList = servingModelConfigService.list(wrapper);
        if (CollectionUtils.isEmpty(servingModelConfigList)) {
            return predictParamVO;
        }
        //grpc协议
        if (ServingTypeEnum.GRPC.getType().equals(servingInfo.getType())) {
            predictParamVO.setRequestMethod("gRPC");
            String url = servingModelConfigList.get(0).getUrl();
            predictParamVO.setUrl(url);
            Map<String, String> inputs = new HashMap<>();
            inputs.put("DataRequest", "List<Image>");
            predictParamVO.setInputs(inputs);
            Map<String, Map<String, String>> other = new HashMap<>();
            Map<String, String> image = new HashMap<>();
            image.put("image_file", "String");
            image.put("image_name", "String");
            other.put("Image", image);
            Map<String, String> outputs = new HashMap<>();
            outputs.put("DataResponse", "String");
            predictParamVO.setOutputs(outputs);
            predictParamVO.setOther(other);
        } else if (ServingTypeEnum.HTTP.getType().equals(servingInfo.getType())) {
            String url = "http://" + servingInfo.getUuid() + GATEWAY_URI_POSTFIX + ServingConstant.INFERENCE_INTERFACE_NAME;
            predictParamVO.setUrl(url);
            Map<String, String> inputs = new HashMap<>();
            inputs.put("image_files", "File");
            predictParamVO.setInputs(inputs);

            Map<String, String> outputs = new HashMap<>();
            outputs.put("label", "String");
            outputs.put("probability", "Float");
            predictParamVO.setOutputs(outputs);
            predictParamVO.setRequestMethod(SymbolConstant.POST);
        } else {
            throw new BusinessException(ServingErrorEnum.PROTOCOL_NOT_SUPPORTED);
        }
        return predictParamVO;
    }

    /**
     * 根据servingId获取模型配置列表
     *
     * @param servingId 在线服务id
     * @return List<ServingModelConfig> 模型配置列表
     */
    public List<ServingModelConfig> getModelConfigByServingId(Long servingId) {
        LambdaQueryWrapper<ServingModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServingModelConfig::getServingId, servingId);
        return servingModelConfigService.list(wrapper);
    }

    /**
     * 获取容器监控信息
     *
     * @param id 模型部署信息id
     * @return ServingMetricsVO 返回监控信息对象
     */
    @Override
    public ServingMetricsVO getMetricsDetail(Long id) {
        //从会话中获取用户信息
        UserDTO user = JwtUtils.getCurrentUserDto();
        ServingInfoDetailDTO queryDTO = new ServingInfoDetailDTO();
        queryDTO.setId(id);
        ServingInfoDetailVO detail = this.getDetail(queryDTO);
        List<ServingConfigMetricsVO> configMetricsVOS = new ArrayList<>();
        for (ServingModelConfigVO servingModelConfigVO : detail.getModelConfigList()) {
            // 获取id、模型等信息
            ServingConfigMetricsVO metricsTemp = new ServingConfigMetricsVO();
            BeanUtil.copyProperties(servingModelConfigVO, metricsTemp);
            // 获取实时监控数据
            assert Objects.nonNull(user);
            List<PtPodsVO> podsVOS = metricsApi.getPodMetricsRealTime(k8sUtil.getNamespace(user), k8sUtil.getResourceName(servingModelConfigVO.getResourceInfo()));
            if (CollectionUtils.isNotEmpty(podsVOS)) {
                List<ServingPodMetricsVO> podMetricsVOS = podsVOS.stream().map(podsVO -> {
                    ServingPodMetricsVO servingPodMetricsVO = new ServingPodMetricsVO();
                    BeanUtil.copyProperties(podsVO, servingPodMetricsVO);
                    servingPodMetricsVO.setGrafanaUrl(k8sPodMetricsGrafanaUrl.concat(podsVO.getPodName()));
                    return servingPodMetricsVO;
                }).collect(Collectors.toList());
                metricsTemp.setPodList(podMetricsVOS);
            } else {
                metricsTemp.setPodList(Collections.emptyList());
            }
            // 接口调用信息
            Map<String, String> statistics = servingLuaScriptService.countCallsByServingConfigId(servingModelConfigVO.getId());
            metricsTemp.setTotalNum(statistics.getOrDefault("callCount", SymbolConstant.ZERO));
            metricsTemp.setFailNum(statistics.getOrDefault("failedCount", SymbolConstant.ZERO));
            configMetricsVOS.add(metricsTemp);
        }
        return new ServingMetricsVO().setId(id).setServingConfigList(configMetricsVOS);
    }

    /**
     * 新增和删除在线服务路由通知
     *
     * @param saveIdList   新增的路由ID列表
     * @param deleteIdList 删除的路由ID列表
     */
    @Override
    public void notifyUpdateServingRoute(List<Long> saveIdList, List<Long> deleteIdList) {
        String message = StringUtils.EMPTY;
        // 处理新增路由消息
        if (CollectionUtils.isNotEmpty(saveIdList)) {
            String idString = StringUtils.join(saveIdList.toArray(), SymbolConstant.COMMA);
            message = ServingRouteEventEnum.SAVE.getCode() + SymbolConstant.COLON + idString + SymbolConstant.EVENT_SEPARATOR;
            LogUtil.info(LogEnum.SERVING, "Serving start success and seed notify {}", message);
        }
        // 处理删除路由消息
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            String idString = StringUtils.join(deleteIdList.toArray(), SymbolConstant.COMMA);
            message = message + ServingRouteEventEnum.DELETE.getCode() + SymbolConstant.COLON + idString;
            LogUtil.info(LogEnum.SERVING, "Serving stop success and seed notify {}", message);
        }
        if (StringUtils.isNotBlank(message)) {
            LogUtil.info(LogEnum.SERVING, "Start send message to stream with notify {}", message);
            StringRecord stringRecord = StreamRecords.string(Collections.singletonMap(servingGroup, message)).withStreamKey(ServingConstant.SERVING_STREAM);
            stringRedisTemplate.opsForStream().add(stringRecord);
        }
    }

    /**
     * 获取在线服务所有POD
     *
     * @param id 服务配置id
     * @return List<PodVO> 返回POD列表
     */
    @Override
    public List<PodVO> getPods(Long id) {
        ServingModelConfig modelConfig = servingModelConfigService.getById(id);
        if (modelConfig == null) {
            return Collections.emptyList();
        }
        //从会话中获取用户信息
        UserDTO user = JwtUtils.getCurrentUserDto();
        assert Objects.nonNull(user);
        String nameSpace = k8sUtil.getNamespace(user);
        List<PodVO> list = podService.getPods(new PodQueryDTO(nameSpace, k8sUtil.getResourceName(modelConfig.getResourceInfo())));
        list.sort((pod1, pod2) -> {
            Integer pod1Index = Integer.parseInt(pod1.getDisplayName().substring(NumberConstant.NUMBER_3));
            Integer pod2Index = Integer.parseInt(pod2.getDisplayName().substring(NumberConstant.NUMBER_3));
            return pod1Index - pod2Index;
        });
        return list;
    }

    /**
     * 在线服务回调
     *
     * @param times 回调请求次数
     * @param req   回调请求对象
     * @return boolean 返回是否回调成功
     */
    @Override
    public boolean servingCallback(int times, ServingK8sDeploymentCallbackCreateDTO req) {
        // 根据namespace和podName找到模型配置
        String resourceInfo = k8sNameTool.getResourceInfoFromResourceName(BizEnum.SERVING, req.getResourceName());
        if (StringUtils.isBlank(resourceInfo)) {
            LogUtil.warn(LogEnum.SERVING, "Cannot find modelConfig ID! Request: {}", Thread.currentThread(), times, req.toString());
            return false;
        }
        String idStr = resourceInfo.substring(NumberConstant.NUMBER_4);
        ServingModelConfig servingModelConfig = servingModelConfigService.getById(Long.parseLong(idStr));
        if (Objects.isNull(servingModelConfig)) {
            LogUtil.warn(LogEnum.SERVING, "Cannot find modelConfig! Request: {}", Thread.currentThread(), times, req.toString());
            return false;
        }
        ServingInfo servingInfo = servingInfoMapper.selectById(servingModelConfig.getServingId());
        if (Objects.isNull(servingInfo)) {
            LogUtil.warn(LogEnum.SERVING, "Cannot find servingInfo! Request: {}", Thread.currentThread(), times, req.toString());
            return false;
        }
        //不处理已停止服务的回调
        if (ServingStatusEnum.STOP.getStatus().equals(servingInfo.getStatus())) {
            return false;
        }
        //更新信息
        if (updateByCallback(req, servingModelConfig, servingInfo)) {
            return false;
        }
        //增加发送路由信息
        if (req.getReadyReplicas() > NumberConstant.NUMBER_0 && ServingTypeEnum.HTTP.getType().equals(servingInfo.getType())) {
            this.notifyUpdateServingRoute(Collections.singletonList(servingModelConfig.getId()), Collections.emptyList());
        }
        //增加删除路由信息
        if (req.getReadyReplicas() == NumberConstant.NUMBER_0 && ServingTypeEnum.HTTP.getType().equals(servingInfo.getType())) {
            this.notifyUpdateServingRoute(Collections.emptyList(), Collections.singletonList(servingModelConfig.getId()));
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateByCallback(ServingK8sDeploymentCallbackCreateDTO req, ServingModelConfig servingModelConfig, ServingInfo servingInfo) {
        //更新当前模型配置有效节点数
        servingModelConfig.setReadyReplicas(req.getReadyReplicas());
        int result = servingModelConfigMapper.updateById(servingModelConfig);
        if (result < NumberConstant.NUMBER_1) {
            return true;
        }
        //更新当前服务有效总节点数
        ServingModelConfig another = servingModelConfigMapper.selectAnother(servingInfo.getId(), servingModelConfig.getId());
        if (Objects.nonNull(another)) {
            servingInfo.setRunningNode(req.getReadyReplicas() + another.getReadyReplicas());
        } else {
            servingInfo.setRunningNode(req.getReadyReplicas());
        }
        //运行中的服务回调运行节点数为0则说明服务运行失败
        if (ServingStatusEnum.WORKING.getStatus().equals(servingInfo.getStatus()) && servingInfo.getRunningNode() == NumberConstant.NUMBER_0) {
            servingInfo.setStatus(ServingStatusEnum.EXCEPTION.getStatus());
        }
        if (servingInfo.getRunningNode() > NumberConstant.NUMBER_0) {
            servingInfo.setStatus(ServingStatusEnum.WORKING.getStatus());
        }
        servingInfo.setUpdateTime(DateUtil.getCurrentTimestamp());
        return servingInfoMapper.updateById(servingInfo) < NumberConstant.NUMBER_1;
    }

    /**
     * 获取在线服务回滚信息列表
     *
     * @param servingId 在线服务id
     * @return Map<String, List < ServingModelConfigVO>> 返回回滚信息列表
     */
    @Override
    public Map<String, List<ServingModelConfigVO>> getRollbackList(Long servingId) {
        List<ServingModelConfig> servingModelConfigList = servingModelConfigMapper.getRollbackList(servingId);
        Map<String, List<ServingModelConfigVO>> map = new HashMap<>();
        if (CollectionUtils.isEmpty(servingModelConfigList)) {
            return map;
        }
        servingModelConfigList.forEach(servingModelConfig -> {
            if (!map.containsKey(servingModelConfig.getDeployId())) {
                ServingModelConfigVO servingModelConfigVO = new ServingModelConfigVO();
                BeanUtils.copyProperties(servingModelConfig, servingModelConfigVO);
                List<ServingModelConfigVO> list = new ArrayList<>();
                list.add(servingModelConfigVO);
                map.put(servingModelConfig.getDeployId(), list);
            } else {
                ServingModelConfigVO servingModelConfigVO = new ServingModelConfigVO();
                BeanUtils.copyProperties(servingModelConfig, servingModelConfigVO);
                List<ServingModelConfigVO> list = map.get(servingModelConfig.getDeployId());
                list.add(servingModelConfigVO);
            }
        });
        return map;
    }

}
