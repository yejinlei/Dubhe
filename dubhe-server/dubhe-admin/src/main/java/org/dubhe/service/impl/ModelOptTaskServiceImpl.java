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
import org.dubhe.base.MagicNumConstant;
import org.dubhe.config.NfsConfig;
import org.dubhe.constant.ModelOptConstant;
import org.dubhe.constant.ModelOptErrorEnum;
import org.dubhe.constant.NumberConstant;
import org.dubhe.constant.SymbolConstant;
import org.dubhe.dao.ModelOptBuiltInMapper;
import org.dubhe.dao.ModelOptDatasetMapper;
import org.dubhe.dao.ModelOptTaskMapper;
import org.dubhe.dao.PtModelInfoMapper;
import org.dubhe.data.constant.Constant;
import org.dubhe.domain.PtModelInfo;
import org.dubhe.domain.dto.ModelOptDatasetCreateDTO;
import org.dubhe.domain.dto.ModelOptTaskCreateDTO;
import org.dubhe.domain.dto.ModelOptTaskDeleteDTO;
import org.dubhe.domain.dto.ModelOptTaskQueryDTO;
import org.dubhe.domain.dto.ModelOptTaskSubmitDTO;
import org.dubhe.domain.dto.ModelOptTaskUpdateDTO;
import org.dubhe.domain.dto.PtModelAlgorithmCreateDTO;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.domain.entity.ModelOptDataset;
import org.dubhe.domain.entity.ModelOptTask;
import org.dubhe.domain.entity.ModelOptTaskInstance;
import org.dubhe.domain.entity.PtTrainAlgorithm;
import org.dubhe.domain.vo.ModelOptAlgorithmQueryVO;
import org.dubhe.domain.vo.ModelOptCreateVO;
import org.dubhe.domain.vo.ModelOptDatasetQueryVO;
import org.dubhe.domain.vo.ModelOptDatasetVO;
import org.dubhe.domain.vo.ModelOptModelQueryVO;
import org.dubhe.domain.vo.ModelOptTaskQueryVO;
import org.dubhe.domain.vo.ModelOptUpdateVO;
import org.dubhe.enums.LogEnum;
import org.dubhe.enums.OptimizeTypeEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.service.ModelOptTaskInstanceService;
import org.dubhe.service.ModelOptTaskService;
import org.dubhe.service.PtTrainAlgorithmService;
import org.dubhe.utils.DateUtil;
import org.dubhe.utils.JwtUtils;
import org.dubhe.utils.K8sUtil;
import org.dubhe.utils.LocalFileUtil;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.NfsUtil;
import org.dubhe.utils.PageUtil;
import org.dubhe.utils.PtModelUtil;
import org.dubhe.utils.ReflectionUtils;
import org.dubhe.utils.StringUtils;
import org.dubhe.utils.WrapperHelp;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description 模型优化任务
 * @date 2020-05-22
 */
@Service
public class ModelOptTaskServiceImpl implements ModelOptTaskService {

    @Resource
    private ModelOptTaskMapper modelOptTaskMapper;

    @Resource
    private ModelOptTaskInstanceService modelOptTaskInstanceService;

    @Resource
    private ModelOptBuiltInMapper modelOptBuiltInMapper;

    @Resource
    private PtTrainAlgorithmService ptTrainAlgorithmService;

    @Resource
    private NfsUtil nfsUtil;

    @Resource
    private K8sUtil k8sUtil;

    @Resource
    private LocalFileUtil localFileUtil;

    @Resource
    private NfsConfig nfsConfig;

    @Resource
    private PtModelInfoMapper ptModelInfoMapper;

    @Resource
    private ModelOptDatasetMapper modelOptDatasetMapper;

    public final static List<String> FILED_MANES;

    static {
        FILED_MANES = ReflectionUtils.getFieldNames(ModelOptTaskQueryVO.class);
    }

    /**
     * 查询数据分页
     *
     * @param modelOptTaskQueryDTO 模型优化任务查询参数
     * @return Map<String, Object> 模型优化任务分页对象
     */
    @Override
    public Map<String, Object> queryAll(ModelOptTaskQueryDTO modelOptTaskQueryDTO) {
        String name = modelOptTaskQueryDTO.getName();
        //任务名称或id条件非空
        if (StringUtils.isNotEmpty(name)) {
            //整数匹配
            if (Constant.PATTERN_NUM.matcher(name).matches()) {
                //查询条件为整数时，先根据id精确查询
                modelOptTaskQueryDTO.setId(Long.parseLong(name));
                modelOptTaskQueryDTO.setName(null);
                Map<String, Object> map = queryTask(modelOptTaskQueryDTO);
                //根据id查询结果为空时，再根据任务名称模糊查询
                if (((List<ModelOptTaskQueryVO>) map.get(Constant.RESULT)).size() > NumberConstant.NUMBER_0) {
                    return map;
                } else {
                    modelOptTaskQueryDTO.setName(name);
                }
            }
        }
        return queryTask(modelOptTaskQueryDTO);
    }

    /**
     * 模型优化任务查询
     *
     * @param modelOptTaskQueryDTO 模型优化任务查询条件DTO
     * @return Map<String, Object> 分页查询结果
     */
    public Map<String, Object> queryTask(ModelOptTaskQueryDTO modelOptTaskQueryDTO) {
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.MODEL_OPT, "用户{}查询模型优化任务列表，查询条件为{}", userDTO.getUsername(), JSONObject.toJSONString(modelOptTaskQueryDTO));
        QueryWrapper<ModelOptTask> wrapper = WrapperHelp.getWrapper(modelOptTaskQueryDTO);
        wrapper.eq("create_user_id", userDTO.getId());
        if (StringUtils.isNotEmpty(modelOptTaskQueryDTO.getName())) {
            //任务名称模糊搜索时不区分大小写
            wrapper.like("lower(name)", modelOptTaskQueryDTO.getName().toLowerCase());
        }
        Page page = new Page(null == modelOptTaskQueryDTO.getCurrent() ? MagicNumConstant.ONE : modelOptTaskQueryDTO.getCurrent(),
                null == modelOptTaskQueryDTO.getSize() ? MagicNumConstant.TEN : modelOptTaskQueryDTO.getSize());
        try {
            //排序字段，默认按更新时间降序，否则将驼峰转换为下划线
            String column = modelOptTaskQueryDTO.getSort() != null && FILED_MANES.contains(modelOptTaskQueryDTO.getSort()) ? StringUtils.humpToLine(modelOptTaskQueryDTO.getSort()) : "update_time";
            //排序方式
            boolean isAsc = !StringUtils.isEmpty(modelOptTaskQueryDTO.getOrder()) && !StringUtils.equals(modelOptTaskQueryDTO.getOrder(), Constant.SORT_DESC);
            wrapper.orderBy(true, isAsc, column);
        } catch (Exception e) {
            LogUtil.error(LogEnum.MODEL_OPT, "查询模型优化任务列表展示异常,请求信息:{}，异常信息:{}", JSONObject.toJSONString(modelOptTaskQueryDTO), e);
            throw new BusinessException(ModelOptErrorEnum.INTERNAL_SERVER_ERROR);
        }
        IPage<ModelOptTask> modelOptTasks = modelOptTaskMapper.selectPage(page, wrapper);
        List<ModelOptTaskQueryVO> queryVOList = modelOptTasks.getRecords().stream().map(modelOptTask -> {
            ModelOptTaskQueryVO queryVO = new ModelOptTaskQueryVO();
            BeanUtils.copyProperties(modelOptTask, queryVO);
            return queryVO;
        }).collect(Collectors.toList());
        LogUtil.info(LogEnum.MODEL_OPT, "用户{}查询模型优化任务列表，任务数={}", userDTO.getUsername(), queryVOList.size());
        return PageUtil.toPage(modelOptTasks, queryVOList);
    }

    /**
     * 创建模型优化任务
     *
     * @param modelOptTaskCreateDTO 模型优化任务创建对象
     * @return 返回创建成功的对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ModelOptCreateVO create(ModelOptTaskCreateDTO modelOptTaskCreateDTO) {
        //参数校验
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        checkDatasetExist(modelOptTaskCreateDTO.getDatasetPath());
        assert userDTO != null;
        checkNameExist(modelOptTaskCreateDTO.getName(), userDTO.getId());
        ModelOptTask modelOptTask = new ModelOptTask();
        BeanUtils.copyProperties(modelOptTaskCreateDTO, modelOptTask);
        if (!modelOptTaskCreateDTO.getIsBuiltIn()) {
            checkModelExist(modelOptTask, modelOptTaskCreateDTO.getModelId());
            //选择非内置，且算法为内置算法时（算法id为空），需要添加到我的算法中
            if (modelOptTaskCreateDTO.getAlgorithmId() == null) {
                PtTrainAlgorithm algorithm = forkAlgorithm(modelOptTaskCreateDTO.getAlgorithmType(), modelOptTaskCreateDTO.getAlgorithmName(), modelOptTaskCreateDTO.getAlgorithmPath(), modelOptTaskCreateDTO.getModelAddress());
                modelOptTask.setAlgorithmType(modelOptTaskCreateDTO.getAlgorithmType());
                modelOptTask.setAlgorithmId(algorithm.getId());
                modelOptTask.setAlgorithmName(algorithm.getAlgorithmName());
                modelOptTask.setAlgorithmPath(algorithm.getCodeDir());
                saveTask(userDTO, modelOptTask);
                return new ModelOptCreateVO(algorithm.getId(), algorithm.getCodeDir());
            }
            //非内置，算法为我的算法
            localFileUtil.copyFile(nfsConfig.getBucket() + modelOptTaskCreateDTO.getModelAddress(), nfsConfig.getBucket() + modelOptTaskCreateDTO.getAlgorithmPath() + ModelOptConstant.COPY_MODEL_POSTFIX);
            saveTask(userDTO, modelOptTask);
            return new ModelOptCreateVO(modelOptTaskCreateDTO.getAlgorithmId(), modelOptTaskCreateDTO.getAlgorithmPath());
        }
        //内置
        saveTask(userDTO, modelOptTask);
        return null;
    }

    /**
     * 保存模型任务
     *
     * @param userDTO      用户信息
     * @param modelOptTask 模型任务信息
     */
    private void saveTask(UserDTO userDTO, ModelOptTask modelOptTask) {
        int result = modelOptTaskMapper.insert(modelOptTask);
        if (result < MagicNumConstant.ONE) {
            LogUtil.error(LogEnum.MODEL_OPT, "用户{}创建模型优化任务:{}, 数据库操作失败", userDTO.getUsername(), modelOptTask.getName());
            throw new BusinessException(ModelOptErrorEnum.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * fork内置算法到我的算法
     *
     * @param algorithmName 内置算法名称
     * @param algorithmPath 内置算法路径
     * @param modelAddress  模型路径
     * @return PtTrainAlgorithm 我的算法对象
     */
    private PtTrainAlgorithm forkAlgorithm(Integer type, String algorithmName, String algorithmPath, String modelAddress) {
        String newAlgorithmName = algorithmName + SymbolConstant.HYPHEN + DateUtil.getTimestampStr();
        String newAlgorithmPath = ModelOptConstant.MY_OPT_ALGORITHM_ROOT_PATH + newAlgorithmName;
        String codeDir = SymbolConstant.BLANK;
        if (type != null) {
            codeDir = OptimizeTypeEnum.getCodeDirByType(type);
        }
        //copy算法
        localFileUtil.copyFile(nfsConfig.getBucket() + algorithmPath, nfsConfig.getBucket() + newAlgorithmPath);
        //剪枝算法 我的模型需要拷贝到算法目录下
        if (OptimizeTypeEnum.SLIMMING.getType().equals(type)) {
            localFileUtil.copyDir(nfsConfig.getBucket() + modelAddress, nfsConfig.getBucket() + newAlgorithmPath + codeDir + ModelOptConstant.COPY_MODEL_POSTFIX);
        }
        PtModelAlgorithmCreateDTO algorithmCreateDTO = new PtModelAlgorithmCreateDTO();
        algorithmCreateDTO.setName(newAlgorithmName);
        algorithmCreateDTO.setPath(newAlgorithmPath + codeDir);
        return ptTrainAlgorithmService.modelOptimizationUploadAlgorithm(algorithmCreateDTO);
    }

    /**
     * 提交模型优化任务，创建任务实例
     *
     * @param submitDTO 任务提交参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(ModelOptTaskSubmitDTO submitDTO) {
        ModelOptTask modelOptTask = modelOptTaskMapper.selectById(submitDTO.getId());
        checkTaskExist(modelOptTask);
        if (modelOptTaskInstanceService.checkUnfinishedInst(submitDTO.getId())) {
            LogUtil.error(LogEnum.MODEL_OPT, "模型优化任务存在等待或进行中的实例，提交失败，任务id={}", submitDTO.getId());
            throw new BusinessException(ModelOptErrorEnum.MODEL_OPT_TASK_INSTANCE_EXIST);
        }
        ModelOptTaskInstance modelOptTaskInstance = buildInstance(modelOptTask);
        modelOptTaskInstanceService.create(modelOptTaskInstance);
    }

    /**
     * 修改模型优化任务
     *
     * @param modelOptTaskUpdateDTO 模型优化任务修改对象
     * @return 返回修改后信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ModelOptUpdateVO update(ModelOptTaskUpdateDTO modelOptTaskUpdateDTO) {
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        ModelOptTask oldTask = modelOptTaskMapper.selectById(modelOptTaskUpdateDTO.getId());
        checkTaskExist(oldTask);
        //用户不能修改其他用户的任务
        assert userDTO != null;
        if (!userDTO.getId().equals(oldTask.getCreateUserId())) {
            throw new BusinessException(ModelOptErrorEnum.MODEL_OPT_TASK_ABSENT);
        }
        //如果修改了任务名，校验新的任务名是否存在
        if (!modelOptTaskUpdateDTO.getName().equals(oldTask.getName())) {
            checkNameExist(modelOptTaskUpdateDTO.getName(), userDTO.getId());
        }
        checkDatasetExist(modelOptTaskUpdateDTO.getDatasetPath());
        ModelOptTask newTask = new ModelOptTask();
        BeanUtils.copyProperties(modelOptTaskUpdateDTO, newTask);
        if (!modelOptTaskUpdateDTO.getIsBuiltIn()) {
            checkModelExist(newTask, modelOptTaskUpdateDTO.getModelId());
            //选择非内置，且算法为内置算法时（算法id为空），需要添加到我的算法中
            if (modelOptTaskUpdateDTO.getAlgorithmId() == null) {
                PtTrainAlgorithm algorithm = forkAlgorithm(modelOptTaskUpdateDTO.getAlgorithmType(), modelOptTaskUpdateDTO.getAlgorithmName(), modelOptTaskUpdateDTO.getAlgorithmPath(), modelOptTaskUpdateDTO.getModelAddress());
                newTask.setAlgorithmId(algorithm.getId());
                newTask.setAlgorithmName(algorithm.getAlgorithmName());
                newTask.setAlgorithmPath(algorithm.getCodeDir());
                updateTask(userDTO, newTask);
                return new ModelOptUpdateVO(algorithm.getId(), algorithm.getCodeDir());
            }
            //非内置，算法为我的算法
            localFileUtil.copyFile(nfsConfig.getBucket() + modelOptTaskUpdateDTO.getModelAddress(), nfsConfig.getBucket() + modelOptTaskUpdateDTO.getAlgorithmPath() + "/model");
            updateTask(userDTO, newTask);
            return new ModelOptUpdateVO(modelOptTaskUpdateDTO.getAlgorithmId(), modelOptTaskUpdateDTO.getAlgorithmPath());
        }
        //内置
        updateTask(userDTO, newTask);
        return null;
    }

    /**
     * 修改任务
     *
     * @param userDTO 用户
     * @param newTask 任务
     */
    private void updateTask(UserDTO userDTO, ModelOptTask newTask) {
        int result = modelOptTaskMapper.updateById(newTask);
        if (result < MagicNumConstant.ONE) {
            LogUtil.error(LogEnum.MODEL_OPT, "用户{}修改模型优化任务, 数据库操作失败，任务id={}， 任务名:{}", userDTO.getUsername(),
                    newTask.getId(), newTask.getName());
            throw new BusinessException(ModelOptErrorEnum.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除模型优化任务
     *
     * @param modelOptTaskDeleteDTO 模型优化任务删除参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(ModelOptTaskDeleteDTO modelOptTaskDeleteDTO) {
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        ModelOptTask modelOptTask = modelOptTaskMapper.selectById(modelOptTaskDeleteDTO.getId());
        //用户不能删除其他用户的任务
        assert userDTO != null;
        if (!userDTO.getId().equals(modelOptTask.getCreateUserId())) {
            throw new BusinessException(ModelOptErrorEnum.MODEL_OPT_TASK_ABSENT);
        }
        checkTaskExist(modelOptTask);
        if (modelOptTaskInstanceService.checkUnfinishedInst(modelOptTaskDeleteDTO.getId())) {
            throw new BusinessException(ModelOptErrorEnum.MODEL_OPT_TASK_DELETE_ERROR);
        }
        int result = modelOptTaskMapper.deleteById(modelOptTaskDeleteDTO.getId());
        if (result < MagicNumConstant.ONE) {
            LogUtil.error(LogEnum.MODEL_OPT, "用户{}删除模型优化任务, 数据库操作失败，任务id={}， 任务名:{}", userDTO.getUsername(), modelOptTaskDeleteDTO.getId(), modelOptTask.getName());
            throw new BusinessException(ModelOptErrorEnum.INTERNAL_SERVER_ERROR);
        }
        //删除该任务对应的实例
        modelOptTaskInstanceService.deleteByTaskId(modelOptTaskDeleteDTO.getId());
    }

    /**
     * 获取内置模型
     *
     * @param type      算法类型
     * @param dataset   数据集
     * @param algorithm 训练算法
     * @return List<String> 内置模型列表
     */
    @Override
    public List<ModelOptModelQueryVO> getBuiltInModel(Integer type, String dataset, String algorithm) {
        return modelOptBuiltInMapper.getModel(type, dataset, algorithm);
    }

    /**
     * 获取优化算法
     *
     * @param type    算法类型
     * @param model   模型
     * @param dataset 数据集
     * @return List<ModelOptAlgorithmVO> 获取模型优化算法列表
     */
    @Override
    public List<ModelOptAlgorithmQueryVO> getAlgorithm(Integer type, String model, String dataset) {
        return modelOptBuiltInMapper.getAlgorithm(type, model, dataset);
    }

    /**
     * 获取模型优化数据集
     *
     * @param type      算法类型
     * @param model     模型
     * @param algorithm 训练算法
     * @return List<ModelOptDatasetQueryVO> 模型优化数据集列表
     */
    @Override
    public List<ModelOptDatasetQueryVO> getDataset(Integer type, String model, String algorithm) {
        return modelOptBuiltInMapper.getDataset(type, model, algorithm);
    }

    /**
     * 获取我的模型优化数据集
     *
     * @return List<ModelOptDatasetVO> 我的模型优化数据集列表
     */
    @Override
    public List<ModelOptDatasetVO> getMyDataset() {
        List<ModelOptDataset> list = modelOptDatasetMapper.getAllDataset();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<ModelOptDatasetVO> voList = new ArrayList<>();
        list.forEach(modelOptDataset -> {
            ModelOptDatasetVO vo = new ModelOptDatasetVO();
            BeanUtils.copyProperties(modelOptDataset, vo);
            voList.add(vo);
        });
        return voList;
    }

    /**
     * 创建我的模型优化数据集
     *
     * @param modelOptDatasetCreateDTO 数据集创建参数
     * @return ModelOptDatasetVO       模型优化模块我的数据集查询
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ModelOptDatasetVO createMyDataset(ModelOptDatasetCreateDTO modelOptDatasetCreateDTO) {
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        LambdaQueryWrapper<ModelOptDataset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelOptDataset::getName, modelOptDatasetCreateDTO.getName());
        assert userDTO != null;
        wrapper.eq(ModelOptDataset::getCreateUserId, userDTO.getId());
        List<ModelOptDataset> list = modelOptDatasetMapper.selectList(wrapper);
        if (!CollectionUtils.isEmpty(list)) {
            throw new BusinessException(ModelOptErrorEnum.DATASET_NAME_EXIST);
        }
        //源文件路径
        String sourcePath = nfsConfig.getBucket() + modelOptDatasetCreateDTO.getPath();
        if (nfsUtil.fileOrDirIsEmpty(sourcePath)) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The user {} upload path or source path {} does not exist", userDTO.getUsername(), sourcePath);
            throw new BusinessException("源文件或路径不存在");
        }
        ModelOptDataset modelOptDataset = new ModelOptDataset();
        modelOptDataset.setName(modelOptDatasetCreateDTO.getName());
        //校验path是否带有压缩文件，如有，则解压至当前文件夹并删除压缩文件
        String targetPath = ModelOptConstant.MY_OPT_DATASET_ROOT_PATH + StringUtils.getTimestamp() + SymbolConstant.SLASH;
        if (modelOptDatasetCreateDTO.getPath().endsWith(PtModelUtil.ZIP)) {
            //目标路径
            boolean unzip = localFileUtil.unzipLocalPath(sourcePath, nfsConfig.getBucket() + targetPath);
            if (!unzip) {
                LogUtil.error(LogEnum.BIZ_MODEL, "用户{}解压模型文件失败", userDTO.getUsername());
                throw new BusinessException("数据集文件解压失败");
            }
            modelOptDataset.setPath(targetPath);
        }

        int result = modelOptDatasetMapper.insert(modelOptDataset);
        if (result < MagicNumConstant.ONE) {
            LogUtil.error(LogEnum.MODEL_OPT, "用户{}创建模型优化数据集:{}, 数据库操作失败", userDTO.getUsername(), modelOptDatasetCreateDTO.getName());
            throw new BusinessException(ModelOptErrorEnum.INTERNAL_SERVER_ERROR);
        }
        return new ModelOptDatasetVO(modelOptDataset);
    }

    /**
     * 校验任务是否存在
     *
     * @param modelOptTask 模型优化任务对象
     */
    public void checkTaskExist(ModelOptTask modelOptTask) {
        if (modelOptTask == null) {
            throw new BusinessException(ModelOptErrorEnum.MODEL_OPT_TASK_ABSENT);
        }
    }

    /**
     * 校验任务名称是否已存在
     *
     * @param name   任务名称
     * @param userId 用户id
     */
    public void checkNameExist(String name, Long userId) {
        LambdaQueryWrapper<ModelOptTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelOptTask::getName, name);
        wrapper.eq(ModelOptTask::getCreateUserId, userId);
        List<ModelOptTask> list = modelOptTaskMapper.selectList(wrapper);
        if (CollectionUtil.isNotEmpty(list)) {
            throw new BusinessException(ModelOptErrorEnum.MODEL_OPT_TASK_NAME_EXIST);
        }
    }

    /**
     * 校验数据集是否存在
     *
     * @param datasetPath 数据集路径
     */
    public void checkDatasetExist(String datasetPath) {
        String path = k8sUtil.getAbsoluteNfsPath(datasetPath);
        if (nfsUtil.fileOrDirIsEmpty(path)) {
            throw new BusinessException(ModelOptErrorEnum.MODEL_OPT_DATASET_ABSENT);
        }
    }

    /**
     * 校验模型是否存在
     *
     * @param modelOptTask 模型优化任务
     * @param modelId      模型id
     */
    public void checkModelExist(ModelOptTask modelOptTask, Long modelId) {
        PtModelInfo ptModelInfo = ptModelInfoMapper.selectById(modelId);
        if (ptModelInfo == null) {
            throw new BusinessException(ModelOptErrorEnum.MODEL_OPT_MODEL_NOT_EXIST);
        }
        modelOptTask.setModelName(ptModelInfo.getName());
    }

    /**
     * 根据模型优化任务，生成任务实例
     *
     * @param task 模型优化任务对象
     * @return ModelOptTaskInstance 模型优化任务实例
     */
    private ModelOptTaskInstance buildInstance(ModelOptTask task) {
        ModelOptTaskInstance instance = new ModelOptTaskInstance();
        instance.setTaskId(task.getId());
        instance.setTaskName(task.getName());
        instance.setIsBuiltIn(task.getIsBuiltIn());
        instance.setModelId(task.getModelId());
        instance.setModelName(task.getModelName());
        instance.setModelAddress(task.getModelAddress());
        instance.setAlgorithmType(task.getAlgorithmType());
        instance.setAlgorithmId(task.getAlgorithmId());
        instance.setAlgorithmName(task.getAlgorithmName());
        instance.setAlgorithmPath(task.getAlgorithmPath());
        instance.setDatasetName(task.getDatasetName());
        instance.setDatasetPath(task.getDatasetPath());
        instance.setCommand(task.getCommand());
        instance.setParams(task.getParams());
        return instance;
    }
}
