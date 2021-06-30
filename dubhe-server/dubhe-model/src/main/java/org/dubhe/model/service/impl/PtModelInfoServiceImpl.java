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

package org.dubhe.model.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.dto.PtModelInfoConditionQueryDTO;
import org.dubhe.biz.base.dto.PtModelInfoQueryByIdDTO;
import org.dubhe.biz.base.dto.PtModelStatusQueryDTO;
import org.dubhe.biz.base.enums.DatasetTypeEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.PtModelUtil;
import org.dubhe.biz.base.utils.ReflectionUtils;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.PtModelInfoQueryVO;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.permission.annotation.DataPermissionMethod;
import org.dubhe.biz.permission.base.BaseService;
import org.dubhe.cloud.remotecall.config.RestTemplateHolder;
import org.dubhe.model.constant.ModelConstants;
import org.dubhe.model.dao.PtModelBranchMapper;
import org.dubhe.model.dao.PtModelInfoMapper;
import org.dubhe.model.domain.dto.*;
import org.dubhe.model.domain.entity.PtModelBranch;
import org.dubhe.model.domain.entity.PtModelInfo;
import org.dubhe.model.domain.enums.ModelPackageEnum;
import org.dubhe.model.domain.enums.ModelResourceEnum;
import org.dubhe.model.domain.vo.PtModelInfoByResourceVO;
import org.dubhe.model.domain.vo.PtModelInfoCreateVO;
import org.dubhe.model.domain.vo.PtModelInfoDeleteVO;
import org.dubhe.model.domain.vo.PtModelInfoUpdateVO;
import org.dubhe.model.service.FileService;
import org.dubhe.model.service.PtModelBranchService;
import org.dubhe.model.service.PtModelInfoService;
import org.dubhe.model.utils.ModelStatusUtil;
import org.dubhe.recycle.config.RecycleConfig;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.domain.dto.RecycleDetailCreateDTO;
import org.dubhe.recycle.enums.RecycleModuleEnum;
import org.dubhe.recycle.enums.RecycleResourceEnum;
import org.dubhe.recycle.enums.RecycleTypeEnum;
import org.dubhe.recycle.service.RecycleService;
import org.dubhe.recycle.utils.RecycleTool;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description 模型管理
 * @date 2020-03-24
 */
@Service
public class PtModelInfoServiceImpl implements PtModelInfoService {

    @Autowired
    private PtModelBranchMapper ptModelBranchMapper;

    @Autowired
    private PtModelInfoMapper ptModelInfoMapper;

    @Value("${model.measuring.url.package}")
    private String modelMeasuringUrlPackage;

    @Autowired
    private PtModelBranchService ptModelBranchService;

    @Autowired
    private FileService fileService;

    @Autowired
    private RecycleService recycleService;

    @Autowired
    private RecycleConfig recycleConfig;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private RestTemplateHolder restTemplateHolder;

    @Autowired
    private ModelStatusUtil ModelStatusUtil;

    public final static List<String> FIELD_NAMES;

    static {
        FIELD_NAMES = ReflectionUtils.getFieldNames(PtModelInfoQueryVO.class);
    }

    /**
     * 查询数据分页
     *
     * @param ptModelInfoQueryDTO 模型管理查询参数
     * @return Map<String, Object> 模型管理分页对象
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Object> queryAll(PtModelInfoQueryDTO ptModelInfoQueryDTO) {
        Page page = ptModelInfoQueryDTO.toPage();
        QueryWrapper<PtModelInfo> wrapper = new QueryWrapper<>();
        String modelName = ptModelInfoQueryDTO.getName();
        if (!StringUtils.isEmpty(modelName)) {
            wrapper.and(qw -> qw.eq("id", modelName).or().like("name",
                    modelName));
        }
        ModelResourceEnum modelResourceEnum = ModelResourceEnum.get(ptModelInfoQueryDTO.getModelResource());
        wrapper.eq("model_resource", modelResourceEnum.getCode());

        Integer packaged = ptModelInfoQueryDTO.getPackaged();
        wrapper.eq(packaged != null, "packaged", packaged);

        String modelClassName = ptModelInfoQueryDTO.getModelClassName();
        wrapper.like(!StringUtils.isEmpty(modelClassName), "model_type", modelClassName);

        String orderField = FIELD_NAMES.contains(ptModelInfoQueryDTO.getSort())
                ? StringUtils.humpToLine(ptModelInfoQueryDTO.getSort())
                : PtModelUtil.ID;
        boolean isAsc = PtModelUtil.SORT_ASC.equalsIgnoreCase(ptModelInfoQueryDTO.getOrder());
        wrapper.orderBy(true, isAsc, orderField);

        Page<PtModelInfo> ptModelInfos = ptModelInfoMapper.selectPage(page, wrapper);

        List<PtModelInfoQueryVO> ptModelInfoQueryVOList = ptModelInfos.getRecords().stream().map(x -> {
            PtModelInfoQueryVO vo = new PtModelInfoQueryVO();
            BeanUtils.copyProperties(x, vo);
            //模型是否能提供服务(现有：tensorflow和oneflow,keras的都是savedmodel，pytorch的是pth,以下值为字典值)
            boolean flag = (x.getFrameType() == PtModelUtil.NUMBER_ONE && x.getModelType() == PtModelUtil.NUMBER_ONE)
                    || (x.getFrameType() == PtModelUtil.NUMBER_TWO && x.getModelType() == PtModelUtil.NUMBER_ONE)
                    || (x.getFrameType() == PtModelUtil.NUMBER_FOUR && x.getModelType() == PtModelUtil.NUMBER_ONE)
                    || (x.getFrameType() == PtModelUtil.NUMBER_THREE && x.getModelType() == PtModelUtil.NUMBER_EIGHT);
            vo.setServingModel(flag);
            return vo;
        }).collect(Collectors.toList());
        return PageUtil.toPage(page, ptModelInfoQueryVOList);
    }

    /**
     * 创建
     *
     * @param ptModelInfoCreateDTO 模型管理创建对象
     * @return PtModelInfoCreateVO 模型管理返回创建VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PtModelInfoCreateVO create(PtModelInfoCreateDTO ptModelInfoCreateDTO) {
        //获取用户信息
        UserContext user = userContextService.getCurUser();
        //模型名称校验
        QueryWrapper<PtModelInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", ptModelInfoCreateDTO.getName()).and(wrapper -> wrapper.eq("create_user_id", user.getId()).or().eq("origin_user_id", 0L));
        Integer countResult = ptModelInfoMapper.selectCount(queryWrapper);
        if (countResult > 0) {
            LogUtil.error(LogEnum.BIZ_MODEL, "The user {} fail to save model，the name of model is already exist !", user.getUsername());
            throw new BusinessException("模型名称已存在");
        }

        boolean isAtlas = ModelResourceEnum.ATLAS.getCode().equals(ptModelInfoCreateDTO.getModelResource());
        if (isAtlas) {
            String sourcePath = ptModelInfoCreateDTO.getModelAddress();
            if (StringUtils.isBlank(sourcePath)) {
                LogUtil.error(LogEnum.BIZ_MODEL, "The user {} fail to save model，the address of model is blank !", user.getUsername());
                throw new BusinessException("模型创建失败，请填写炼知模型的地址");
            }
            String targetPath = fileService.transfer(sourcePath, user);
            //修改存储路径
            ptModelInfoCreateDTO.setModelAddress(targetPath);
        }

        //保存任务参数
        PtModelInfo ptModelInfo = new PtModelInfo();
        BeanUtils.copyProperties(ptModelInfoCreateDTO, ptModelInfo);

        if (ptModelInfoMapper.insert(ptModelInfo) < 1) {
            //模型管理未保存成功，抛出异常，并返回失败信息
            LogUtil.error(LogEnum.BIZ_MODEL, "The user {} failed to save the model and failed to insert the model management table", user.getUsername());
            throw new BusinessException("模型创建失败");
        }

        //如果上传的模型存在，则创建一个版本
        if (!isAtlas && ptModelInfoCreateDTO.getModelAddress() != null) {
            PtModelBranchCreateDTO ptModelBranchCreateDTO = new PtModelBranchCreateDTO();
            BeanUtils.copyProperties(ptModelInfoCreateDTO, ptModelBranchCreateDTO);
            ptModelBranchCreateDTO.setParentId(ptModelInfo.getId());
            ptModelBranchService.create(ptModelBranchCreateDTO);
        }
        PtModelInfoCreateVO ptModelInfoCreateVO = new PtModelInfoCreateVO();
        ptModelInfoCreateVO.setId(ptModelInfo.getId());
        return ptModelInfoCreateVO;
    }

    /**
     * 编辑
     *
     * @param ptModelInfoUpdateDTO 模型管理修改对象
     * @return PtModelInfoUpdateVO 模型管理返回更新VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PtModelInfoUpdateVO update(PtModelInfoUpdateDTO ptModelInfoUpdateDTO) {
        //获取用户信息
        UserContext user = userContextService.getCurUser();
        //模型名称校验
        QueryWrapper<PtModelInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", ptModelInfoUpdateDTO.getName()).ne("id", ptModelInfoUpdateDTO.getId());
        Integer countResult = ptModelInfoMapper.selectCount(queryWrapper);
        if (countResult > 0) {
            throw new BusinessException("模型名称已存在");
        }

        //权限校验
        QueryWrapper<PtModelInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("id", ptModelInfoUpdateDTO.getId());
        if (ptModelInfoMapper.selectCount(wrapper) < 1) {
            LogUtil.error(LogEnum.BIZ_MODEL, "The user {} failed to modify the model and has no permission to modify the corresponding data in the model table", user.getUsername());
            throw new BusinessException("您修改的ID不存在请重新输入");
        }

        //修改任务参数
        PtModelInfo ptModelInfo = new PtModelInfo();
        BeanUtils.copyProperties(ptModelInfoUpdateDTO, ptModelInfo);
        if (ptModelInfoMapper.updateById(ptModelInfo) < 1) {
            //任务参数未修改成功，抛出异常，并返回失败信息
            LogUtil.error(LogEnum.BIZ_MODEL, "User {} failed to modify the model, failed to modify the model table", user.getUsername());
            throw new BusinessException("模型更新失败");
        }

        PtModelInfoUpdateVO ptModelInfoUpdateVO = new PtModelInfoUpdateVO();
        ptModelInfoUpdateVO.setId(ptModelInfo.getId());
        return ptModelInfoUpdateVO;
    }

    /**
     * 多选删除
     *
     * @param ptModelInfoDeleteDTO 模型管理删除对象
     * @return PtModelInfoDeleteVO 模型管理返回删除VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PtModelInfoDeleteVO deleteAll(PtModelInfoDeleteDTO ptModelInfoDeleteDTO) {
        //获取用户信息
        UserContext user = userContextService.getCurUser();
        //数组ids去重
        List<Long> ids = Arrays.stream(ptModelInfoDeleteDTO.getIds()).distinct().collect(Collectors.toList());

        //权限校验
        QueryWrapper<PtModelInfo> query = new QueryWrapper<>();
        //非管理员不可删除预置模型
        if (!BaseService.isAdmin(user)) {
            query.eq("model_resource", 0);
        }
        query.in("id", ids);
        if (ptModelInfoMapper.selectCount(query) < ids.size()) {
            LogUtil.error(LogEnum.BIZ_MODEL, "The user {} failed to delete the model list, and has no permission to delete the corresponding data in the model management table", user.getUsername());
            throw new BusinessException("您没有此权限");
        }
        //删除时远程调用查询关联业务模块是否有正在使用该模型(需长期维护：若新增使用模型的微服务，需添加其远程调用)
        PtModelStatusQueryDTO ptModelStatusQueryDTO = new PtModelStatusQueryDTO();
        ptModelStatusQueryDTO.setModelIds(ids);
        ModelStatusUtil.queryModelStatus(user, ptModelStatusQueryDTO, ids);

        //删除任务参数
        if (ptModelInfoMapper.deleteBatchIds(ids) < ids.size()) {
            //模型列表未删除成功,抛出异常，并返回失败信息
            LogUtil.error(LogEnum.BIZ_MODEL, "The user {} failed to delete the model list. The model management table deletion operation based on ID array {} failed", user.getUsername(), ids);
            throw new BusinessException("模型删除失败");
        }

        QueryWrapper queryBranch = new QueryWrapper<>();
        queryBranch.in("parent_id", ids);

        List<PtModelBranch> ptModelBranches = ptModelBranchMapper.selectList(queryBranch);
        List<Long> branchlists = ptModelBranches.stream().map(x -> {
            return x.getId();
        }).collect(Collectors.toList());
        if (branchlists.size() > 0) {
            if (ptModelBranchMapper.deleteBatchIds(branchlists) < branchlists.size()) {
                LogUtil.error(LogEnum.BIZ_MODEL, "User {} failed to delete model version. Deleting model version table according to ID array {} failed", user.getUsername(), ids);
                throw new BusinessException("模型删除失败");
            }
            //定时任务删除相应的模型文件
            for (PtModelBranch ptModelBranch : ptModelBranches) {
                RecycleCreateDTO recycleCreateDTO = RecycleCreateDTO.builder()
                        .recycleModule(RecycleModuleEnum.BIZ_MODEL.getValue())
                        .recycleDelayDate(recycleConfig.getModelValid())
                        .recycleNote(RecycleTool.generateRecycleNote("删除模型文件", ptModelBranch.getId()))
                        .remark(ptModelBranch.getId().toString())
                        .restoreCustom(RecycleResourceEnum.MODEL_RECYCLE_FILE.getClassName())
                        .build();
                recycleCreateDTO.addRecycleDetailCreateDTO(RecycleDetailCreateDTO.builder()
                        .recycleType(RecycleTypeEnum.FILE.getCode())
                        .recycleCondition(fileService.getAbsolutePath(ptModelBranch.getModelAddress()))
                        .recycleNote(RecycleTool.generateRecycleNote("删除模型文件", ptModelBranch.getId()))
                        .build()
                );
                recycleService.createRecycleTask(recycleCreateDTO);
            }
        }

        //返回删除的模型管理参数id数组
        PtModelInfoDeleteVO ptModelInfoDeleteVO = new PtModelInfoDeleteVO();
        ptModelInfoDeleteVO.setIds(ptModelInfoDeleteDTO.getIds());
        return ptModelInfoDeleteVO;
    }

    /**
     * 根据模型来源查询模型信息
     *
     * @param ptModelInfoByResourceDTO 模型查询对象
     * @return PtModelInfoByResourceVO  模型返回查询VO
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public List<PtModelInfoByResourceVO> getModelByResource(PtModelInfoByResourceDTO ptModelInfoByResourceDTO) {

        LambdaQueryWrapper<PtModelInfo> query = new LambdaQueryWrapper<>();
        query.eq(PtModelInfo::getModelResource, ptModelInfoByResourceDTO.getModelResource())
                .eq(ModelPackageEnum.isValid(ptModelInfoByResourceDTO.getPackaged()), PtModelInfo::getPackaged, ptModelInfoByResourceDTO.getPackaged())
                .isNotNull(PtModelInfo::getModelAddress)
                .ne(PtModelInfo::getModelAddress, SymbolConstant.BLANK).orderByDesc(PtModelInfo::getId);

        List<PtModelInfo> ptModelInfos = ptModelInfoMapper.selectList(query);
        ArrayList<PtModelInfoByResourceVO> ptModelInfoByResourceVOS = new ArrayList<>();

        ptModelInfos.forEach(ptModelInfo -> {
            PtModelInfoByResourceVO ptModelInfoByResourceVO = new PtModelInfoByResourceVO();
            BeanUtil.copyProperties(ptModelInfo, ptModelInfoByResourceVO);
            ptModelInfoByResourceVO.setUrl(ptModelInfo.getModelAddress());
            ptModelInfoByResourceVOS.add(ptModelInfoByResourceVO);
        });

        return ptModelInfoByResourceVOS;
    }

    /**
     * 根据模型id查询模型详情
     *
     * @param ptModelInfoQueryByIdDTO 根据模型id查询模型详情查询参数
     * @return PtModelBranchQueryByIdVO 根据模型id查询模型详情返回结果
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public PtModelInfoQueryVO queryByModelId(PtModelInfoQueryByIdDTO ptModelInfoQueryByIdDTO) {
        PtModelInfo ptModelInfo = ptModelInfoMapper.selectById(ptModelInfoQueryByIdDTO.getId());
        if (ptModelInfo == null) {
            return null;
        }
        PtModelInfoQueryVO ptModelInfoQueryByIdVO = new PtModelInfoQueryVO();
        BeanUtils.copyProperties(ptModelInfo, ptModelInfoQueryByIdVO);
        return ptModelInfoQueryByIdVO;
    }

    /**
     * 根据模型条件查询模型详情列表
     *
     * @param ptModelInfoConditionQueryDTO 查询条件
     * @return List<PtModelInfoQueryVO> 查询结果列表
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public List<PtModelInfoQueryVO> getConditionQuery(PtModelInfoConditionQueryDTO ptModelInfoConditionQueryDTO) {
        LambdaQueryWrapper<PtModelInfo> query = new LambdaQueryWrapper<>();
        query.eq(PtModelInfo::getModelResource, ptModelInfoConditionQueryDTO.getModelResource())
                .in(PtModelInfo::getId, ptModelInfoConditionQueryDTO.getIds())
                .isNotNull(PtModelInfo::getModelAddress)
                .ne(PtModelInfo::getModelAddress, SymbolConstant.BLANK);
        List<PtModelInfo> modelInfoList = ptModelInfoMapper.selectList(query);
        List<PtModelInfoQueryVO> modelInfoQueryList = modelInfoList.stream().map(x -> {
                    PtModelInfoQueryVO ptModelInfoQueryVO = new PtModelInfoQueryVO();
                    BeanUtils.copyProperties(x, ptModelInfoQueryVO);
                    return ptModelInfoQueryVO;
                }
        ).collect(Collectors.toList());
        return modelInfoQueryList;
    }

    /**
     * 构建远程打包参数
     *
     * @param ptModelInfoPackageDTO 打包炼知模型DTO
     * @param ptModelInfo           模型信息
     * @return 打包参数
     */
    private JSONObject buildPackageAtlasModelParams(PtModelInfoPackageDTO ptModelInfoPackageDTO, PtModelInfo ptModelInfo) {
        //拼装参数
        JSONObject metadata = new JSONObject();
        JSONObject params = new JSONObject();
        JSONObject input = new JSONObject();
        input.put(ModelConstants.SIZE, ptModelInfoPackageDTO.getSize());
        input.put(ModelConstants.RANGE, ptModelInfoPackageDTO.getRange());
        input.put(ModelConstants.SPACE, ptModelInfoPackageDTO.getRange());

        JSONObject normalize = new JSONObject();
        input.put(ModelConstants.NORMALIZE, normalize);
        normalize.put(ModelConstants.STD, ptModelInfoPackageDTO.getStd());
        normalize.put(ModelConstants.MEAN, ptModelInfoPackageDTO.getMean());

        metadata.put(ModelConstants.NAME, ptModelInfoPackageDTO.getName());
        metadata.put(ModelConstants.URL, ptModelInfoPackageDTO.getUrl());
        metadata.put(ModelConstants.TASK, ptModelInfoPackageDTO.getTask());
        metadata.put(ModelConstants.INPUT, input);
        metadata.put(ModelConstants.DATASET, ptModelInfoPackageDTO.getDataset());

        JSONObject entryArgs = new JSONObject();
        entryArgs.put(ModelConstants.PRETRAINED, ptModelInfoPackageDTO.getEntryPretrained());
        entryArgs.put(ModelConstants.NUM_CLASSES, ptModelInfoPackageDTO.getEntryNumClasses());
        metadata.put(ModelConstants.ENTRY_ARGS, entryArgs);

        JSONObject otherMetadata = new JSONObject();
        otherMetadata.put(ModelConstants.NUM_CLASSES, ptModelInfoPackageDTO.getOtherNumClasses());
        metadata.put(ModelConstants.OTHER_METADATA, otherMetadata);

        params.put(ModelConstants.METADATA, metadata);


        //添加打包路径
        params.put(ModelConstants.CKPT, ptModelInfo.getModelAddress());
        params.put(ModelConstants.ENTRY_NAME, ptModelInfoPackageDTO.getEntryName());
        params.put(ModelConstants.README, ptModelInfoPackageDTO.getReadme());
        return params;
    }

    /**
     * 将炼知模型打包
     *
     * @param ptModelInfoPackageDTO 打包参数
     * @return Boolean              打包结果 true 成功 false 失败
     */
    @Override
    public String packageAtlasModel(PtModelInfoPackageDTO ptModelInfoPackageDTO) {
        //获取用户信息
        UserContext user = userContextService.getCurUser();
        Long modelId = ptModelInfoPackageDTO.getId();
        PtModelInfo ptModelInfo = validateAtlasModel(modelId);

        JSONObject params = buildPackageAtlasModelParams(ptModelInfoPackageDTO, ptModelInfo);

        JSONArray jsonArray = new JSONArray();

        jsonArray.add(params);

        RestTemplate restTemplate = restTemplateHolder.getRestTemplate();

        LogUtil.error(LogEnum.BIZ_MODEL, "远程打包服务调用参数：url:{},jsonArray{}", modelMeasuringUrlPackage, jsonArray);
        DataResponseBody<List<?>> result = restTemplate.postForObject(modelMeasuringUrlPackage, jsonArray, DataResponseBody.class);

        if (result == null || !result.succeed()) {
            LogUtil.error(LogEnum.BIZ_MODEL, "用户【{}】模型打包失败，远程打包服务调用异常。返回参数：{}", user.getUsername(), result);
            throw new BusinessException("远程打包服务调用异常！");
        }
        if (CollectionUtils.isEmpty(result.getData())) {
            LogUtil.error(LogEnum.BIZ_MODEL, "用户【{}】模型打包失败,远程打包后无返回的路径，", user.getUsername());
            throw new BusinessException("远程打包后无返回的路径！");
        }

        PtModelInfo updateEntity = new PtModelInfo();
        updateEntity.setId(ptModelInfoPackageDTO.getId());
        String savePath = result.getData().get(0) + "";
        //更新打包后的路径
        updateEntity.setModelAddress(savePath);
        updateEntity.setPackaged(ModelPackageEnum.PACKAGED.getCode());
        params.put(ModelConstants.SAVE_PATH, savePath);
        updateEntity.setTags(params.toJSONString());

        int res = ptModelInfoMapper.updateById(updateEntity);
        if (res < 1) {
            LogUtil.error(LogEnum.BIZ_MODEL, "用户【{}】模型打包失败,远程打包完成后，更新地址失败", user.getUsername());
            throw new BusinessException("更新地址失败！");
        }

        return result.getMsg();
    }

    /**
     * 校验炼知模型
     *
     * @param modelId 模型id
     */
    private PtModelInfo validateAtlasModel(Long modelId) {
        UserContext user = userContextService.getCurUser();
        //校验是否为炼知模型
        PtModelInfo ptModelInfo = ptModelInfoMapper.selectById(modelId);

        if (null == ptModelInfo) {
            LogUtil.error(LogEnum.BIZ_MODEL, "用户【{}】打包炼知模型失败,该ID【{}】下数据不存在！", user.getUsername(), modelId);
            throw new BusinessException("无此模型！");
        }

        if (!ModelResourceEnum.ATLAS.getCode().equals(ptModelInfo.getModelResource())) {
            LogUtil.error(LogEnum.BIZ_MODEL, "用户【{}】打包炼知模型失败，该模型不是炼知模型！", user.getUsername());
            throw new BusinessException("该模型不是炼知模型！");
        }
        if (ModelPackageEnum.PACKAGED.getCode().equals(ptModelInfo.getPackaged())) {
            LogUtil.error(LogEnum.BIZ_MODEL, "用户【{}】打包炼知模型失败，该炼知模型已经打包过，请勿重复打包", user.getUsername());
            throw new BusinessException("请勿重复打包！");
        }
        return ptModelInfo;
    }

    /**
     * 模型优化上传模型
     *
     * @param ptModelOptimizationCreateDTO 模型优化上传模型入参
     * @return PtModelInfoByResourceVO 模型优化上传模型返回值
     */
    @Override
    public PtModelInfoByResourceVO modelOptimizationUploadModel(PtModelOptimizationCreateDTO ptModelOptimizationCreateDTO) {
        PtModelInfoCreateDTO ptModelInfoCreateDTO = new PtModelInfoCreateDTO();
        ptModelInfoCreateDTO.setName(ptModelOptimizationCreateDTO.getName()).setModelAddress(ptModelOptimizationCreateDTO.getPath()).setModelSource(PtModelUtil.NUMBER_ZERO).setFrameType(PtModelUtil.NUMBER_ONE).setModelType(PtModelUtil.NUMBER_ONE).setModelClassName("模型优化").setModelDescription("模型优化上传模型");
        PtModelInfoCreateVO ptModelInfoCreateVO = create(ptModelInfoCreateDTO);
        PtModelInfo ptModelInfo = ptModelInfoMapper.selectById(ptModelInfoCreateVO.getId());
        PtModelInfoByResourceVO ptModelInfoByResourceVO = new PtModelInfoByResourceVO();
        BeanUtil.copyProperties(ptModelInfo, ptModelInfoByResourceVO);
        return ptModelInfoByResourceVO;
    }

    /**
     * 查询能提供服务的模型
     * 现有：tensorflow和oneflow,keras的都是savedmodel，pytorch的是pth
     * @return 能提供服务的模型实体集合
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public List<PtModelInfoQueryVO> getServingModel(ServingModelDTO servingModelDTO) {
        UserContext user = userContextService.getCurUser();
        QueryWrapper<PtModelInfo> query = new QueryWrapper<>();
        query.eq("model_resource", servingModelDTO.getModelResource()).eq("model_format", PtModelUtil.NUMBER_ONE).in("frame_type", PtModelUtil.NUMBER_ONE, PtModelUtil.NUMBER_TWO, PtModelUtil.NUMBER_FOUR)
                .isNotNull("model_version").ne("model_version", "");
        query.or(qw -> qw.eq("model_resource", servingModelDTO.getModelResource()).eq("model_format", PtModelUtil.NUMBER_EIGHT).eq("frame_type", PtModelUtil.NUMBER_THREE)
                .isNotNull("model_version").ne("model_version", ""));
        query.orderByDesc("id");
        List<PtModelInfo> ptModelInfos = null;
        try {
            ptModelInfos = ptModelInfoMapper.selectList(query);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_MODEL, "Exception for user {} to query the details of the model that can provide serving, because:{}", user.getUsername(), e);
            throw new BusinessException("模型查询异常");
        }
        if (CollectionUtils.isEmpty(ptModelInfos)) {
            return null;
        }
        List<PtModelInfoQueryVO> servingModelList = ptModelInfos.stream().map(x -> {
            PtModelInfoQueryVO vo = new PtModelInfoQueryVO();
            BeanUtils.copyProperties(x, vo);
            return vo;
        }).collect(Collectors.toList());
        return servingModelList;
    }

}