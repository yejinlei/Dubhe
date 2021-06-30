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
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.permission.base.BaseService;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.redis.utils.RedisUtils;
import org.dubhe.cloud.authconfig.utils.JwtUtils;
import org.dubhe.data.constant.Constant;
import org.dubhe.data.constant.DatasetLabelEnum;
import org.dubhe.data.constant.ErrorEnum;
import org.dubhe.data.constant.LabelGroupTypeEnum;
import org.dubhe.data.dao.LabelGroupMapper;
import org.dubhe.data.dao.LabelMapper;
import org.dubhe.data.domain.dto.LabelDTO;
import org.dubhe.data.domain.dto.LabelDeleteDTO;
import org.dubhe.data.domain.dto.LabelUpdateDTO;
import org.dubhe.data.domain.entity.*;
import org.dubhe.data.service.DatasetGroupLabelService;
import org.dubhe.data.service.DatasetLabelService;
import org.dubhe.data.service.DatasetService;
import org.dubhe.data.service.LabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static org.dubhe.data.constant.Constant.*;

/**
 * @description 数据集标签 服务实现类
 * @date 2020-04-01
 */
@Service
public class LabelServiceImpl extends ServiceImpl<LabelMapper, Label> implements LabelService {

    /**
     * 数据集标签信息服务
     */
    @Autowired
    private DatasetLabelService datasetLabelService;

    /**
     * 标签组标签中间表服务
     */
    @Autowired
    private DatasetGroupLabelService datasetGroupLabelService;

    /**
     * 数据文件标注服务
     */
    @Autowired
    private DataFileAnnotationServiceImpl dataFileAnnotationService;

    /**
     * 标签组管理 Mapper 接口
     */
    @Autowired
    private LabelGroupMapper labelGroupMapper;

    /**
     * 数据集信息服务
     */
    @Resource
    @Lazy
    private DatasetService datasetService;


    /**
     * redis工具类
     */
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 根据类型获取预置标签集合
     *
     * @param labelGroupType 标签组类型
     * @return List<Label> 预置标签集合
     */
    @Override
    public List<Label> listSupportAutoByType(Integer labelGroupType) {
        return getPubLabels(labelGroupType);
    }

    /**
     * 标签查询
     *
     * @param datasetId 数据集id
     * @return List<Label> 标签列表
     */
    @Override
    public List<LabelDTO> list(Long datasetId) {
        LabelGroup labelGroup = labelGroupMapper.getLabelGroupByDataId(datasetId);
        Long start = System.currentTimeMillis();
        List<Label> labels =  getBaseMapper().listLabelByDatasetId(datasetId);
        List<Long> labelIds = labelGroup == null ? new ArrayList<>():
                datasetGroupLabelService.getLabelIdsByGroupId(labelGroup.getId());
        List<Long> pubLabelIds = labelGroup == null ? new ArrayList<>():
                getPubLabelIds(labelGroup.getLabelGroupType());
        return CollectionUtils.isEmpty(labels)?new ArrayList<LabelDTO>():labels.stream().map(a -> {
                    LabelDTO dto = new LabelDTO();
                    dto.setName(a.getName());
                    dto.setColor(a.getColor());
                    dto.setLabelGroupId(pubLabelIds.contains(a.getId()) ? COCO_ID : (labelIds.contains(a.getId())?labelGroup.getId():null));
                    dto.setType(a.getType());
                    dto.setId(a.getId());
                    return dto;
                }).collect(Collectors.toList());
    }

    /**
     * 删除数据集标签
     *
     * @param id         数据集id
     * @param deleteFlag 删除标识
     */
    @Override
    public void updateStatusByDatasetId(Long id, Boolean deleteFlag) {
        datasetLabelService.updateStatusByDatasetId(id,deleteFlag);
    }


    /**
     * 保存标签
     *
     * @param label     标签
     * @param datasetId 数据集id
     */
    @Override
    public Long save(Label label, Long datasetId) {
        save(Collections.singletonList(label), datasetId);
        return label.getId();
    }

    /**
     * 根据标签名获取标签id
     *
     * @param name 标签名
     * @return Long 标签id
     */
    public Long getAutoByName(String name) {
        if (name == null) {
            return null;
        }
        QueryWrapper<Label> labelQueryWrapper = new QueryWrapper<>();
        labelQueryWrapper.lambda().eq(Label::getName, name).le(Label::getId, Constant.RESERVED_LABEL_ID);
        List<Label> labels = getBaseMapper().selectList(labelQueryWrapper);
        if (CollectionUtils.isEmpty(labels)) {
            return null;
        }
        return labels.get(MagicNumConstant.ZERO).getId();
    }

    /**
     * 保存标签
     *
     * @param labels    标签
     * @param datasetId 数据集id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(List<Label> labels, Long datasetId) {
        if (CollectionUtils.isEmpty(labels)) {
            return;
        }
        List<DatasetLabel> rels = new LinkedList<>();
        List<Label> newLabels = new LinkedList<>();
        for (Label label : labels) {
            Long id = label.getId();
            if (id == null) {
                id = getAutoByName(label.getName());
                label.setId(id);
            }
            if (id != null && exist(id)) {
                rels.add(DatasetLabel.builder()
                        .datasetId(datasetId)
                        .labelId(id)
                        .build());
                continue;
            }
            if (StringUtils.isEmpty(label.getName())) {
                continue;
            }
            newLabels.add(label);
        }

        for (Label label : newLabels) {
            saveCustom(label, datasetId);
            rels.add(DatasetLabel.builder()
                    .datasetId(datasetId)
                    .labelId(label.getId())
                    .build());
        }

        if (CollectionUtils.isEmpty(rels)) {
            return;
        }
        rels = datasetLabelService.filterExist(rels);
        if (rels.size() > rels.stream().distinct().count()) {
            throw new BusinessException(ErrorEnum.LABEL_NAME_DUPLICATION);
        }
        datasetLabelService.saveList(rels);
    }

    /**
     * 存在标签
     *
     * @param name      标签名
     * @param datasetId 数据集id
     * @return boolean 是否存在结果
     */
    public boolean exist(String name, Long datasetId) {
        List<Long> ids = datasetLabelService.list(datasetId).stream()
                .map(DatasetLabel::getLabelId)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }

        QueryWrapper<Label> labelQueryWrapper = new QueryWrapper<>();
        labelQueryWrapper.lambda().eq(Label::getName, name).in(Label::getId, ids);

        return getBaseMapper().selectCount(labelQueryWrapper) > MagicNumConstant.ZERO;
    }

    /**
     * 存在标签
     *
     * @param labelId 标签id
     * @return boolean 是否存在标签
     */
    public boolean exist(Long labelId) {
        boolean exist = getById(labelId) != null;
        if (!exist) {
            LogUtil.warn(LogEnum.BIZ_DATASET, "label does not exit. labelId:{}", labelId);
        }
        return exist;
    }

    /**
     * 添加自定义标签，不保存关联关系
     *
     * @param label     需提前过滤系统已有标签
     * @param datasetId 数据集id
     */
    public void saveCustom(Label label, Long datasetId) {
        if (label == null) {
            return;
        }

        if (exist(label.getName(), datasetId)) {
            throw new BusinessException(ErrorEnum.LABEL_NAME_EXIST, label.getName(), null);
        }
        label.setCreateUserId(Constant.DEFAULT_USER);
        label.setUpdateUserId(Constant.DEFAULT_USER);
        save(label);
    }

    /**
     * 获取指定类型下所有标签
     *
     * @param type 标签类型
     * @return List<Label> 指定类型下所有标签
     */
    @Override
    public List<Label> listByType(Integer type) {
        return baseMapper.selectListByType(type);
    }

    /**
     * 获取数据集下所有标签类型
     *
     * @param datasetId 数据集id
     * @return List<Integer> 数据集下所有标签类型
     */
    @Override
    public List<Integer> getDatasetLabelTypes(Long datasetId) {
        return baseMapper.getDatasetLabelTypes(datasetId);
    }


    /**
     * 修改标签
     *
     * @param labelUpdateDTO 修改标签DTO
     * @return boolean      修改结果是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(LabelUpdateDTO labelUpdateDTO) {

        //校验该标签是否属于标签组
        if(!CollectionUtils.isEmpty(datasetGroupLabelService.listByLabelId(labelUpdateDTO.getLabelId()))){
            throw new BusinessException(ErrorEnum.LABEL_NAME_REPEAT);
        }

        //名称重复性校验
        if(this.checkoutLabelIsRepeat(labelUpdateDTO.getDatasetId(),labelUpdateDTO.getName())){
            throw new BusinessException(ErrorEnum.LABEL_NAME_REPEAT);
        }

        //查询标签信息
        Label label = baseMapper.selectById(labelUpdateDTO.getLabelId());
        //管理员才可以修改预置数据集
        //普通标签只有创建者才可以修改
        if(label.getType() != MagicNumConstant.ZERO){
            if (!BaseService.isAdmin()){
                throw new BusinessException(ErrorEnum.LABEL_PUBLIC_EORROR);
            }
        }else if (JwtUtils.getCurUserId()!=null &&
                !JwtUtils.getCurUserId().equals(label.getCreateUserId())){
            throw new BusinessException(ErrorEnum.LABEL_AUTHORITY_ERROR);
        }
        label.setColor(labelUpdateDTO.getColor());
        label.setName(labelUpdateDTO.getName());
        baseMapper.updateById(label);
        return true;
    }

    /**
     * 新增标签
     *
     * @param label 标签实体
     * @return  新增标签结果
     */
    @Override
    public int insert(Label label) {
         return baseMapper.insert(label);
    }


    /**
     * 根据标签组获取标签列表
     *
     * @param labelGroupId 标签组ID
     * @return List<Label> 标签组列表
     */
    @Override
    public List<Label> listByGroupId(Long labelGroupId) {
        return baseMapper.listByGroupId(labelGroupId);
    }


    /**
     * 根据标签组类型获取标签列表
     *
     * @param type 标签组类型
     * @return List<Long> 标签ids
     */
    @Override
    public List<Long> listPubLabelByType(Integer type) {
        return baseMapper.listPubLabelByType(type);
    }


    /**
     * 编辑标签
     *
     * @param label 标签实体
     */
    @Override
    public void updateLabel(Label label) {
        baseMapper.updateById(label);
    }

    /**
     * 获取预置标签组下的标签id
     *
     * @param labelGroupType 标签组数据类型
     * @return 预置标签ids
     */
    @Override
    public List<Long> getPubLabelIds(Integer labelGroupType) {
        Object obj = redisUtils.hget(DATASET_LABEL_PUB_KEY + SymbolConstant.COLON + labelGroupType, DATASET_DIRECTORY);
        List<Label> pubLabels = Objects.isNull(obj) ? getPubLabels(labelGroupType) : JSONObject.parseArray((String) obj, Label.class);
        return !CollectionUtils.isEmpty(pubLabels)
                ? pubLabels.stream().map(a->a.getId()).collect(Collectors.toList()) : new ArrayList<>();
    }

    /**
     * 获取预置标签组下的标签
     *
     * @param labelGroupType 标签组数据类型
     * @return 预置标签集合
     */
    @Override
    public List<Label> getPubLabels(Integer labelGroupType) {
        LambdaQueryWrapper<Label> labelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        labelLambdaQueryWrapper.eq(Label::getDeleted,MagicNumConstant.ZERO)
                .eq(Label::getType,LabelGroupTypeEnum.VISUAL.getValue().equals(labelGroupType) ?
                        DatasetLabelEnum.MS_COCO.getType(): DatasetLabelEnum.TXT.getType());
        List<Label> labels = baseMapper.selectList(labelLambdaQueryWrapper);
        redisUtils.hset(DATASET_LABEL_PUB_KEY +":"+ labelGroupType, DATASET_DIRECTORY, JSONObject.toJSONString(labels));
        return labels;
    }


    /**
     * 获取标签数量
     *
     * @param id 标签组Id
     * @return  标签数量
     */
    @Override
    public int selectCount(Long id) {
        return datasetGroupLabelService.listByGroupId(id).size();
    }

    /**
     * 根据标签组类型获取标签列表
     *
     * @param labelGroupType 标签组类型
     * @return List<Label> 标签组列表
     */
    @Override
    public List<Label> findByLabelGroupType(Integer labelGroupType) {
        if(LabelGroupTypeEnum.TXT.getValue().equals(labelGroupType)){
            return listByType(DatasetLabelEnum.MS_COCO.getType());
        }
        return listByType(DatasetLabelEnum.AUTO.getType());
    }


    /**
     * 校验数据集下标签名称是否重复
     *
     * @param datasetId 数据集ID
     * @param labelName 标签名称
     * @return 是否重复
     */
    @Override
    public boolean checkoutLabelIsRepeat(Long datasetId,String labelName) {
        List<Label> labelDTOS = baseMapper.batchListByIds(datasetId);
        if(!CollectionUtils.isEmpty(labelDTOS)){
            for (Label labelDTO : labelDTOS) {
                if(labelName.equals(labelDTO.getName())){
                    return true;
                }
            }
        }
        return false;
    }




    /**
     * 批量删除标签
     *
     * @param ids 需删除的标签ID
     */
    @Override
    public void deleteByIds(List<Long> ids) {
        getBaseMapper().deleteBatchIds(ids);
    }

    /**
     * 删除标签
     *
     * @param labelDeleteDTO 需删除的标签DTO
     */
    @Override
    public void delete(LabelDeleteDTO labelDeleteDTO) {

        //校验该标签是否属于标签组
        if(!CollectionUtils.isEmpty(datasetGroupLabelService.listByLabelId(labelDeleteDTO.getLabelId()))){
            throw new BusinessException(ErrorEnum.LABEL_NAME_REPEAT);
        }
        //校验当前标签是否存在引用
        Dataset dataset = datasetService.getOneById(labelDeleteDTO.getDatasetId());

        //获取当前数据集下标签引用
        List<DataFileAnnotation> dataFileAnnotations = dataFileAnnotationService.getBaseMapper().selectList(
                new LambdaQueryWrapper<DataFileAnnotation>() {{
                    eq(DataFileAnnotation::getDatasetId, labelDeleteDTO.getDatasetId());
                    eq(DataFileAnnotation::getLabelId, labelDeleteDTO.getLabelId());
                }}.last(" limit " + NumberConstant.NUMBER_0 + ", " + NumberConstant.NUMBER_1)
        );
        if (!CollectionUtils.isEmpty(dataFileAnnotations)){
            throw new BusinessException(ErrorEnum.LABEL_QUOTE_DEL_ERROR);
        }
        //查询标签信息
        Label label = baseMapper.selectById(labelDeleteDTO.getLabelId());
        //管理员才可以删除预置标签
        //普通标签只有创建者才可以删除
        if(label.getType() != MagicNumConstant.ZERO){
            if (!BaseService.isAdmin()){
                throw new BusinessException(ErrorEnum.LABEL_PUBLIC_EORROR);
            }
        }else if (JwtUtils.getCurUserId()!=null &&
                !JwtUtils.getCurUserId().equals(label.getCreateUserId())){
            throw new BusinessException(ErrorEnum.LABEL_AUTHORITY_ERROR);
        }
        baseMapper.deleteById(labelDeleteDTO.getLabelId());

    }



    /**
     * 通过标签ID修改标签状态
     *
     * @param labelIds   标签ID
     * @param deleteFlag 删除标识
     */
    @Override
    public void updateStatusByLabelIds(List<Long> labelIds, Boolean deleteFlag) {
        baseMapper.updateStatusByLabelIds(labelIds,deleteFlag);
    }


    /**
     * 根据标签组ID删除标签数据
     *
     * @param groupId  标签组ID
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
        List<Long> labelIds = datasetGroupLabelService.getLabelIdsByGroupId(groupId);
        if(!CollectionUtils.isEmpty(labelIds)){
            this.updateStatusByLabelIds(labelIds,deletedFlag);
        }
    }


}