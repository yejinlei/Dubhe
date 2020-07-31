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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.dao.PtDatasetMapper;
import org.dubhe.domain.PtDataset;
import org.dubhe.domain.dto.PtDatasetDTO;
import org.dubhe.domain.dto.PtDatasetQueryCriteria;
import org.dubhe.service.PtDatasetService;
import org.dubhe.service.convert.PtDatasetConvert;
import org.dubhe.utils.FileUtil;
import org.dubhe.utils.PageUtil;
import org.dubhe.utils.StringUtils;
import org.dubhe.utils.WrapperHelp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @description 数据集管理
 * @date 2020-03-17
 */
@Service
@CacheConfig(cacheNames = "ptDataset")
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class PtDatasetServiceImpl implements PtDatasetService {

    @Autowired
    private PtDatasetMapper ptDatasetMapper;
    @Autowired
    private PtDatasetConvert ptDatasetConvert;


    @Override
    @Cacheable
    public Map<String, Object> queryAll(PtDatasetQueryCriteria criteria, Page page) {
        IPage<PtDataset> ptDatasets = ptDatasetMapper.selectPage(page, WrapperHelp.getWrapper(criteria));
        return PageUtil.toPage(ptDatasets, ptDatasetConvert::toDto);
    }

    @Override
    @Cacheable
    public List<PtDatasetDTO> queryAll(PtDatasetQueryCriteria criteria) {
        return ptDatasetConvert.toDto(ptDatasetMapper.selectList(WrapperHelp.getWrapper(criteria)));
    }

    @Override
    @Cacheable(key = "#p0")
    public PtDatasetDTO findById(Long id) {
        PtDataset ptDataset = ptDatasetMapper.selectById(id);
        return ptDatasetConvert.toDto(ptDataset);
    }

    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public PtDatasetDTO create(PtDataset resources) {
        ptDatasetMapper.insert(resources);
        return ptDatasetConvert.toDto(resources);
    }

    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void update(PtDataset resources) {
        PtDataset ptDataset = ptDatasetMapper.selectById(resources.getId());
        ptDataset.copy(resources);
        ptDatasetMapper.updateById(ptDataset);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void deleteAll(Long[] ids) {
        for (Long id : ids) {
            ptDatasetMapper.deleteById(id);
        }
    }

    @Override
    public void download(List<PtDatasetDTO> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (PtDatasetDTO ptDataset : all) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(" name", ptDataset.getName());
            map.put(" remark", ptDataset.getRemark());
            map.put(" type", ptDataset.getType());
            map.put(" team", ptDataset.getTeam());
            map.put(" createUser", ptDataset.getCreateUser());
            map.put(" createTime", ptDataset.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}
