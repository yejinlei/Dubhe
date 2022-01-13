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

package org.dubhe.data.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.biz.permission.annotation.DataPermissionMethod;
import org.dubhe.biz.permission.annotation.RolePermission;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.context.DataContext;
import org.dubhe.biz.base.dto.CommonPermissionDataDTO;
import org.dubhe.biz.base.enums.DatasetTypeEnum;
import org.dubhe.biz.base.enums.SwitchEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.utils.RandomUtil;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.permission.base.BaseService;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.db.utils.WrapperHelp;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.cloud.authconfig.utils.JwtUtils;
import org.dubhe.data.constant.*;
import org.dubhe.data.dao.DatasetMapper;
import org.dubhe.data.dao.LabelGroupMapper;
import org.dubhe.data.domain.dto.*;
import org.dubhe.data.domain.entity.DatasetGroupLabel;
import org.dubhe.data.domain.entity.Label;
import org.dubhe.data.domain.entity.LabelGroup;
import org.dubhe.data.domain.vo.LabelGroupQueryVO;
import org.dubhe.data.domain.vo.LabelGroupVO;
import org.dubhe.data.domain.vo.LabelVO;
import org.dubhe.data.service.DatasetGroupLabelService;
import org.dubhe.data.service.DatasetLabelService;
import org.dubhe.data.service.LabelGroupService;
import org.dubhe.data.service.LabelService;
import org.dubhe.data.util.FileUtil;
import org.dubhe.data.util.JsonUtil;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.domain.dto.RecycleDetailCreateDTO;
import org.dubhe.recycle.enums.RecycleModuleEnum;
import org.dubhe.recycle.enums.RecycleResourceEnum;
import org.dubhe.recycle.enums.RecycleTypeEnum;
import org.dubhe.recycle.service.RecycleService;
import org.dubhe.recycle.utils.RecycleTool;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static org.dubhe.data.constant.Constant.SORT_ASC;

/**
 * @description 标签组 服务实现类
 * @date 2020-09-22
 */
@Service
public class LabelGroupServiceImpl extends ServiceImpl<LabelGroupMapper, LabelGroup> implements LabelGroupService {

    @Autowired
    private LabelService labelService;

    @Autowired
    private DatasetLabelService datasetLabelService;

    @Autowired
    private DatasetMapper datasetService;

    @Autowired
    private DatasetGroupLabelService datasetGroupLabelService;

    /**
     * 数据回收服务
     */
    @Autowired
    private RecycleService recycleService;

    /**
     * 创建标签组
     *
     * @param labelGroupCreateDTO 创建标签组DTO
     * @return Long 标签组id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long creatLabelGroup(LabelGroupCreateDTO labelGroupCreateDTO) {

        //1 标签组名称唯一校验
        labelGroupCreateDTO.setOriginUserId(JwtUtils.getCurUserId());
        if (checkoutLabelGroupName(labelGroupCreateDTO.getName())) {
            throw new BusinessException(ErrorEnum.LABELGROUP_NAME_DUPLICATED_ERROR);
        }
        LabelGroup labelGroup = LabelGroupCreateDTO.from(labelGroupCreateDTO);
        try {
            //2 落地标签组数据
            save(labelGroup);
        } catch (Exception e) {
            throw new BusinessException(ErrorEnum.LABELGROUP_NAME_DUPLICATED_ERROR);
        }
        if (StringUtils.isEmpty(labelGroupCreateDTO.getLabels())) {
            throw new BusinessException(ErrorEnum.LABELGROUP_JSON_FILE_ERROR);
        }
        //3 标签校验json格式校验
        List<LabelDTO> labelList = analyzeLabelData(labelGroupCreateDTO.getLabels());

        //4 组装原标签数据
        if (!CollectionUtils.isEmpty(labelList)) {
            buildLabelDataByCreate(labelGroup, labelList);
        }
        return labelGroup.getId();
    }

    /**
     * 更新（编辑）标签组
     *
     * @param labelGroupId        标签组ID
     * @param labelGroupCreateDTO 创建标签组DTO
     * @return Boolean 是否更新成功
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(Long labelGroupId, LabelGroupCreateDTO labelGroupCreateDTO) {
        LabelGroup labelGroup = getBaseMapper().selectById(labelGroupId);
        //1 校验标签组是否存在
        if (Objects.isNull(labelGroup)) {
            throw new BusinessException(ErrorEnum.LABELGROUP_DOES_NOT_EXIST);
        }
        //2 校验标签组名称唯一性
        if (checkoutLabelGroupName(labelGroupCreateDTO.getName()) && !labelGroup.getName().equals(labelGroupCreateDTO.getName())) {
            throw new BusinessException(ErrorEnum.LABELGROUP_NAME_DUPLICATED_ERROR);
        }
        LabelGroup group = LabelGroup.builder()
                .id(labelGroupId).name(labelGroupCreateDTO.getName()).remark(labelGroupCreateDTO.getRemark()).build();
        try {
            //3 修改标签组数据
            updateById(group);
        } catch (Exception e) {
            throw new BusinessException(ErrorEnum.LABELGROUP_NAME_DUPLICATED_ERROR);
        }
        //4 解析标签信息
        List<LabelDTO> labelList = analyzeLabelData(labelGroupCreateDTO.getLabels());

        //5 组装原标签数据
        if (!CollectionUtils.isEmpty(labelList)) {
            // 6-0 查询标签组下全部标签
            List<Label> dbLabels = labelService.listByGroupId(labelGroup.getId());
            if (!CollectionUtils.isEmpty(dbLabels)) {
                //获取预置标签信息
                Map<Long, String> pubLabels = getPubLabels(labelGroupCreateDTO.getLabelGroupType());
                Map<Long, List<Label>> dbListMap = dbLabels.stream().collect(Collectors.groupingBy(Label::getId));
                //校验标签组是否关联数据集
                int count = datasetService.getCountByLabelGroupId(labelGroupId);
                if (count > 0) {
                    buildLabelDataByUpdate(labelGroup, labelList, dbListMap, pubLabels);
                } else {
                    buildLabelDataByUpdate(dbListMap, labelGroup, labelList, pubLabels);
                }
            }
        }
    }

    /**
     * 构建编辑标签组方法
     *
     * @param dbListMap  数据库标签map key: 标签id value: 标签
     * @param labelGroup 标签组
     * @param labelList  标签列表
     * @param pubLabels  公共标签
     */
    private void buildLabelDataByUpdate(Map<Long, List<Label>> dbListMap, LabelGroup labelGroup, List<LabelDTO> labelList, Map<Long, String> pubLabels) {

        //删除标签和标签组的关联关系
        datasetGroupLabelService.deleteByGroupId(labelGroup.getId());

        Map<String, Long> nameMap = new HashMap<>(labelList.size());

        for (LabelDTO dto : labelList) {
            checkoutNameAndColor(dto, nameMap);
            //校验id是否存在
            if (!Objects.isNull(dto.getId())) {
                //公共数据集
                if (!Objects.isNull(pubLabels.get(dto.getId()))) {
                    //公共数据集名称匹配
                    if (!dto.getName().equals(pubLabels.get(dto.getId()))) {
                        //公共数据集名称不匹配
                        throw new BusinessException(ErrorEnum.LABELGROUP_LABELG_ID_ERROR);
                    }
                    //落地标签组标签中间表数据
                    datasetGroupLabelService.insert(
                            DatasetGroupLabel.builder()
                                    .labelId(dto.getId())
                                    .labelGroupId(labelGroup.getId()).build());
                } else {
                    //校验原数据是否存在该标签
                    List<Label> labels = dbListMap.get(dto.getId());
                    if (!CollectionUtils.isEmpty(labels)) {
                        labelService.updateLabel(Label.builder().id(dto.getId()).color(dto.getColor()).name(dto.getName()).build());
                        //落地标签组标签中间表数据
                        datasetGroupLabelService.insert(
                                DatasetGroupLabel.builder()
                                        .labelId(dto.getId())
                                        .labelGroupId(labelGroup.getId()).build());
                    } else {
                        //非公共预置标签组名称不匹配
                        throw new BusinessException(ErrorEnum.LABELGROUP_LABELG_ID_ERROR);
                    }
                }
            } else {
                // 7-2 落地标签数据
                Label buildLabel = Label.builder()
                        .color(dto.getColor())
                        .name(dto.getName())
                        .type(DatasetLabelEnum.CUSTOM.getType())
                        .build();
                labelService.insert(buildLabel);
                //落地标签组标签中间表数据
                datasetGroupLabelService.insert(
                        DatasetGroupLabel.builder()
                                .labelId(buildLabel.getId())
                                .labelGroupId(labelGroup.getId()).build());
            }

        }
    }

    /**
     * 删除标签组
     *
     * @param labelGroupDeleteDTO 删除标签组DTO
     */
    @Override
    public void delete(LabelGroupDeleteDTO labelGroupDeleteDTO) {
        if (CollectionUtils.isEmpty(Collections.singleton(labelGroupDeleteDTO.getIds()))) {
            return;
        }
        for (Long id : labelGroupDeleteDTO.getIds()) {
            delete(id);
        }
    }

    /**
     * 删除标签组方法
     *
     * @param labelGroupId 标签组ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Long labelGroupId) {
        LabelGroup labelGroup = baseMapper.selectById(labelGroupId);
        //校验标签组是否存在
        if(Objects.isNull(labelGroup)){
            throw new BusinessException(ErrorEnum.LABELGROUP_DOES_NOT_EXIST);
        }
        //校验预置标签组是否为管理员操作
        if (labelGroup.getType().compareTo(MagicNumConstant.ONE) == 0) {
            BaseService.checkAdminPermission();
        }

        //校验标签组是否被数据集引用
        if (datasetService.getCountByLabelGroupId(labelGroupId) > 0) {
            throw new BusinessException(ErrorEnum.LABELGROUP_LABEL_GROUP_QUOTE_DEL_ERROR);
        }

        List<Label> labels = labelService.listByGroupId(labelGroupId);

        if (!CollectionUtils.isEmpty(labels)) {

            //过滤预置标签组
            List<Long> ids = labelService.getPubLabelIds(labelGroup.getLabelGroupType());
            if (!CollectionUtils.isEmpty(ids)) {
                labels = labels.stream().filter(label -> !ids.contains(label.getId())).collect(Collectors.toList());
            }
            if (!CollectionUtils.isEmpty(labels)) {
                List<Long> labelIds = new ArrayList<>();
                labels.forEach(label -> labelIds.add(label.getId()));
                if (datasetLabelService.isLabelGroupInUse(labels)) {
                    throw new BusinessException(ErrorEnum.LABELGROUP_IN_USE_STATUS);
                }
                //删除标签
                labelService.updateStatusByLabelIds(labelIds,true);
                //删除标签和标签组关联关系
                datasetGroupLabelService.updateStatusByGroupId(labelGroupId,true);
            }
        }
        //删除标签组数据
        getBaseMapper().updateStatusByGroupId(labelGroupId,true);
        //添加回收数据
        try {
            addRecycleDataByDeleteDataset(labelGroup);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "LabelGroupServiceImpl addRecycleDataByDeleteDataset error:{}", e);
        }


    }


    /**
     * 添加回收数据
     *
     * @param labelGroup 标签组实体
     */
    private void addRecycleDataByDeleteDataset( LabelGroup labelGroup){

        //落地回收详情数据文件回收信息
        List<RecycleDetailCreateDTO> detailList = new ArrayList<>();
        detailList.add( RecycleDetailCreateDTO.builder()
                .recycleCondition(labelGroup.getId().toString())
                .recycleType(RecycleTypeEnum.TABLE_DATA.getCode())
                .recycleNote(RecycleTool.generateRecycleNote("落地 标签组DB 数据回收", labelGroup.getId()))
                .build());
        //落地回收信息
        RecycleCreateDTO recycleCreateDTO = RecycleCreateDTO.builder()
                .recycleModule(RecycleModuleEnum.BIZ_DATASET.getValue())
                .recycleCustom(RecycleResourceEnum.LABEL_GROUP_RECYCLE_FILE.getClassName())
                .restoreCustom(RecycleResourceEnum.LABEL_GROUP_RECYCLE_FILE.getClassName())
                .recycleDelayDate(NumberConstant.NUMBER_1)
                .recycleNote(RecycleTool.generateRecycleNote("删除标签组相关信息", labelGroup.getId()))
                .detailList(detailList)
                .build();
        recycleService.createRecycleTask(recycleCreateDTO);
    }

    /**
     * 标签组分页列表
     *
     * @param page              分页信息
     * @param labelGroupQueryVO 查询条件
     * @return Map<String, Object> 查询出对应的标签组
     */
    @Override
    @DataPermissionMethod
    public Map<String, Object> listVO(Page<LabelGroup> page, LabelGroupQueryVO labelGroupQueryVO) {
        String name = labelGroupQueryVO.getName();
        if(MagicNumConstant.ONE == labelGroupQueryVO.getType()){
            DataContext.set(CommonPermissionDataDTO.builder().type(true).build());
        }
        if (StringUtils.isEmpty(name)) {
            return queryLabelGroups(page, labelGroupQueryVO, null);
        }
        boolean nameFlag = Constant.PATTERN_NUM.matcher(name).matches();
        if (nameFlag) {
            LabelGroupQueryVO queryCriteriaId = new LabelGroupQueryVO();
            BeanUtils.copyProperties(labelGroupQueryVO, queryCriteriaId);
            queryCriteriaId.setName(null);
            queryCriteriaId.setId(Long.parseLong(labelGroupQueryVO.getName()));
            Map<String, Object> map = queryLabelGroups(page, queryCriteriaId, null);
            if (((List) map.get(Constant.RESULT)).size() > 0) {
                queryCriteriaId.setName(name);
                queryCriteriaId.setId(null);
                return queryLabelGroups(page, queryCriteriaId, Long.parseLong(labelGroupQueryVO.getName()));
            }
        }
        return queryLabelGroups(page, labelGroupQueryVO, null);
    }

    public Map<String, Object> queryLabelGroups(Page<LabelGroup> page, LabelGroupQueryVO labelGroupQueryVO, Long labelGroupId) {

        QueryWrapper<LabelGroup> queryWrapper = WrapperHelp.getWrapper(labelGroupQueryVO);
        queryWrapper.eq("deleted", MagicNumConstant.ZERO);
        if (labelGroupId != null) {
            queryWrapper.or().eq("id", labelGroupId);
        }
        if(!Objects.isNull(labelGroupQueryVO.getLabelGroupType())){
            queryWrapper.eq("label_group_type",labelGroupQueryVO.getLabelGroupType());
        }
        if (StringUtils.isNotEmpty(labelGroupQueryVO.getSort()) && StringUtils.isNotEmpty(labelGroupQueryVO.getOrder())) {
            queryWrapper.orderBy(true, SORT_ASC.equals(labelGroupQueryVO.getOrder().toLowerCase()),
                    StringUtils.humpToLine(labelGroupQueryVO.getSort())
            );
        } else {
            queryWrapper.orderByDesc("update_time");
        }
        Page<LabelGroup> labelGroupPage = baseMapper.selectPage(page, queryWrapper);
        List<LabelGroupQueryVO> labelGroups = new ArrayList<>();
        if(!CollectionUtils.isEmpty(labelGroupPage.getRecords())){
            List<LabelGroup> records = labelGroupPage.getRecords();
            List<Long> groupIds = records.stream().map(a -> a.getId()).collect(Collectors.toList());
            Map<Long, Integer> labelGroupMap = datasetGroupLabelService.getLabelByGroupIds(groupIds);
            labelGroups = records.stream().map(labelGroup -> {
                LabelGroupQueryVO labelGroupQuery = LabelGroupQueryVO.builder()
                        .id(labelGroup.getId()).name(labelGroup.getName())
                        .operateType(labelGroup.getOperateType())
                        .type(labelGroup.getType())
                        .createTime(labelGroup.getCreateTime())
                        .labelGroupType(labelGroup.getLabelGroupType())
                        .remark(labelGroup.getRemark()).updateTime(labelGroup.getUpdateTime()).build();
                labelGroupQuery.setCount(labelGroupMap.get(labelGroup.getId()));
                return labelGroupQuery;
            }).collect(Collectors.toList());
        }
        Map<String, Object> stringObjectMap = PageUtil.toPage(page,labelGroups);
        return stringObjectMap;
    }

    /**
     * 标签组详情
     *
     * @param labelGroupId 标签组id
     * @return org.dubhe.data.domain.vo.LabelGroupVO 根据Id查询出对应的标签组
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public LabelGroupVO get(Long labelGroupId) {
        LabelGroup labelGroup = baseMapper.selectById(labelGroupId);
        if (labelGroup == null) {
            throw new BusinessException(ErrorEnum.LABELGROUP_DOES_NOT_EXIST);
        }
        List<Label> labels = labelService.listByGroupId(labelGroup.getId());
        List<LabelVO> labelVOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(labels)) {
            labelVOS = labels.stream().map(a -> {
                return LabelVO.builder().id(a.getId()).color(a.getColor()).name(a.getName()).build();
            }).collect(Collectors.toList());
        }
        return LabelGroupVO.builder()
                .id(labelGroupId)
                .type(labelGroup.getType())
                .name(labelGroup.getName())
                .remark(labelGroup.getRemark())
                .operateType(labelGroup.getOperateType())
                .labelGroupType(labelGroup.getLabelGroupType())
                .labels(labelVOS).build();
    }

    /**
     * 标签组列表
     *
     * @param labelGroupQueryDTO 查询条件
     * @return List<LabelGroup> 查询出对应的标签组
     */
    @Override
    @DataPermissionMethod
    public List<LabelGroup> getList(LabelGroupQueryDTO labelGroupQueryDTO) {
        if(MagicNumConstant.ONE == labelGroupQueryDTO.getType()){
            DataContext.set(CommonPermissionDataDTO.builder().type(true).build());
        }
        Integer groupType = LabelGroupTypeEnum.convertGroup(DatatypeEnum.getEnumValue(labelGroupQueryDTO.getDataType())).getValue();
        LambdaQueryWrapper<LabelGroup> labelGroupLambdaQueryWrapper = new LambdaQueryWrapper<>();
        labelGroupLambdaQueryWrapper.eq(LabelGroup::getDeleted, MagicNumConstant.ZERO)
                .eq(LabelGroup::getType, labelGroupQueryDTO.getType())
                .eq(LabelGroup::getLabelGroupType,groupType);
        if (MagicNumConstant.ONE == labelGroupQueryDTO.getType()) {
            if(AnnotateTypeEnum.OBJECT_DETECTION.getValue().compareTo(labelGroupQueryDTO.getAnnotateType()) == 0
                    || AnnotateTypeEnum.OBJECT_TRACK.getValue().compareTo(labelGroupQueryDTO.getAnnotateType()) == 0
                    || AnnotateTypeEnum.SEMANTIC_CUP.getValue().compareTo(labelGroupQueryDTO.getAnnotateType()) == 0){
                labelGroupLambdaQueryWrapper.ne(LabelGroup::getId,MagicNumConstant.TWO);
            }
            labelGroupLambdaQueryWrapper.orderByAsc(LabelGroup::getId);
        }else {
            labelGroupLambdaQueryWrapper.orderByDesc(LabelGroup::getUpdateTime);
        }
        return baseMapper.selectList(labelGroupLambdaQueryWrapper);

    }


    /**
     * 导入标签组
     *
     * @param labelGroupImportDTO 标签组导入DTO
     * @param file                导入文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long importLabelGroup(LabelGroupImportDTO labelGroupImportDTO, MultipartFile file) {
        //文件格式/大小/属性校验
        FileUtil.checkoutFile(file);

        //解析文件标签
        String labels = null;
        try {
            labels = FileUtil.readFile(file);
        } catch (Exception e) {
            throw new BusinessException(ErrorEnum.LABELGROUP_JSON_FILE_FORMAT_ERROR);
        }
        LabelGroupCreateDTO createDTO = LabelGroupCreateDTO.builder()
                .labels(labels)
                .name(labelGroupImportDTO.getName())
                .labelGroupType(labelGroupImportDTO.getLabelGroupType())
                .remark(labelGroupImportDTO.getRemark()).build();

        //调用新增标签方法
        return this.creatLabelGroup(createDTO);
    }

    /**
     * 标签组复制
     *
     * @param labelGroupCopyDTO 标签组复制DTO
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void copy(LabelGroupCopyDTO labelGroupCopyDTO) {

        LabelGroup group = LabelGroup.builder().name(labelGroupCopyDTO.getName()).remark(labelGroupCopyDTO.getRemark()).build();

        //校验标签组是否存在
        LabelGroup oldLabelGroup = getBaseMapper().selectOne(
                new LambdaUpdateWrapper<LabelGroup>().eq(LabelGroup::getId, labelGroupCopyDTO.getId()));
        if (Objects.isNull(oldLabelGroup)) {
            throw new BusinessException(ErrorEnum.LABELGROUP_DOES_NOT_EXIST);
        }

        //校验名称唯一性
        LabelGroup labelGroup = getBaseMapper().selectOne(
                new LambdaUpdateWrapper<LabelGroup>().eq(LabelGroup::getName, labelGroupCopyDTO.getName()));
        if (!Objects.isNull(labelGroup)) {
            group.setName(buildLabelGroupName(labelGroup.getName()));
        }


        //落地标签组数据
        LabelGroup dbLabelGroup = LabelGroup.builder()
                .labelGroupType(oldLabelGroup.getLabelGroupType())
                .name(group.getName()).remark(group.getRemark()).originUserId(oldLabelGroup.getCreateUserId()).build();
        baseMapper.insert(dbLabelGroup);

        //获取标签组关联关系
        List<DatasetGroupLabel> datasetGroupLabels = datasetGroupLabelService.listByGroupId(labelGroupCopyDTO.getId());

        //获取标签组下标签
        List<Label> labels = labelService.listByGroupId(labelGroupCopyDTO.getId());
        Map<Long, List<Label>> labelListMap = new HashMap<>(labels.size());
        if (!CollectionUtils.isEmpty(labels)) {
            labelListMap = labels.stream().collect(Collectors.groupingBy(Label::getId));
        }

        List<Label> pubLabels = labelService.getPubLabels(oldLabelGroup.getLabelGroupType());
        Map<Long, String> longListMap = new HashMap<>(pubLabels.size());
        if (!CollectionUtils.isEmpty(pubLabels)) {
            longListMap = pubLabels.stream().collect(Collectors.toMap(Label::getId, Label::getName));
        }

        if (!CollectionUtils.isEmpty(datasetGroupLabels)) {
            for (DatasetGroupLabel groupLabel : datasetGroupLabels) {
                //查看标签是否属于预置标签组
                String labelName = longListMap.get(groupLabel.getLabelId());
                //不属于预置标签组
                if (Objects.isNull(labelName)) {
                    Label buildLabel = Label.builder()
                            .color(labelListMap.get(groupLabel.getLabelId()).get(0).getColor())
                            .name(labelListMap.get(groupLabel.getLabelId()).get(0).getName())
                            .type(DatasetLabelEnum.CUSTOM.getType())
                            .build();
                    labelService.insert(buildLabel);

                    //落地标签标签组数据关系
                    datasetGroupLabelService.insert(
                            DatasetGroupLabel.builder()
                                    .labelGroupId(dbLabelGroup.getId())
                                    .labelId(buildLabel.getId()).build());
                } else {
                    //落地标签标签组数据关系
                    datasetGroupLabelService.insert(
                            DatasetGroupLabel.builder()
                                    .labelGroupId(dbLabelGroup.getId())
                                    .labelId(groupLabel.getLabelId()).build());
                }
            }
        }
    }


    /**
     * 构建标签组名称
     *
     * @param name 原标签组名称
     * @return  构建后标签组名称
     */
    private String buildLabelGroupName(String name){
        int length = name.length();
        if(name.length() > MagicNumConstant.TWENTY){
            name = name.substring(0,length-MagicNumConstant.SEVEN);
        }
        return name+ RandomUtil.randomCode();
    }

    /**
     * 根据标签组ID 校验是否能自动标注
     *
     * @param labelGroupId  标签组
     * @return  true: 能  false: 否
     */
    @Override
    public boolean isAnnotationByGroupId(Long labelGroupId) {
        if(Objects.isNull(labelGroupId)){
            throw new BusinessException(ErrorEnum.LABEL_GROUP_ID_IS_NULL);
        }
        LabelGroup labelGroup = baseMapper.selectById(labelGroupId);
        if(Objects.isNull(labelGroup)){
            throw new BusinessException(ErrorEnum.LABELGROUP_DOES_NOT_EXIST);
        }
        return MagicNumConstant.ONE == labelGroup.getType();
    }


    /**
     * 普通标签组转预置
     *
     * @param groupConvertPresetDTO 普通标签组转预置请求实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @RolePermission
    public void convertPreset(GroupConvertPresetDTO groupConvertPresetDTO) {
        LabelGroup labelGroup = baseMapper.selectById(groupConvertPresetDTO.getLabelGroupId());
        if(Objects.isNull(labelGroup)){
           throw new BusinessException("标签组数据不存在");
        }
        if(MagicNumConstant.ZERO != labelGroup.getType()){
            throw new BusinessException("标签组已为预置标签组");
        }
        baseMapper.updateInfoByGroupId(MagicNumConstant.ONE, (long) MagicNumConstant.ZERO,groupConvertPresetDTO.getLabelGroupId());
    }

    /**
     * 根据标签组ID查询标签组数据
     *
     * @param groupId 标签组ID
     */
    @Override
    public void deleteByGroupId(Long groupId) {
        baseMapper.deleteByGroupId(groupId);
    }

    /**
     * 根据标签组ID修改状态
     *
     * @param groupId 标签组ID
     * @param deletedFlag 删除标识
     */
    @Override
    public void updateStatusByGroupId(Long groupId, Boolean deletedFlag) {
            baseMapper.updateStatusByGroupId(groupId,deletedFlag);
    }


    /**
     * 校验标签组名称重复接口
     *
     * @param name 标签组名称
     * @return true: 已存在 false:不存在
     */
    private boolean checkoutLabelGroupName(String name) {
        //校验标签组名称唯一
        LabelGroup labelGroup = baseMapper.selectOne(
                new LambdaQueryWrapper<LabelGroup>()
                        .eq(LabelGroup::getName, name)
                        .eq(LabelGroup::getDeleted, SwitchEnum.getBooleanValue(SwitchEnum.OFF.getValue()))
        );

        return !Objects.isNull(labelGroup);
    }

    /**
     * 新增时构建标签数据
     *
     * @param labelGroup 标签组
     * @param labelList  标签集合
     */
    private void buildLabelDataByCreate(LabelGroup labelGroup, List<LabelDTO> labelList) {

        //获取预置标签信息
        Map<Long, String> pubLabels = getPubLabels(labelGroup.getLabelGroupType());

        Map<String, Long> nameMap = new HashMap<>(labelList.size());
        for (LabelDTO label : labelList) {

            // 5-1 校验标签名称 颜色 标签组内名称唯一
            checkoutNameAndColor(label, nameMap);

            // 5-3 根据录入标签id校验标签逻辑
            //录入标签id存在
            if (!Objects.isNull(label.getId())) {
                //5-3-1 校验录入标签是否属于预置标签组
                String pubLabelName = pubLabels.get(label.getId());
                //录入标签不属于预置标签组
                if (Objects.isNull(pubLabelName)) {
                    throw new BusinessException(ErrorEnum.LABELGROUP_LABELG_ID_ERROR);
                    //录入标签属于预置标签组但名称不匹配
                } else if (!pubLabelName.equals(label.getName())) {
                    throw new BusinessException(ErrorEnum.LABELGROUP_LABEL_NAME_ERROR);
                } else {
                    //录入标签属于预置标签组且名称匹配
                    //落地标签组标签中间表数据
                    datasetGroupLabelService.insert(
                            DatasetGroupLabel.builder()
                                    .labelId(label.getId())
                                    .labelGroupId(labelGroup.getId()).build());
                }
            } else {
                // 5-3 落地标签数据
                Label buildLabel = Label.builder()
                        .color(label.getColor())
                        .name(label.getName())
                        .type(DatasetLabelEnum.CUSTOM.getType())
                        .build();
                labelService.insert(buildLabel);
                //落地标签组标签中间表数据
                datasetGroupLabelService.insert(
                        DatasetGroupLabel.builder()
                                .labelId(buildLabel.getId())
                                .labelGroupId(labelGroup.getId()).build());
            }
        }
    }

    /**
     * 获取预置标签信息
     *
     * @param labelGroupType 标签组类型
     * @return 预置标签信息 key: 标签id value: 标签名称
     */
    private Map<Long, String> getPubLabels(Integer labelGroupType) {
        List<Label> pubLabels = labelService.getPubLabels(labelGroupType);
        Map<Long, String> pubListMap = new HashMap<>(pubLabels.size());
        if (!CollectionUtils.isEmpty(pubLabels)) {
            pubListMap = pubLabels.stream().collect(Collectors.toMap(Label::getId, Label::getName));
        }
        return pubListMap;
    }

    /**
     * 解析标签信息
     *
     * @param labels 标签Json字符串
     * @return 解析后的标签实体
     */
    private List<LabelDTO> analyzeLabelData(String labels) {
        if (!JsonUtil.isJson(labels)) {
            throw new BusinessException(ErrorEnum.LABELGROUP_JSON_FILE_FORMAT_ERROR);
        }

        //5 解析标签数据
        List<LabelDTO> labelList = new ArrayList<>();
        try {
            labelList = JSONObject.parseArray(labels, LabelDTO.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorEnum.LABELGROUP_JSON_FILE_FORMAT_ERROR);
        }

        return labelList;
    }

    /**
     * 修改时构建标签数据
     *
     * @param dbListMap  数据库标签map key: 标签id value: 标签
     * @param labelGroup 标签组
     * @param labelList  标签列表
     * @param pubLabels  公共标签
     */
    private void buildLabelDataByUpdate(LabelGroup labelGroup, List<LabelDTO> labelList, Map<Long, List<Label>> dbListMap, Map<Long, String> pubLabels) {

        Map<String, Long> nameMap = new HashMap<>(labelList.size());

        for (LabelDTO label : labelList) {
            List<Label> labels = dbListMap.get(label.getId());
            //校验标签名称 颜色 标签组内名称唯一
            checkoutNameAndColor(label, nameMap);

            // 6-4 传入标签旧数据校验
            if (!CollectionUtils.isEmpty(labels) && label.getName().equals(labels.get(0).getName())) {
                continue;
            }

            // 7 校验ID是否存在
            // 7-1 预置标签组内标签不许修改名称
            String pubLabelName = pubLabels.get(label.getId());
            if (!Objects.isNull(label.getId())) {

                //属于预置标签组
                if (!Objects.isNull(pubLabelName)) {
                    //标签组标签关联表中包含录入标签id
                    if (!pubLabelName.equals(label.getName()) && dbListMap.keySet().contains(label.getId())) {
                        throw new BusinessException(ErrorEnum.LABELGROUP_OPERATE_LABEL_ID_ERROR);
                    } else {
                        //落地标签组标签中间表数据
                        datasetGroupLabelService.insert(
                                DatasetGroupLabel.builder()
                                        .labelId(label.getId())
                                        .labelGroupId(labelGroup.getId()).build());
                    }
                    //不属于预置标签组 属于当前标签组
                } else if (!CollectionUtils.isEmpty(labels)) {
                    Label buildLabel = Label.builder()
                            .color(label.getColor())
                            .name(label.getName())
                            .id(labels.get(0).getId())
                            .build();
                    labelService.updateLabel(buildLabel);
                } else {//不属于预置标签组 不属于当前标签组
                    throw new BusinessException(ErrorEnum.LABELGROUP_LABELG_ID_ERROR);
                }
            } else {
                // 7-2 落地标签数据
                Label buildLabel = Label.builder()
                        .color(label.getColor())
                        .name(label.getName())
                        .type(DatasetLabelEnum.CUSTOM.getType())
                        .build();
                labelService.insert(buildLabel);
                //落地标签组标签中间表数据
                datasetGroupLabelService.insert(
                        DatasetGroupLabel.builder()
                                .labelId(buildLabel.getId())
                                .labelGroupId(labelGroup.getId()).build());
            }
        }
    }

    /**
     * 校验标签名称 颜色 标签组内名称唯一
     *
     * @param label   标签实体
     * @param nameMap 标签名称Map key:标签名称 value:标签ID
     */
    private void checkoutNameAndColor(LabelDTO label, Map<String, Long> nameMap) {

        //名称和颜色字段校验
        if (Objects.isNull(label.getName()) || Objects.isNull(label.getColor())) {
            throw new BusinessException(ErrorEnum.LABEL_FORMAT_IS_ERROR);
        }

        //同标签组内名称/ID唯一校验
        Long labelId = nameMap.get(label.getName());
        if (nameMap.containsKey(label.getName()) ||
                ((!Objects.isNull(labelId)) && labelId.equals(label.getId()))
        ) {
            throw new BusinessException(ErrorEnum.LABEL_NAME_DUPLICATION);
        }
        nameMap.put(label.getName(), label.getId());
    }

}