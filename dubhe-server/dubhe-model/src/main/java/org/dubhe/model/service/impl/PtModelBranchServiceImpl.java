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

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.dto.PtModelBranchConditionQueryDTO;
import org.dubhe.biz.base.dto.PtModelBranchQueryByIdDTO;
import org.dubhe.biz.base.dto.PtModelStatusQueryDTO;
import org.dubhe.biz.base.enums.DatasetTypeEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.PtModelUtil;
import org.dubhe.biz.base.utils.ReflectionUtils;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.PtModelBranchQueryVO;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.db.utils.WrapperHelp;
import org.dubhe.biz.file.enums.BizPathEnum;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.permission.annotation.DataPermissionMethod;
import org.dubhe.cloud.remotecall.config.RestTemplateHolder;
import org.dubhe.k8s.utils.K8sNameTool;
import org.dubhe.model.dao.PtModelBranchMapper;
import org.dubhe.model.dao.PtModelInfoMapper;
import org.dubhe.model.domain.dto.*;
import org.dubhe.model.domain.entity.PtModelBranch;
import org.dubhe.model.domain.entity.PtModelInfo;
import org.dubhe.model.domain.enums.ModelConvertEnum;
import org.dubhe.model.domain.enums.ModelCopyStatusEnum;
import org.dubhe.model.domain.vo.PtModelBranchCreateVO;
import org.dubhe.model.domain.vo.PtModelBranchDeleteVO;
import org.dubhe.model.domain.vo.PtModelBranchUpdateVO;
import org.dubhe.model.domain.vo.PtModelConvertOnnxVO;
import org.dubhe.model.service.FileService;
import org.dubhe.model.service.PtModelBranchService;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @description 模型版本管理
 * @date 2020-03-24
 */
@Service
public class PtModelBranchServiceImpl implements PtModelBranchService {

    @Autowired
    private PtModelBranchMapper ptModelBranchMapper;

    @Autowired
    private PtModelInfoMapper ptModelInfoMapper;

    @Autowired
    private RecycleService recycleService;

    @Autowired
    private RecycleConfig recycleConfig;

    @Autowired
    private FileService fileService;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private ModelStatusUtil ModelStatusUtil;

    @Autowired
    private K8sNameTool k8sNameTool;

    @Autowired
    private RestTemplateHolder restTemplateHolder;

    @Value("${model.converter.url}")
    private String modelConverterUrl;

    public final static List<String> FIELD_NAMES;

    static {
        FIELD_NAMES = ReflectionUtils.getFieldNames(PtModelBranchQueryVO.class);
    }

    /**
     * 查询数据分页
     *
     * @param ptModelBranchQueryDTO 模型版本管理查询参数
     * @return Map<String, Object>  模型版本管理分页对象
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Object> queryAll(PtModelBranchQueryDTO ptModelBranchQueryDTO) {
        Page page = ptModelBranchQueryDTO.toPage();
        QueryWrapper wrapper = WrapperHelp.getWrapper(ptModelBranchQueryDTO);

        String orderField = FIELD_NAMES.contains(ptModelBranchQueryDTO.getSort())
                ? StringUtils.humpToLine(ptModelBranchQueryDTO.getSort())
                : PtModelUtil.ID;
        boolean isAsc = PtModelUtil.SORT_ASC.equalsIgnoreCase(ptModelBranchQueryDTO.getOrder());
        wrapper.orderBy(true, isAsc, orderField);

        IPage<PtModelBranch> ptModelBranches = ptModelBranchMapper.selectPage(page, wrapper);

        PtModelInfo ptModelInfo = ptModelInfoMapper.selectById(ptModelBranchQueryDTO.getParentId());
        List<PtModelBranchQueryVO> ptModelBranchQueryVOs = ptModelBranches.getRecords().stream().map(x -> {
            PtModelBranchQueryVO ptModelBranchQueryVO = new PtModelBranchQueryVO();
            BeanUtils.copyProperties(x, ptModelBranchQueryVO);
            ptModelBranchQueryVO.setName(ptModelInfo.getName()).setModelDescription(ptModelInfo.getModelDescription());
            //模型是否能提供服务(现有：tensorflow和oneflow,keras的都是savedmodel，pytorch的是pth,以下值为字典值)
            boolean flag = (ptModelInfo.getFrameType() == PtModelUtil.NUMBER_ONE && ptModelInfo.getModelType() == PtModelUtil.NUMBER_ONE) ||
                    (ptModelInfo.getFrameType() == PtModelUtil.NUMBER_TWO && ptModelInfo.getModelType() == PtModelUtil.NUMBER_ONE) ||
                    (ptModelInfo.getFrameType() == PtModelUtil.NUMBER_FOUR && ptModelInfo.getModelType() == PtModelUtil.NUMBER_ONE) ||
                    (ptModelInfo.getFrameType() == PtModelUtil.NUMBER_THREE && ptModelInfo.getModelType() == PtModelUtil.NUMBER_EIGHT);
            ptModelBranchQueryVO.setServingModel(flag);
            return ptModelBranchQueryVO;
        }).collect(Collectors.toList());
        return PageUtil.toPage(page, ptModelBranchQueryVOs);
    }

    /**
     * 创建
     *
     * @param ptModelBranchCreateDTO 模型版本管理创建对象
     * @return PtModelBranchCreateVO 模型版本管理返回创建VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PtModelBranchCreateVO create(PtModelBranchCreateDTO ptModelBranchCreateDTO) {
        //获取用户信息
        UserContext user = userContextService.getCurUser();
        PtModelBranch ptModelBranch = new PtModelBranch();
        BeanUtils.copyProperties(ptModelBranchCreateDTO, ptModelBranch);
        QueryWrapper<PtModelInfo> ptModelInfoQueryWrapper = new QueryWrapper<PtModelInfo>();
        ptModelInfoQueryWrapper.eq("id", ptModelBranchCreateDTO.getParentId());
        PtModelInfo ptModelInfo = ptModelInfoMapper.selectOne(ptModelInfoQueryWrapper);
        if (ptModelInfo == null) {
            LogUtil.error(LogEnum.BIZ_MODEL, "User {} failed to update model list", user.getUsername());
            throw new BusinessException("模型版本创建失败");
        }
        ptModelBranch.setVersion(getVersion(ptModelInfo));
        ptModelBranch.setModelPath("");
        ptModelBranch.setCreateUserId(ptModelInfo.getCreateUserId());
        //源文件路径
        String sourcePath = ptModelBranchCreateDTO.getModelAddress();
        fileService.validatePath(sourcePath);

        if (ptModelBranchCreateDTO.getModelSource() == PtModelUtil.USER_UPLOAD) {
            String targetPath = fileService.transfer(sourcePath, user);
            //修改存储路径
            ptModelBranch.setModelAddress(targetPath);
            //判断模型版本是否已存在
            checkModelVersion(ptModelBranchCreateDTO, user, ptModelBranch);
            if (ptModelBranchMapper.insert(ptModelBranch) < 1) {
                LogUtil.error(LogEnum.BIZ_MODEL, "User {} failed to create new version", user.getUsername());
                throw new BusinessException("模型版本创建失败");
            }
        } else if (ptModelBranchCreateDTO.getModelSource() == PtModelUtil.TRAINING_IMPORT || ptModelBranchCreateDTO.getModelSource() == PtModelUtil.MODEL_OPTIMIZATION
        ||ptModelBranchCreateDTO.getModelSource() == PtModelUtil.AUTOMATIC_MACHINE_LEARNING) {
            //文件拷贝中
            ptModelBranch.setStatus(ModelCopyStatusEnum.COPING.getCode());
            //判断模型版本是否已存在
            checkModelVersion(ptModelBranchCreateDTO, user, ptModelBranch);
            if (ptModelBranchMapper.insert(ptModelBranch) < 1) {
                LogUtil.error(LogEnum.BIZ_MODEL, "User {} failed to create new version", user.getUsername());
                throw new BusinessException("模型版本创建失败");
            }

            fileService.copyFileAsync(sourcePath, user,
                    (targetPath) -> {
                        //文件拷贝成功
                        ptModelBranch.setStatus(ModelCopyStatusEnum.SUCCESS.getCode());
                        ptModelBranch.setModelAddress(targetPath);
                        ptModelBranchMapper.updateById(ptModelBranch);
                    },
                    (e) -> {
                        //文件拷贝失败
                        ptModelBranch.setStatus(ModelCopyStatusEnum.FAIL.getCode());
                        ptModelBranchMapper.updateById(ptModelBranch);
                    }
            );
        }
        //模型信息更新
        ptModelInfo.setVersion(ptModelBranch.getVersion());
        ptModelInfo.setModelAddress(ptModelBranch.getModelAddress());
        ptModelInfo.setTotalNum(ptModelInfo.getTotalNum() + 1);
        if (ptModelInfoMapper.updateById(ptModelInfo) < 1) {
            LogUtil.error(LogEnum.BIZ_MODEL, "User {} failed to modify version, failed to modify version table", user.getUsername());
            throw new BusinessException("模型版本创建失败");
        }

        PtModelBranchCreateVO ptModelBranchCreateVO = new PtModelBranchCreateVO();
        ptModelBranchCreateVO.setId(ptModelBranch.getId());
        return ptModelBranchCreateVO;
    }

    /**
     * 判断模型版本是否已存在
     * @param ptModelBranchCreateDTO 入参
     * @param user                   用户
     * @param ptModelBranch          模型
     */
    private void checkModelVersion(PtModelBranchCreateDTO ptModelBranchCreateDTO, UserContext user, PtModelBranch ptModelBranch) {
        QueryWrapper<PtModelBranch> ptModelBranchWrapper = new QueryWrapper<>();
        ptModelBranchWrapper.eq("version", ptModelBranch.getVersion()).eq("parent_id", ptModelBranchCreateDTO.getParentId());
        List<PtModelBranch> ptModelBrancheList = ptModelBranchMapper.selectList(ptModelBranchWrapper);
        if (!CollectionUtils.isEmpty(ptModelBrancheList)) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "Version = {} of model_parent_id = {} created by user {} already exists", ptModelBranch.getVersion(), ptModelBranchCreateDTO.getParentId(), user.getUsername());
            throw new BusinessException("该模型版本已存在");
        }
    }

    /**
     * 模型版本获取
     *
     * @param ptModelInfo      模型版本管理创建对象
     * @return String          模型版本
     */
    private String getVersion(PtModelInfo ptModelInfo) {
        String version = "V" + String.format("%04d", ptModelInfo.getTotalNum() + 1);
        return version;
    }

    /**
     * 编辑
     *
     * @param ptModelBranchUpdateDTO 模型版本管理修改对象
     * @return PtModelBranchUpdateVO 模型版本管理返回更新VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PtModelBranchUpdateVO update(PtModelBranchUpdateDTO ptModelBranchUpdateDTO) {
        //获取用户信息
        UserContext user = userContextService.getCurUser();
        QueryWrapper<PtModelBranch> wrapper = new QueryWrapper<>();
        wrapper.eq("id", ptModelBranchUpdateDTO.getId());
        if (ptModelBranchMapper.selectCount(wrapper) < 1) {
            LogUtil.error(LogEnum.BIZ_MODEL, "The user {} failed to modify the model version, and has no permission to modify the corresponding data in the model version table", user.getUsername());
            throw new BusinessException("您修改的ID不存在请重新输入");
        }
        PtModelBranch ptModelBranch = ptModelBranchMapper.selectById(ptModelBranchUpdateDTO.getId());
        BeanUtils.copyProperties(ptModelBranchUpdateDTO, ptModelBranch);

        if (ptModelBranchMapper.updateById(ptModelBranch) < 1) {
            //模型版本未修改成功，抛出异常，并返回失败信息
            LogUtil.error(LogEnum.BIZ_MODEL, "The user {} failed to modify the model version, and failed to modify the model version table", user.getUsername());
            throw new BusinessException("模型版本更新失败");
        }

        //返回修改模型版本id
        PtModelBranchUpdateVO ptModelBranchUpdateVO = new PtModelBranchUpdateVO();
        ptModelBranchUpdateVO.setId(ptModelBranch.getId());
        return ptModelBranchUpdateVO;
    }

    /**
     * 多选删除
     *
     * @param ptModelBranchDeleteDTO 模型版本管理删除对象
     * @return PtModelBranchDeleteVO 模型版本管理返回删除VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PtModelBranchDeleteVO deleteAll(PtModelBranchDeleteDTO ptModelBranchDeleteDTO) {
        //获取用户信息
        UserContext user = userContextService.getCurUser();
        //数组ids去重
        List<Long> ids = Arrays.stream(ptModelBranchDeleteDTO.getIds()).distinct().collect(Collectors.toList());

        //权限校验
        QueryWrapper<PtModelBranch> query = new QueryWrapper<>();
        query.in("id", ids);
        if (ptModelBranchMapper.selectCount(query) < ids.size()) {
            throw new BusinessException("您没有此权限");
        }
        //删除时远程调用查询关联业务模块是否有正在使用该模型(需长期维护：若新增使用模型的微服务，需添加其远程调用)
        PtModelStatusQueryDTO ptModelStatusQueryDTO = new PtModelStatusQueryDTO();
        ptModelStatusQueryDTO.setModelBranchIds(ids);
        ModelStatusUtil.queryModelStatus(user, ptModelStatusQueryDTO, ids);

        //获取parentID
        List<PtModelBranch> ptModelBranches = ptModelBranchMapper.selectBatchIds(ids);
        List<Long> parentIdLists = ptModelBranches.stream().map(x -> {
            return x.getParentId();
        }).distinct().collect(Collectors.toList());

        //删除任务参数
        if (ptModelBranchMapper.deleteBatchIds(ids) < ids.size()) {
            //模型版本未删除成功,抛出异常，并返回失败信息
            LogUtil.error(LogEnum.BIZ_MODEL, "User {} failed to delete model version. Deleting model version table according to ID array {} failed", user.getUsername(), ids);
            throw new BusinessException("模型版本删除失败");
        }

        //更新parent的状态
        LogUtil.info(LogEnum.BIZ_MODEL, "Parentid of update algorithm{}", parentIdLists);
        for (int num = 0; num < parentIdLists.size(); num++) {
            QueryWrapper<PtModelBranch> queryWrapper = new QueryWrapper<PtModelBranch>();
            queryWrapper.eq("parent_id", parentIdLists.get(num));
            queryWrapper.orderByDesc("id");
            queryWrapper.last("limit 1");
            List<PtModelBranch> ptModelBranchList = ptModelBranchMapper.selectList(queryWrapper);
            PtModelInfo ptModelInfo = ptModelInfoMapper.selectById(parentIdLists.get(num));
            if (ptModelBranchList.size() > 0) {
                ptModelInfo.setVersion(ptModelBranchList.get(0).getVersion());
                ptModelInfo.setModelAddress(ptModelBranchList.get(0).getModelAddress());
                if (ptModelInfoMapper.updateById(ptModelInfo) < 1) {
                    LogUtil.error(LogEnum.BIZ_MODEL, "The user {} failed to delete the model version and failed to modify the model management table", user.getUsername());
                    throw new BusinessException("模型版本删除失败");
                }
            } else {
                ptModelInfo.setVersion("");
                ptModelInfo.setModelAddress("");
                if (ptModelInfoMapper.updateById(ptModelInfo) < 1) {
                    LogUtil.error(LogEnum.BIZ_MODEL, "The user {} failed to delete the model version and failed to modify the model management table", user.getUsername());
                    throw new BusinessException("模型版本删除失败");
                }
            }
        }
        //定时任务删除相应的模型文件
        for (PtModelBranch ptModelBranch : ptModelBranches) {
            RecycleCreateDTO recycleCreateDTO = RecycleCreateDTO.builder()
                    .recycleModule(RecycleModuleEnum.BIZ_MODEL.getValue())
                    .recycleDelayDate(recycleConfig.getModelValid())
                    .recycleNote(RecycleTool.generateRecycleNote("删除模型版本文件", ptModelBranch.getId()))
                    .remark(ptModelBranch.getId().toString())
                    .restoreCustom(RecycleResourceEnum.MODEL_RECYCLE_FILE.getClassName())
                    .build();
            recycleCreateDTO.addRecycleDetailCreateDTO(RecycleDetailCreateDTO.builder()
                    .recycleType(RecycleTypeEnum.FILE.getCode())
                    .recycleCondition(fileService.getAbsolutePath(ptModelBranch.getModelAddress()))
                    .recycleNote(RecycleTool.generateRecycleNote("删除模型版本文件", ptModelBranch.getId()))
                    .build()
            );
            recycleService.createRecycleTask(recycleCreateDTO);
        }
        PtModelBranchDeleteVO ptModelBranchDeleteVO = new PtModelBranchDeleteVO();
        ptModelBranchDeleteVO.setIds(ptModelBranchDeleteDTO.getIds());
        return ptModelBranchDeleteVO;
    }


    /**
     * 根据模型版本id查询模型版本详情
     *
     * @param ptModelBranchQueryByIdDTO 据模型版本id查询模型版本详情查询参数
     * @return PtModelBranchQueryByIdVO 根据模型版本id查询模型版本详情返回结果
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public PtModelBranchQueryVO queryByBranchId(PtModelBranchQueryByIdDTO ptModelBranchQueryByIdDTO) {
        PtModelBranch ptModelBranch = ptModelBranchMapper.selectById(ptModelBranchQueryByIdDTO.getId());
        PtModelBranchQueryVO ptModelBranchQueryByIdVO = new PtModelBranchQueryVO();
        BeanUtils.copyProperties(ptModelBranch, ptModelBranchQueryByIdVO);
        return ptModelBranchQueryByIdVO;
    }

    /**
     * 条件查询模型版本详情
     *
     * @param ptModelBranchConditionQueryDTO 查询条件
     * @return PtModelBranchQueryVO 模型版本查询返回结果
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public PtModelBranchQueryVO getConditionQuery(PtModelBranchConditionQueryDTO ptModelBranchConditionQueryDTO) {
        LambdaQueryWrapper<PtModelBranch> wrapper = new LambdaQueryWrapper();
        wrapper.eq(PtModelBranch::getParentId, ptModelBranchConditionQueryDTO.getParentId());
        wrapper.eq(PtModelBranch::getModelAddress, ptModelBranchConditionQueryDTO.getModelAddress());
        PtModelBranch ptModelBranch = ptModelBranchMapper.selectOne(wrapper);
        if (ptModelBranch == null) {
            return null;
        }
        PtModelBranchQueryVO ptModelBranchQueryVO = new PtModelBranchQueryVO();
        BeanUtils.copyProperties(ptModelBranch, ptModelBranchQueryVO);
        return ptModelBranchQueryVO;
    }

    /**
     * 我的模型转预置模型
     * @param modelConvertPresetDTO 模型版本id请求体
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void convertPreset(ModelConvertPresetDTO modelConvertPresetDTO) {
        //获取用户信息
        UserContext user = userContextService.getCurUser();
        //预置模型名称校验
        QueryWrapper<PtModelInfo> nameVerification = new QueryWrapper<>();
        nameVerification.eq("name", modelConvertPresetDTO.getName());
        Integer countResult = ptModelInfoMapper.selectCount(nameVerification);
        if (countResult > 0) {
            throw new BusinessException("名称已存在");
        }
        //获取我的模型版本详情
        QueryWrapper<PtModelBranch> modelBranchWrapper = new QueryWrapper<>();
        modelBranchWrapper.eq("id", modelConvertPresetDTO.getId()).last(" limit 1 ");
        PtModelBranch ptModelBranch = ptModelBranchMapper.selectOne(modelBranchWrapper);
        if (ptModelBranch == null) {
            throw new BusinessException("该版本模型不存在");
        }
        //获取我的模型详情
        QueryWrapper<PtModelInfo> modelInfoWrapper = new QueryWrapper<>();
        modelInfoWrapper.eq("id", ptModelBranch.getParentId()).last(" limit 1 ");
        PtModelInfo ptModelInfo = ptModelInfoMapper.selectOne(modelInfoWrapper);
        if (ptModelInfo == null) {
            throw new BusinessException("该模型不存在");
        }
        //新增预置模型到pt_model_info表
        String targetPath = fileService.convertPreset(ptModelBranch.getModelAddress(), user);
        PtModelInfo preModelInfo = new PtModelInfo();
        preModelInfo.setName(modelConvertPresetDTO.getName()).setFrameType(ptModelInfo.getFrameType()).setModelType(ptModelInfo.getModelType())
                .setModelAddress(targetPath).setModelClassName(ptModelInfo.getModelClassName())
                .setModelResource(1).setOriginUserId(0L).setPackaged(ptModelInfo.getPackaged())
                .setTotalNum(1).setVersion("V0001");
        if (ptModelInfo.getTags() != null) {
            preModelInfo.setTags(ptModelInfo.getTags());
        }
        if (ptModelInfo.getTeamId() != null) {
            preModelInfo.setTeamId(ptModelInfo.getTeamId());
        }
        if (modelConvertPresetDTO.getModelDescription() != null) {
            preModelInfo.setModelDescription(modelConvertPresetDTO.getModelDescription());
        }
        try {
            ptModelInfoMapper.insert(preModelInfo);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_MODEL, "User {} failed to transfer my model to preset model because ：{}", user.getUsername(), e);
            throw new BusinessException("我的模型转预置模型失败");
        }
        //新增预置模型到pt_model_branch表
        PtModelBranch preModelBranch = new PtModelBranch();
        preModelBranch.setParentId(preModelInfo.getId()).setVersion("V0001").setModelAddress(targetPath).setModelPath(ptModelBranch.getModelPath())
                .setOriginUserId(0L).setModelSource(ptModelBranch.getModelSource()).setTeamId(ptModelBranch.getTeamId()).setAlgorithmId(ptModelBranch.getAlgorithmId())
                .setAlgorithmName(ptModelBranch.getAlgorithmName()).setAlgorithmSource(ptModelBranch.getAlgorithmSource());
        try {
            ptModelBranchMapper.insert(preModelBranch);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_MODEL, "User {} failed to transfer my model to preset model because ：{}", user.getUsername(), e);
            throw new BusinessException("我的模型转预置模型失败");
        }
    }

    /**
     * 模型删除文件还原
     * @param dto 还原实体
     */
    @Override
    public void modelRecycleFileRollback(RecycleCreateDTO dto) {
        //获取用户信息
        UserContext user = userContextService.getCurUser();
        if (dto == null) {
            LogUtil.error(LogEnum.BIZ_MODEL, "User {} restore model failed to delete the file because RecycleCreateDTO is null", user.getUsername());
            throw new BusinessException("非法入参");
        }
        //模型版本id
        Long ptModelBranchId = Long.valueOf(dto.getRemark());
        PtModelBranch ptModelBranchAll = ptModelBranchMapper.selectAllById(ptModelBranchId);
        //模型id
        Long ptModelInfoId = ptModelBranchAll.getParentId();
        //校验该模型名称是否已存在
        String modelName = ptModelInfoMapper.selectNameById(ptModelInfoId);
        QueryWrapper<PtModelInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("name", modelName).last(" limit 1 ");
        PtModelInfo modelInfo = ptModelInfoMapper.selectOne(wrapper);
        if (modelInfo != null && !ptModelInfoId.equals(modelInfo.getId())) {
            throw new BusinessException("模型已存在");
        }
        try {
            //还原模型版本
            ptModelBranchMapper.updateStatusById(ptModelBranchId, false);
            //还原模型
            PtModelInfo ptModelInfo = ptModelInfoMapper.selectById(ptModelInfoId);
            if (ptModelInfo == null) {
                ptModelInfoMapper.updateStatusById(ptModelInfoId, false, ptModelBranchAll.getVersion());
            } else {
                if (ptModelInfo.getVersion() == null || Integer.parseInt(ptModelBranchAll.getVersion().substring(PtModelUtil.NUMBER_ONE, PtModelUtil.NUMBER_FIVE)) > Integer.parseInt(ptModelInfo.getVersion().substring(PtModelUtil.NUMBER_ONE, PtModelUtil.NUMBER_FIVE))) {
                    ptModelInfoMapper.updateModelVersionById(ptModelInfoId, ptModelBranchAll.getVersion());

                }
            }

        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_MODEL, "User {} restore model failed to delete the file because:{}", user.getUsername(), e);
            throw new BusinessException("还原失败");
        }
    }


    /**
     * TensorFlow SaveModel 模型转换为ONNX 模型
     *
     * @param ptModelConvertOnnxDTO 模型版本 id 请求体
     * @return PtModelConvertOnnxVO 模型转换返回 VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PtModelConvertOnnxVO convertToOnnx(PtModelConvertOnnxDTO ptModelConvertOnnxDTO) {
        // 获取用户信息
        UserContext user = userContextService.getCurUser();

        // 获取我的模型版本详情
        QueryWrapper<PtModelBranch> modelBranchWrapper = new QueryWrapper<>();
        modelBranchWrapper.eq("id", ptModelConvertOnnxDTO.getId()).last(" limit 1 ");
        PtModelBranch ptModelBranch = ptModelBranchMapper.selectOne(modelBranchWrapper);
        if (ptModelBranch == null) {
            throw new BusinessException("该版本模型不存在");
        }

        // 获取我的模型详情
        QueryWrapper<PtModelInfo> modelInfoWrapper = new QueryWrapper<>();
        modelInfoWrapper.eq("id", ptModelBranch.getParentId()).last(" limit 1 ");
        PtModelInfo ptModelInfo = ptModelInfoMapper.selectOne(modelInfoWrapper);
        if (ptModelInfo == null) {
            throw new BusinessException("该模型不存在");
        }
        String modelName = "onnx-from-" + ptModelInfo.getName() + SymbolConstant.HYPHEN + ptModelBranch.getVersion();
        String modelDescription = "This is an onnx model converted from " + ptModelInfo.getName() + SymbolConstant.COLON + ptModelBranch.getVersion();

        // 模型名称校验
        QueryWrapper<PtModelInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", modelName).and(wrapper -> wrapper.eq("create_user_id", user.getId()).or().eq("origin_user_id", 0L));
        Integer countResult = ptModelInfoMapper.selectCount(queryWrapper);
        if (countResult > 0) {
            LogUtil.error(LogEnum.BIZ_MODEL, "The user {} fail to convert model，the name of model is already exist !", user.getUsername());
            throw new BusinessException("该版本 ONNX 模型已存在");
        }
        String onnxModelPath = k8sNameTool.getPath(BizPathEnum.MODEL, user.getId());

        // 模型转换
        String onnxModelUrl = generateOnnxModel(ptModelBranch.getModelAddress(), onnxModelPath);
        if (StringUtils.isEmpty(onnxModelUrl)) {
            throw new BusinessException(ModelConvertEnum.CONVERT_SERVER_ERROR.getMsg());
        }

        // 新增 ONNX 模型到pt_model_info表
        PtModelInfo onnxModelInfo = new PtModelInfo();
        onnxModelInfo.setName(modelName).setFrameType(ptModelInfo.getFrameType()).setModelType(5).setModelDescription(modelDescription)
                .setModelAddress(onnxModelPath).setModelClassName(ptModelInfo.getModelClassName()).setModelResource(0).setTotalNum(1).setVersion("V0001");
        if (ptModelInfo.getTeamId() != null) {
            onnxModelInfo.setTeamId(ptModelInfo.getTeamId());
        }

        try {
            ptModelInfoMapper.insert(onnxModelInfo);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_MODEL, "User {} failed to insert ONNX model_info to database because ：{}", user.getUsername(), e);
            throw new BusinessException("ONNX 模型保存失败");
        }

        // 新增 onnx 模型到pt_model_branch表
        PtModelBranch onnxModelBranch = new PtModelBranch();
        onnxModelBranch.setParentId(onnxModelInfo.getId()).setVersion("V0001").setModelAddress(onnxModelPath).setModelSource(3)
                .setModelPath(ptModelBranch.getModelPath()).setTeamId(ptModelBranch.getTeamId()).setAlgorithmId(ptModelBranch.getAlgorithmId())
                .setAlgorithmName(ptModelBranch.getAlgorithmName()).setAlgorithmSource(ptModelBranch.getAlgorithmSource());
        try {
            ptModelBranchMapper.insert(onnxModelBranch);
            PtModelConvertOnnxVO ptModelConvertOnnxVO = new PtModelConvertOnnxVO();
            ptModelConvertOnnxVO.setId(onnxModelBranch.getParentId());
            return ptModelConvertOnnxVO;
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_MODEL, "User {} failed to insert onnx model_branch to database because ：{}", user.getUsername(), e);
            throw new BusinessException("ONNX 模型保存失败");
        }
    }

    /**
     * 生成 ONNX 模型
     *
     * @param modelPath 模型路径
     * @param outputPath 转换后的 ONNX 模型路径
     * @return
     */
    private String generateOnnxModel(String modelPath, String outputPath) {
        JSONObject params = new JSONObject();
        params.put("model_path", modelPath);
        params.put("output_path", outputPath);
        RestTemplate restTemplate = restTemplateHolder.getRestTemplate();

        // 调用模型转换的Python服务
        DataResponseBody result;
        try {
            result = restTemplate.postForObject(modelConverterUrl, params, DataResponseBody.class);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_MODEL, "generate ONNX model fail,cause of exception msg {}", e.getMessage());
            throw new BusinessException(ModelConvertEnum.CONVERT_SERVER_ERROR.getMsg());
        }
        if (result != null) {
            if (result.succeed()) {
                return (String) result.getData();
            }
            throw new BusinessException(ModelConvertEnum.getModelConvertEnum(result.getCode()).getMsg());
        }
        return SymbolConstant.BLANK;
    }


}
