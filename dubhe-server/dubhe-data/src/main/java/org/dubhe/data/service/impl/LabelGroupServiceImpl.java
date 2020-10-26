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

package org.dubhe.data.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.annotation.DataPermissionMethod;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.data.constant.Constant;
import org.dubhe.data.constant.DatasetLabelEnum;
import org.dubhe.data.constant.ErrorEnum;
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
import org.dubhe.enums.DatasetTypeEnum;
import org.dubhe.enums.SwitchEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.utils.*;
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
     * 创建标签组
     *
     * @param labelGroupCreateDTO 创建标签组DTO
     * @return Long 标签组id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void creatLabelGroup(LabelGroupCreateDTO labelGroupCreateDTO) {

        //1 标签组名称唯一校验
        labelGroupCreateDTO.setOriginUserId(JwtUtils.getCurrentUserDto().getId());
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
        //4 解析标签信息
        List<LabelDTO> labelList = analyzeLabelData(labelGroupCreateDTO.getLabels());

        //5 组装原标签数据
        if (!CollectionUtils.isEmpty(labelList)) {
            buildLabelDataByCreate(labelGroup, labelList);
        }

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
                Map<Long, String> pubLabels = getPubLabels();
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
     * @param dbListMap 数据库标签map key: 标签id value: 标签
     * @param labelGroup 标签组
     * @param labelList 标签列表
     * @param pubLabels 公共标签
     */
    private void buildLabelDataByUpdate(Map<Long, List<Label>> dbListMap, LabelGroup labelGroup, List<LabelDTO> labelList, Map<Long, String> pubLabels) {

        //删除标签和标签组的关联关系
        datasetGroupLabelService.deleteById(labelGroup.getId());
        for (LabelDTO dto : labelList) {
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
        if (labelGroup.getType().equals(MagicNumConstant.ONE)) {
            throw new BusinessException(ErrorEnum.LABELGROUP_PUBLIC_ERROR);
        }
        //校验标签组是否被数据集引用
        if(datasetService.getCountByLabelGroupId(labelGroupId) > 0){
            throw new BusinessException(ErrorEnum.LABELGROUP_LABEL_GROUP_QUOTE_DEL_ERROR);
        }

        List<Label> labels = labelService.listByGroup(labelGroupId);

        if (!CollectionUtils.isEmpty(labels)) {

            //过滤预置标签组
            List<Long> ids = labelService.getPubLabelIds();
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
                labelService.deleteByIds(labelIds);
                //删除标签和标签组关联关系
                labelService.deleteByIds(labelIds);
            }

        }
        //删除标签组数据
        getBaseMapper().deleteById(labelGroupId);
    }

    /**
     * 标签组分页列表
     *
     * @param page              分页信息
     * @param labelGroupQueryVO 查询条件
     * @return Map<String, Object> 查询出对应的标签组
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Object> listVO(Page<LabelGroup> page, LabelGroupQueryVO labelGroupQueryVO) {
        String name = labelGroupQueryVO.getName();
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
        if (StringUtils.isNotEmpty(labelGroupQueryVO.getSort()) && StringUtils.isNotEmpty(labelGroupQueryVO.getOrder())) {
            queryWrapper.orderBy(true, SORT_ASC.equals(labelGroupQueryVO.getOrder().toLowerCase()),
                    StringUtils.humpToLine(labelGroupQueryVO.getSort())
            );
        } else {
            queryWrapper.orderByDesc("update_time");
        }
        Page<LabelGroup> labelGroupPage = baseMapper.selectPage(page, queryWrapper);

        List<LabelGroupQueryVO> labelGroups = labelGroupPage.getRecords().stream().map(labelGroup -> {
            LabelGroupQueryVO labelGroupQueryVO1 = LabelGroupQueryVO.builder()
                    .id(labelGroup.getId()).name(labelGroup.getName())
                    .operateType(labelGroup.getOperateType())
                    .type(labelGroup.getType())
                    .createTime(labelGroup.getCreateTime())
                    .remark(labelGroup.getRemark()).updateTime(labelGroup.getUpdateTime()).build();
            int count = labelService.selectCount(labelGroup.getId());
            labelGroupQueryVO1.setCount(count);
            return labelGroupQueryVO1;
        }).collect(Collectors.toList());
        Map<String, Object> stringObjectMap = PageUtil.toPage(page, labelGroups);
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
        if(!CollectionUtils.isEmpty(labels)){
            labelVOS = labels.stream().map(a -> {
                return LabelVO.builder().id(a.getId()).color(a.getColor()).name(a.getName()).build();
            }).collect(Collectors.toList());
        }
        return LabelGroupVO.builder()
                .id(labelGroupId)
                .type(labelGroup.getType())
                .name(labelGroup.getName())
                .operateType(labelGroup.getOperateType())
                .labels(labelVOS).build();
    }

    /**
     * 标签组列表
     *
     * @param type 标签组类型
     * @return List<LabelGroup> 查询出对应的标签组
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public List<LabelGroup> getList(Integer type) {
        QueryWrapper<LabelGroup> labelGroupQueryWrapper = new QueryWrapper<>();
        labelGroupQueryWrapper.eq("deleted", MagicNumConstant.ZERO)
                .eq("type", type);
        return baseMapper.selectList(labelGroupQueryWrapper);
    }

    /**
     * 导入标签组
     *
     * @param labelGroupImportDTO 标签组导入DTO
     * @param file                导入文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importLabelGroup(LabelGroupImportDTO labelGroupImportDTO, MultipartFile file) {
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
                .remark(labelGroupImportDTO.getRemark()).build();

        //调用新增标签方法
        this.creatLabelGroup(createDTO);
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
            group.setName(labelGroup.getName() + RandomUtil.randomCode());
        }


        //落地标签组数据
        LabelGroup dbLabelGroup = LabelGroup.builder().name(group.getName()).remark(group.getRemark()).originUserId(oldLabelGroup.getCreateUserId()).build();
        baseMapper.insert(dbLabelGroup);

        //获取标签组关联关系
        List<DatasetGroupLabel> datasetGroupLabels = datasetGroupLabelService.listByGroupId(labelGroupCopyDTO.getId());

        //获取标签组下标签
        List<Label> labels = labelService.listByGroupId(labelGroupCopyDTO.getId());
        Map<Long, List<Label>> labelListMap = new HashMap<>(labels.size());
        if (!CollectionUtils.isEmpty(labels)) {
            labelListMap = labels.stream().collect(Collectors.groupingBy(Label::getId));
        }

        List<Label> pubLabels = labelService.getPubLabels();
        Map<Long, String> longListMap = new HashMap<>(pubLabels.size());
        if (!CollectionUtils.isEmpty(pubLabels)) {
            longListMap = pubLabels.stream().collect(Collectors.toMap(Label::getId, Label::getName));
        }

        if (!CollectionUtils.isEmpty(datasetGroupLabels)) {
            for (DatasetGroupLabel groupLabel : datasetGroupLabels) {
                //查看标签是否属于预置标签组
                String labelName = longListMap.get(groupLabel.getLabelId());
                if (Objects.isNull(labelName)) { //不属于预置标签组
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
     * @param labelList 标签集合
     */
    private void buildLabelDataByCreate(LabelGroup labelGroup, List<LabelDTO> labelList) {

        //获取预置标签信息
        Map<Long, String> pubLabels = getPubLabels();

        Map<String, String> nameMap = new HashMap<>(labelList.size());
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
     * @return 预置标签信息 key: 标签id value: 标签名称
     */
    private Map<Long, String> getPubLabels() {
        List<Label> pubLabels = labelService.getPubLabels();
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
     * @param dbListMap 数据库标签map key: 标签id value: 标签
     * @param labelGroup 标签组
     * @param labelList 标签列表
     * @param pubLabels 公共标签
     */
    private void buildLabelDataByUpdate(LabelGroup labelGroup, List<LabelDTO> labelList, Map<Long, List<Label>> dbListMap, Map<Long, String> pubLabels) {

        Map<String, String> nameMap = new HashMap<>(labelList.size());

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
     * @param label 标签实体
     * @param nameMap 标签名称 map
     */
    private void checkoutNameAndColor(LabelDTO label, Map<String, String> nameMap) {

        // 6-1 名称和颜色校验
        if (Objects.isNull(label.getName()) || Objects.isNull(label.getColor())) {
            throw new BusinessException(ErrorEnum.LABEL_NAME_COLOR_NOT_NULL);
        }

        // 6-3 同标签组内名称唯一校验
        String name = nameMap.get(label.getName());
        if (!Objects.isNull(name)) {
            throw new BusinessException(ErrorEnum.LABEL_NAME_DUPLICATION);
        }
        nameMap.put(label.getName(), label.getName());
    }

}