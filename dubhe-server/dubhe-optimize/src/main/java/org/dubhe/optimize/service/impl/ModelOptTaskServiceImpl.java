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

package org.dubhe.optimize.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.dto.ModelOptAlgorithmCreateDTO;
import org.dubhe.biz.base.dto.PtModelInfoQueryByIdDTO;
import org.dubhe.biz.base.enums.DatasetTypeEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.DateUtil;
import org.dubhe.biz.base.utils.PtModelUtil;
import org.dubhe.biz.base.utils.ReflectionUtils;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.ModelOptAlgorithmQureyVO;
import org.dubhe.biz.base.vo.PtModelInfoQueryVO;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.db.utils.WrapperHelp;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.permission.annotation.DataPermissionMethod;
import org.dubhe.biz.permission.base.BaseService;
import org.dubhe.k8s.utils.K8sNameTool;
import org.dubhe.optimize.client.AlgorithmClient;
import org.dubhe.optimize.client.ModelInfoClient;
import org.dubhe.optimize.constant.ModelOptConstant;
import org.dubhe.optimize.enums.ModelOptErrorEnum;
import org.dubhe.optimize.dao.ModelOptBuiltInMapper;
import org.dubhe.optimize.dao.ModelOptDatasetMapper;
import org.dubhe.optimize.dao.ModelOptTaskMapper;
import org.dubhe.optimize.domain.dto.ModelOptDatasetCreateDTO;
import org.dubhe.optimize.domain.dto.ModelOptTaskCreateDTO;
import org.dubhe.optimize.domain.dto.ModelOptTaskDeleteDTO;
import org.dubhe.optimize.domain.dto.ModelOptTaskQueryDTO;
import org.dubhe.optimize.domain.dto.ModelOptTaskSubmitDTO;
import org.dubhe.optimize.domain.dto.ModelOptTaskUpdateDTO;
import org.dubhe.optimize.domain.entity.ModelOptDataset;
import org.dubhe.optimize.domain.entity.ModelOptTask;
import org.dubhe.optimize.domain.entity.ModelOptTaskInstance;
import org.dubhe.optimize.domain.vo.ModelOptAlgorithmQueryVO;
import org.dubhe.optimize.domain.vo.ModelOptCreateVO;
import org.dubhe.optimize.domain.vo.ModelOptDatasetQueryVO;
import org.dubhe.optimize.domain.vo.ModelOptDatasetVO;
import org.dubhe.optimize.domain.vo.ModelOptModelQueryVO;
import org.dubhe.optimize.domain.vo.ModelOptTaskQueryVO;
import org.dubhe.optimize.domain.vo.ModelOptUpdateVO;
import org.dubhe.optimize.enums.OptimizeTypeEnum;
import org.dubhe.optimize.service.ModelOptTaskInstanceService;
import org.dubhe.optimize.service.ModelOptTaskService;
import org.dubhe.recycle.config.RecycleConfig;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.service.RecycleService;
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
    private AlgorithmClient algorithmClient;

    @Resource
    private K8sNameTool k8sNameTool;

    @Resource
    private ModelInfoClient modelInfoClient;

    @Resource
    private ModelOptDatasetMapper modelOptDatasetMapper;

    @Resource
    private UserContextService userContextService;

    @Resource
    private RecycleConfig recycleConfig;

    @Resource
    private RecycleService recycleService;

    @Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;

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
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Object> queryAll(ModelOptTaskQueryDTO modelOptTaskQueryDTO) {
        modelOptTaskQueryDTO.timeConvert();
        String name = modelOptTaskQueryDTO.getName();
        //任务名称或id条件非空
        if (StringUtils.isNotEmpty(name)) {
            //整数匹配
            if (ModelOptConstant.PATTERN_NUM.matcher(name).matches()) {
                //查询条件为整数时，先根据id精确查询
                modelOptTaskQueryDTO.setId(Long.parseLong(name));
                modelOptTaskQueryDTO.setName(null);
                Map<String, Object> map = queryTask(modelOptTaskQueryDTO);
                //根据id查询结果为空时，再根据任务名称模糊查询
                if (((List<ModelOptTaskQueryVO>) map.get(ModelOptConstant.RESULT)).size() > NumberConstant.NUMBER_0) {
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
        UserContext curUser = userContextService.getCurUser();
        LogUtil.info(LogEnum.MODEL_OPT, "用户{}查询模型优化任务列表，查询条件为{}", curUser.getUsername(), JSONObject.toJSONString(modelOptTaskQueryDTO));
        QueryWrapper<ModelOptTask> wrapper = WrapperHelp.getWrapper(modelOptTaskQueryDTO);
        if (!BaseService.isAdmin(curUser)) {
            wrapper.eq("create_user_id", curUser.getId());
        }
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
            boolean isAsc = !StringUtils.isEmpty(modelOptTaskQueryDTO.getOrder()) && !StringUtils.equals(modelOptTaskQueryDTO.getOrder(), StringConstant.SORT_DESC);
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
        LogUtil.info(LogEnum.MODEL_OPT, "用户{}查询模型优化任务列表，任务数={}", curUser.getUsername(), queryVOList.size());
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
        UserContext curUser = userContextService.getCurUser();
        checkDatasetExist(modelOptTaskCreateDTO.getDatasetPath());
        if (curUser == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        checkNameExist(modelOptTaskCreateDTO.getName(), curUser.getId());
        ModelOptTask modelOptTask = new ModelOptTask();
        BeanUtils.copyProperties(modelOptTaskCreateDTO, modelOptTask);
        if (!modelOptTaskCreateDTO.getIsBuiltIn()) {
            checkModelExist(modelOptTask, modelOptTaskCreateDTO.getModelId());
            //选择非内置，且算法为内置算法时（算法id为空），需要添加到我的算法中
            if (modelOptTaskCreateDTO.getAlgorithmId() == null) {
                ModelOptAlgorithmQureyVO algorithm = forkAlgorithm(modelOptTaskCreateDTO.getAlgorithmType(), modelOptTaskCreateDTO.getAlgorithmName(), modelOptTaskCreateDTO.getAlgorithmPath(), modelOptTaskCreateDTO.getModelAddress());
                modelOptTask.setAlgorithmType(modelOptTaskCreateDTO.getAlgorithmType());
                modelOptTask.setAlgorithmId(algorithm.getId());
                modelOptTask.setAlgorithmName(algorithm.getAlgorithmName());
                modelOptTask.setAlgorithmPath(algorithm.getCodeDir());
                saveTask(curUser, modelOptTask);
                return new ModelOptCreateVO(algorithm.getId(), algorithm.getCodeDir());
            }
            //非内置，算法为我的算法
            fileStoreApi.copyPath(fileStoreApi.getRootDir() + fileStoreApi.getBucket() + modelOptTaskCreateDTO.getModelAddress(), fileStoreApi.getRootDir() + fileStoreApi.getBucket() + modelOptTaskCreateDTO.getAlgorithmPath() + ModelOptConstant.COPY_MODEL_POSTFIX);
            saveTask(curUser, modelOptTask);
            return new ModelOptCreateVO(modelOptTaskCreateDTO.getAlgorithmId(), modelOptTaskCreateDTO.getAlgorithmPath());
        }
        //内置
        saveTask(curUser, modelOptTask);
        return null;
    }

    /**
     * 保存模型任务
     *
     * @param curUser      用户信息
     * @param modelOptTask 模型任务信息
     */
    private void saveTask(UserContext curUser, ModelOptTask modelOptTask) {
        int result = modelOptTaskMapper.insert(modelOptTask);
        if (result < MagicNumConstant.ONE) {
            LogUtil.error(LogEnum.MODEL_OPT, "用户{}创建模型优化任务:{}, 数据库操作失败", curUser.getUsername(), modelOptTask.getName());
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
    private ModelOptAlgorithmQureyVO forkAlgorithm(Integer type, String algorithmName, String algorithmPath, String modelAddress) {
        String newAlgorithmName = algorithmName + SymbolConstant.HYPHEN + DateUtil.getTimestampStr();
        String newAlgorithmPath = ModelOptConstant.MY_OPT_ALGORITHM_ROOT_PATH + newAlgorithmName;
        String codeDir = SymbolConstant.BLANK;
        if (type != null) {
            codeDir = OptimizeTypeEnum.getCodeDirByType(type);
        }
        //copy算法
        fileStoreApi.copyPath(fileStoreApi.getRootDir() + fileStoreApi.getBucket() + algorithmPath, fileStoreApi.getRootDir() + fileStoreApi.getBucket() + newAlgorithmPath);
        //剪枝算法 我的模型需要拷贝到算法目录下
        if (OptimizeTypeEnum.SLIMMING.getType().equals(type)) {
            fileStoreApi.copyDir(fileStoreApi.getRootDir() + fileStoreApi.getBucket() + modelAddress, fileStoreApi.getRootDir() + fileStoreApi.getBucket() + newAlgorithmPath + codeDir + ModelOptConstant.COPY_MODEL_POSTFIX);
        }
        ModelOptAlgorithmCreateDTO modelOptAlgorithmCreateDTO = new ModelOptAlgorithmCreateDTO();
        modelOptAlgorithmCreateDTO.setName(newAlgorithmName);
        modelOptAlgorithmCreateDTO.setPath(newAlgorithmPath + codeDir);
        DataResponseBody<ModelOptAlgorithmQureyVO> dataResponseBody = algorithmClient.uploadAlgorithm(modelOptAlgorithmCreateDTO);
        ModelOptAlgorithmQureyVO modelOptAlgorithmQureyVO = null;
        if (dataResponseBody.succeed()) {
            modelOptAlgorithmQureyVO = dataResponseBody.getData();
        }
        return modelOptAlgorithmQureyVO;
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
        UserContext curUser = userContextService.getCurUser();
        ModelOptTask oldTask = modelOptTaskMapper.selectById(modelOptTaskUpdateDTO.getId());
        checkTaskExist(oldTask);
        //用户不能修改其他用户的任务
        if (curUser == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        if (!curUser.getId().equals(oldTask.getCreateUserId()) && !BaseService.isAdmin(curUser)) {
            throw new BusinessException(ModelOptErrorEnum.MODEL_OPT_TASK_ABSENT);
        }
        //如果修改了任务名，校验新的任务名是否存在
        if (!modelOptTaskUpdateDTO.getName().equals(oldTask.getName())) {
            checkNameExist(modelOptTaskUpdateDTO.getName(), curUser.getId());
        }
        checkDatasetExist(modelOptTaskUpdateDTO.getDatasetPath());
        ModelOptTask newTask = new ModelOptTask();
        BeanUtils.copyProperties(modelOptTaskUpdateDTO, newTask);
        if (!modelOptTaskUpdateDTO.getIsBuiltIn()) {
            checkModelExist(newTask, modelOptTaskUpdateDTO.getModelId());
            //选择非内置，且算法为内置算法时（算法id为空），需要添加到我的算法中
            if (modelOptTaskUpdateDTO.getAlgorithmId() == null) {
                ModelOptAlgorithmQureyVO algorithm = forkAlgorithm(modelOptTaskUpdateDTO.getAlgorithmType(), modelOptTaskUpdateDTO.getAlgorithmName(), modelOptTaskUpdateDTO.getAlgorithmPath(), modelOptTaskUpdateDTO.getModelAddress());
                newTask.setAlgorithmId(algorithm.getId());
                newTask.setAlgorithmName(algorithm.getAlgorithmName());
                newTask.setAlgorithmPath(algorithm.getCodeDir());
                updateTask(curUser, newTask);
                return new ModelOptUpdateVO(algorithm.getId(), algorithm.getCodeDir());
            }
            //非内置，算法为我的算法
            fileStoreApi.copyPath(fileStoreApi.getRootDir() + fileStoreApi.getBucket() + modelOptTaskUpdateDTO.getModelAddress(), fileStoreApi.getRootDir() + fileStoreApi.getBucket() + modelOptTaskUpdateDTO.getAlgorithmPath() + "/model");
            updateTask(curUser, newTask);
            return new ModelOptUpdateVO(modelOptTaskUpdateDTO.getAlgorithmId(), modelOptTaskUpdateDTO.getAlgorithmPath());
        }
        //内置
        updateTask(curUser, newTask);
        return null;
    }

    /**
     * 修改任务
     *
     * @param curUser 用户
     * @param newTask 任务
     */
    private void updateTask(UserContext curUser, ModelOptTask newTask) {
        int result = modelOptTaskMapper.updateById(newTask);
        if (result < MagicNumConstant.ONE) {
            LogUtil.error(LogEnum.MODEL_OPT, "用户{}修改模型优化任务, 数据库操作失败，任务id={}， 任务名:{}", curUser.getUsername(),
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
        UserContext curUser = userContextService.getCurUser();
        ModelOptTask modelOptTask = modelOptTaskMapper.selectById(modelOptTaskDeleteDTO.getId());
        if (curUser == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        if (!curUser.getId().equals(modelOptTask.getCreateUserId()) && !BaseService.isAdmin(curUser)) {
            //普通用户不能删除其他用户的任务
            throw new BusinessException(ModelOptErrorEnum.MODEL_OPT_TASK_ABSENT);
        }
        checkTaskExist(modelOptTask);
        if (modelOptTaskInstanceService.checkUnfinishedInst(modelOptTaskDeleteDTO.getId())) {
            throw new BusinessException(ModelOptErrorEnum.MODEL_OPT_TASK_DELETE_ERROR);
        }
        int result = modelOptTaskMapper.deleteById(modelOptTaskDeleteDTO.getId());
        if (result < MagicNumConstant.ONE) {
            LogUtil.error(LogEnum.MODEL_OPT, "用户{}删除模型优化任务, 数据库操作失败，任务id={}， 任务名:{}", curUser.getUsername(), modelOptTaskDeleteDTO.getId(), modelOptTask.getName());
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
        UserContext curUser = userContextService.getCurUser();
        LambdaQueryWrapper<ModelOptDataset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelOptDataset::getName, modelOptDatasetCreateDTO.getName());
        if (curUser == null) {
            throw new BusinessException("当前用户信息已失效");
        }
        wrapper.eq(ModelOptDataset::getCreateUserId, curUser.getId());
        List<ModelOptDataset> list = modelOptDatasetMapper.selectList(wrapper);
        if (!CollectionUtils.isEmpty(list)) {
            throw new BusinessException(ModelOptErrorEnum.DATASET_NAME_EXIST);
        }
        //源文件路径
        String sourcePath = fileStoreApi.getBucket() + modelOptDatasetCreateDTO.getPath();
        if (!fileStoreApi.fileOrDirIsExist(fileStoreApi.getRootDir() + sourcePath)) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The user {} upload path or source path {} does not exist", curUser.getUsername(), sourcePath);
            throw new BusinessException("源文件或路径不存在");
        }
        ModelOptDataset modelOptDataset = new ModelOptDataset();
        modelOptDataset.setName(modelOptDatasetCreateDTO.getName());
        //校验path是否带有压缩文件，如有，则解压至当前文件夹并删除压缩文件
        String targetPath = ModelOptConstant.MY_OPT_DATASET_ROOT_PATH + StringUtils.getTimestamp() + SymbolConstant.SLASH;
        if (modelOptDatasetCreateDTO.getPath().endsWith(PtModelUtil.ZIP)) {
            //目标路径
            boolean unzip = fileStoreApi.unzip(sourcePath, fileStoreApi.getBucket() + targetPath);
            if (!unzip) {
                LogUtil.error(LogEnum.BIZ_MODEL, "用户{}解压模型文件失败", curUser.getUsername());
                throw new BusinessException("数据集文件解压失败");
            }
            modelOptDataset.setPath(targetPath);
        }

        int result = modelOptDatasetMapper.insert(modelOptDataset);
        if (result < MagicNumConstant.ONE) {
            LogUtil.error(LogEnum.MODEL_OPT, "用户{}创建模型优化数据集:{}, 数据库操作失败", curUser.getUsername(), modelOptDatasetCreateDTO.getName());
            throw new BusinessException(ModelOptErrorEnum.INTERNAL_SERVER_ERROR);
        }
        return new ModelOptDatasetVO(modelOptDataset);
    }

    @Override
    public void recycleRollback(RecycleCreateDTO dto) {
        if (dto == null) {
            throw new BusinessException("模型优化任务数据还原失败!");
        }
        if (StrUtil.isNotBlank(dto.getRemark())) {
            modelOptTaskMapper.updateStatusById(Long.valueOf(dto.getRemark()), false);
        }
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
        String path = k8sNameTool.getAbsolutePath(datasetPath);
        if (!fileStoreApi.fileOrDirIsExist(path)) {
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
        PtModelInfoQueryByIdDTO ptModelInfoQueryByIdDTO = new PtModelInfoQueryByIdDTO();
        ptModelInfoQueryByIdDTO.setId(modelId);
        DataResponseBody<PtModelInfoQueryVO> modelInfoPresetDataResponseBody = modelInfoClient.getByModelId(ptModelInfoQueryByIdDTO);
        PtModelInfoQueryVO ptModelInfoPresetQueryVO = null;
        if (modelInfoPresetDataResponseBody.succeed()) {
            ptModelInfoPresetQueryVO = modelInfoPresetDataResponseBody.getData();
        }
        if (ptModelInfoPresetQueryVO == null) {
            throw new BusinessException(ModelOptErrorEnum.MODEL_OPT_MODEL_NOT_EXIST);
        }
        modelOptTask.setModelName(ptModelInfoPresetQueryVO.getName());
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
        instance.setModelBranchId(task.getModelBranchId());
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
        //实例创建用户与任务保持一致
        instance.setCreateUserId(task.getCreateUserId());
        return instance;
    }
}
