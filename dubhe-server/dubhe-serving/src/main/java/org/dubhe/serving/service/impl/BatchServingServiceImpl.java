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


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.dto.*;
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
import org.dubhe.biz.file.utils.MinioUtil;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.permission.annotation.DataPermissionMethod;
import org.dubhe.biz.permission.base.BaseService;
import org.dubhe.biz.redis.utils.RedisUtils;
import org.dubhe.cloud.authconfig.service.AdminClient;
import org.dubhe.k8s.cache.ResourceCache;
import org.dubhe.k8s.domain.dto.PodQueryDTO;
import org.dubhe.k8s.domain.vo.PodVO;
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
import org.dubhe.serving.dao.BatchServingMapper;
import org.dubhe.serving.domain.dto.*;
import org.dubhe.serving.domain.entity.BatchServing;
import org.dubhe.serving.domain.vo.*;
import org.dubhe.serving.enums.ServingErrorEnum;
import org.dubhe.serving.enums.ServingStatusEnum;
import org.dubhe.serving.service.BatchServingService;
import org.dubhe.serving.task.DeployServingAsyncTask;
import org.dubhe.serving.utils.ServingStatusDetailDescUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * @description 批量服务管理
 * @date 2020-08-26
 */
@Service
public class BatchServingServiceImpl extends ServiceImpl<BatchServingMapper, BatchServing> implements BatchServingService {

    @Resource
    private BatchServingMapper batchServingMapper;
    @Resource
    private ModelBranchClient modelBranchClient;
    @Resource
    private ModelInfoClient modelInfoClient;
    @Resource
    private AdminClient adminClient;
    @Resource
    private DeployServingAsyncTask deployServingAsyncTask;
    @Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;
    @Resource
    private MinioUtil minioUtil;
    @Resource
    private K8sNameTool k8sNameTool;
    @Resource
    private PodService podService;
    @Resource
    private RecycleService recycleService;
    @Resource
    private UserContextService userContextService;
    @Value("${minio.bucketName}")
    private String bucketName;
    @Resource
    private RecycleConfig recycleConfig;
    @Resource
    private ImageClient imageClient;
    @Resource
    private TrainHarborConfig trainHarborConfig;
    @Resource
    private AlgorithmClient algorithmClient;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private ResourceCache resourceCache;
    @Value("Task:BatchServing:"+"${spring.profiles.active}_batch_serving_id_")
    private String batchServingIdPrefix;
    /**
     * 批量服务文件根路径
     */
    @Value("${serving.batchRootPath}")
    private String batchRootPath;

    private final static List<String> FILE_NAMES;

    static {
        FILE_NAMES = ReflectionUtils.getFieldNames(BatchServingQueryVO.class);
    }

    /**
     * 批量服务查询
     *
     * @param batchServingQueryDTO 批量服务查询参数
     * @return Map<String, Object> 批量服务查询返回分页对象
     */
    @Override
    public Map<String, Object> query(BatchServingQueryDTO batchServingQueryDTO) {
        String name = batchServingQueryDTO.getName();
        //批量服务名称或id条件非空
        if (StringUtils.isNotBlank(name)) {
            //整数匹配
            if (StringConstant.PATTERN_NUM.matcher(name).matches()) {
                batchServingQueryDTO.setId(Long.parseLong(name));
                batchServingQueryDTO.setName(null);
                Map<String, Object> map = queryBatchServing(batchServingQueryDTO);
                if (((List<ServingInfoQueryVO>) map.get(StringConstant.RESULT)).size() > NumberConstant.NUMBER_0) {
                    return map;
                } else {
                    batchServingQueryDTO.setId(null);
                    batchServingQueryDTO.setName(name);
                }
            }
        }
        return queryBatchServing(batchServingQueryDTO);
    }

    /**
     * 批量服务查询
     *
     * @param batchServingQueryDTO 批量服务查询参数
     * @return Map<String, Object> 批量服务查询返回分页对象
     */
    public Map<String, Object> queryBatchServing(BatchServingQueryDTO batchServingQueryDTO) {
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        LogUtil.info(LogEnum.SERVING, "User {} queried the online service list with the query of{}", user.getUsername(), JSONObject.toJSONString(batchServingQueryDTO));

        QueryWrapper<BatchServing> wrapper = WrapperHelp.getWrapper(batchServingQueryDTO);
        //管理员可以看到全部信息
        if (!BaseService.isAdmin(user)) {
            wrapper.eq("create_user_id", user.getId());
        }
        Page page = new Page(null == batchServingQueryDTO.getCurrent() ? NumberConstant.NUMBER_1 : batchServingQueryDTO.getCurrent(),
                null == batchServingQueryDTO.getSize() ? NumberConstant.NUMBER_10 : batchServingQueryDTO.getSize());
        try {
            //排序字段，默认按更新时间降序，否则将驼峰转换为下划线
            String column = batchServingQueryDTO.getSort() != null && FILE_NAMES.contains(batchServingQueryDTO.getSort()) ? StringUtils.humpToLine(batchServingQueryDTO.getSort()) : "update_time";
            //排序方式
            boolean isAsc = StringUtils.isBlank(batchServingQueryDTO.getOrder()) || StringUtils.equals(batchServingQueryDTO.getOrder(), StringConstant.SORT_DESC) ? false : true;
            wrapper.orderBy(true, isAsc, column);
        } catch (Exception e) {
            LogUtil.error(LogEnum.SERVING, "Query online service with an exception, query info:{}，exception info:{}", JSONObject.toJSONString(batchServingQueryDTO), e);
            throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
        }
        IPage<BatchServing> batchServings = batchServingMapper.selectPage(page, wrapper);
        List<BatchServingQueryVO> queryList = batchServings.getRecords().stream().map(batchServing -> {
            BatchServingQueryVO batchServingQueryVO = new BatchServingQueryVO();
            BeanUtils.copyProperties(batchServing, batchServingQueryVO);
            return batchServingQueryVO;
        }).collect(Collectors.toList());
        LogUtil.info(LogEnum.SERVING, "User {} queried batching service list, the number of batching service is {}", user.getUsername(), queryList.size());
        return PageUtil.toPage(page, queryList);
    }

    /**
     * 创建批量服务
     *
     * @param batchServingCreateDTO 批量服务创建参数
     * @return BatchServingCreateVO 返回创建后结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public BatchServingCreateVO create(BatchServingCreateDTO batchServingCreateDTO) {
        UserContext user = userContextService.getCurUser();
        //参数校验
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        checkNameExist(batchServingCreateDTO.getName(), user.getId());
        BatchServing batchServing = new BatchServing();
        BeanUtils.copyProperties(batchServingCreateDTO, batchServing);

        String imageUrl = getImageUrl(batchServingCreateDTO.getImageName(), batchServingCreateDTO.getImageTag());
        batchServing.setImage(trainHarborConfig.getAddress() + SymbolConstant.SLASH + imageUrl);
        PtModelInfoQueryVO ptModelInfoQueryVO = getPtModelInfo(batchServingCreateDTO.getModelId());
        batchServing.setModelAddress(ptModelInfoQueryVO.getModelAddress());
        if (ptModelInfoQueryVO.getFrameType() > NumberConstant.NUMBER_4) {
            throw new BusinessException(ServingErrorEnum.MODEL_FRAME_TYPE_NOT_SUPPORTED);
        }
        checkScriptPath(batchServing);
        batchServing.setFrameType(ptModelInfoQueryVO.getFrameType());
        checkResourceType(batchServing.getFrameType(), batchServingCreateDTO.getResourcesPoolType());
        checkInputExist(batchServingCreateDTO.getInputPath());
        if (NumberConstant.NUMBER_0 == batchServing.getModelResource()) {
            PtModelBranchQueryVO ptModelBranchQueryVO = getModelBranch(batchServing.getModelBranchId());
            batchServing.setModelAddress(ptModelBranchQueryVO.getModelAddress());
        }
        checkModelAddress(batchServing.getModelAddress());
        batchServing.setStatus(ServingStatusEnum.IN_DEPLOYMENT.getStatus());
        String outputPath = ServingConstant.OUTPUT_NFS_PATH + user.getId() + File.separator + StringUtils.getTimestamp() + File.separator;
        batchServing.setOutputPath(outputPath);
        saveBatchServing(user, batchServing);
        String taskIdentify = resourceCache.getTaskIdentify(batchServing.getId(), batchServing.getName(), batchServingIdPrefix);
        deployServingAsyncTask.deployBatchServing(user, batchServing, taskIdentify);
        return new BatchServingCreateVO(batchServing.getId(), batchServing.getStatus());
    }

    /**
     * 获取推理脚本路径
     *
     * @param batchServing 批量服务信息
     */
    private void checkScriptPath(BatchServing batchServing) {
        //使用自定义推理脚本，校验脚本文件是否存在
        if (batchServing.getUseScript() && batchServing.getAlgorithmId() != null) {
            TrainAlgorithmQureyVO dataAlgorithm = getAlgorithm(batchServing.getAlgorithmId());
            //校验推理脚本是否存在
            String scriptPath = k8sNameTool.getAbsolutePath(dataAlgorithm.getCodeDir());
            if (!fileStoreApi.fileOrDirIsExist(scriptPath)) {
                throw new BusinessException(ServingErrorEnum.SCRIPT_NOT_EXIST);
            }
            batchServing.setScriptPath(dataAlgorithm.getCodeDir());
        }
    }

    /**
     * 创建批量服务并保存数据
     *
     * @param user         用户信息
     * @param batchServing 批量服务信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveBatchServing(UserContext user, BatchServing batchServing) {
        if (!save(batchServing)) {
            LogUtil.error(LogEnum.SERVING, "User {} failed to save the batching service info to the database, service name：{}", user.getUsername(), batchServing.getName());
            throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 校验资源类型
     *
     * @param frameType        模型框架
     * @param resourcePoolType 节点类型
     */
    public void checkResourceType(Integer frameType, Integer resourcePoolType) {
        // oneflow 暂不支持cpu
        if (NumberConstant.NUMBER_1 == frameType && NumberConstant.NUMBER_0 == resourcePoolType) {
            throw new BusinessException(ServingErrorEnum.CPU_NOT_SUPPORTED_BY_ONEFLOW);
        }
    }

    /**
     * 创建serving回收任务
     *
     * @param batchServing serving实体对象
     * @param recyclePath  回收文件路径
     * @param isRollBack   是否需要还原表数据
     */
    public void createRecycleTask(BatchServing batchServing, String recyclePath, boolean isRollBack) {
        RecycleCreateDTO recycleCreateDTO = RecycleCreateDTO.builder()
                .recycleModule(RecycleModuleEnum.BIZ_SERVING.getValue())
                .recycleDelayDate(recycleConfig.getServingValid())  //默认3天
                .recycleNote(RecycleTool.generateRecycleNote("删除批量服务文件", batchServing.getName(), batchServing.getId()))
                .recycleCustom(RecycleResourceEnum.BATCH_SERVING_RECYCLE_FILE.getClassName())
                .restoreCustom(RecycleResourceEnum.BATCH_SERVING_RECYCLE_FILE.getClassName())
                .build();
        recycleCreateDTO.addRecycleDetailCreateDTO(RecycleDetailCreateDTO.builder()
                .recycleCondition(recyclePath)
                .recycleType(RecycleTypeEnum.FILE.getCode())
                .recycleNote(RecycleTool.generateRecycleNote("删除批量服务文件", batchServing.getName(), batchServing.getId()))
                .remark(String.valueOf(batchServing.getId()))
                .build()
        );
        //可以还原业务表数据状态（deleted=0）
        if (isRollBack) {
            recycleCreateDTO.setRemark(String.valueOf(batchServing.getId()));
        }
        recycleService.createRecycleTask(recycleCreateDTO);
    }

    /**
     * 校验名称是否存在
     *
     * @param name   服务名称
     * @param userId 用户ID
     */
    public void checkNameExist(String name, Long userId) {
        LambdaQueryWrapper<BatchServing> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BatchServing::getName, name);
        wrapper.eq(BatchServing::getCreateUserId, userId);
        int count = batchServingMapper.selectCount(wrapper);
        if (count > NumberConstant.NUMBER_0) {
            throw new BusinessException(ServingErrorEnum.SERVING_NAME_EXIST);
        }
    }

    /**
     * 校验模型路径是否存在
     *
     * @param modelAddress 模型路径
     */
    public void checkModelAddress(String modelAddress) {
        String path = k8sNameTool.getAbsolutePath(modelAddress);
        if (!fileStoreApi.fileOrDirIsExist(path)) {
            throw new BusinessException(ServingErrorEnum.MODEL_FILE_NOT_EXIST);
        }
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
        DataResponseBody<PtModelInfoQueryVO> modelInfoPresetDataResponseBody = modelInfoClient.getByModelId(ptModelInfoQueryByIdDTO);
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
     * 更新批量服务
     *
     * @param batchServingUpdateDTO 批量服务修改参数
     * @return BatchServingUpdateVO 返回更新后结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public BatchServingUpdateVO update(BatchServingUpdateDTO batchServingUpdateDTO) {
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        BatchServing batchServing = checkBatchServingExist(batchServingUpdateDTO.getId(), user.getId());
        checkBatchServingStatus(batchServing.getStatus());
        batchServing.setStatusDetail(SymbolConstant.BRACKETS);
        deployServingAsyncTask.deleteBatchServing(user, batchServing, batchServing.getResourceInfo());
        //修改输入路径时，定时删除之前路径下的文件
        if (!batchServing.getInputPath().equals(batchServingUpdateDTO.getInputPath())) {
            createRecycleTask(batchServing, k8sNameTool.getAbsolutePath(batchServing.getInputPath()), true);
        }
        //重新校验模型
        PtModelInfoQueryVO ptModelInfoQueryVO = getPtModelInfo(batchServingUpdateDTO.getModelId());
        batchServing.setModelAddress(ptModelInfoQueryVO.getModelAddress());
        if (ptModelInfoQueryVO.getFrameType() > NumberConstant.NUMBER_4) {
            throw new BusinessException(ServingErrorEnum.MODEL_FRAME_TYPE_NOT_SUPPORTED);
        }
        batchServing.setFrameType(ptModelInfoQueryVO.getFrameType());
        batchServing.setModelAddress(ptModelInfoQueryVO.getModelAddress());
        if (NumberConstant.NUMBER_0 == batchServing.getModelResource()) {
            PtModelBranchQueryVO ptModelBranchQueryVO = getModelBranch(batchServing.getModelBranchId());
            batchServing.setModelAddress(ptModelBranchQueryVO.getModelAddress());
        }
        BeanUtils.copyProperties(batchServingUpdateDTO, batchServing);

        String imageUrl = getImageUrl(batchServingUpdateDTO.getImageName(), batchServingUpdateDTO.getImageTag());
        batchServing.setImage(trainHarborConfig.getAddress() + SymbolConstant.SLASH + imageUrl);
        checkScriptPath(batchServing);
        checkResourceType(batchServing.getFrameType(), batchServing.getResourcesPoolType());
        batchServing.setStatus(ServingStatusEnum.IN_DEPLOYMENT.getStatus());
        batchServing.setUpdateTime(DateUtil.getCurrentTimestamp());
        String outputPath = ServingConstant.OUTPUT_NFS_PATH + user.getId() + File.separator + StringUtils.getTimestamp() + File.separator;
        batchServing.setOutputPath(outputPath);
        updateBatchServing(user, batchServing);
        String taskIdentify = resourceCache.getTaskIdentify(batchServing.getId(), batchServing.getName(), batchServingIdPrefix);
        deployServingAsyncTask.deployBatchServing(user, batchServing, taskIdentify);
        return new BatchServingUpdateVO(batchServing.getId(), batchServing.getStatus());
    }

    /**
     * 获取镜像url
     *
     * @param imageName 镜像名称
     * @param imageTag 镜像标签
     * @return 镜像url
     */
    private String getImageUrl(String imageName, String imageTag) {
        PtImageQueryUrlDTO ptImageQueryUrlDTO = new PtImageQueryUrlDTO();
        ptImageQueryUrlDTO.setProjectType(ImageTypeEnum.TRAIN.getType());
        ptImageQueryUrlDTO.setImageName(imageName);
        ptImageQueryUrlDTO.setImageTag(imageTag);
        DataResponseBody<String> dataResponseBody = imageClient.getImageUrl(ptImageQueryUrlDTO);
        if (!dataResponseBody.succeed()) {
            throw new BusinessException(ServingErrorEnum.CALL_IMAGE_SERVER_FAIL);
        }
        if (StringUtils.isBlank(dataResponseBody.getData())) {
            throw new BusinessException(ServingErrorEnum.IMAGE_NOT_EXIST);
        }
        return dataResponseBody.getData();
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
        DataResponseBody<TrainAlgorithmQureyVO> algorithmResponseBody = algorithmClient.selectById(trainAlgorithmSelectByIdDTO);
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
     * 修改批量服务并保存数据
     *
     * @param user         用户信息
     * @param batchServing 批量服务信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateBatchServing(UserContext user, BatchServing batchServing) {
        int result = batchServingMapper.updateById(batchServing);
        if (result < 1) {
            LogUtil.error(LogEnum.SERVING, "User {} failed modifying the batching service in the database, service id={}", user.getUsername(), batchServing.getId());
            throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 校验批量服务是否存在
     *
     * @param id     批量服务id
     * @param userId 用户id
     * @return BatchServing 批量服务对象
     */
    BatchServing checkBatchServingExist(Long id, Long userId) {
        BatchServing batchServing = batchServingMapper.selectById(id);
        if (batchServing == null) {
            throw new BusinessException(ServingErrorEnum.SERVING_INFO_ABSENT);
        } else {
            //管理员可以看到所有用户的服务，非管理员只能看到自己创建的
            if (!BaseService.isAdmin()) {
                if (!userId.equals(batchServing.getCreateUserId())) {
                    throw new BusinessException(ServingErrorEnum.SERVING_INFO_ABSENT);
                }
            }
        }
        return batchServing;
    }

    /**
     * 校验批量服务状态是否为停止状态
     *
     * @param status 批量服务状态
     */
    void checkBatchServingStatus(String status) {
        if (ServingStatusEnum.WORKING.getStatus().equals(status) ||
                ServingStatusEnum.IN_DEPLOYMENT.getStatus().equals(status)) {
            throw new BusinessException(ServingErrorEnum.OPERATION_NOT_ALLOWED);
        }
    }

    /**
     * 校验输入图片是否为空
     *
     * @param inputPath 图片路径
     */
    void checkInputExist(String inputPath) {
        if (!fileStoreApi.fileOrDirIsExist(k8sNameTool.getAbsolutePath(inputPath))) {
            throw new BusinessException(ServingErrorEnum.INPUT_FILE_NOT_EXIST);
        }
    }

    /**
     * 删除批量服务
     *
     * @param batchServingDeleteDTO 批量服务删除参数
     * @return BatchServingDeleteVO 返回删除后结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchServingDeleteVO delete(BatchServingDeleteDTO batchServingDeleteDTO) {
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        BatchServing batchServing = checkBatchServingExist(batchServingDeleteDTO.getId(), user.getId());
        checkBatchServingStatus(batchServing.getStatus());
        deleteBatchServing(batchServingDeleteDTO, user);
        String taskIdentify = (String) redisUtils.get(batchServingIdPrefix + String.valueOf(batchServing.getId()));
        if (StringUtils.isNotEmpty(taskIdentify)){
            redisUtils.del(taskIdentify, batchServingIdPrefix + String.valueOf(batchServing.getId()));
        }
        String sourcePath = k8sNameTool.getAbsolutePath(batchRootPath + batchServing.getCreateUserId() + File.separator + batchServing.getId() + File.separator);
        String recyclePath = k8sNameTool.getAbsolutePath(batchServing.getInputPath()) + StrUtil.COMMA + k8sNameTool.getAbsolutePath(batchServing.getOutputPath()) + StrUtil.COMMA + sourcePath;
        createRecycleTask(batchServing, recyclePath, true);
        return new BatchServingDeleteVO(batchServing.getId());
    }

    /**
     * 删除批量服务并保存数据
     *
     * @param batchServingDeleteDTO 批量服务信息
     * @param user                  用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatchServing(BatchServingDeleteDTO batchServingDeleteDTO, UserContext user) {
        if (!removeById(batchServingDeleteDTO.getId())) {
            LogUtil.error(LogEnum.SERVING, "User {} failed deleting the batching service in the database, service id={}", user.getUsername(), batchServingDeleteDTO.getId());
            throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 启动批量服务
     *
     * @param batchServingStartDTO 批量服务启动参数
     * @return BatchServingStartVO 返回启动后信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchServingStartVO start(BatchServingStartDTO batchServingStartDTO) {
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        BatchServing batchServing = checkBatchServingExist(batchServingStartDTO.getId(), user.getId());
        if (StringUtils.equalsAny(batchServing.getStatus(), ServingStatusEnum.IN_DEPLOYMENT.getStatus(), ServingStatusEnum.WORKING.getStatus(), ServingStatusEnum.COMPLETED.getStatus())) {
            LogUtil.error(LogEnum.SERVING, "User {} failed starting the batching service, service id={}, service name:{}, service status：{}", user.getUsername(), batchServing.getId(), batchServing.getName(), batchServing.getStatus());
            throw new BusinessException(ServingErrorEnum.OPERATION_NOT_ALLOWED);
        }
        //删除之前的推理结果，重新推理
        createRecycleTask(batchServing, k8sNameTool.getAbsolutePath(batchServing.getOutputPath()), true);
        batchServing.setProgress(SymbolConstant.ZERO);
        batchServing.setStatus(ServingStatusEnum.IN_DEPLOYMENT.getStatus());
        //生成新的输出路径
        String outputPath = ServingConstant.OUTPUT_NFS_PATH + user.getId() + File.separator + StringUtils.getTimestamp() + File.separator;
        batchServing.setOutputPath(outputPath);
        //对重新运行的详情数据清空
        batchServing.setStatusDetail(SymbolConstant.BRACKETS);
        updateBatchServing(user, batchServing);
        String taskIdentify = resourceCache.getTaskIdentify(batchServing.getId(), batchServing.getName(), batchServingIdPrefix);
        deployServingAsyncTask.deployBatchServing(user, batchServing, taskIdentify);
        return new BatchServingStartVO(batchServing.getId(), batchServing.getStatus(), batchServing.getProgress());
    }

    /**
     * 停止批量服务
     *
     * @param batchServingStopDTO 批量服务停止参数
     * @return BatchServingStopVO 返回停止后信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchServingStopVO stop(BatchServingStopDTO batchServingStopDTO) {
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        BatchServing batchServing = checkBatchServingExist(batchServingStopDTO.getId(), user.getId());
        if (ServingStatusEnum.STOP.getStatus().equals(batchServing.getStatus()) || ServingStatusEnum.EXCEPTION.equals(batchServing.getStatus())) {
            LogUtil.error(LogEnum.SERVING, "The service is not running, user {} failed stopping the service. Service id={}, service name:{}, service status:{}",
                    user.getUsername(), batchServing.getId(), batchServing.getName(), batchServing.getStatus());
            throw new BusinessException(ServingErrorEnum.OPERATION_NOT_ALLOWED);
        }
        deployServingAsyncTask.deleteBatchServing(user, batchServing, batchServing.getResourceInfo());
        batchServing.setStatus(ServingStatusEnum.STOP.getStatus());
        updateBatchServing(user, batchServing);
        return new BatchServingStopVO(batchServing.getId(), batchServing.getStatus());
    }

    /**
     * 获取批量服务详情
     *
     * @param batchServingDetailDTO 批量服务详情参数
     * @return BatchServingDetailVO 返回批量服务详情
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public BatchServingDetailVO getDetail(BatchServingDetailDTO batchServingDetailDTO) {
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        BatchServing batchServing = checkBatchServingExist(batchServingDetailDTO.getId(), user.getId());
        BatchServingDetailVO batchServingDetailVO = new BatchServingDetailVO();
        BeanUtils.copyProperties(batchServing, batchServingDetailVO);
        PtModelInfoQueryVO ptModelInfoQueryVO = getPtModelInfo(batchServingDetailVO.getModelId());
        batchServingDetailVO.setModelName(ptModelInfoQueryVO.getName());
        batchServingDetailVO.setModelAddress(ptModelInfoQueryVO.getModelAddress());
        if (NumberConstant.NUMBER_0 == batchServing.getModelResource()) {
            PtModelBranchQueryVO ptModelBranchQueryVO = getModelBranch(batchServingDetailVO.getModelBranchId());
            batchServingDetailVO.setModelVersion(ptModelBranchQueryVO.getVersion());
        }
        //运行中的任务计算进度
        if (batchServing.getStatus().equals(ServingStatusEnum.WORKING.getStatus())) {
            String progress = queryProgressByMinIO(batchServing);
            batchServingDetailVO.setProgress(progress);
        }
        //采用自定义脚本且算法id不为空时，获取算法名称
        if (batchServing.getUseScript() && batchServing.getAlgorithmId() != null) {
            TrainAlgorithmQureyVO dataAlgorithm = getAlgorithm(batchServing.getAlgorithmId());
            batchServingDetailVO.setAlgorithmName(dataAlgorithm.getAlgorithmName());
        }
        return batchServingDetailVO;
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
        DataResponseBody<PtModelBranchQueryVO> modelBranchQueryVODataResponseBody = modelBranchClient.getByBranchId(ptModelBranchQueryByIdDTO);
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
     * 批量服务回调
     *
     * @param times 回调请求次数
     * @param req   回调请求对象
     * @return boolean 返回回调结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchServingCallback(int times, BatchServingK8sPodCallbackCreateDTO req) {

        // 根据namespace和podName找到模型配置
        String resourceInfo = k8sNameTool.getResourceInfoFromResourceName(BizEnum.BATCH_SERVING, req.getResourceName());
        if (StringUtils.isBlank(resourceInfo)) {
            LogUtil.warn(LogEnum.SERVING, "Cannot find modelConfig ID! Request: {}", Thread.currentThread(), times, req.toString());
            return false;
        }
        Long id = Long.parseLong(resourceInfo.substring(NumberConstant.NUMBER_4));
        BatchServing batchServing = batchServingMapper.selectById(id);
        if (Objects.isNull(batchServing)) {
            LogUtil.warn(LogEnum.SERVING, "Cannot find batchServing! Request: {}", Thread.currentThread(), times, req.toString());
            return false;
        }
        // 除部署中状态外，其他状态时不处理slave（子节点创建失败时不会创建主节点，所以需要处理子节点的回调）
        if (req.getPodName().contains(ServingConstant.SLAVE_POD) && !ServingStatusEnum.IN_DEPLOYMENT.getStatus().equals(batchServing.getStatus())) {
            return true;
        }
        // 对于当前状态是已完成的不处理
        if (ServingStatusEnum.COMPLETED.getStatus().equals(batchServing.getStatus())) {
            return true;
        }
        if (PodPhaseEnum.PENDING.getPhase().equals(req.getPhase())) {
            batchServing.setStatus(ServingStatusEnum.IN_DEPLOYMENT.getStatus());
        }
        if (PodPhaseEnum.RUNNING.getPhase().equals(req.getPhase())) {
            //从部署中状态转变为运行中时设置开始时间
            if (ServingStatusEnum.IN_DEPLOYMENT.getStatus().equals(batchServing.getStatus())) {
                batchServing.setStartTime(DateUtil.getCurrentTimestamp());
            }
            batchServing.setStatus(ServingStatusEnum.WORKING.getStatus());
        }
        if (PodPhaseEnum.SUCCEEDED.getPhase().equals(req.getPhase())) {
            batchServing.setEndTime(DateUtil.getCurrentTimestamp());
            batchServing.setStatus(ServingStatusEnum.COMPLETED.getStatus());
            batchServing.setProgress(String.valueOf(NumberConstant.NUMBER_100));
            // 批量推理成功异步发送短信通知
            DataResponseBody<UserDTO> userDTODataResponseBody = adminClient.getUsers(batchServing.getCreateUserId());
            if (userDTODataResponseBody.succeed() && userDTODataResponseBody.getData() != null) {
                deployServingAsyncTask.asyncSendServingMail(userDTODataResponseBody.getData().getEmail(), batchServing.getId());
            }
        }
        if (PodPhaseEnum.FAILED.getPhase().equals(req.getPhase())) {
            String progress = queryProgressByMinIO(batchServing);
            batchServing.setProgress(progress);
            batchServing.setStatus(ServingStatusEnum.EXCEPTION.getStatus());
        }
        //运行失败被删掉的服务，不修改状态
        if (PodPhaseEnum.DELETED.getPhase().equals(req.getPhase()) && !ServingStatusEnum.EXCEPTION.getStatus().equals(batchServing.getStatus())) {
            String progress = queryProgressByMinIO(batchServing);
            batchServing.setProgress(progress);
            batchServing.setStatus(ServingStatusEnum.STOP.getStatus());
        }
        if (PodPhaseEnum.UNKNOWN.getPhase().equals(req.getPhase())) {
            batchServing.setStatus(ServingStatusEnum.UNKNOWN.getStatus());
        }

        //记录回调返回的信息，若没有返回则删除已记录的信息或不记录
        String statusDetailKey = ServingStatusDetailDescUtil.getServingStatusDetailKey(ServingStatusDetailDescUtil.BULK_SERVICE_CONTAINER_INFORMATION, req.getPodName());
        //若返回的信息状态是delete并且信息为空，不予删除前次记录的异常信息
        if (StringUtils.isEmpty(req.getMessages()) && !PodPhaseEnum.DELETED.getPhase().equals(req.getPhase())) {
            batchServing.removeStatusDetail(statusDetailKey);
        } else {
            batchServing.putStatusDetail(statusDetailKey, req.getMessages());
        }
        LogUtil.info(LogEnum.SERVING, "The callback batch serving message: {} ,req message: {}", batchServing, req);
        return updateById(batchServing);
    }

    /**
     * 获取批量服务所有POD
     *
     * @param id 服务配置id
     * @return List<PodVO> 返回POD列表
     */
    @Override
    public List<PodVO> getPods(Long id) {
        BatchServing batchServing = batchServingMapper.selectById(id);
        if (batchServing == null) {
            return Collections.emptyList();
        }
        //从会话中获取用户信息
        UserContext user = userContextService.getCurUser();
        if (user == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        String namespace = k8sNameTool.getNamespace(batchServing.getCreateUserId());
        return podService.getPods(new PodQueryDTO(namespace, k8sNameTool.generateResourceName(BizEnum.BATCH_SERVING, batchServing.getResourceInfo())));
    }

    /**
     * 查询批量服务状态与进度
     *
     * @param id 批量服务id
     * @return BatchServingQueryVO 返回查询结果
     */
    @Override
    public BatchServingQueryVO queryStatusAndProgress(Long id) {
        BatchServing batchServing = batchServingMapper.selectById(id);
        if (Objects.isNull(batchServing)) {
            throw new BusinessException(ServingErrorEnum.SERVING_INFO_ABSENT);
        }
        String progress = queryProgressByMinIO(batchServing);
        return BatchServingQueryVO.builder()
                .id(id)
                .name(batchServing.getName())
                .description(batchServing.getDescription())
                .status(batchServing.getStatus())
                .statusDetail(batchServing.getStatusDetail())
                .progress(progress)
                .startTime(batchServing.getStartTime())
                .endTime(batchServing.getEndTime())
                .outputPath(batchServing.getOutputPath())
                .build();
    }

    /**
     * 通过minio查询推理进度
     *
     * @param batchServing 批量服务信息
     * @return String 返回进度结果
     */
    private String queryProgressByMinIO(BatchServing batchServing) {
        DecimalFormat df = new DecimalFormat(String.valueOf(NumberConstant.NUMBER_0));
        int inputCount = queryCount(batchServing.getInputPath());
        int outputCount = queryCount(batchServing.getOutputPath());
        String progress = String.valueOf(NumberConstant.NUMBER_0);
        if (inputCount != NumberConstant.NUMBER_0) {
            progress = df.format((float) outputCount / inputCount * NumberConstant.NUMBER_100);
        }
        return progress;
    }

    /**
     * 通过minio查询文件数量
     *
     * @param path 文件路径
     * @return int 返回文件数量
     */
    private int queryCount(String path) {
        try {
            if (!fileStoreApi.fileOrDirIsExist(k8sNameTool.getAbsolutePath(path))) {
                return 0;
            }
            return minioUtil.getCount(bucketName, path);
        } catch (Exception e) {
            LogUtil.error(LogEnum.SERVING, "query count failed by path in minio: {}, exception: {}", path, e);
        }
        return NumberConstant.NUMBER_0;
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

        if (CollectionUtils.isNotEmpty(ptModelStatusQueryDTO.getModelIds()) && CollectionUtils.isNotEmpty(ptModelStatusQueryDTO.getModelBranchIds())) {
            LogUtil.error(LogEnum.SERVING, "The modelId and modelBranchId cannot be passed in at the same time");
            throw new BusinessException("modelId和ModelBranchId不能同时传入");
        }

        QueryWrapper<BatchServing> query = new QueryWrapper<>();
        if (CollectionUtils.isNotEmpty(ptModelStatusQueryDTO.getModelIds())) {
            query.in("model_id", ptModelStatusQueryDTO.getModelIds());
        } else if (CollectionUtils.isNotEmpty(ptModelStatusQueryDTO.getModelBranchIds())) {
            query.in("model_branch_id", ptModelStatusQueryDTO.getModelBranchIds());
        } else {
            LogUtil.error(LogEnum.SERVING, "The modelId and modelBranchId set is empty at the same time");
            throw new BusinessException("模型传入参数不合法");
        }
        List<BatchServing> batchServings = batchServingMapper.selectList(query);
        for (BatchServing batchServing : batchServings) {
            if (StringUtils.equalsAny(batchServing.getStatus(), ServingStatusEnum.IN_DEPLOYMENT.getStatus(), ServingStatusEnum.WORKING.getStatus())) {
                return true;
            }
        }
        return false;
    }

    /**
     * serving批量服务数据还原
     *
     * @param dto 还原DTO对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recycleRollback(RecycleCreateDTO dto) {
        if (StrUtil.isNotBlank(dto.getRemark())) {
            batchServingMapper.updateStatusById(Long.valueOf(dto.getRemark()), false);
        }
    }
}
