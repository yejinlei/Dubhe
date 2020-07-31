/**
 * Copyright 2019-2020 Zheng Jie
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
 */
package org.dubhe.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.dao.DictDetailMapper;
import org.dubhe.domain.dto.DictDetailCreateDTO;
import org.dubhe.domain.dto.DictDetailDTO;
import org.dubhe.domain.dto.DictDetailQueryDTO;
import org.dubhe.domain.dto.DictDetailUpdateDTO;
import org.dubhe.domain.entity.DictDetail;
import org.dubhe.exception.BusinessException;
import org.dubhe.service.DictDetailService;
import org.dubhe.service.convert.DictDetailConvert;
import org.dubhe.utils.PageUtil;
import org.dubhe.utils.WrapperHelp;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description :字典详情服务 实现类
 * @Date 2020-06-01
 */
@Service
@CacheConfig(cacheNames = "dictDetail")
public class DictDetailServiceImpl implements DictDetailService {

    @Autowired
    private DictDetailMapper dictDetailMapper;

    @Autowired
    private DictDetailConvert dictDetailConvert;

    /**
     * 分页查询字典详情
     *
     * @param criteria 字典详情查询实体
     * @param page     分页实体
     * @return java.util.Map<java.lang.String, java.lang.Object> 字典详情分页实例
     */
    @Override
    @Cacheable
    public Map<String, Object> queryAll(DictDetailQueryDTO criteria, Page<DictDetail> page) {
        IPage<DictDetail> dictDetails = dictDetailMapper.selectPage(page, WrapperHelp.getWrapper(criteria));
        return PageUtil.toPage(dictDetails, dictDetailConvert::toDto);
    }


    /**
     * 按条件查询字典列表
     *
     * @param criteria 字典详情查询实体
     * @return java.util.List<org.dubhe.domain.dto.DictDetailDTO> 字典详情实例
     */
    @Override
    public List<DictDetailDTO> queryAll(DictDetailQueryDTO criteria) {
        List<DictDetail> list = dictDetailMapper.selectList(WrapperHelp.getWrapper(criteria));
        return dictDetailConvert.toDto(list);
    }


    /**
     * 根据ID查询字典详情
     *
     * @param id 字典详情ID
     * @return org.dubhe.domain.dto.DictDetailDTO 字典详情实例
     */
    @Override
    @Cacheable(key = "#p0")
    public DictDetailDTO findById(Long id) {
        DictDetail dictDetail = dictDetailMapper.selectById(id);
        return dictDetailConvert.toDto(dictDetail);
    }

    /**
     * 新增字典性情
     *
     * @param resources 字典详情新增实体
     * @return org.dubhe.domain.dto.DictDetailDTO 字典详情实例
     */
    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public DictDetailDTO create(DictDetailCreateDTO resources) {
        if (!ObjectUtil.isNull(dictDetailMapper.selectByDictIdAndLabel(resources.getDictId(), resources.getLabel()))) {
            throw new BusinessException("字典标签已存在");
        }
        DictDetail dictDetail = DictDetail.builder().build();
        BeanUtils.copyProperties(resources, dictDetail);
        dictDetailMapper.insert(dictDetail);
        return dictDetailConvert.toDto(dictDetail);
    }

    /**
     * 修改字典详情
     *
     * @param resources 字典详情修改实体
     * @return void
     */
    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void update(DictDetailUpdateDTO resources) {
        DictDetail detail = dictDetailMapper.selectByDictIdAndLabel(resources.getDictId(), resources.getLabel());
        if (detail != null && !detail.getId().equals(resources.getId())) {
            throw new BusinessException("字典标签已存在");
        }
        DictDetail dbDetail = DictDetail.builder().build();
        BeanUtils.copyProperties(resources, dbDetail);
        dictDetailMapper.updateById(dbDetail);
    }

    /**
     * 删除字典详情
     *
     * @param ids 字典详情ID
     * @return void
     */
    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        dictDetailMapper.deleteBatchIds(ids);
    }
}
