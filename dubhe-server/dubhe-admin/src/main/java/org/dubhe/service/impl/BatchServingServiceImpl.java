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

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.annotation.DataPermissionMethod;
import org.dubhe.constant.NumberConstant;
import org.dubhe.constant.ServingConstant;
import org.dubhe.constant.StringConstant;
import org.dubhe.constant.SymbolConstant;
import org.dubhe.dao.BatchServingMapper;
import org.dubhe.dao.PtModelBranchMapper;
import org.dubhe.dao.PtModelInfoMapper;
import org.dubhe.domain.PtModelBranch;
import org.dubhe.domain.PtModelInfo;
import org.dubhe.domain.dto.BatchServingCreateDTO;
import org.dubhe.domain.dto.BatchServingDeleteDTO;
import org.dubhe.domain.dto.BatchServingDetailDTO;
import org.dubhe.domain.dto.BatchServingQueryDTO;
import org.dubhe.domain.dto.BatchServingStartDTO;
import org.dubhe.domain.dto.BatchServingStopDTO;
import org.dubhe.domain.dto.BatchServingUpdateDTO;
import org.dubhe.domain.dto.RecycleTaskCreateDTO;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.domain.entity.BatchServing;
import org.dubhe.domain.vo.BatchServingCreateVO;
import org.dubhe.domain.vo.BatchServingDeleteVO;
import org.dubhe.domain.vo.BatchServingDetailVO;
import org.dubhe.domain.vo.BatchServingQueryVO;
import org.dubhe.domain.vo.BatchServingStartVO;
import org.dubhe.domain.vo.BatchServingStopVO;
import org.dubhe.domain.vo.BatchServingUpdateVO;
import org.dubhe.domain.vo.ServingInfoQueryVO;
import org.dubhe.dto.callback.BatchServingK8sPodCallbackCreateDTO;
import org.dubhe.enums.BizEnum;
import org.dubhe.enums.DatasetTypeEnum;
import org.dubhe.enums.LogEnum;
import org.dubhe.enums.RecycleModuleEnum;
import org.dubhe.enums.RecycleResourceEnum;
import org.dubhe.enums.RecycleTypeEnum;
import org.dubhe.enums.ServingErrorEnum;
import org.dubhe.enums.ServingStatusEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.k8s.domain.dto.PodQueryDTO;
import org.dubhe.k8s.domain.vo.PodVO;
import org.dubhe.k8s.enums.PodPhaseEnum;
import org.dubhe.k8s.service.PodService;
import org.dubhe.service.BatchServingService;
import org.dubhe.service.RecycleTaskService;
import org.dubhe.service.UserService;
import org.dubhe.task.DeployServingAsyncTask;
import org.dubhe.utils.DateUtil;
import org.dubhe.utils.JwtUtils;
import org.dubhe.utils.K8sNameTool;
import org.dubhe.utils.K8sUtil;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.MinioUtil;
import org.dubhe.utils.NfsUtil;
import org.dubhe.utils.PageUtil;
import org.dubhe.utils.ReflectionUtils;
import org.dubhe.utils.StringUtils;
import org.dubhe.utils.WrapperHelp;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
    private PtModelInfoMapper ptModelInfoMapper;

    @Resource
    private PtModelBranchMapper ptModelBranchMapper;

    @Resource
    private DeployServingAsyncTask deployServingAsyncTask;

    @Resource
    private NfsUtil nfsUtil;

    @Resource
    private K8sUtil k8sUtil;

    @Resource
    private MinioUtil minioUtil;

    @Resource
    private K8sNameTool k8sNameTool;

    @Resource
    private PodService podService;

    @Resource
    private RecycleTaskService recycleTaskService;

    @Resource
    private UserService userService;

    @Value("${minio.bucketName}")
    private String bucketName;


    public final static List<String> FILE_NAMES;

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
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        assert Objects.nonNull(userDTO);
        LogUtil.info(LogEnum.SERVING, "User {} queried the online service list with the query of{}", userDTO.getUsername(), JSONObject.toJSONString(batchServingQueryDTO));

        QueryWrapper<BatchServing> wrapper = WrapperHelp.getWrapper(batchServingQueryDTO);
        wrapper.eq("create_user_id", userDTO.getId());
        if (StringUtils.isNotBlank(batchServingQueryDTO.getName())) {
            //按名称搜索时不区分大小写
            wrapper.like("lower(name)", batchServingQueryDTO.getName().toLowerCase());
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
        List<BatchServingQueryVO> queryVOList = batchServings.getRecords().stream().map(batchServing -> {
            BatchServingQueryVO batchServingQueryVO = new BatchServingQueryVO();
            BeanUtils.copyProperties(batchServing, batchServingQueryVO);
            return batchServingQueryVO;
        }).collect(Collectors.toList());
        LogUtil.info(LogEnum.SERVING, "User {} queried batching service list, the number of batching service is {}", userDTO.getUsername(), queryVOList.size());
        return PageUtil.toPage(page, queryVOList);
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
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        //参数校验
        assert Objects.nonNull(userDTO);
        checkNameExist(batchServingCreateDTO.getName(), userDTO.getId());
        Integer frameType = getFrameType(batchServingCreateDTO.getModelId(), batchServingCreateDTO.getModelAddress());
        checkResourceType(frameType, batchServingCreateDTO.getResourcesPoolType());
        checkInputExist(batchServingCreateDTO.getInputPath());
        BatchServing batchServing = new BatchServing();
        BeanUtils.copyProperties(batchServingCreateDTO, batchServing);
        batchServing.setStatus(ServingStatusEnum.IN_DEPLOYMENT.getStatus());
        batchServing.setFrameType(frameType);
        String outputPath = ServingConstant.OUTPUT_NFS_PATH + userDTO.getId() + SymbolConstant.SLASH + StringUtils.getTimestamp() + SymbolConstant.SLASH;
        batchServing.setOutputPath(outputPath);
        saveBatchServing(userDTO, batchServing);
        //输入文件定时清理
        createRecycleTask(batchServing.getInputPath(), NumberConstant.NUMBER_1);
        deployServingAsyncTask.deployBatchServing(userDTO, batchServing);
        return new BatchServingCreateVO(batchServing.getId(), batchServing.getStatus());
    }

    /**
     * 创建批量服务并保存数据
     *
     * @param userDTO 用户信息
     * @param batchServing 批量服务信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveBatchServing(UserDTO userDTO, BatchServing batchServing) {
        if (!save(batchServing)) {
            LogUtil.error(LogEnum.SERVING, "User {} failed to save the batching service info to the database, service name：{}", userDTO.getUsername(), batchServing.getName());
            throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 校验资源类型
     *
     * @param frameType 模型框架
     * @param resourcePoolType 节点类型
     */
    public void checkResourceType(Integer frameType, Integer resourcePoolType) {
        // oneflow 暂不支持cpu
        if (NumberConstant.NUMBER_1 == frameType && NumberConstant.NUMBER_0 == resourcePoolType) {
            throw new BusinessException(ServingErrorEnum.CPU_NOT_SUPPORTED_BY_ONEFLOW);
        }
    }

    /**
     * 文件定时清理
     *
     * @param path 文件路径
     * @param day  保留天数
     */
    public void createRecycleTask(String path, Integer day) {
        RecycleTaskCreateDTO recycleTaskCreateDTO = new RecycleTaskCreateDTO()
                .setRecycleCustom(RecycleResourceEnum.SERVING_RECYCLE_FILE.getClassName())
                .setRecycleCondition(k8sUtil.getAbsoluteNfsPath(path))
                .setRecycleDelayDate(day)
                .setRecycleType(RecycleTypeEnum.FILE.getCode())
                .setRecycleModule(RecycleModuleEnum.BIZ_SERVING.getValue())
                .setRecycleNote(RecycleResourceEnum.SERVING_RECYCLE_FILE.getMessage());
        recycleTaskService.createRecycleTask(recycleTaskCreateDTO);
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
     * 获取模型框架
     *
     * @param modelId      模型ID
     * @param modelAddress 模型路径
     * @return 模型框架
     */
    public Integer getFrameType(Long modelId, String modelAddress) {
        PtModelInfo ptModelInfo = ptModelInfoMapper.selectById(modelId);
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
     * 更新批量服务
     *
     * @param batchServingUpdateDTO 批量服务修改参数
     * @return BatchServingUpdateVO 返回更新后结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public BatchServingUpdateVO update(BatchServingUpdateDTO batchServingUpdateDTO) {
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        assert Objects.nonNull(userDTO);
        BatchServing batchServing = checkBatchServingExist(batchServingUpdateDTO.getId(), userDTO.getId());
        checkBatchServingStatus(batchServing.getStatus());
        //修改输入路径时，定时删除之前路径下的文件
        if (!batchServing.getInputPath().equals(batchServingUpdateDTO.getInputPath())) {
            createRecycleTask(batchServing.getInputPath(), NumberConstant.NUMBER_1);
        }
        createRecycleTask(batchServing.getOutputPath(), NumberConstant.NUMBER_1);
        //修改模型或版本时，重新校验模型
        if (!(batchServing.getModelId().equals(batchServingUpdateDTO.getModelId()) &&
                batchServing.getModelAddress().equals(batchServingUpdateDTO.getModelAddress()))) {
            Integer frameType = getFrameType(batchServingUpdateDTO.getModelId(), batchServingUpdateDTO.getModelAddress());
            batchServing.setFrameType(frameType);
        }
        checkResourceType(batchServing.getFrameType(), batchServing.getResourcesPoolType());
        BeanUtils.copyProperties(batchServingUpdateDTO, batchServing);
        batchServing.setStatus(ServingStatusEnum.IN_DEPLOYMENT.getStatus());
        batchServing.setUpdateTime(DateUtil.getCurrentTimestamp());
        String outputPath = ServingConstant.OUTPUT_NFS_PATH + userDTO.getId() + SymbolConstant.SLASH + StringUtils.getTimestamp() + SymbolConstant.SLASH;
        batchServing.setOutputPath(outputPath);
        updateBatchServing(userDTO, batchServing);
        deployServingAsyncTask.deleteBatchServing(userDTO, batchServing);
        deployServingAsyncTask.deployBatchServing(userDTO, batchServing);
        createRecycleTask(batchServingUpdateDTO.getInputPath(), NumberConstant.NUMBER_1);
        return new BatchServingUpdateVO(batchServing.getId(), batchServing.getStatus());
    }

    /**
     * 修改批量服务并保存数据
     *
     * @param userDTO 用户信息
     * @param batchServing 批量服务信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateBatchServing(UserDTO userDTO, BatchServing batchServing) {
        int result = batchServingMapper.updateById(batchServing);
        if (result < 1) {
            LogUtil.error(LogEnum.SERVING, "User {} failed modifying the batching service in the database, service id={}", userDTO.getUsername(), batchServing.getId());
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
        //校验服务是否存在，或是否为当前用户创建
        if (batchServing == null || (!userId.equals(batchServing.getCreateUserId()))) {
            throw new BusinessException(ServingErrorEnum.SERVING_INFO_ABSENT);
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
        if (nfsUtil.fileOrDirIsEmpty(k8sUtil.getAbsoluteNfsPath(inputPath))) {
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
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        assert Objects.nonNull(userDTO);
        BatchServing batchServing = checkBatchServingExist(batchServingDeleteDTO.getId(), userDTO.getId());
        checkBatchServingStatus(batchServing.getStatus());
        deleteBatchServing(batchServingDeleteDTO, userDTO);
        createRecycleTask(batchServing.getInputPath(), NumberConstant.NUMBER_1);
        createRecycleTask(batchServing.getOutputPath(), NumberConstant.NUMBER_1);
        return new BatchServingDeleteVO(batchServing.getId());
    }

    /**
     * 删除批量服务并保存数据
     *
     * @param batchServingDeleteDTO 批量服务信息
     * @param userDTO               用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatchServing(BatchServingDeleteDTO batchServingDeleteDTO, UserDTO userDTO) {
        if (!removeById(batchServingDeleteDTO.getId())) {
            LogUtil.error(LogEnum.SERVING, "User {} failed deleting the batching service in the database, service id={}", userDTO.getUsername(), batchServingDeleteDTO.getId());
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
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        assert Objects.nonNull(userDTO);
        BatchServing batchServing = checkBatchServingExist(batchServingStartDTO.getId(), userDTO.getId());
        if (StringUtils.equalsAny(batchServing.getStatus(), ServingStatusEnum.IN_DEPLOYMENT.getStatus(), ServingStatusEnum.WORKING.getStatus(), ServingStatusEnum.COMPLETED.getStatus())) {
            LogUtil.error(LogEnum.SERVING, "User {} failed starting the batching service, service id={}, service name:{}, service status：{}", userDTO.getUsername(), batchServing.getId(), batchServing.getName(), batchServing.getStatus());
            throw new BusinessException(ServingErrorEnum.OPERATION_NOT_ALLOWED);
        }
        //删除之前的推理结果，重新推理
        createRecycleTask(batchServing.getOutputPath(), NumberConstant.NUMBER_1);
        batchServing.setProgress(SymbolConstant.ZERO);
        batchServing.setStatus(ServingStatusEnum.IN_DEPLOYMENT.getStatus());
        //生成新的输出路径
        String outputPath = ServingConstant.OUTPUT_NFS_PATH + userDTO.getId() + SymbolConstant.SLASH + StringUtils.getTimestamp() + SymbolConstant.SLASH;
        batchServing.setOutputPath(outputPath);
        updateBatchServing(userDTO, batchServing);
        deployServingAsyncTask.deployBatchServing(userDTO, batchServing);
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
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        assert Objects.nonNull(userDTO);
        BatchServing batchServing = checkBatchServingExist(batchServingStopDTO.getId(), userDTO.getId());
        if (ServingStatusEnum.STOP.getStatus().equals(batchServing.getStatus()) || ServingStatusEnum.EXCEPTION.equals(batchServing.getStatus())) {
            LogUtil.error(LogEnum.SERVING, "The service is not running, user {} failed stopping the service. Service id={}, service name:{}, service status:{}",
                    userDTO.getUsername(), batchServing.getId(), batchServing.getName(), batchServing.getStatus());
            throw new BusinessException(ServingErrorEnum.OPERATION_NOT_ALLOWED);
        }
        updateBatchServing(userDTO, batchServing);
        deployServingAsyncTask.deleteBatchServing(userDTO, batchServing);
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
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        assert Objects.nonNull(userDTO);
        BatchServing batchServing = checkBatchServingExist(batchServingDetailDTO.getId(), userDTO.getId());
        BatchServingDetailVO batchServingDetailVO = new BatchServingDetailVO();
        BeanUtils.copyProperties(batchServing, batchServingDetailVO);
        PtModelInfo ptModelInfo = ptModelInfoMapper.selectById(batchServingDetailVO.getModelId());
        if (ptModelInfo == null) {
            LogUtil.error(LogEnum.SERVING, "User {} failed deploying the batch service. The model is not exist: {}, Service id={}", userDTO.getUsername(), ptModelInfo.getName(), batchServingDetailDTO.getId());
            throw new BusinessException(ServingErrorEnum.MODEL_NOT_EXIST);
        }
        batchServingDetailVO.setModelName(ptModelInfo.getName());
        LambdaQueryWrapper<PtModelBranch> wrapper = new LambdaQueryWrapper();
        wrapper.eq(PtModelBranch::getParentId, batchServingDetailVO.getModelId());
        wrapper.eq(PtModelBranch::getModelAddress, batchServingDetailVO.getModelAddress());
        PtModelBranch ptModelBranch = ptModelBranchMapper.selectOne(wrapper);
        if (ptModelBranch != null) {
            batchServingDetailVO.setModelVersion(ptModelBranch.getVersionNum());
        }
        //运行中的任务计算进度
        if (batchServing.getStatus().equals(ServingStatusEnum.WORKING.getStatus())) {
            String progress = queryProgressByMinIO(batchServing);
            batchServingDetailVO.setProgress(progress);
        }
        return batchServingDetailVO;
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
        // 不处理slave
        if (req.getPodName().contains(ServingConstant.SLAVE_POD)) {
            return true;
        }
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
            deployServingAsyncTask.asyncSendServingMail(userService.findById(batchServing.getCreateUserId()).getEmail(), batchServing.getId());
        }
        if (PodPhaseEnum.FAILED.getPhase().equals(req.getPhase())) {
            String progress = queryProgressByMinIO(batchServing);
            batchServing.setProgress(progress);
            batchServing.setStatus(ServingStatusEnum.EXCEPTION.getStatus());
        }
        if (PodPhaseEnum.DELETED.getPhase().equals(req.getPhase())) {
            String progress = queryProgressByMinIO(batchServing);
            batchServing.setProgress(progress);
            batchServing.setStatus(ServingStatusEnum.STOP.getStatus());
        }
        if (PodPhaseEnum.UNKNOWN.getPhase().equals(req.getPhase())) {
            batchServing.setStatus(ServingStatusEnum.UNKNOWN.getStatus());
        }
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
        UserDTO user = JwtUtils.getCurrentUserDto();
        assert Objects.nonNull(user);
        String nameSpace = k8sUtil.getNamespace(user);
        return podService.getPods(new PodQueryDTO(nameSpace, k8sUtil.getBatchResourceName(batchServing.getResourceInfo())));
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
            if (nfsUtil.fileOrDirIsEmpty(k8sUtil.getAbsoluteNfsPath(path))) {
                return 0;
            }
            return minioUtil.getCount(bucketName, path);
        } catch (Exception e) {
            LogUtil.error(LogEnum.SERVING, "query count failed by path in minio: {}", path, e);
        }
        return NumberConstant.NUMBER_0;
    }

}
