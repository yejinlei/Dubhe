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

package org.dubhe.serving.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.grpc.ManagedChannel;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.dto.PtImageQueryUrlDTO;
import org.dubhe.biz.base.dto.PtModelBranchQueryByIdDTO;
import org.dubhe.biz.base.dto.PtModelInfoQueryByIdDTO;
import org.dubhe.biz.base.dto.PtModelStatusQueryDTO;
import org.dubhe.biz.base.dto.TrainAlgorithmSelectByIdDTO;
import org.dubhe.biz.base.enums.BizEnum;
import org.dubhe.biz.base.enums.DatasetTypeEnum;
import org.dubhe.biz.base.enums.ImageTypeEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.DateUtil;
import org.dubhe.biz.base.utils.ReflectionUtils;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.PtModelBranchQueryVO;
import org.dubhe.biz.base.vo.PtModelInfoQueryVO;
import org.dubhe.biz.base.vo.TrainAlgorithmQureyVO;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.db.utils.WrapperHelp;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.permission.annotation.DataPermissionMethod;
import org.dubhe.biz.permission.base.BaseService;
import org.dubhe.biz.redis.utils.RedisUtils;
import org.dubhe.k8s.api.MetricsApi;
import org.dubhe.k8s.cache.ResourceCache;
import org.dubhe.k8s.domain.dto.PodQueryDTO;
import org.dubhe.k8s.domain.vo.PodVO;
import org.dubhe.k8s.domain.vo.PtPodsVO;
import org.dubhe.k8s.enums.PodPhaseEnum;
import org.dubhe.k8s.service.PodService;
import org.dubhe.k8s.utils.K8sNameTool;
import org.dubhe.recycle.config.RecycleConfig;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.domain.dto.RecycleDetailCreateDTO;
import org.dubhe.recycle.enums.RecycleModuleEnum;
import org.dubhe.recycle.enums.RecycleResourceEnum;
import org.dubhe.recycle.enums.RecycleTypeEnum;
import org.dubhe.recycle.service.RecycleService;
import org.dubhe.recycle.utils.RecycleTool;
import org.dubhe.serving.client.AlgorithmClient;
import org.dubhe.serving.client.ImageClient;
import org.dubhe.serving.client.ModelBranchClient;
import org.dubhe.serving.client.ModelInfoClient;
import org.dubhe.serving.config.TrainHarborConfig;
import org.dubhe.serving.constant.ServingConstant;
import org.dubhe.serving.dao.ServingInfoMapper;
import org.dubhe.serving.dao.ServingModelConfigMapper;
import org.dubhe.serving.domain.dto.PredictParamDTO;
import org.dubhe.serving.domain.dto.ServingInfoCreateDTO;
import org.dubhe.serving.domain.dto.ServingInfoDeleteDTO;
import org.dubhe.serving.domain.dto.ServingInfoDetailDTO;
import org.dubhe.serving.domain.dto.ServingInfoQueryDTO;
import org.dubhe.serving.domain.dto.ServingInfoUpdateDTO;
import org.dubhe.serving.domain.dto.ServingK8sDeploymentCallbackCreateDTO;
import org.dubhe.serving.domain.dto.ServingK8sPodCallbackCreateDTO;
import org.dubhe.serving.domain.dto.ServingModelConfigDTO;
import org.dubhe.serving.domain.dto.ServingStartDTO;
import org.dubhe.serving.domain.dto.ServingStopDTO;
import org.dubhe.serving.domain.entity.DataInfo;
import org.dubhe.serving.domain.entity.ServingInfo;
import org.dubhe.serving.domain.entity.ServingModelConfig;
import org.dubhe.serving.domain.vo.PredictParamVO;
import org.dubhe.serving.domain.vo.ServingConfigMetricsVO;
import org.dubhe.serving.domain.vo.ServingInfoCreateVO;
import org.dubhe.serving.domain.vo.ServingInfoDeleteVO;
import org.dubhe.serving.domain.vo.ServingInfoDetailVO;
import org.dubhe.serving.domain.vo.ServingInfoQueryVO;
import org.dubhe.serving.domain.vo.ServingInfoUpdateVO;
import org.dubhe.serving.domain.vo.ServingMetricsVO;
import org.dubhe.serving.domain.vo.ServingModelConfigVO;
import org.dubhe.serving.domain.vo.ServingPodMetricsVO;
import org.dubhe.serving.domain.vo.ServingStartVO;
import org.dubhe.serving.domain.vo.ServingStopVO;
import org.dubhe.serving.enums.ServingErrorEnum;
import org.dubhe.serving.enums.ServingRouteEventEnum;
import org.dubhe.serving.enums.ServingStatusEnum;
import org.dubhe.serving.enums.ServingTypeEnum;
import org.dubhe.serving.service.ServingLuaScriptService;
import org.dubhe.serving.service.ServingModelConfigService;
import org.dubhe.serving.service.ServingService;
import org.dubhe.serving.task.DeployServingAsyncTask;
import org.dubhe.serving.utils.GrpcClient;
import org.dubhe.serving.utils.ServingStatusDetailDescUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    private ModelInfoClient modelInfoClient;
    @Resource
    private ModelBranchClient modelBranchClient;
    @Resource
    private DeployServingAsyncTask deployServingAsyncTask;
    @Resource
    private ServingLuaScriptService servingLuaScriptService;
    @Resource
    private PodService podService;
    @Resource
    private MetricsApi metricsApi;
    @Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;
    @Resource
    private K8sNameTool k8sNameTool;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserContextService userContextService;
    @Resource
    private GrpcClient grpcClient;
    @Resource
    private ImageClient imageClient;
    @Resource
    private TrainHarborConfig trainHarborConfig;
    @Resource
    private AlgorithmClient algorithmClient;
    @Resource
    private RecycleConfig recycleConfig;
    @Resource
    private RecycleService recycleService;
    @Resource
    private RecycleTool recycleTool;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private ResourceCache resourceCache;
    @Value("Task:Serving:" + "${spring.profiles.active}_serving_id_")
    private String servingIdPrefix;

    /**
     * 在线服务文件根路径
     */
    @Value("${serving.onlineRootPath}")
    private String onlineRootPath;

    private final static List<String> FILE_NAMES;

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
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Object> query(ServingInfoQueryDTO servingInfoQueryDTO) {
        String name = servingInfoQueryDTO.getName();
        // 服务名称或id条件非空
        if (StringUtils.isNotBlank(name)) {
            // 整数匹配
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
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        LogUtil.info(LogEnum.SERVING, "User {} queried online service list, with the query {}", user.getUsername(),
                JSONObject.toJSONString(servingInfoQueryDTO));
        QueryWrapper<ServingInfo> wrapper = WrapperHelp.getWrapper(servingInfoQueryDTO);
        if (!BaseService.isAdmin(user)) {
            wrapper.eq("create_user_id", user.getId());
        }
        Page page = new Page(
                null == servingInfoQueryDTO.getCurrent() ? NumberConstant.NUMBER_1 : servingInfoQueryDTO.getCurrent(),
                null == servingInfoQueryDTO.getSize() ? NumberConstant.NUMBER_10 : servingInfoQueryDTO.getSize());
        try {
            // 排序字段，默认按更新时间降序，否则将驼峰转换为下划线
            String column = servingInfoQueryDTO.getSort() != null && FILE_NAMES.contains(servingInfoQueryDTO.getSort())
                    ? StringUtils.humpToLine(servingInfoQueryDTO.getSort())
                    : "update_time";
            // 排序方式
            boolean isAsc = !StringUtils.isBlank(servingInfoQueryDTO.getOrder())
                    && !StringUtils.equals(servingInfoQueryDTO.getOrder(), StringConstant.SORT_DESC);
            wrapper.orderBy(true, isAsc, column);
        } catch (Exception e) {
            LogUtil.error(LogEnum.SERVING,
                    "User queried online service with an exception, request info: {}，exception info: {}",
                    JSONObject.toJSONString(servingInfoQueryDTO), e);
            throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
        }
        IPage<ServingInfo> servingInfos = servingInfoMapper.selectPage(page, wrapper);
        List<ServingInfoQueryVO> queryList = servingInfos.getRecords().stream().map(servingInfo -> {
            ServingInfoQueryVO servingInfoQueryVO = new ServingInfoQueryVO();
            BeanUtils.copyProperties(servingInfo, servingInfoQueryVO);
            servingInfoQueryVO.setUrl(servingInfo.getUuid() + GATEWAY_URI_POSTFIX);
            Map<String, String> statistics = servingLuaScriptService.countCallsByServingInfoId(servingInfo.getId());
            servingInfoQueryVO.setTotalNum(statistics.getOrDefault("callCount", SymbolConstant.ZERO));
            servingInfoQueryVO.setFailNum(statistics.getOrDefault("failedCount", SymbolConstant.ZERO));
            return servingInfoQueryVO;
        }).collect(Collectors.toList());
        LogUtil.info(LogEnum.SERVING, "User {} queried online service list, online service count = {}",
                user.getUsername(), queryList.size());
        return PageUtil.toPage(page, queryList);
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
        UserContext user = userContextService.getCurUser();
        // 参数校验
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        checkNameExist(servingInfoCreateDTO.getName(), user.getId());
        // gRPC不支持灰度发布
        if (NumberConstant.NUMBER_1 == servingInfoCreateDTO.getType()
                && servingInfoCreateDTO.getModelConfigList().size() > NumberConstant.NUMBER_1) {
            throw new BusinessException(ServingErrorEnum.GRPC_PROTOCOL_NOT_SUPPORTED);
        }
        ServingInfo servingInfo = buildServingInfo(servingInfoCreateDTO);
        List<ServingModelConfig> modelConfigList = insertServing(servingInfoCreateDTO, user, servingInfo);
        // 异步部署容器
        String taskIdentify = resourceCache.getTaskIdentify(servingInfo.getId(), servingInfo.getName(), servingIdPrefix);
        deployServingAsyncTask.deployServing(user, servingInfo, modelConfigList, taskIdentify);
        return new ServingInfoCreateVO(servingInfo.getId(), servingInfo.getStatus());
    }

    /**
     * 创建服务并保存数据
     *
     * @param servingInfoCreateDTO 在线服务信息创建对象
     * @param user                 用户信息
     * @param servingInfo          在线服务信息对象
     * @return List<ServingModelConfig> 在线服务模型配置信息列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<ServingModelConfig> insertServing(ServingInfoCreateDTO servingInfoCreateDTO, UserContext user,
                                                  ServingInfo servingInfo) {
        int result = servingInfoMapper.insert(servingInfo);
        if (result < NumberConstant.NUMBER_1) {
            LogUtil.error(LogEnum.SERVING, "User {} failed saving service into into the database. Service name: {}",
                    user.getUsername(), servingInfoCreateDTO.getName());
            throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
        }
        return saveModelConfig(servingInfoCreateDTO.getModelConfigList(), servingInfo);
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
        int totalNode = servingInfoCreateDTO.getModelConfigList().stream()
                .mapToInt(ServingModelConfigDTO::getResourcesPoolNode).sum();
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
        if (servingModelConfig.getFrameType() == NumberConstant.NUMBER_1
                && servingModelConfig.getResourcesPoolType() == NumberConstant.NUMBER_0) {
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
    List<ServingModelConfig> saveModelConfig(List<ServingModelConfigDTO> modelConfigDTOList, ServingInfo servingInfo) {
        List<ServingModelConfig> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(modelConfigDTOList)) {
            throw new BusinessException(ServingErrorEnum.MODEL_CONFIG_NOT_EXIST);
        }
        // 生成4位随机字符串，作为部署id
        String deployId = StringUtils.getTimestamp();

        PtImageQueryUrlDTO ptImageQueryUrlDTO = new PtImageQueryUrlDTO();
        ptImageQueryUrlDTO.setProjectType(ImageTypeEnum.TRAIN.getType());

        for (ServingModelConfigDTO servingModelConfigDTO : modelConfigDTOList) {
            ServingModelConfig servingModelConfig = new ServingModelConfig();
            BeanUtils.copyProperties(servingModelConfigDTO, servingModelConfig);
            servingModelConfig.setServingId(servingInfo.getId());
            servingModelConfig.setDeployId(deployId);
            PtModelInfoQueryVO ptModelInfoQueryVO = getPtModelInfo(servingModelConfig.getModelId());
            // 校验框架
            if (ptModelInfoQueryVO.getFrameType() > NumberConstant.NUMBER_4) {
                throw new BusinessException(ServingErrorEnum.MODEL_FRAME_TYPE_NOT_SUPPORTED);
            }
            servingModelConfig.setFrameType(ptModelInfoQueryVO.getFrameType());
            servingModelConfig.setModelAddress(ptModelInfoQueryVO.getModelAddress());
            servingModelConfig.setModelName(ptModelInfoQueryVO.getName());
            checkResourceType(servingModelConfig);
            if (NumberConstant.NUMBER_0 == servingModelConfig.getModelResource()) {
                PtModelBranchQueryVO ptModelBranchQueryVO = getModelBranch(servingModelConfig.getModelBranchId());
                servingModelConfig.setModelAddress(ptModelBranchQueryVO.getModelAddress());
                servingModelConfig.setModelVersion(ptModelBranchQueryVO.getVersion());
            }

            ptImageQueryUrlDTO.setImageName(servingModelConfigDTO.getImageName());
            ptImageQueryUrlDTO.setImageTag(servingModelConfigDTO.getImageTag());
            DataResponseBody<String> dataResponseBody = imageClient.getImageUrl(ptImageQueryUrlDTO);
            if (!dataResponseBody.succeed()) {
                throw new BusinessException(ServingErrorEnum.CALL_IMAGE_SERVER_FAIL);
            }
            servingModelConfig
                    .setImage(trainHarborConfig.getAddress() + SymbolConstant.SLASH + dataResponseBody.getData());

            // 校验模型文件是否存在
            String path = k8sNameTool.getAbsolutePath(servingModelConfig.getModelAddress());
            if (!fileStoreApi.fileOrDirIsExist(path)) {
                throw new BusinessException(ServingErrorEnum.MODEL_FILE_NOT_EXIST);
            }
            // 使用自定义推理脚本，校验脚本文件是否存在
            if (servingModelConfig.getUseScript()) {
                TrainAlgorithmQureyVO dataAlgorithm = getAlgorithm(servingModelConfig.getAlgorithmId());
                // 校验推理脚本是否存在
                String scriptPath = k8sNameTool.getAbsolutePath(dataAlgorithm.getCodeDir());
                if (!fileStoreApi.fileOrDirIsExist(scriptPath)) {
                    throw new BusinessException(ServingErrorEnum.SCRIPT_NOT_EXIST);
                }
                servingModelConfig.setScriptPath(dataAlgorithm.getCodeDir());
            }

            if (servingModelConfigMapper.insert(servingModelConfig) < NumberConstant.NUMBER_1) {
                throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
            }
            list.add(servingModelConfig);
        }
        return list;
    }

    /**
     * 获取推理算法
     *
     * @param algorithmId 算法id
     * @return 算法信息
     */
    private TrainAlgorithmQureyVO getAlgorithm(Long algorithmId) {
        TrainAlgorithmSelectByIdDTO trainAlgorithmSelectByIdDTO = new TrainAlgorithmSelectByIdDTO();
        trainAlgorithmSelectByIdDTO.setId(algorithmId);
        DataResponseBody<TrainAlgorithmQureyVO> algorithmResponseBody = algorithmClient
                .selectById(trainAlgorithmSelectByIdDTO);
        ;
        if (!algorithmResponseBody.succeed()) {
            throw new BusinessException(ServingErrorEnum.CALL_ALGORITHM_SERVER_FAIL);
        }
        if (algorithmResponseBody.getData() == null) {
            throw new BusinessException(ServingErrorEnum.ALGORITHM_NOT_EXIST);
        }
        return algorithmResponseBody.getData();
    }

    /**
     * 获取模型信息
     *
     * @param modelId 模型id
     * @return PtModelInfoQueryVO 模型信息
     */
    private PtModelInfoQueryVO getPtModelInfo(Long modelId) {
        PtModelInfoQueryByIdDTO ptModelInfoQueryByIdDTO = new PtModelInfoQueryByIdDTO();
        ptModelInfoQueryByIdDTO.setId(modelId);
        DataResponseBody<PtModelInfoQueryVO> modelInfoPresetDataResponseBody = modelInfoClient
                .getByModelId(ptModelInfoQueryByIdDTO);
        PtModelInfoQueryVO ptModelInfoPresetQueryVO = null;
        if (modelInfoPresetDataResponseBody.succeed()) {
            ptModelInfoPresetQueryVO = modelInfoPresetDataResponseBody.getData();
        }
        if (ptModelInfoPresetQueryVO == null) {
            throw new BusinessException(ServingErrorEnum.MODEL_NOT_EXIST);
        }
        return ptModelInfoPresetQueryVO;
    }

    /**
     * 获取模型版本信息
     *
     * @param modelBranchId 模型版本id
     * @return PtModelBranchQueryVO 模型版本详情
     */
    public PtModelBranchQueryVO getModelBranch(Long modelBranchId) {
        PtModelBranchQueryByIdDTO ptModelBranchQueryByIdDTO = new PtModelBranchQueryByIdDTO();
        ptModelBranchQueryByIdDTO.setId(modelBranchId);
        DataResponseBody<PtModelBranchQueryVO> modelBranchQueryVODataResponseBody = modelBranchClient
                .getByBranchId(ptModelBranchQueryByIdDTO);
        PtModelBranchQueryVO ptModelBranchQueryVO = null;
        if (modelBranchQueryVODataResponseBody.succeed()) {
            ptModelBranchQueryVO = modelBranchQueryVODataResponseBody.getData();
        }
        if (ptModelBranchQueryVO == null) {
            throw new BusinessException(ServingErrorEnum.MODEL_NOT_EXIST);
        }
        return ptModelBranchQueryVO;
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
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        ServingInfo servingInfo = checkServingInfoExist(servingInfoUpdateDTO.getId(), user.getId());
        checkRunningNode(servingInfo.getRunningNode());
        // 如果修改了任务名，校验新的任务名是否存在
        if (!servingInfo.getName().equals(servingInfoUpdateDTO.getName())) {
            checkNameExist(servingInfoUpdateDTO.getName(), user.getId());
        }
        servingInfo.setName(servingInfoUpdateDTO.getName()).setDescription(servingInfoUpdateDTO.getDescription())
                .setType(servingInfoUpdateDTO.getType()).setStatus(ServingStatusEnum.IN_DEPLOYMENT.getStatus())
                .setUpdateTime(DateUtil.getCurrentTimestamp());
        servingInfo.setUuid(StringUtils.getUUID());
        int totalNode = servingInfoUpdateDTO.getModelConfigList().stream()
                .mapToInt(ServingModelConfigDTO::getResourcesPoolNode).sum();
        servingInfo.setTotalNode(totalNode);
        Set<Long> oldIds = servingModelConfigService.getIdsByServingId(servingInfoUpdateDTO.getId());
        // 删除去掉的模型配置信息
        if (CollectionUtils.isNotEmpty(oldIds)) {
            List<ServingModelConfig> oldModelConfigList = servingModelConfigService.listByIds(oldIds);
            if (!servingModelConfigService.removeByIds(oldIds)) {
                LogUtil.error(LogEnum.SERVING,
                        "User {} modified online service model config but failed deleting online service model config. Model config ids={}",
                        user.getUsername(), oldIds);
                throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
            }
            servingInfo.setStatusDetail(SymbolConstant.BRACKETS);
            deployServingAsyncTask.deleteServing(servingInfo, oldModelConfigList);
            // 删除拷贝的文件
            for (ServingModelConfig oldModelConfig : oldModelConfigList) {
                String recyclePath = k8sNameTool.getAbsolutePath(onlineRootPath + servingInfo.getCreateUserId() + File.separator + servingInfo.getId() + File.separator + oldModelConfig.getId());
                fileStoreApi.deleteDirOrFile(recyclePath);
            }

            // 删除路由信息
            if (ServingTypeEnum.HTTP.getType().equals(servingInfo.getType())) {
                this.notifyUpdateServingRoute(Collections.emptyList(),
                        oldModelConfigList.stream().map(ServingModelConfig::getId).collect(Collectors.toList()));
            }
        }
        List<ServingModelConfig> modelConfigList = updateServing(servingInfoUpdateDTO, user, servingInfo);
        String taskIdentify = resourceCache.getTaskIdentify(servingInfo.getId(), servingInfo.getName(), servingIdPrefix);
        // 异步部署容器
        deployServingAsyncTask.deployServing(user, servingInfo, modelConfigList, taskIdentify);
        return new ServingInfoUpdateVO(servingInfo.getId(), servingInfo.getStatus());
    }

    /**
     * 修改服务并保存数据
     *
     * @param servingInfoUpdateDTO 服务对象修改
     * @param user                 用户信息
     * @param servingInfo          在线服务信息
     * @return List<ServingModelConfig> 模型配置对象列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<ServingModelConfig> updateServing(ServingInfoUpdateDTO servingInfoUpdateDTO, UserContext user,
                                                  ServingInfo servingInfo) {
        int result = servingInfoMapper.updateById(servingInfo);
        if (result < NumberConstant.NUMBER_1) {
            LogUtil.error(LogEnum.SERVING, "User {} failed deleting online service from the database, service id={}",
                    user.getUsername(), servingInfo.getId());
            throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
        }
        return saveModelConfig(servingInfoUpdateDTO.getModelConfigList(), servingInfo);
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
        if (servingInfo == null) {
            throw new BusinessException(ServingErrorEnum.SERVING_INFO_ABSENT);
        } else {
            // 管理员可以看到所有用户的服务，非管理员只能看到自己创建的
            if (!BaseService.isAdmin()) {
                if (!userId.equals(servingInfo.getCreateUserId())) {
                    throw new BusinessException(ServingErrorEnum.SERVING_INFO_ABSENT);
                }
            }
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
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        ServingInfo servingInfo = checkServingInfoExist(servingInfoDeleteDTO.getId(), user.getId());
        List<ServingModelConfig> modelConfigList = getModelConfigByServingId(servingInfo.getId());
        deployServingAsyncTask.deleteServing(servingInfo, modelConfigList);
        deleteServing(servingInfoDeleteDTO, user, servingInfo);
        String taskIdentify = (String) redisUtils.get(servingIdPrefix + String.valueOf(servingInfo.getId()));
        if (StringUtils.isNotEmpty(taskIdentify)) {
            redisUtils.del(taskIdentify, servingIdPrefix + String.valueOf(servingInfo.getId()));
        }
        Map<String, Object> map = new HashMap<>(NumberConstant.NUMBER_2);
        map.put("serving_id", servingInfo.getId());
        if (!servingModelConfigService.removeByMap(map)) {
            LogUtil.error(LogEnum.SERVING,
                    "User {} failed update online service in the database, service id={}, service name:{}",
                    user.getUsername(), servingInfoDeleteDTO.getId(), servingInfo.getName());
            throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
        }
        servingInfo.setStatusDetail(SymbolConstant.BRACKETS);
        // 删除路由信息
        if (ServingTypeEnum.HTTP.getType().equals(servingInfo.getType())) {
            this.notifyUpdateServingRoute(Collections.emptyList(),
                    modelConfigList.stream().map(ServingModelConfig::getId).collect(Collectors.toList()));
        }
        //创建垃圾回收任务
        recycle(servingInfo, modelConfigList);
        return new ServingInfoDeleteVO(servingInfo.getId());
    }

    /**
     * 垃圾回收当前服务信息
     *
     * @param servingInfo 服务信息
     * @param modelConfigList 模型配置信息
     */
    private void recycle(ServingInfo servingInfo, List<ServingModelConfig> modelConfigList) {
        List<String> recyclePath = new ArrayList<>();
        List<String> modelConfigIds = new ArrayList<>();
        for (ServingModelConfig servingModelConfig : modelConfigList) {
            String path = k8sNameTool.getAbsolutePath(onlineRootPath + servingInfo.getCreateUserId() + File.separator + servingInfo.getId() + File.separator + servingModelConfig.getId());
            recyclePath.add(path);
            modelConfigIds.add(String.valueOf(servingModelConfig.getId()));
        }
        if (CollectionUtils.isNotEmpty(recyclePath) && CollectionUtils.isNotEmpty(modelConfigIds)) {
            createRecycleTask(servingInfo, String.join(SymbolConstant.COMMA, modelConfigIds), String.join(SymbolConstant.COMMA, recyclePath), true);
        }
    }

    /**
     * 删除服务并保存数据
     *
     * @param servingInfoDeleteDTO 服务信息删除对象
     * @param user                 用户信息
     * @param servingInfo          在线服务信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteServing(ServingInfoDeleteDTO servingInfoDeleteDTO, UserContext user, ServingInfo servingInfo) {
        int result = servingInfoMapper.deleteById(servingInfo.getId());
        if (result < NumberConstant.NUMBER_1) {
            LogUtil.error(LogEnum.SERVING,
                    "User {} failed deleting online service from the database, service id={}, service name:{}",
                    user.getUsername(), servingInfoDeleteDTO.getId(), servingInfo.getName());
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
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        ServingInfo servingInfo = checkServingInfoExist(servingInfoDetailDTO.getId(), user.getId());
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
                // 采用自定义脚本且算法id不为空时，获取算法名称
                if (servingModelConfig.getUseScript() && servingModelConfig.getAlgorithmId() != null) {
                    TrainAlgorithmQureyVO algorithmQueryVO = getAlgorithm(servingModelConfig.getAlgorithmId());
                    vo.setAlgorithmName(algorithmQueryVO.getAlgorithmName());
                }
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
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        ServingInfo servingInfo = checkServingInfoExist(servingStartDTO.getId(), user.getId());
        checkRunningNode(servingInfo.getRunningNode());
        List<ServingModelConfig> modelConfigList = getModelConfigByServingId(servingInfo.getId());
        servingInfo.setUuid(StringUtils.getUUID());
        servingInfo.setStatus(ServingStatusEnum.IN_DEPLOYMENT.getStatus());
        //对开始运行数据的详情数据清空
        servingInfo.setStatusDetail(SymbolConstant.BRACKETS);
        updateServingStart(user, servingInfo, modelConfigList);
        // 异步部署容器
        String taskIdentify = resourceCache.getTaskIdentify(servingInfo.getId(), servingInfo.getName(), servingIdPrefix);
        deployServingAsyncTask.deployServing(user, servingInfo, modelConfigList, taskIdentify);
        return new ServingStartVO(servingInfo.getId(), servingInfo.getStatus());
    }

    /**
     * 启动服务并保存数据
     *
     * @param user        用户信息
     * @param servingInfo 在线服务信息
     * @param modelConfigList 模型配置对象列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateServingStart(UserContext user, ServingInfo servingInfo, List<ServingModelConfig> modelConfigList) {
        int result = servingInfoMapper.updateById(servingInfo);
        if (result < NumberConstant.NUMBER_1) {
            LogUtil.error(LogEnum.SERVING, "User {} failed update online service in the database, service id={}",
                    user.getUsername(), servingInfo.getId());
            throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
        }
        for (ServingModelConfig servingModelConfig : modelConfigList) {
            servingModelConfig.setResourceInfo(SymbolConstant.BLANK);
            servingModelConfig.setUrl(SymbolConstant.BLANK);
            if (servingModelConfigMapper.updateById(servingModelConfig) < NumberConstant.NUMBER_1) {
                throw new BusinessException(ServingErrorEnum.DATABASE_ERROR);
            }
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
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        ServingInfo servingInfo = checkServingInfoExist(id, user.getId());
        // 校验服务状态是否为运行中
        if (servingInfo.getRunningNode() == NumberConstant.NUMBER_0) {
            throw new BusinessException(ServingErrorEnum.SERVING_NOT_WORKING);
        }
        if (StringUtils.isBlank(url)) {
            throw new BusinessException(ServingErrorEnum.SERVING_NOT_WORKING);
        }
        // 校验推理图片
        if (files == null || files.length == NumberConstant.NUMBER_0) {
            throw new BusinessException(ServingErrorEnum.PREDICT_IMAGE_EMPTY);
        }
        List<DataInfo> dataInfoList = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            // 获取文件后缀名
            if (fileName == null) {
                throw new BusinessException("文件名不能为空");
            }
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
                DataInfo imageInfo = new DataInfo();
                imageInfo.setDataName(fileName);
                imageInfo.setDataFile(base64File);
                dataInfoList.add(imageInfo);
            }
        }
        if (dataInfoList.isEmpty()) {
            throw new BusinessException(ServingErrorEnum.IMAGE_FORMAT_ERROR);
        }
        ManagedChannel channel = grpcClient.getChannel(id, url);
        return GrpcClient.getResult(channel, dataInfoList).getJsonResult();
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
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        ServingInfo servingInfo = checkServingInfoExist(servingStopDTO.getId(), user.getId());
        // 修改状态及可用节点数
        servingInfo.setStatus(ServingStatusEnum.STOP.getStatus());
        servingInfo.setRunningNode(NumberConstant.NUMBER_0);
        List<ServingModelConfig> modelConfigList = getModelConfigByServingId(servingInfo.getId());
        deployServingAsyncTask.deleteServing(servingInfo, modelConfigList);
        updateServingStop(user, servingInfo, modelConfigList);
        servingInfo.setStatusDetail(SymbolConstant.BRACKETS);
        // 删除路由信息
        if (ServingTypeEnum.HTTP.getType().equals(servingInfo.getType())) {
            this.notifyUpdateServingRoute(Collections.emptyList(),
                    modelConfigList.stream().map(ServingModelConfig::getId).collect(Collectors.toList()));
        }
        return new ServingStopVO(servingInfo.getId(), servingInfo.getStatus());
    }

    /**
     * 停止服务并保存数据
     *
     * @param user            用户信息
     * @param servingInfo     在线服务信息
     * @param modelConfigList 模型配置列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateServingStop(UserContext user, ServingInfo servingInfo, List<ServingModelConfig> modelConfigList) {
        int result = servingInfoMapper.updateById(servingInfo);
        if (result < NumberConstant.NUMBER_1) {
            LogUtil.error(LogEnum.SERVING,
                    "User {} failed stopping the online service and failed to update the service in the database. Service id={}, service name:{}, running node number:{}",
                    user.getUsername(), servingInfo.getId(), servingInfo.getName(), servingInfo.getRunningNode());
            throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
        }
        modelConfigList.forEach(servingModelConfig -> {
            servingModelConfig.setReadyReplicas(NumberConstant.NUMBER_0);
            servingModelConfigMapper.updateById(servingModelConfig);
        });
        if (ServingTypeEnum.GRPC.getType().equals(servingInfo.getType())) {
            GrpcClient.shutdownChannel(servingInfo.getId());
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
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        ServingInfo servingInfo = checkServingInfoExist(predictParamDTO.getId(), user.getId());
        PredictParamVO predictParamVO = new PredictParamVO();
        LambdaQueryWrapper<ServingModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServingModelConfig::getServingId, predictParamDTO.getId());
        List<ServingModelConfig> servingModelConfigList = servingModelConfigService.list(wrapper);
        if (CollectionUtils.isEmpty(servingModelConfigList)) {
            return predictParamVO;
        }
        // grpc协议
        if (ServingTypeEnum.GRPC.getType().equals(servingInfo.getType())) {
            predictParamVO.setRequestMethod("gRPC");
            String url = servingModelConfigList.get(0).getUrl();
            predictParamVO.setUrl(url);
            Map<String, String> inputs = new HashMap<>();
            inputs.put("DataRequest", "List<Data>");
            predictParamVO.setInputs(inputs);
            Map<String, Map<String, String>> other = new HashMap<>();
            Map<String, String> data = new HashMap<>();
            data.put("data_file", "String");
            data.put("data_name", "String");
            other.put("Data", data);
            Map<String, String> outputs = new HashMap<>();
            outputs.put("DataResponse", "String");
            predictParamVO.setOutputs(outputs);
            predictParamVO.setOther(other);
        } else if (ServingTypeEnum.HTTP.getType().equals(servingInfo.getType())) {
            String url = "http://" + servingInfo.getUuid() + GATEWAY_URI_POSTFIX
                    + ServingConstant.INFERENCE_INTERFACE_NAME;
            predictParamVO.setUrl(url);
            Map<String, String> inputs = new HashMap<>();
            inputs.put("files", "File");
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
        // 从会话中获取用户信息
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        ServingInfoDetailDTO queryDTO = new ServingInfoDetailDTO();
        queryDTO.setId(id);
        ServingInfoDetailVO detail = this.getDetail(queryDTO);
        List<ServingConfigMetricsVO> configMetricsVOS = new ArrayList<>();
        for (ServingModelConfigVO servingModelConfigVO : detail.getModelConfigList()) {
            // 获取id、模型等信息
            ServingConfigMetricsVO metricsTemp = new ServingConfigMetricsVO();
            BeanUtil.copyProperties(servingModelConfigVO, metricsTemp);
            // 获取实时监控数据
            List<PtPodsVO> podsVOS = metricsApi.getPodMetricsRealTime(
                    k8sNameTool.getNamespace(detail.getCreateUserId()),
                    k8sNameTool.generateResourceName(BizEnum.SERVING, servingModelConfigVO.getResourceInfo()));
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
            Map<String, String> statistics = servingLuaScriptService
                    .countCallsByServingConfigId(servingModelConfigVO.getId());
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
            message = ServingRouteEventEnum.SAVE.getCode() + SymbolConstant.COLON + idString
                    + SymbolConstant.EVENT_SEPARATOR;
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
            StringRecord stringRecord = StreamRecords.string(Collections.singletonMap(servingGroup, message))
                    .withStreamKey(ServingConstant.SERVING_STREAM);
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
        ServingInfo servingInfo = servingInfoMapper.selectById(modelConfig.getServingId());
        if (servingInfo == null) {
            throw new BusinessException(ServingErrorEnum.SERVING_INFO_ABSENT);
        }
        // 从会话中获取用户信息
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        String namespace = k8sNameTool.getNamespace(servingInfo.getCreateUserId());
        List<PodVO> list = podService
                .getPods(new PodQueryDTO(namespace, k8sNameTool.generateResourceName(BizEnum.SERVING, modelConfig.getResourceInfo())));
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
     * @param req 回调请求对象
     * @return boolean 返回是否回调成功
     */
    @Override
    public boolean servingDeploymentCallback(ServingK8sDeploymentCallbackCreateDTO req) {
        // 根据namespace和podName找到模型配置
        String resourceInfo = k8sNameTool.getResourceInfoFromResourceName(BizEnum.SERVING, req.getResourceName());
        if (StringUtils.isBlank(resourceInfo)) {
            LogUtil.warn(LogEnum.SERVING, "Cannot find modelConfig ID! Request: {}", req.toString());
            return false;
        }
        String idStr = resourceInfo.substring(NumberConstant.NUMBER_4);
        ServingModelConfig servingModelConfig = servingModelConfigService.getById(Long.parseLong(idStr));
        if (Objects.isNull(servingModelConfig)) {
            LogUtil.warn(LogEnum.SERVING, "Cannot find modelConfig! Request: {}", req.toString());
            return false;
        }
        ServingInfo servingInfo = servingInfoMapper.selectById(servingModelConfig.getServingId());
        if (Objects.isNull(servingInfo)) {
            LogUtil.warn(LogEnum.SERVING, "Cannot find servingInfo! Request: {}", req.toString());
            return false;
        }
        // 不处理已停止服务的回调
        if (ServingStatusEnum.STOP.getStatus().equals(servingInfo.getStatus())) {
            return true;
        }
        // 更新信息
        if (updateByCallback(req, servingModelConfig, servingInfo)) {
            return false;
        }
        // 增加发送路由信息
        if (req.getReadyReplicas() > NumberConstant.NUMBER_0
                && ServingTypeEnum.HTTP.getType().equals(servingInfo.getType())) {
            this.notifyUpdateServingRoute(Collections.singletonList(servingModelConfig.getId()),
                    Collections.emptyList());
        }
        // 增加删除路由信息
        if (req.getReadyReplicas() == NumberConstant.NUMBER_0
                && ServingTypeEnum.HTTP.getType().equals(servingInfo.getType())) {
            this.notifyUpdateServingRoute(Collections.emptyList(),
                    Collections.singletonList(servingModelConfig.getId()));
        }
        return true;
    }

    @Override
    public boolean servingPodCallback(int times, ServingK8sPodCallbackCreateDTO req) {
        // 根据namespace和podName找到模型配置
        String resourceInfo = k8sNameTool.getResourceInfoFromResourceName(BizEnum.SERVING, req.getResourceName());
        if (StringUtils.isBlank(resourceInfo)) {
            LogUtil.warn(LogEnum.SERVING, "Cannot find modelConfig ID! Request: {}", Thread.currentThread(), times, req.toString());
            return false;
        }
        Long id = Long.parseLong(resourceInfo.substring(NumberConstant.NUMBER_4));
        ServingInfo servingInfo = servingInfoMapper.selectById(id);
        if (Objects.isNull(servingInfo)) {
            LogUtil.warn(LogEnum.SERVING, "Cannot find podServing! Request: {}", Thread.currentThread(), times, req.toString());
            return false;
        }
        //记录回调返回的信息，若没有返回则删除已记录的信息或不记录
        String statusDetailKey = ServingStatusDetailDescUtil.getServingStatusDetailKey(ServingStatusDetailDescUtil.CONTAINER_INFORMATION, req.getPodName());
        //若返回的信息状态是delete并且信息为空，不予删除前次记录的异常信息
        if (StringUtils.isEmpty(req.getMessages()) && !PodPhaseEnum.DELETED.getPhase().equals(req.getPhase())) {
            servingInfo.removeStatusDetail(statusDetailKey);
        } else {
            servingInfo.putStatusDetail(statusDetailKey, req.getMessages());
        }
        LogUtil.info(LogEnum.SERVING, "The callback serving message:{} ,req message:{}", servingInfo, req);
        return servingInfoMapper.updateStatusDetail(servingInfo.getId(), servingInfo.getStatusDetail()) < NumberConstant.NUMBER_1;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateByCallback(ServingK8sDeploymentCallbackCreateDTO req, ServingModelConfig servingModelConfig,
                                    ServingInfo servingInfo) {
        // 更新当前模型配置有效节点数
        servingModelConfig.setReadyReplicas(req.getReadyReplicas());
        int result = servingModelConfigMapper.updateById(servingModelConfig);
        if (result < NumberConstant.NUMBER_1) {
            return true;
        }
        // 更新当前服务有效总节点数
        ServingModelConfig another = servingModelConfigMapper.selectAnother(servingInfo.getId(),
                servingModelConfig.getId());
        if (Objects.nonNull(another)) {
            servingInfo.setRunningNode(req.getReadyReplicas() + another.getReadyReplicas());
        } else {
            servingInfo.setRunningNode(req.getReadyReplicas());
        }
        String uniqueName = ServingStatusDetailDescUtil.getUniqueName(servingModelConfig.getModelName(), servingModelConfig.getModelVersion());
        String statusDetailKey = ServingStatusDetailDescUtil.getServingStatusDetailKey(ServingStatusDetailDescUtil.CLOUD_SERVICE_UPDATE_EXCEPTION, uniqueName);

        servingInfo.removeStatusDetail(statusDetailKey);
        // 运行中的服务回调运行节点数为0则说明服务运行失败
        if (ServingStatusEnum.WORKING.getStatus().equals(servingInfo.getStatus())
                && servingInfo.getRunningNode() == NumberConstant.NUMBER_0) {
            LogUtil.info(LogEnum.SERVING,
                    "Update to EXCEPTION status. The number of running node is {}. Current request: {}",
                    servingInfo.getRunningNode(), req);
            servingInfo.putStatusDetail(statusDetailKey, "运行中的节点数为0");
            servingInfo.setStatus(ServingStatusEnum.EXCEPTION.getStatus());
            // 删除已创建的pod
            List<ServingModelConfig> deleteList = getModelConfigByServingId(servingInfo.getId());
            deployServingAsyncTask.deleteServing(servingInfo, deleteList);
        }
        if (servingInfo.getRunningNode() > NumberConstant.NUMBER_0) {
            LogUtil.info(LogEnum.SERVING,
                    "Update to WORKING status. The number of running node is {}, Current request: {}",
                    servingInfo.getRunningNode(), req);
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
                servingModelConfigVO.setDeployParams(servingModelConfig.getDeployParams());
                List<ServingModelConfigVO> list = new ArrayList<>();
                list.add(servingModelConfigVO);
                map.put(servingModelConfig.getDeployId(), list);
            } else {
                ServingModelConfigVO servingModelConfigVO = new ServingModelConfigVO();
                BeanUtils.copyProperties(servingModelConfig, servingModelConfigVO);
                servingModelConfigVO.setDeployParams(servingModelConfig.getDeployParams());
                List<ServingModelConfigVO> list = map.get(servingModelConfig.getDeployId());
                list.add(servingModelConfigVO);
            }
        });
        return map;
    }

    /**
     * 判断模型是否正在使用
     *
     * @param ptModelStatusQueryDTO 模型查询条件
     * @return Boolean 是否在用（true：使用中；false：未使用）
     */
    @Override
    public Boolean getServingModelStatus(PtModelStatusQueryDTO ptModelStatusQueryDTO) {
        if (ptModelStatusQueryDTO == null) {
            LogUtil.error(LogEnum.SERVING, "The ptModelStatusQueryDTO set is empty");
            throw new BusinessException("模型为空");
        }

        if (CollectionUtils.isNotEmpty(ptModelStatusQueryDTO.getModelIds())
                && CollectionUtils.isNotEmpty(ptModelStatusQueryDTO.getModelBranchIds())) {
            LogUtil.error(LogEnum.SERVING, "The modelId and modelBranchId cannot be passed in at the same time");
            throw new BusinessException("modelId和ModelBranchId不能同时传入");
        }

        QueryWrapper<ServingModelConfig> query = new QueryWrapper<>();
        if (CollectionUtils.isNotEmpty(ptModelStatusQueryDTO.getModelIds())) {
            query.in("model_id", ptModelStatusQueryDTO.getModelIds());
        } else if (CollectionUtils.isNotEmpty(ptModelStatusQueryDTO.getModelBranchIds())) {
            query.in("model_branch_id", ptModelStatusQueryDTO.getModelBranchIds());
        } else {
            LogUtil.error(LogEnum.SERVING, "The modelId and modelBranchId set is empty at the same time");
            throw new BusinessException("模型传入参数不合法");
        }
        List<ServingModelConfig> servingModelConfigs = servingModelConfigMapper.selectList(query);
        if (CollectionUtils.isNotEmpty(servingModelConfigs)) {
            for (ServingModelConfig servingModelConfig : servingModelConfigs) {
                ServingInfo servingInfo = servingInfoMapper.selectById(servingModelConfig.getServingId());
                if (StringUtils.equalsAny(servingInfo.getStatus(), ServingStatusEnum.IN_DEPLOYMENT.getStatus(),
                        ServingStatusEnum.WORKING.getStatus())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * serving在线服务数据还原
     *
     * @param dto 还原DTO对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recycleRollback(RecycleCreateDTO dto) {
        if (StringUtils.isNotBlank(dto.getRemark())) {
            String[] modelConfigIds = dto.getRemark().split(SymbolConstant.COMMA);
            Long servingInfoId = null;
            for (String modelConfigId : modelConfigIds) {
                servingModelConfigMapper.updateStatusById(Long.valueOf(modelConfigId), false);
                if (servingInfoId == null) {
                    ServingModelConfig servingModelConfig = servingModelConfigMapper.selectById(modelConfigId);
                    servingInfoId = servingModelConfig.getServingId();
                }
            }
            if (servingInfoId != null) {
                servingInfoMapper.rollbackById(servingInfoId, false);
            }
        }
    }


    /**
     * 创建serving回收任务
     *
     * @param servingInfo serving服务实体信息
     * @param recyclePath  回收文件路径
     * @param isRollBack   是否需要还原表数据
     */
    public void createRecycleTask(ServingInfo servingInfo, String modelConfigIds, String recyclePath, boolean isRollBack) {
        RecycleCreateDTO recycleCreateDTO = RecycleCreateDTO.builder()
                .recycleModule(RecycleModuleEnum.BIZ_SERVING.getValue())
                .recycleDelayDate(recycleConfig.getServingValid())  //默认3天
                .recycleNote(RecycleTool.generateRecycleNote("删除在线服务文件", servingInfo.getName(), servingInfo.getId()))
                .recycleCustom(RecycleResourceEnum.SERVING_RECYCLE_FILE.getClassName())
                .restoreCustom(RecycleResourceEnum.SERVING_RECYCLE_FILE.getClassName())
                .build();
        recycleCreateDTO.addRecycleDetailCreateDTO(RecycleDetailCreateDTO.builder()
                .recycleCondition(recyclePath)
                .recycleType(RecycleTypeEnum.FILE.getCode())
                .recycleNote(RecycleTool.generateRecycleNote("删除在线服务文件", servingInfo.getName(), servingInfo.getId()))
                .remark(modelConfigIds)
                .build()
        );
        //可以还原业务表数据状态（deleted=0）
        if (isRollBack) {
            recycleCreateDTO.setRemark(modelConfigIds);
        }
        recycleService.createRecycleTask(recycleCreateDTO);
    }

}
