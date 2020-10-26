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
import org.dubhe.dao.PtStorageMapper;
import org.dubhe.domain.PtStorage;
import org.dubhe.domain.dto.PtStorageDTO;
import org.dubhe.domain.dto.PtStorageQueryCriteria;
import org.dubhe.service.PtStorageService;
import org.dubhe.service.convert.PtStorageConvert;
import org.dubhe.utils.FileUtil;
import org.dubhe.utils.PageUtil;
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
 * @description  存储服务类
 * @date 2020-03-17
 */
@Service
@CacheConfig(cacheNames = "ptStorage")
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class PtStorageServiceImpl implements PtStorageService {

    @Autowired
    private PtStorageMapper ptStorageMapper;

    @Autowired
    private PtStorageConvert ptStorageConvert;


    @Override
    @Cacheable
    public Map<String, Object> queryAll(PtStorageQueryCriteria criteria, Page page) {
        IPage<PtStorage> ptStorages = ptStorageMapper.selectPage(page, WrapperHelp.getWrapper(criteria));
        return PageUtil.toPage(ptStorages, ptStorageConvert::toDto);
    }

    @Override
    @Cacheable
    public List<PtStorageDTO> queryAll(PtStorageQueryCriteria criteria) {
        return ptStorageConvert.toDto(ptStorageMapper.selectList(WrapperHelp.getWrapper(criteria)));
    }

    @Override
    @Cacheable(key = "#p0")
    public PtStorageDTO findById(Long id) {
        PtStorage ptStorage = ptStorageMapper.selectById(id);
        return ptStorageConvert.toDto(ptStorage);
    }

    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public PtStorageDTO create(PtStorage resources) {
        ptStorageMapper.insert(resources);
        return ptStorageConvert.toDto(resources);
    }

    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void update(PtStorage resources) {
        PtStorage ptStorage = ptStorageMapper.selectById(resources.getId());
        ptStorage.copy(resources);
        ptStorageMapper.updateById(ptStorage);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void deleteAll(Long[] ids) {
        for (Long id : ids) {
            ptStorageMapper.deleteById(id);
        }
    }

    @Override
    public void download(List<PtStorageDTO> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (PtStorageDTO ptStorage : all) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(" name", ptStorage.getName());
            map.put(" size", ptStorage.getSize());
            map.put(" storageclass", ptStorage.getStorageclass());
            map.put(" createUser", ptStorage.getCreateUser());
            map.put(" createTime", ptStorage.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}
