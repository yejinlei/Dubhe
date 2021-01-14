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

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.constant.ModelOptConstant;
import org.dubhe.constant.ModelOptErrorEnum;
import org.dubhe.constant.ModelOptInstanceStatusEnum;
import org.dubhe.constant.NumberConstant;
import org.dubhe.constant.SymbolConstant;
import org.dubhe.dao.ModelOptTaskInstanceMapper;
import org.dubhe.domain.dto.DictDTO;
import org.dubhe.domain.dto.DictDetailDTO;
import org.dubhe.domain.dto.ModelOptTaskInstanceCancelDTO;
import org.dubhe.domain.dto.ModelOptTaskInstanceDeleteDTO;
import org.dubhe.domain.dto.ModelOptTaskInstanceDetailDTO;
import org.dubhe.domain.dto.ModelOptTaskInstanceQueryDTO;
import org.dubhe.domain.dto.ModelOptTaskInstanceResubmitDTO;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.domain.entity.ModelOptTaskInstance;
import org.dubhe.domain.vo.ModelOptResultQueryVO;
import org.dubhe.domain.vo.ModelOptTaskInstanceQueryVO;
import org.dubhe.dto.callback.ModelOptK8sPodCallbackCreateDTO;
import org.dubhe.enums.BizEnum;
import org.dubhe.enums.BizNfsEnum;
import org.dubhe.enums.DistillCommandEnum;
import org.dubhe.enums.LogEnum;
import org.dubhe.enums.OptimizeTypeEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.k8s.api.ModelOptJobApi;
import org.dubhe.k8s.api.NamespaceApi;
import org.dubhe.k8s.domain.bo.PtModelOptimizationJobBO;
import org.dubhe.k8s.domain.dto.PodQueryDTO;
import org.dubhe.k8s.domain.resource.BizJob;
import org.dubhe.k8s.domain.resource.BizNamespace;
import org.dubhe.k8s.domain.vo.PodVO;
import org.dubhe.k8s.enums.K8sResponseEnum;
import org.dubhe.k8s.enums.PodPhaseEnum;
import org.dubhe.k8s.service.PodService;
import org.dubhe.service.DictService;
import org.dubhe.service.ModelOptTaskInstanceService;
import org.dubhe.utils.DateUtil;
import org.dubhe.utils.JwtUtils;
import org.dubhe.utils.K8sNameTool;
import org.dubhe.utils.K8sUtil;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.MinioUtil;
import org.dubhe.utils.PageUtil;
import org.dubhe.utils.WrapperHelp;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @description 模型优化任务实例
 * @date 2020-05-22
 */
@Service
public class ModelOptTaskInstanceServiceImpl extends ServiceImpl<ModelOptTaskInstanceMapper, ModelOptTaskInstance> implements ModelOptTaskInstanceService {

    @Resource
    private ModelOptTaskInstanceMapper modelOptTaskInstanceMapper;

    @Resource
    private ModelOptJobApi modelOptJobApi;

    @Resource
    private NamespaceApi namespaceApi;

    @Resource
    private DictService dictService;

    @Resource
    private K8sNameTool k8sNameTool;

    @Resource
    private K8sUtil k8sUtil;

    @Resource
    private PodService podService;

    @Resource
    private MinioUtil minioUtil;

    @Value("${minio.bucketName}")
    private String bucket;

    @Value("${optimize.image}")
    private String optimizeImage;

    /**
     * 分页查询任务执行记录实例列表
     *
     * @param instanceQueryDTO 查询条件
     * @return Map<String, Object> 分页对象
     */
    @Override
    public Map<String, Object> queryAll(ModelOptTaskInstanceQueryDTO instanceQueryDTO) {
        QueryWrapper<ModelOptTaskInstance> wrapper = WrapperHelp.getWrapper(instanceQueryDTO);
        wrapper.orderByDesc("id");
        Page page = new Page(null == instanceQueryDTO.getCurrent() ? MagicNumConstant.ONE : instanceQueryDTO.getCurrent()
                , null == instanceQueryDTO.getSize() ? MagicNumConstant.TEN : instanceQueryDTO.getSize());
        IPage<ModelOptTaskInstance> modelOptTaskInstances = modelOptTaskInstanceMapper.selectPage(page, wrapper);
        List<ModelOptTaskInstanceQueryVO> list = modelOptTaskInstances.getRecords().stream().map(modelOptTaskInstance -> {
            ModelOptTaskInstanceQueryVO modelOptTaskInstanceVO = buildResult(modelOptTaskInstance);
            return modelOptTaskInstanceVO;
        }).collect(Collectors.toList());
        return PageUtil.toPage(modelOptTaskInstances, list);
    }

    /**
     * 新增任务实例
     *
     * @param modelOptTaskInstance 任务实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ModelOptTaskInstance modelOptTaskInstance) {
        modelOptTaskInstance.setStatus(ModelOptInstanceStatusEnum.WAITING.getValue());
        if (StringUtils.isBlank(modelOptTaskInstance.getAlgorithmPath()) || StringUtils.isBlank(modelOptTaskInstance.getAlgorithmPath())) {
            LogUtil.error(LogEnum.MODEL_OPT, "模型优化实例id={}的算法路径为空，算法：{}", modelOptTaskInstance.getId(), modelOptTaskInstance.getAlgorithmPath());
            throw new BusinessException(ModelOptErrorEnum.INTERNAL_SERVER_ERROR);
        }
        modelOptTaskInstanceMapper.insert(modelOptTaskInstance);
        if (!runTask(modelOptTaskInstance)) {
            throw new BusinessException(ModelOptErrorEnum.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 重新提交任务实例
     *
     * @param resubmitDTO 重新提交任务实例参数
     */
    @Override
    public void resubmit(ModelOptTaskInstanceResubmitDTO resubmitDTO) {
        ModelOptTaskInstance modelOptTaskInstance = modelOptTaskInstanceMapper.selectById(resubmitDTO.getId());
        checkInstExist(modelOptTaskInstance);
        // 校验该任务是否存在进行中和等待中的实例
        if (checkUnfinishedInst(modelOptTaskInstance.getTaskId())) {
            LogUtil.error(LogEnum.MODEL_OPT, "模型优化任务实例状态:{}，提交失败，实例id={}", modelOptTaskInstance.getStatus(), resubmitDTO.getId());
            throw new BusinessException(ModelOptErrorEnum.MODEL_OPT_TASK_INSTANCE_EXIST);
        }
        ModelOptTaskInstance newInstance = rebuild(modelOptTaskInstance);
        create(newInstance);
    }

    /**
     * 取消模型优化任务
     *
     * @param cancelDTO 取消模型优化任务参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(ModelOptTaskInstanceCancelDTO cancelDTO) {
        ModelOptTaskInstance modelOptTaskInstance = modelOptTaskInstanceMapper.selectById(cancelDTO.getId());
        checkInstExist(modelOptTaskInstance);
        //等待、进行中的实例才能取消
        if (!StringUtils.equalsAny(modelOptTaskInstance.getStatus(), ModelOptInstanceStatusEnum.WAITING.getValue(), ModelOptInstanceStatusEnum.RUNNING.getValue())) {
            LogUtil.error(LogEnum.MODEL_OPT, "模型优化任务实例状态:{}，取消失败，实例id={}", modelOptTaskInstance.getStatus(), cancelDTO.getId());
            throw new BusinessException(ModelOptErrorEnum.MODEL_OPT_TASK_INSTANCE_STATUS_ERROR);
        }
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        // 调用k8s删除相关资源
        assert userDTO != null;
        modelOptJobApi.deleteByResourceName(getNamespace(userDTO), k8sNameTool.generateResourceName(BizEnum.MODEL_OPT, cancelDTO.getId().toString()));
        modelOptTaskInstance.setStatus(ModelOptInstanceStatusEnum.CANCELED.getValue());
        modelOptTaskInstance.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        modelOptTaskInstanceMapper.updateById(modelOptTaskInstance);
    }

    /**
     * 查看单个任务实例详情
     *
     * @param detailDTO 查看任务实例详情参数
     * @return ModelOptTaskInstanceVO 任务实例对象
     */
    @Override
    public ModelOptTaskInstanceQueryVO getInstDetail(ModelOptTaskInstanceDetailDTO detailDTO) {
        ModelOptTaskInstance modelOptTaskInstance = modelOptTaskInstanceMapper.selectById(detailDTO.getId());
        checkInstExist(modelOptTaskInstance);
        return buildResult(modelOptTaskInstance);
    }

    /**
     * k8s回调模型优化方法
     *
     * @param req 模型优化自定义回调参数类
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean modelOptCallBack(ModelOptK8sPodCallbackCreateDTO req) {
        String resourceInfo = k8sNameTool.getResourceInfoFromResourceName(BizEnum.MODEL_OPT, req.getResourceName());
        if (StringUtils.isBlank(resourceInfo)) {
            LogUtil.error(LogEnum.MODEL_OPT, "can't resolve resourceName {}", req.getResourceName());
            return false;
        }
        Long instId = Long.valueOf(resourceInfo);
        ModelOptTaskInstance modelOptTaskInstance = modelOptTaskInstanceMapper.selectById(instId);
        checkInstExist(modelOptTaskInstance);
        //对当前状态是已完成或已取消的不执行回调操作
        if (ModelOptInstanceStatusEnum.COMPLETED.getValue().equals(modelOptTaskInstance.getStatus())
                || ModelOptInstanceStatusEnum.CANCELED.getValue().equals(modelOptTaskInstance.getStatus())) {
            return true;
        }
        if (PodPhaseEnum.RUNNING.getPhase().equals(req.getPhase())) {
            modelOptTaskInstance.setStatus(ModelOptInstanceStatusEnum.RUNNING.getValue());
            modelOptTaskInstance.setStartTime(DateUtil.getCurrentTimestamp());
        }
        if (PodPhaseEnum.FAILED.getPhase().equals(req.getPhase())) {
            modelOptTaskInstance.setStatus(ModelOptInstanceStatusEnum.EXEC_FAILED.getValue());
        }
        if (PodPhaseEnum.DELETED.getPhase().equals(req.getPhase())) {
            modelOptTaskInstance.setStatus(ModelOptInstanceStatusEnum.CANCELED.getValue());
        }
        if (PodPhaseEnum.SUCCEEDED.getPhase().equals(req.getPhase())) {
            modelOptTaskInstance.setStatus(ModelOptInstanceStatusEnum.COMPLETED.getValue());
            modelOptTaskInstance.setEndTime(DateUtil.getCurrentTimestamp());
            if (StringUtils.isBlank(modelOptTaskInstance.getOptResultBefore())
                    || StringUtils.isBlank(modelOptTaskInstance.getOptResultAfter())) {
                try {
                    String optResultBefore = minioUtil.readString(bucket, modelOptTaskInstance.getOptResultJsonPathBefore() + ModelOptConstant.OPTIMIZE_JSON_NAME);
                    String optResultAfter = minioUtil.readString(bucket, modelOptTaskInstance.getOptResultJsonPathAfter() + ModelOptConstant.OPTIMIZE_JSON_NAME);
                    modelOptTaskInstance.setOptResultBefore(optResultBefore);
                    modelOptTaskInstance.setOptResultAfter(optResultAfter);
                } catch (Exception e) {
                    LogUtil.error(LogEnum.MODEL_OPT, "Read json file field by k8s callback，instance id={},Exception={}", instId, e);
                }
            }
        }
        return updateById(modelOptTaskInstance);
    }

    /**
     * 查询K8S同步相关实例状态
     */
    @Override
    public void syncInstanceStatus() {
        // 获取等待状态超过五分钟的任务实例
        List<ModelOptTaskInstance> taskInstanceList = modelOptTaskInstanceMapper.selectWaitingFor5MinutesInstances();
        // 若相应实例已启动失败更新相关实例状态
        for (ModelOptTaskInstance instance : taskInstanceList) {
            UserDTO tempUser = new UserDTO();
            tempUser.setId(instance.getCreateUserId());
            tempUser.setUsername("更新优化实例状态定时任务");
            String namespace = getNamespace(tempUser);
            String resourceName = k8sNameTool.generateResourceName(BizEnum.MODEL_OPT, instance.getId().toString());
            BizJob bizJob = modelOptJobApi.getWithResourceName(namespace, resourceName);
            if (K8sResponseEnum.NOT_FOUND.getCode().equals(bizJob.getCode())) {
                // k8s侧无pod存在当做实例已停止
                ModelOptTaskInstance modelOptTaskInstance = modelOptTaskInstanceMapper.selectById(instance.getId());
                checkInstExist(modelOptTaskInstance);
                modelOptTaskInstanceMapper.markInstanceExecFailed(instance.getId());
            }
        }
    }

    /**
     * 校验任务实例是否存在
     *
     * @param modelOptTaskInstance 模型优化实例对象
     */
    public void checkInstExist(ModelOptTaskInstance modelOptTaskInstance) {
        if (modelOptTaskInstance == null) {
            throw new BusinessException(ModelOptErrorEnum.MODEL_OPT_TASK_INSTANCE_ABSENT);
        }
    }

    /**
     * 重新构建任务实例
     *
     * @param instance 任务实例参数来源
     * @return 填充参数后的实例对象
     */
    private ModelOptTaskInstance rebuild(ModelOptTaskInstance instance) {
        ModelOptTaskInstance newInstance = new ModelOptTaskInstance();
        newInstance.setTaskId(instance.getTaskId())
                .setTaskName(instance.getTaskName())
                .setIsBuiltIn(instance.getIsBuiltIn())
                .setModelId(instance.getModelId())
                .setModelName(instance.getModelName())
                .setModelAddress(instance.getModelAddress())
                .setAlgorithmId(instance.getAlgorithmId())
                .setAlgorithmType(instance.getAlgorithmType())
                .setAlgorithmName(instance.getAlgorithmName())
                .setAlgorithmPath(instance.getAlgorithmPath())
                .setDatasetName(instance.getDatasetName())
                .setDatasetPath(instance.getDatasetPath())
                .setCommand(instance.getCommand())
                .setParams(instance.getParams());
        return newInstance;
    }

    /**
     * @param instance 运行实例
     * @return boolean 调用k8s是否成功
     */
    private boolean runTask(ModelOptTaskInstance instance) {
        PtModelOptimizationJobBO jobBo = new PtModelOptimizationJobBO();
        UserDTO currentUser = JwtUtils.getCurrentUserDto();
        // 检查namespace,不存在就创建
        assert currentUser != null;
        String namespace = getNamespace(currentUser);
        String command = getCommand(instance);
        LogUtil.info(LogEnum.MODEL_OPT, "用户{}执行模型优化输入的命令为{}，实例id={}", currentUser.getUsername(), command, instance.getId());
        String logPath = SymbolConstant.SLASH + BizNfsEnum.MODEL_OPT.getBizNfsPath() + SymbolConstant.SLASH + currentUser.getId() + SymbolConstant.SLASH + instance.getTaskId() + SymbolConstant.SLASH + instance.getId() + ModelOptConstant.OPTIMIZE_LOG;
        String outputModelDir = SymbolConstant.SLASH + BizNfsEnum.MODEL_OPT.getBizNfsPath() + SymbolConstant.SLASH + currentUser.getId() + SymbolConstant.SLASH + instance.getTaskId() + SymbolConstant.SLASH + instance.getId() + ModelOptConstant.OPTIMIZE_MODEL;
        String optResultJsonPathBefore = SymbolConstant.SLASH + BizNfsEnum.MODEL_OPT.getBizNfsPath() + SymbolConstant.SLASH + currentUser.getId() + SymbolConstant.SLASH + instance.getTaskId() + SymbolConstant.SLASH + instance.getId() + ModelOptConstant.OPTIMIZE_JSON_BEFORE;
        String optResultJsonPathAfter = SymbolConstant.SLASH + BizNfsEnum.MODEL_OPT.getBizNfsPath() + SymbolConstant.SLASH + currentUser.getId() + SymbolConstant.SLASH + instance.getTaskId() + SymbolConstant.SLASH + instance.getId() + ModelOptConstant.OPTIMIZE_JSON_AFTER;
        jobBo.setNamespace(namespace);
        jobBo.setName(k8sNameTool.generateResourceName(BizEnum.MODEL_OPT, instance.getId().toString()));
        jobBo.setCpuNum(ModelOptConstant.CPU_NUM);
        jobBo.setMemNum(ModelOptConstant.MEMORY_NUM);
        jobBo.setGpuNum(ModelOptConstant.GPU_NUM);
        jobBo.setCmdLines(Arrays.asList("-c", command));
        jobBo.putNfsMounts(ModelOptConstant.DATASET_MOUNT_PATH, k8sNameTool.getAbsoluteNfsPath(instance.getDatasetPath()));
        jobBo.putNfsMounts(ModelOptConstant.INPUT_MODEL_BEFORE_MOUNT_PATH, k8sNameTool.getAbsoluteNfsPath(instance.getModelAddress()));
        jobBo.putNfsMounts(ModelOptConstant.ALGORITHM_MOUNT_PATH, k8sNameTool.getAbsoluteNfsPath(instance.getAlgorithmPath()));
        jobBo.putNfsMounts(ModelOptConstant.OUTPUT_LOG_MOUNT_PATH, k8sNameTool.getAbsoluteNfsPath(logPath));
        jobBo.putNfsMounts(ModelOptConstant.OUTPUT_MODEL_MOUNT_PATH, k8sNameTool.getAbsoluteNfsPath(outputModelDir));
        jobBo.putNfsMounts(ModelOptConstant.OUTPUT_RESULT_BEFORE_MOUNT_PATH, k8sNameTool.getAbsoluteNfsPath(optResultJsonPathBefore));
        jobBo.putNfsMounts(ModelOptConstant.OUTPUT_RESULT_AFTER_MOUNT_PATH, k8sNameTool.getAbsoluteNfsPath(optResultJsonPathAfter));
        jobBo.setBusinessLabel(k8sNameTool.getPodLabel(BizEnum.MODEL_OPT));
        jobBo.setImage(optimizeImage);

        // 调用k8s接口
        BizJob bizJob = modelOptJobApi.create(jobBo);
        if (null == bizJob || !bizJob.isSuccess()) {
            String message = null == bizJob ? "未知的错误" : bizJob.getMessage();
            LogUtil.error(
                    LogEnum.MODEL_OPT,
                    "用户{}创建模型优化任务实例, k8s创建过程中失败, 实例id={}, 传递参数为{}, 错误的信息为{}",
                    currentUser.getUsername(), instance.getId(), instance, message);
            return false;
        }
        instance.setLogPath(logPath);
        instance.setOutputModelDir(outputModelDir);
        instance.setOptResultJsonPathBefore(optResultJsonPathBefore);
        instance.setOptResultJsonPathAfter(optResultJsonPathAfter);
        return updateById(instance);
    }

    /**
     * 获取模型优化执行命令行
     *
     * @param instance 模型优化实例
     * @return 返回执行命令行
     */
    private String getCommand(ModelOptTaskInstance instance) {
        String command = SymbolConstant.BLANK;
        if (instance.getIsBuiltIn()) {
            // 内置为默认命令
            if (OptimizeTypeEnum.DISTILL.getType().equals(instance.getAlgorithmType())) {
                command = DistillCommandEnum.getCommandByName(instance.getAlgorithmName());
                assert command != null;
                command = String.format(command, ModelOptConstant.DATASET_MOUNT_PATH,
                        ModelOptConstant.INPUT_MODEL_BEFORE_MOUNT_PATH,
                        ModelOptConstant.OUTPUT_LOG_MOUNT_PATH,
                        ModelOptConstant.OUTPUT_RESULT_BEFORE_MOUNT_PATH,
                        instance.getDatasetName(),
                        ModelOptConstant.DATASET_MOUNT_PATH,
                        ModelOptConstant.OUTPUT_MODEL_MOUNT_PATH + ModelOptConstant.OPTIMIZE_JSON_AFTER,
                        ModelOptConstant.OUTPUT_LOG_MOUNT_PATH,
                        ModelOptConstant.OUTPUT_RESULT_AFTER_MOUNT_PATH,
                        instance.getDatasetName());
            }
            if (OptimizeTypeEnum.SLIMMING.getType().equals(instance.getAlgorithmType())) {
                command = String.format(ModelOptConstant.OPT_START_SLIMMING_COMMAND,
                        ModelOptConstant.NEURONAL_PRUNING.equals(instance.getAlgorithmName()) ? "bn" : instance.getAlgorithmName(),
                        instance.getModelName(),
                        instance.getDatasetName(),
                        ModelOptConstant.DATASET_MOUNT_PATH,
                        ModelOptConstant.OUTPUT_MODEL_MOUNT_PATH,
                        ModelOptConstant.OUTPUT_LOG_MOUNT_PATH,
                        ModelOptConstant.OUTPUT_RESULT_BEFORE_MOUNT_PATH,
                        ModelOptConstant.OUTPUT_RESULT_AFTER_MOUNT_PATH);
            }
            if (OptimizeTypeEnum.QUANTIFY.getType().equals(instance.getAlgorithmType())) {
                command = String.format(ModelOptConstant.OPT_START_QUANTIFY_COMMAND, instance.getModelName(),
                        ModelOptConstant.INPUT_MODEL_BEFORE_MOUNT_PATH,
                        ModelOptConstant.DATASET_MOUNT_PATH,
                        ModelOptConstant.OUTPUT_LOG_MOUNT_PATH,
                        ModelOptConstant.OUTPUT_RESULT_BEFORE_MOUNT_PATH,
                        instance.getModelName(),
                        ModelOptConstant.INPUT_MODEL_BEFORE_MOUNT_PATH,
                        ModelOptConstant.DATASET_MOUNT_PATH,
                        ModelOptConstant.OUTPUT_LOG_MOUNT_PATH,
                        ModelOptConstant.OUTPUT_RESULT_AFTER_MOUNT_PATH);
            }
        } else {
            // 非内置为用户输入命令
            command = String.format(ModelOptConstant.MY_OPT_COMMAND,
                    ModelOptConstant.ALGORITHM_MOUNT_PATH,
                    instance.getCommand() + getParams(instance.getParams()));
        }
        return command;
    }

    /**
     * 解析运行参数
     *
     * @return String 运行参数字符串
     */
    public String getParams(JSONObject params) {
        StringBuilder paramStr = new StringBuilder();
        for (String key : params.keySet()) {
            if (Objects.nonNull(params.get(key))) {
                paramStr.append(" --").append(key).append(SymbolConstant.FLAG_EQUAL + SymbolConstant.APOSTROPHE).append(params.get(key)).append(SymbolConstant.APOSTROPHE);
            }
        }
        return paramStr.toString();
    }

    /**
     * @param currentUser 当前用户
     * @return namespace k8s的命名空间
     */
    private String getNamespace(UserDTO currentUser) {
        String namespaceStr = k8sNameTool.generateNamespace(currentUser.getId());
        BizNamespace bizNamespace = namespaceApi.get(namespaceStr);
        if (null == bizNamespace) {
            BizNamespace namespace = namespaceApi.create(namespaceStr, null);
            if (null == namespace || !namespace.isSuccess()) {
                LogUtil.error(LogEnum.MODEL_OPT, "用户{}启动k8s模型优化失败，namespace为空", currentUser.getUsername());
                throw new BusinessException(ModelOptErrorEnum.INTERNAL_SERVER_ERROR);
            }
        }
        return namespaceStr;
    }

    /**
     * 校验该任务是否存在进行中和等待中的实例
     *
     * @param taskId 任务id
     * @return Boolean 是否存在进行中和等待中的实例
     */
    @Override
    public Boolean checkUnfinishedInst(Long taskId) {
        LambdaQueryWrapper<ModelOptTaskInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelOptTaskInstance::getTaskId, taskId)
                .in(ModelOptTaskInstance::getStatus, Arrays.asList(ModelOptInstanceStatusEnum.WAITING.getValue(), ModelOptInstanceStatusEnum.RUNNING.getValue()));
        List<ModelOptTaskInstance> list = modelOptTaskInstanceMapper.selectList(wrapper);
        return CollectionUtil.isNotEmpty(list);
    }

    /**
     * 根据任务id删除实例
     *
     * @param taskId 任务id
     * @return Integer 删除实例数量
     */
    @Override
    public int deleteByTaskId(Long taskId) {
        LambdaQueryWrapper<ModelOptTaskInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelOptTaskInstance::getTaskId, taskId);
        return modelOptTaskInstanceMapper.delete(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(ModelOptTaskInstanceDeleteDTO modelOptTaskInstanceDeleteDTO) {
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        assert Objects.nonNull(userDTO);
        ModelOptTaskInstance modelOptTaskInstance = modelOptTaskInstanceMapper.selectById(modelOptTaskInstanceDeleteDTO.getId());
        checkInstExist(modelOptTaskInstance);
        if (StringUtils.equalsAny(modelOptTaskInstance.getStatus(), ModelOptInstanceStatusEnum.RUNNING.getValue(), ModelOptInstanceStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ModelOptErrorEnum.MODEL_OPT_TASK_INSTANCE_STATUS_ERROR);
        }
        modelOptTaskInstanceMapper.deleteById(modelOptTaskInstanceDeleteDTO.getId());
        // 调用k8s删除相关资源
        modelOptJobApi.deleteByResourceName(getNamespace(userDTO),
                k8sNameTool.generateResourceName(BizEnum.MODEL_OPT, modelOptTaskInstanceDeleteDTO.getId().toString()));
    }

    /**
     * 构建优化结果
     *
     * @param modelOptTaskInstance 任务实例对象
     * @return ModelOptTaskInstanceQueryVO 模型优化任务实例
     */
    public ModelOptTaskInstanceQueryVO buildResult(ModelOptTaskInstance modelOptTaskInstance) {
        //有返回结果，构造返回结果
        ModelOptTaskInstanceQueryVO modelOptTaskInstanceVO = new ModelOptTaskInstanceQueryVO();
        BeanUtils.copyProperties(modelOptTaskInstance, modelOptTaskInstanceVO);
        //从会话中获取用户信息
        UserDTO user = JwtUtils.getCurrentUserDto();
        assert Objects.nonNull(user);
        String nameSpace = k8sUtil.getNamespace(user);
        List<PodVO> pods = podService.getPods(new PodQueryDTO(nameSpace, k8sNameTool.generateResourceName(BizEnum.MODEL_OPT, modelOptTaskInstance.getId().toString())));
        if (!CollectionUtils.isEmpty(pods)) {
            modelOptTaskInstanceVO.setPodName(pods.get(NumberConstant.NUMBER_0).getPodName());
        }
        if (StringUtils.isEmpty(modelOptTaskInstance.getOptResultBefore()) ||
                StringUtils.isEmpty(modelOptTaskInstance.getOptResultAfter())) {
            return modelOptTaskInstanceVO;
        }
        //获取模型优化任务结果字典
        DictDTO dictDTO = dictService.findByName(ModelOptConstant.TASK_RESULT_DICT_NAME);
        List<ModelOptResultQueryVO> resultList = new ArrayList<>();
        if (dictDTO == null || CollectionUtils.isEmpty(dictDTO.getDictDetails())) {
            LogUtil.error(LogEnum.MODEL_OPT, "模型优化任务结果字典为空，opt_result字典为{}", JSONObject.toJSONString(dictDTO));
            throw new BusinessException(ModelOptErrorEnum.MODEL_OPT_TASK_RESULT_DICT_EMPTY);
        }
        //解析优化结果字符串
        JSONObject before = JSONObject.parseObject(modelOptTaskInstance.getOptResultBefore());
        JSONObject after = JSONObject.parseObject(modelOptTaskInstance.getOptResultAfter());
        for (DictDetailDTO dictDetail : dictDTO.getDictDetails()) {
            if (StringUtils.isEmpty(dictDetail.getLabel()) || StringUtils.isEmpty(dictDetail.getValue())) {
                LogUtil.error(LogEnum.MODEL_OPT, "模型优化任务结果字典详情为空，opt_result字典详情为{}", JSONObject.toJSONString(dictDetail));
                throw new BusinessException(ModelOptErrorEnum.MODEL_OPT_TASK_RESULT_DICT_EMPTY);
            }
            //构造模型优化结果
            ModelOptResultQueryVO modelOptResultVO = new ModelOptResultQueryVO();
            modelOptResultVO.setName(dictDetail.getLabel());
            //去掉模型优化结果中的单位
            String beforeStr = before.getOrDefault(dictDetail.getLabel(), SymbolConstant.ZERO).toString().replace(dictDetail.getValue(), SymbolConstant.BLANK);
            String afterStr = after.getOrDefault(dictDetail.getLabel(), SymbolConstant.ZERO).toString().replace(dictDetail.getValue(), SymbolConstant.BLANK);
            Float beforeValue = Float.parseFloat(beforeStr);
            Float afterValue = Float.parseFloat(afterStr);
            if (ModelOptConstant.ACCURACY.equals(dictDetail.getLabel())) {
                beforeValue = beforeValue * NumberConstant.NUMBER_100;
                afterValue = afterValue * NumberConstant.NUMBER_100;
            }
            modelOptResultVO.setBefore(beforeValue);
            modelOptResultVO.setAfter(afterValue);
            modelOptResultVO.setUnit(dictDetail.getValue());
            modelOptResultVO.setPositive(Integer.toString(afterValue.compareTo(beforeValue)));
            resultList.add(modelOptResultVO);
        }
        modelOptTaskInstanceVO.setOptResult(resultList);
        return modelOptTaskInstanceVO;
    }
}
