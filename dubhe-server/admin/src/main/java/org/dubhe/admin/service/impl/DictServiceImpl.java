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
package org.dubhe.admin.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.admin.dao.DictDetailMapper;
import org.dubhe.admin.dao.DictMapper;
import org.dubhe.admin.domain.dto.*;
import org.dubhe.admin.domain.entity.Dict;
import org.dubhe.admin.service.DictService;
import org.dubhe.admin.service.convert.DictConvert;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.db.utils.WrapperHelp;
import org.dubhe.biz.file.utils.DubheFileUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @description 字典服务 实现类
 * @date 2020-06-01
 */
@Service
public class DictServiceImpl implements DictService {

    @Autowired
    private DictMapper dictMapper;

    @Autowired
    private DictDetailMapper dictDetailMapper;

    @Autowired
    private DictConvert dictConvert;


    /**
     * 分页查询字典信息
     *
     * @param criteria 字典查询实体
     * @param page     分页实体
     * @return java.util.Map<java.lang.String, java.lang.Object> 字典分页实例
     */
    @Override
    public Map<String, Object> queryAll(DictQueryDTO criteria, Page<Dict> page) {
        IPage<Dict> dicts = dictMapper.selectCollPage(page, WrapperHelp.getWrapper(criteria));
        return PageUtil.toPage(dicts, dictConvert::toDto);
    }


    /**
     * 按条件查询字典列表
     *
     * @param criteria 字典查询实体
     * @return java.util.List<org.dubhe.domain.dto.DictDTO> 字典实例
     */
    @Override
    public List<DictDTO> queryAll(DictQueryDTO criteria) {
        List<Dict> list = dictMapper.selectCollList(WrapperHelp.getWrapper(criteria));
        return dictConvert.toDto(list);
    }

    /**
     * 通过ID查询字典详情
     *
     * @param id 字典ID
     * @return org.dubhe.domain.dto.DictDTO 字典实例
     */
    @Override
    public DictDTO findById(Long id) {
        Dict dict = dictMapper.selectCollById(id);
        return dictConvert.toDto(dict);
    }

    /**
     * 通过Name查询字典详情
     *
     * @param name 字典名称
     * @return org.dubhe.domain.dto.DictDTO 字典实例
     */
    @Override
    public DictDTO findByName(String name) {
        Dict dict = dictMapper.selectCollByName(name);
        return dictConvert.toDto(dict);
    }

    /**
     * 新增字典
     *
     * @param resources 字典新增实体
     * @return org.dubhe.domain.dto.DictDTO 字典实例
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictDTO create(DictCreateDTO resources) {
        if (dictMapper.selectCollByName(resources.getName()) != null) {
            throw new BusinessException("字典名称已存在");
        }
        Dict dict = Dict.builder().build();
        BeanUtils.copyProperties(resources, dict);
        dictMapper.insert(dict);
        // 级联保存子表
        resources.getDictDetails().forEach(detail -> {
            detail.setDictId(dict.getId());
            dictDetailMapper.insert(detail);
        });
        return dictConvert.toDto(dict);
    }

    /**
     * 字典修改
     *
     * @param resources 字典修改实体
     * @return void
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DictUpdateDTO resources) {
        Dict dict = dictMapper.selectCollByName(resources.getName());
        if (dict != null && !dict.getId().equals(resources.getId())) {
            throw new BusinessException("字典名称已存在");
        }
        Dict dbDict = Dict.builder().build();
        BeanUtils.copyProperties(resources, dbDict);
        dictMapper.updateById(dbDict);
        //级联保存子表
        resources.getDictDetails().forEach(detail -> {
            detail.setDictId(dbDict.getId());
            if (detail.getId() == null) {
                dictDetailMapper.insert(detail);
            } else {
                dictDetailMapper.updateById(detail);
            }

        });
    }


    /**
     * 字典批量删除
     *
     * @param ids 字典ID
     * @return void
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(Set<Long> ids) {
        for (Long id : ids) {
            dictMapper.deleteById(id);
            dictDetailMapper.deleteByDictId(id);
        }
    }

    /**
     * 字典导出
     *
     * @param dictDtos 字典导出实体
     * @param response
     * @return void
     */
   @Override
   public void download(List<DictDTO> dictDtos, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DictDTO dictDTO : dictDtos) {
            if (CollectionUtil.isNotEmpty(dictDTO.getDictDetails())) {
                for (DictDetailDTO dictDetail : dictDTO.getDictDetails()) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("字典名称", dictDTO.getName());
                    map.put("字典描述", dictDTO.getRemark());
                    map.put("字典标签", dictDetail.getLabel());
                    map.put("字典值", dictDetail.getValue());
                    map.put("创建日期", dictDetail.getCreateTime());
                    list.add(map);
                }
            } else {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("字典名称", dictDTO.getName());
                map.put("字典描述", dictDTO.getRemark());
                map.put("字典标签", null);
                map.put("字典值", null);
                map.put("创建日期", dictDTO.getCreateTime());
                list.add(map);
            }
        }
        DubheFileUtil.downloadExcel(list, response);
    }
}
